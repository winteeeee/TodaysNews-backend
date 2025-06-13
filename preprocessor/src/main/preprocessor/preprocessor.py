import os
from typing import Dict

import yaml
from sentence_transformers import SentenceTransformer

from src.main.persistence.models import PreprocessedArticle, Cluster, PreprocessedCluster
from src.main.persistence.repository.preprocessed_article_repository import PreprocessedArticleRepository
from src.main.preprocessor.custom_tokenizer import CustomTokenizer
from src.main.preprocessor.summarizer.kobart_summarizer import KoBARTSummarizer
from src.main.preprocessor.summarizer.multi_docs_summarizer import Centroid, MultiDocsSummarizer
from src.main.utils import get_logger
from konlpy.tag import Okt


class Preprocessor:
    def __init__(self):
        self.EMBEDDING_TARGET = os.getenv('EMBEDDING_TARGET', 'title')
        self.TOKENIZER_TARGET = os.getenv('TOKENIZER_TARGET', 'title')
        self.EMBEDDING_MODEL = os.getenv('EMBEDDING_MODEL', 'jhgan/ko-sroberta-nli')
        self.summarizer = KoBARTSummarizer()
        self.lead_extractor = KoBARTSummarizer()
        self.logger = get_logger('Preprocessor')
        self.tokenizer = CustomTokenizer(Okt())
        self.embedding_model = SentenceTransformer(self.EMBEDDING_MODEL)
        self.mds = MultiDocsSummarizer(SentenceTransformer(self.EMBEDDING_MODEL))
        self.preprocessed_article_repository = PreprocessedArticleRepository()

    def preprocess(self, msg):
        self.logger.info("preprocess start")
        if msg.commend == "preprocess":
            article_list = msg.data
            preprocessed_list = self._preprocess(article_list)
            article_iter = iter(article_list)
            for preprocessed_article in preprocessed_list:
                preprocessed_article.article_id = article_iter.__next__().article_id
            self.preprocessed_article_repository.insert(preprocessed_list)
        elif msg.commend == "make_preprocessed_clusters":
            data = msg.data
            centroids = self._extract_centroids(
                article_list=data['article_list'],
                labels=data['labels'],
                topics=data['topics']
            )

            labeled_clusters = self._make_labeled_clusters(labels=data['labels'],
                                                           t_datetime=data['t_datetime'],
                                                           counts=data['counts'],
                                                           topic_words=data['topics'],
                                                           centroids=centroids,
                                                           section_id=data['section_id'])

            labeled_preprocessed_clusters = self._make_preprocessed_clusters(labels=data['labels'],
                                                                             labeled_clusters=labeled_clusters,
                                                                             centroids=centroids,
                                                                             topic_words=data['topics'])
            self.preprocessed_article_repository.insert(list(labeled_preprocessed_clusters.values()))



    def _preprocess(self, article_list) -> list[PreprocessedArticle]:
        self.logger.info("_preprocess start")
        preprocessed_list = []
        for article in article_list:
            lead = self.lead_extractor.summarize(article.content) if len(article.content) > 200 else article.content
            summary = self.summarizer.summarize(article.content[len(article.content) // 2:]) if len(article.content) > 650 else ""

            preprocessed_list.append(PreprocessedArticle(
                tokens=self.tokenizer(article.__getattribute__(self.TOKENIZER_TARGET)),
                embedding=self.embedding_model.encode(article.__getattribute__(self.EMBEDDING_TARGET),
                                                      show_progress_bar=False),
                lead=lead,
                summary=summary))
        return preprocessed_list

    def _extract_centroids(self,
                           article_list,
                           labels,
                           topics: Dict[int, list[tuple[str, float]]]) -> Dict[int, Centroid]:
        self.logger.info("extract_centroid start")
        centroids = {}
        for label, article in zip(labels, article_list):
            if label not in centroids:
                centroids[label] = []
            centroids[label].append(article)

        topics[-1] = [('temp', 0)]
        for label, articles_list_in_cluster in centroids.items():
            topic_words = []
            for topics_in_cluster in topics[label]:
                topic_words.append(topics_in_cluster[0])

            preprocessed_list = self.preprocessed_article_repository.find_all_by_article(
                articles=articles_list_in_cluster)
            centroids[label] = self.mds.summarize(article_list=articles_list_in_cluster,
                                                  preprocessed_list=preprocessed_list,
                                                  topics=topic_words)

        return centroids

    def _make_labeled_clusters(self,
                               labels,
                               t_datetime,
                               counts: dict[int, int],
                               topic_words: Dict[int, list[tuple[str, float]]],
                               centroids: Dict[int, Centroid],
                               section_id):
        self.logger.info("make_labeled_cluster start")
        labeled_clusters = {}
        for label in labels:
            if label != -1:
                new_topic = Cluster(regdate=t_datetime,
                                    img_url=centroids[label].article.img_url,
                                    title=centroids[label].article.title,
                                    summary=centroids[label].summary,
                                    words=','.join([e[0] for e in topic_words[label]]),
                                    size=counts[label],
                                    centroid_id=centroids[label].article.article_id,
                                    section_id=section_id,
                                    related_cluster_id=None)
                labeled_clusters[label] = new_topic

        return labeled_clusters

    def _make_preprocessed_clusters(self,
                                    labels,
                                    labeled_clusters,
                                    centroids: Dict[int, Centroid],
                                    topic_words):
        self.logger.info("make_preprocessed_clusters start")
        labeled_preprocessed_clusters = {}
        for label in labels:
            if label != -1:
                preprocessed_article = self.preprocessed_article_repository.find_all_by_article(centroids[label].article)
                if len(preprocessed_article) > 0:
                    new_pre_cluster = PreprocessedCluster(cluster_id=labeled_clusters[label].cluster_id,
                                                          embedding=preprocessed_article[0].embedding,
                                                          words=[e[0] for e in topic_words[label]])
                    labeled_preprocessed_clusters[label] = new_pre_cluster

        return labeled_preprocessed_clusters