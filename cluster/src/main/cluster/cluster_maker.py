import threading
import warnings
import numpy as np
import pandas as pd
import queue

from umap import UMAP
from ._clustering import clustering
from typing import List, Dict
from ._ctfidf import ClassTfidfTransformer
from datetime import datetime, date
from src.main.utils import get_logger
from sklearn.feature_extraction.text import CountVectorizer

from src.main.message_queue.message import Message
from src.main.message_queue.mq_producer import MessageQueueProducer
from src.main.persistence.models import Cluster, Article, HotCluster
from src.main.persistence.repository.article_repository import ArticleRepository
from src.main.persistence.repository.cluster_repository import ClusterRepository
from src.main.persistence.repository.hot_cluster_repository import HotClusterRepository
from src.main.persistence.repository.section_repository import SectionRepository
from src.main.persistence.repository.preprocessed_article_repository import PreprocessedArticleRepository

warnings.filterwarnings("ignore", message=".*The 'nopython' keyword.*")
CONFIG_PATH = 'src/main/resources/config/cluster_maker/config.yml'



class ClusterMaker:
    def __init__(self):
        self.umap = UMAP(n_neighbors=15,
                         n_components=5,
                         min_dist=0.0,
                         metric='cosine',
                         low_memory=False)
        self.vectorizer = CountVectorizer(tokenizer=_no_process, preprocessor=_no_process, token_pattern=None)
        self.ctfidf = ClassTfidfTransformer()
        self.article_repository = ArticleRepository()
        self.preprocessed_article_repository = PreprocessedArticleRepository()
        self.cluster_repository = ClusterRepository()
        self.hot_cluster_repository = HotClusterRepository()
        self.noise_threshold = 0.5
        self.min_cluster_size = 3
        self.min_document = 20
        self.section_id = {}
        self.load_section()
        self.logger = get_logger('ClusterMaker')
        self.message_queue = queue.Queue()
        self.mq_producer = MessageQueueProducer('preprocessor', self.message_queue)
        producer_thread = threading.Thread(target=self.mq_producer.start, daemon=True)
        producer_thread.start()

    def clustering(self, section_name: str, t_date: date = None):
        self.logger.info("clustering start")

        if t_date is None:
            t_date = date.today()

        try:
            self._clustering(section_name, t_date)

            # 핫 클러스터 생성
            new_hot_clusters = self.make_hot_cluster(t_date)
            old_hot_clusters = self.hot_cluster_repository.find_all_by_duration(t_date)
            self.hot_cluster_repository.delete(old_hot_clusters)
            self.hot_cluster_repository.insert(new_hot_clusters)
        except Exception as e:
            self.logger.error(e)

        self.logger.info("clustering finished")

    def _clustering(self, section_name: str, t_date: date):
        self.logger.info("_clustering start")
        t_datetime = datetime.combine(t_date, datetime.min.time())
        # 현재 섹션에 대한 모든 뉴스 기사 리스트를 가져옴
        article_list = self.article_repository.find_all_by_section_id(section_id=self.section_id[section_name],
                                                                      t_date=t_date)

        # 뉴스 기사 리스트가 특정 임계값 이상이라면 클러스터링 수행
        if self.min_document < len(article_list):
            # 클러스터링 메인 비즈니스 로직 실행
            topic_words, labels = self._topic_clustering(article_list)
            # 너무 작은 클러스터는 제거
            topic_words, labels = self._remove_minimum_cluster(topic_words, labels)
            # 각 클러스터의 크기 계산
            counts = _count_articles(labels)
            labeled_clusters = self._make_labeled_clusters(labels=labels,
                                                           t_datetime=t_datetime,
                                                           counts=counts,
                                                           topic_words=topic_words,
                                                           section_name=section_name)

            # 클러스터 교체
            # 이전에 존재하던 섹션의 클러스터를 조회
            clusters = self.cluster_repository.find_all_by_section_id(section_id=self.section_id[section_name],
                                                                      duration=t_date)

            # 이전 클러스터를 지우고
            self.cluster_repository.delete(clusters)
            # 현재 클러스터를 삽입
            self.cluster_repository.insert(list(labeled_clusters.values()))

            # 전처리 요청
            msg = Message('make_preprocessed_clusters', {
                'article_list': article_list,
                'labels': labels,
                'topics': topic_words,
                't_datetime': t_datetime,
                'counts': counts,
                'section_id': self.section_id[section_name],
            })
            self.message_queue.put((msg, True))

            for label, article in zip(labels, article_list):
                if label == -1:
                    article.cluster_id = None
                else:
                    article.cluster_id = labeled_clusters[label].cluster_id

            article_list = self.article_repository.update(article_list)

        else:
            self.logger.debug(f'{section_name} section, too small')

    def load_section(self):
        section_repository = SectionRepository()
        section_list = section_repository.find_all()
        for section in section_list:
            self.section_id[section.section_name] = section.section_id

    def _make_labeled_clusters(self,
                               labels,
                               t_datetime,
                               counts: dict[int, int],
                               topic_words: Dict[int, list[tuple[str, float]]],
                               section_name):
        labeled_clusters = {}
        for label in labels:
            if label != -1:
                new_topic = Cluster(regdate=t_datetime,
                                    words=','.join([e[0] for e in topic_words[label]]),
                                    size=counts[label],
                                    section_id=self.section_id[section_name],
                                    related_cluster_id=None)
                labeled_clusters[label] = new_topic

        return labeled_clusters

    def _topic_clustering(self, article_list):
        # 1. embeddings, tokens 리스트 생성
        preprocessed_list = self.preprocessed_article_repository.find_all_by_article(articles=article_list)
        embeddings = []
        tokens_list = []
        for e in preprocessed_list:
            embeddings.append(e.embedding)
            tokens_list.append(e.tokens)

        # 2. UMAP 알고리즘 사용하여 차원축소
        reduced_embeddings = np.nan_to_num(self.umap.fit_transform(embeddings))

        # 3. HDBSCAN, Mean-Shift 사용하여 클러스터링
        labels = clustering(reduced_embeddings)

        # 4. 각 군집에 대하여 c-TF-IDF로 토픽 추출
        classed_tokens = _tokens_per_label(labels, tokens_list)
        c_tf_idf, c_words = self._extract_topic(classed_tokens)
        topics = _extract_words_per_topic(c_tf_idf, c_words, labels, 5)

        # 5. 토픽으로 노이즈 제거
        topics = self._remove_noise_topics(topics, article_list, labels)
        labels = self._remove_noise_articles(topics, article_list, labels)

        # 6. 노이즈 제거된 군집에 대하여, c-TF-IDF로 다시 토픽 추출
        classed_tokens = _tokens_per_label(labels, tokens_list)
        c_tf_idf, c_words = self._extract_topic(classed_tokens)
        topics = _extract_words_per_topic(c_tf_idf, c_words, labels, 3)

        return topics, labels

    def _extract_topic(self, tokens_list):
        self.vectorizer.fit(tokens_list)
        X = self.vectorizer.transform(tokens_list)
        words = self.vectorizer.get_feature_names_out()

        ctfidf = self.ctfidf.fit(X)
        c_tf_idf = ctfidf.transform(X)

        return c_tf_idf, words

    def _remove_noise_topics(self, topics, article_list: list[Article], labels):
        reprocessed_topics = {}
        for label in topics.keys():
            reprocessed_topics[label] = {}
            for word, tf_idf in topics[label]:
                reprocessed_topics[label][word] = 0.0

        for cluster_idx, article in zip(labels, article_list):
            cur_topics = reprocessed_topics[cluster_idx]
            for word in cur_topics.keys():
                if word != '':
                    word_content_frequency = article.content.count(word)
                    word_title_frequency = article.title.count(word)
                    cur_topics[word] += (word_title_frequency * 2 + word_content_frequency)

        for label in topics.keys():
            threshold = sum(reprocessed_topics[label].values()) / len(reprocessed_topics[label])
            for idx in list(reversed(range(len(topics[label])))):
                word = topics[label][idx][0]
                if reprocessed_topics[label][word] <= threshold:
                    del topics[label][idx]

        return topics

    def _remove_noise_articles(self, topics, article_list: list[Article], labels):
        score_list = []
        thresholds = {}

        for article_idx in range(labels.size):
            cluster_idx = labels[article_idx]
            cluster_topics = topics[cluster_idx]
            article = article_list[article_idx]
            cur_score = 0

            if cluster_idx not in thresholds:
                thresholds[cluster_idx] = []

            for word, _ in cluster_topics:
                if word != '':
                    word_content_frequency = article.content.count(word)
                    word_title_frequency = article.title.count(word)
                    cur_score += (word_title_frequency * 2 + word_content_frequency)

            score_list.append(cur_score)
            thresholds[cluster_idx].append(cur_score)

        for cluster_idx in thresholds.keys():
            threshold = (sum(thresholds[cluster_idx]) / len(thresholds[cluster_idx])) * 0.5
            thresholds[cluster_idx] = threshold

        labels_idx = 0
        for label, score in zip(labels, score_list):
            if thresholds[label] >= score:
                labels[labels_idx] = -1
            labels_idx += 1

        return labels

    def _remove_minimum_cluster(self, topic_words, labels):
        count = {key: 0 for key in topic_words.keys()}
        for label in labels:
            count[label] += 1

        delete_labels = []
        for key, count in count.items():
            if count < self.min_cluster_size:
                delete_labels.append(key)

        for idx in range(len(labels)):
            if labels[idx] in delete_labels:
                labels[idx] = -1

        return topic_words, labels

    def make_hot_cluster(self, t_date: date) -> List[HotCluster]:
        self.logger.info("make_hot_cluster start")
        clusters = self.cluster_repository.find_all_by_duration(duration=t_date)
        counted_clusters = []
        for cluster in clusters:
            counted_clusters.append((cluster, self.article_repository.count_by_cluster_id(cluster.cluster_id)))
        counted_clusters = sorted(counted_clusters, key=lambda e: e[1], reverse=True)

        hot_clusters = []
        for idx in range(10):
            if idx >= len(counted_clusters):
                break
            cur_cluster = counted_clusters[idx][0]
            cur_count = counted_clusters[idx][1]

            hot_cluster = HotCluster(
                cluster_id=cur_cluster.cluster_id,
                regdate=cur_cluster.regdate,
                size=cur_count
            )
            hot_clusters.append(hot_cluster)

        return hot_clusters


def _no_process(e):
    return e


def _top_n_idx_sparse(matrix, n: int) -> np.ndarray:
    indices = []
    for le, ri in zip(matrix.indptr[:-1], matrix.indptr[1:]):
        n_row_pick = min(n, ri - le)
        values = matrix.indices[le + np.argpartition(matrix.data[le:ri], -n_row_pick)[-n_row_pick:]]
        values = [values[index] if len(values) >= index + 1 else None for index in range(n)]
        indices.append(values)
    return np.array(indices)


def _top_n_values_sparse(matrix, indices: np.ndarray) -> np.ndarray:
    top_values = []
    for row, values in enumerate(indices):
        scores = np.array([matrix[row, value] if value is not None else 0 for value in values])
        top_values.append(scores)
    return np.array(top_values)


def _extract_words_per_topic(c_tf_idf, words: List[str], labels, top_n: int = 30):
    labels = sorted(list(set(labels)))
    indices = _top_n_idx_sparse(c_tf_idf, top_n)
    scores = _top_n_values_sparse(c_tf_idf, indices)
    sorted_indices = np.argsort(scores, 1)
    indices = np.take_along_axis(indices, sorted_indices, axis=1)
    scores = np.take_along_axis(scores, sorted_indices, axis=1)

    topics = {label: [(words[word_index], score)
                      if word_index is not None and score > 0 else ("", 0.00001)
                      for word_index, score in zip(indices[index][::-1], scores[index][::-1])]
              for index, label in enumerate(labels)}
    return topics


def _tokens_per_label(labels, tokens_list):
    labeled_tokens = pd.DataFrame(columns=['Label', 'Tokens'], data=zip(labels, tokens_list))
    tokens_per_label = labeled_tokens.groupby(['Label'], as_index=False).agg({'Tokens': 'sum'})
    classed_tokens = tokens_per_label.Tokens.values
    return classed_tokens


def _count_articles(labels):
    counts = {}
    for label in labels:
        if label not in counts.keys():
            counts[label] = 0
        else:
            counts[label] += 1
    return counts
