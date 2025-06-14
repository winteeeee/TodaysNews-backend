# 오늘의 뉴스
## 개요
### 시스템의 목표
오늘의 뉴스는 간결하고 빠른 정보 전달을 위해, 기존 기사를 후처리하여 제공하는 뉴스 플랫폼입니다.
본 시스템은 대한민국 10대 중앙 종합 일간지(경향신문, 국민일보, 동아일보, 문화일보, 서울신문, 세계일보, 조선일보, 중앙일보, 한겨레, 한국일보)의 뉴스들을 대상으로, 뉴스 클러스터링, 클러스터 단위 요약, 연관 클러스터 식별 기능을 제공합니다.

기존 뉴스 플랫폼들은 성장에 따라 사용자에게 많은 기능을 제공하고 있습니다. 하지만 사용자 경험을 해칠 정도의 많은 기능이 존재하여 복잡한 인터페이스를 제공하기도 합니다. 따라서 본 시스템에서는 가볍게 뉴스 플랫폼을 이용하는 사용자에 초점을 맞추어, 사용자 경험을 추구하는 기능을 제공하고자 하였습니다. 본 시스템에서는 아래의 개선점들을 제공합니다.

- 중복된 정보는 모아서 보여준다.
- 한 페이지에서 다양한 정보를 얻을 수 있다.
- 기사 원문을 읽지 않고도 정보를 얻을 수 있다.

## 프로젝트 구조
![오늘의 뉴스 프로젝트 구조](https://github.com/user-attachments/assets/7f06d27b-d37f-4ad7-b09d-2eb2cecb2244)

## 기여 내용
### 커스텀 BERTopic 개발
![커스텀 BERTopic](https://github.com/user-attachments/assets/c597f583-a904-476c-8d6c-17fec769fa76)
수집된 뉴스를 클러스터링하고 토픽을 추출하기 위해 BERTopic 토픽 모델링 기법을 사용합니다.

Sentence BERT, UMAP, HDBSCAN, Mean-Shift, CountVectorizer, c-TF-IDF를 이용합니다.

뉴스 도메인에 적용될 것이므로 추가적으로 노이즈 제거 알고리즘까지 적용해두었습니다.

#### 임베딩
임베딩은 일반적으로 가장 많이 쓰이는 것으로 알려진 Sentence BERT를 사용하였습니다.

#### 차원축소
현재 도메인과 알고리즘 자체의 특성을 고려하여 가장 적합하다고 생각되는 UMAP을 사용했습니다. 

1. 주성분 분석(PCA)
   - 데이터에 가장 가까운 초평면을 구한 다음 차원을 축소시키는 방법입니다.
   - 데이터를 투영시키기 위해 초평면을 구하는 과정이 필요합니다. 이를 위해 데이터의 분산이 최대가 되는 축을 찾아 투영하여 분산을 최대한 보존합니다.
   - 정보 손실을 최소화하고 노이즈에 영향을 받지 않는 특성 추출이 가능합니다.
   - 비선형적인 데이터의 경우 부적합하고 군집된 데이터들이 뭉게집니다.
   - 뉴스라는 도메인을 생각해볼 때 뉴스의 토픽이 무작위로 생성될 것이므로 선형적인 데이터라고 판단하기 어렵습니다. 따라서 PCA는 부적합하다고 생각했습니다.
2. t-분포 확률적 임베딩(t-SNE)
   - t 분포를 활용하여 높은 차원의 복잡한 데이터를 차원 축소하는 방법입니다.
   - 낮은 차원 공간의 시각화에 주로 사용하며 차원 축소할 때는 비슷한 구조끼리 데이터를 정리한 상태이므로 데이터의 구조를 이해하는 데 도움을 줍니다.
   - 비선형적인 차원 축소 방법이고 고차원 벡터의 유사성이 저차원에서도 유지됩니다.
   - 정보의 손실이 발생하고 시간복잡도가 높습니다. 또한 2, 3차원으로만 축소가 가능합니다.
   - 차원 축소보다는 차원의 시각화에 주로 사용된다는 점과 함께, 정보 손실과 시간복잡도가 높다는 점에서 부적합하다고 판단했습니다.
3. 균일 다양체 근사 및 투영(UMAP)
   - 고차원 공간에서의 데이터를 그래프화 시키고 그래프를 투영시켜 차원을 축소시킵니다.
   - 비선형적인 차원축소 방법이며 속도가 빠르고 일반적인 차원 축소 알고리즘입니다. 전체적인 구조 또한 PCA보다 더 잘 보존합니다.
   - 조절할 하이퍼파라미터가 존재합니다.
   - 하이퍼파라미터는 개발 도중 테스트를 통해 조절할 수 있기 때문에 가장 적합한 방법이라고 생각했습니다.  

#### 클러스터링
![HDBSCAN 단점](https://github.com/user-attachments/assets/9ae99569-28fd-466b-bc2c-103d23cad613)
BERTopic의 기본 클러스터링 알고리즘은 HDBSCAN입니다. HDBSCAN만 사용하였을 때 두 밀도가 높은 군집을 잇는 관찰치가 존재할 수 있었습니다. HDBSCAN의 경우 무게 중심 기반 클러스터링이기 때문에 하나의 클러스터로 합쳐질 수 있었습니다.

추가적으로 밀도 기반 클러스터링 기법인 Mean-Shift까지 적용시켜 위와 같은 관찰치에서 명확하게 3개의 군집으로 나눠지도록 하였습니다.

#### 토픽 단어 추출
BERTopic의 기본 사용 기술인 c-TF-IDF를 그대로 사용했습니다. CountVectorizer로 단어의 빈도를 계산한 후 c-TF-IDF로 중요 단어를 식별하여 토픽으로 추출합니다.

#### 노이즈 문서 제거
추출된 토픽과 클러스터링된 결과를 기반으로 노이즈 문서를 제거합니다. 뉴스 플랫폼에서 노이즈 문서의 존재는 사용자 경험을 크게 해칠 수 있다고 판단하였기 때문에 정상 문서가 일부 잘려나가더라도 노이즈 문서를 확실하게 제거할 수 있도록 하였습니다.

![노이즈 제거 이전](https://github.com/user-attachments/assets/38dbc579-218c-402f-a652-cbdc6ee91995)
2023-10-17 경제 섹션에서 클러스터링된(노이즈 제거X) 문서입니다. 2번 인덱스에 있는 뉴스는 토픽과 다소 동떨어져 있지만 같은 클러스터로 묶여 있는 것을 볼 수 있습니다. 이를 노이즈로 식별하고 분리해내는 것이 목표입니다.

#### 1. 토픽 단어 및 TF-IDF 수치 합산
![토픽 단어 수치 합산](https://github.com/user-attachments/assets/3fcfa112-a88d-4a77-99df-87f34b497745)
먼저 추출된 토픽 별로 모든 문서에서의 TF-IDF 수치를 구해 합산하여 각 토픽이 뉴스에서 얼마나 중요한지에 대한 정보를 추출합니다.

#### 2. 주요 토픽 단어 식별
![토픽 TOP3](https://github.com/user-attachments/assets/a81f2d72-daaf-48aa-ac92-867051c649cb)

1번의 과정으로 나온 수치가 가장 높은 3개의 단어를 뽑아 전체 클러스터의 대표 토픽으로 삼습니다.

#### 3. 노이즈 기사 제거
2에서 뽑힌 주요 토픽 단어를 이용하여 모든 뉴스별로 TF-IDF 수치를 다시 합산하여 각 뉴스가 추출된 토픽에 대한 정보를 얼마나 많이 담고 있는지를 확인합니다.

이후 해당 수치가 임계값보다 낮다면 노이즈 기사로 판단(토픽에 대한 정보를 많이 담고 있지 않다고 판단)하여 제거합니다.

#### 4. 결과
![결과](https://github.com/user-attachments/assets/733d1495-28b8-4902-9ae2-fb9cbc1859f7)
알고리즘 적용 후 정상적으로 2번 인덱스에 있는 뉴스가 노이즈 뉴스로 제거된 것을 확인할 수 있습니다.

### 뉴스 클러스터 다중 문서 요약 알고리즘 고안
추출된 뉴스 클러스터의 다중 문서 요약 알고리즘을 구현하여 사용자가 뉴스 클러스터를 클릭하였을 때 해당 클러스터의 정보를 한 눈에 알아볼 수 있도록 하였습니다.

#### 뉴스의 구조
뉴스 클러스터의 다중 문서 요약 알고리즘을 구현하기 위해서는 먼저 뉴스라는 도메인에 대한 이해가 필요하다고 생각했습니다.

![뉴스의 구조](https://github.com/user-attachments/assets/9d2eb604-d193-46fc-8126-135f4422d8ce)

위는 뉴스의 구조를 나타내고 있습니다. 다른 문서와 다른 뉴스만의 특징은

1. 뉴스는 전문 인력(기자)가 직접 작성하므로 타 문서에 비해 문서의 품질이 매우 우수하다.
2. 뉴스의 경우 역피라미드 구조라는 정형화된 구조가 존재한다.
3. 제목과 리드에는 기자가 직접 작성한 전체 뉴스의 요약본이 존재한다.

로 식별하고 이를 바탕으로 로직을 설계하기 시작했습니다.

#### 어떤 요약문을 뽑을 것인가?
다중 문서 요약의 쟁점 사항은 아래와 같습니다.

1. 어떤 문서를 선택할 것인가?
2. 요약문은 어떻게 도출할 것인가?

먼저 뉴스 특성1에 의해 굳이 복수 개의 뉴스를 선택할 필요없이 잘 작성된 뉴스 하나로도 충분히 전체 군집을 대표할 수 있을 것이라고 생각했습니다. 쟁점 사항 2의 경우 뉴스의 특정 2, 3에 주목했습니다. 해당 특성 때문에 뉴스는 전체 문서와 동시에 정답인 요약문이 동시에 존재하는 문서로 볼 수 있습니다. 따라서 AI가 문서 요약을 학습할 때 사용하는 요약 메트릭인 ROUGE와 RDASS를 다중 문서 요약에서 이용할 수 있을 것이라 생각했습니다.

#### 알고리즘 구상
![다중 문서 요약 알고리즘](https://github.com/user-attachments/assets/141d6895-8aab-4607-a7ee-064f56185989)

최종적으로 도출된 알고리즘은 위와 같습니다. 

1. 뉴스 클러스터를 입력받습니다.
2. 딥러닝 모델(본 시스템에서는 koBART 사용)을 이용하여 본문의 후반부 요약문을 추출합니다.
   - 후반부 요약문을 이용하는 이유는 제목 및 리드와 내용 상으로 겹치지 않게 하기 위해서입니다.
   - 또한 리드와 제목을 요약의 메트릭으로 이용하는 것이므로 제목과 리드를 포함하지 않은 채로 메트릭을 적용시키는 쪽이 더 자연스럽다고 생각했습니다.
3. 이후 각 문서별로 추출된 제목, 리드, 요약문을 기반으로 ROUGE와 RDASS 점수를 각 뉴스별로 산출합니다.
   - 만일 모델이 요약문을 잘 생성했다면 ROUGE와 RDASS 점수가 높게 나올 것입니다.
   - ROUGE와 RDASS 점수가 가장 높은 요약문을 선택한다면 요약문의 품질이 일정 이상 보장될 것이라 생각할 수 있습니다.
4. 요약 메트릭의 점수가 가장 높은 요약문을 택하고 해당 뉴스의 제목과 리드를 붙여 전체 요약문으로 제시합니다.
   - 선택된 뉴스의 리드와 제목을 직접 가져다 사용하는 것이 전체 요약문의 시점에서 보았을 때 가장 자연스러운 요약문이 될 것이라 판단했습니다.
  
#### 요약문 예시
![image](https://github.com/user-attachments/assets/fe7ec7db-f772-4eed-ab25-8d5593a1d416)
1. 정치
   - 김정은 북한 국무위원장과 블라디미르 푸틴 러시아 대통령 정상 회담이 공식 발표됐다. 김 위원장은 심야 또는 12일 러시아 극동 블라디보스토크에 도착해 푸틴 대통령과 회담할 것으로 예상되며 두 사람이 만나게 된다면 2019년 4월 북러 정상회담 이후 4년 5개월 만에 재회하게 된다.
2. 경제
   - LG가 국제박람회기구(BIE) 총회 개최지인 프랑스 파리에서 2030 세계박람회(엑스포)의 부산 유치를 홍보하는 광고에 나섰다. LG는 E 총회를 파리에서 개최하고 2030년 엑스포 개최지를 발표하게 되며 이시레몰리노 지역 인근 광고판 110개를 통해 부산엑스포 유치 홍보 활동을 펼쳤다.
3. 사회
   - 사기를 칠 목적으로 ‘가짜 온라인 쇼핑몰’을 운영해 총 9억원 이상을 가로챈 일당이 경찰에 붙잡혔다. 가짜 쇼핑몰 사이트로 접속을 유도한 뒤 계좌이체를 하는 수법으로 범행을 저지른 일당은 피해자에게 신뢰감을 주기 위해 타인의 신분증을 도용해 유명 쇼핑몰에 실제 TV와 냉장고 등 전자제품 판매자인 것처럼 허위 등록했다.

### 연관 문서 군집 탐색 알고리즘 개발
![image](https://github.com/user-attachments/assets/2912ec04-b132-4645-a3e3-d10c0dbea027)
```python
    def find_related_cluster(self, target_cluster: Cluster, limit_date: date) -> list[Cluster]:
        """연관 클러스터 탐색
        주어진 클러스터와 가장 유사한 클러스터를 추출한다.
        기준 클러스터의 임베딩과 DB에서 조회한 클러스터의 임베딩의 코사인 유사도를 측정하여 유사도 스코어를 산출한다.
        기준 클러스터의 토픽과 DB에서 조회한 클러스터의 토픽과의 일치도를 비교하여 토픽 스코어를 산출한다.
        두 스코어의 합을 기준으로 연관 클러스터를 판단한다.

        :param target_cluster: 기준이 될 클러스터
        :param limit_date: DB에서 조회할 날짜 제한(금일부터 limit_date까지만을 조회)
        :return: 기준 클러스터와 가장 유사하다고 판단되는 클러스터 모델 (최대 3개)
        """
        duration: tuple[date, datetime] = (limit_date, target_cluster.regdate - timedelta(days=1))
        preprocessed_target: PreprocessedCluster = self._preprocessed_cluster_repository.find_all_by_cluster(target_cluster)[0]
        clusters: list[Cluster] = self._cluster_repository.find_all_by_section_id_and_duration(section_id=target_cluster.section_id,
                                                                                               duration=duration)
        preprocessed_clusters: list[PreprocessedCluster] = self._preprocessed_cluster_repository.find_all_by_cluster(clusters=clusters)
        relational_clusters: list[tuple[float, Cluster]] = []

        for cluster, preprocessed_cluster in zip(clusters, preprocessed_clusters):
            embedding = preprocessed_cluster.embedding
            words = preprocessed_cluster.words
            sim_score: float = self._cal_cos_sim(preprocessed_target.embedding, embedding)
            topic_score: int = self._is_matching(preprocessed_target.words, words)
            # topic_score의 최대치는 3, sim_score의 최대치는 1이므로 sim_score에 3 가중치 설정

            relational_clusters.append((sim_score * 3 + topic_score, cluster))

        relational_clusters = sorted(relational_clusters, key=lambda x: x[0], reverse=True)
        result_list: list[Cluster] = []
        for idx, element in enumerate(relational_clusters):
            if idx == self.MAX_OF_RELATIONAL:
                break

            score, cluster = element
            if score > self._threshold:
                result_list.append(cluster)

        return result_list
```

식별된 뉴스 군집과 가장 유사한 군집을 찾아냅니다.

현재 군집과 유사한 군집이 가질 수 있는 특징은 아래와 같다고 생각했습니다.

비슷한 주제를 다룰 것이므로 임베딩의 코사인 유사도가 유사할 것이다.
비슷한 토픽이 뽑혔을 가능성이 높다.

따라서 위 두 가지 척도를 기반으로 특정 기간 내에 생성된 뉴스 군집을 대상으로 코사인 유사도 점수와 토픽 일치 점수를 뽑아 최상위 3개의 뉴스 군집을 추출하였습니다.

### 뉴스 전처리 아키텍처 개선
최초로 개발되었을 때 Crawler, Preprocessor, Cluster, Scheduler가 모두 하나로 합쳐져 있는 상태였습니다.

그에 따라 아래와 같은 문제점이 발생했습니다.

- 확장성 부족
  - 단 하나의 Worker 내부에서 멀티프로세스로 모듈들이 동작 중인 상태
  - 작업의 병렬성이 없어 Worker를 늘리기 어려움
- 느린 작업 속도
  - 모든 로직이 순차적으로 수행되므로 Scale-out에 따른 작업 속도 증가를 볼 수 없음
- 단일 장애 지점
  - Worker를 다중으로 배치할 수 없으니 단일 장애 지점화
- 재시도 로직 부재
  - Worker에서 수행되는 작업은 오랜 시간이 걸리는 무거운 작업들이 대부분
  - 기타 사유로 작업이 실패하거나 서버가 죽을 경우 해당 Worker가 맡고 있는 작업 자체가 유실

#### 작업 병렬화
느린 작업 속도, 단일 장애 지점은 결국 Worker의 확장성이 매우 크게 떨어지는 상태이기 때문에 발생하는 문제입니다. 그리고 Worker의 확장성이 떨어지는 이유는 수행하는 작업이 병렬적으로 수행될 수가 없는 상태였기 때문입니다.

따라서 가장 먼저 해결해야할 과제는 작업을 병렬화 시키는 것라고 생각했습니다.

또한 각 모듈을 분리하였습니다. 이를 통해 각 기능에 맞춰 독립적인 확장이 가능해지고, 서비스 사이의 의존성을 낮출 수 있을 것이라고 생각했습니다.

```python
# Crawler Worker
    def crawling(self, t_date: date) -> (list, list):
        article_list = []
        
        # self.conf['CODE']에 전체 언론사 정보가 담겨있다
        # 결론적으로 전체 언론사에 대한 크롤링을 수행하는 구조
        for press in self.conf['CODE']:
            code = self.conf['CODE'][press]

            self.logger.debug(str(press) + ' crawling start..')
            news_url_list = self.get_updated_url_list(code, t_date)
            cur_article_list = self.get_news_list(news_url_list, press)
            article_list += cur_article_list

        return article_list

->

	# 크롤링을 수행할 언론사를 외부에서 주입받게 변경
    def _crawling(self, t_date: date, press: str) -> (list, list):
        article_list = []

        code = self.conf['CODE'][press]
        self.logger.debug(str(press) + ' crawling start..')
        news_url_list = self.get_updated_url_list(code, t_date)
        cur_article_list = self.get_news_list(news_url_list, press)
        article_list += cur_article_list

        return article_list
```
하나의 Crawler 프로세스가 전체 언론사에 대한 뉴스를 크롤링 하는 것이 아닌 특정 한 언론사에 대한 크롤링을 담당하도록 하여 병렬적으로 여러 언론사의 기사를 크롤링할 수 있도록 변경하였습니다.

```python
# Cluster

# 모든 섹션을 대상으로 차례대로 클러스터링 수행
for section_name in self.section_id.keys():
	self._clustering(section_name, t_date)

->

self._clustering(section_name, t_date)
```
클러스터링 역시 전체 섹션이 아닌 한 섹션에 대해 수행하도록 변경하여 여러 Cluster Worker에 배치되면 클러스터링의 속도 또한 빨라질 수 있도록 하였습니다.

또한, Crawler와 Cluster에는 크롤링, 클러스터에 대한 로직 뿐만 아니라 뉴스의 전처리에 대한 로직이 혼재되어 있었습니다. 이는 단일 책임 원칙을 위반하여 유지보수성을 떨어뜨린다고 판단하여  preprocessor라는 별도의 모듈로 분리하였습니다.

#### 메시지 큐 도입
이전 단계에서 각 모듈들을 분리해냈습니다. 각 모듈은 스케줄러로부터 작업을 수행하라는 명령을 받아야 하고, 또 전처리 로직의 경우 크롤러와 클러스터와의 통신이 필요했습니다.

모듈 간의 통신은 HTTP를 이용한 API 호출과 메시지 큐 정도로 구현할 수 있을텐데 MQ를 사용하면 재시도 로직까지 같이 구현할 수 있으므로 MQ를 채택했습니다.

#### RabbitMQ 관련 트러블슈팅
```python
class MessageQueueConsumer:
    def __init__(self, task_callback):
        self.host = os.getenv('MQ_HOST', 'localhost')
        self.queue = os.getenv('MQ_QUEUE', 'crawler')
        self.logger = get_logger('MessageQueueConsumer')
        self.task_callback = task_callback
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=self.host))
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=self.queue, durable=True)
        self.channel.basic_qos(prefetch_count=1)
        self.channel.basic_consume(queue=self.queue, on_message_callback=self.callback)

    def start(self):
        self.logger.info('waiting message')
        self.channel.start_consuming()

    def _send_heartbeat(self):
        interval = 30
        if self.connection.is_open:
            self.connection.process_data_events(time_limit=interval)

    def callback(self, ch, method, properties, body):
        try:
            data = body.decode('utf-8')
        except UnicodeDecodeError:
            data = pickle.loads(body)

        self.logger.info(f"Task Request - {data}")
        task_thread = threading.Thread(target=self.task_callback, args=(data,), daemon=True)
        task_thread.start()
        while task_thread.is_alive():
            self._send_heartbeat()
        self.logger.info(f"Task Done - {data}")
        # 작업 완료 ACK 전송
        ch.basic_ack(delivery_tag=method.delivery_tag)
```
MQ로부터 메세지를 수신하고 작업을 수행하는 컨슈머 클래스입니다.

큐를 정의할 때 durable을 True로 줌으로서 MQ 자체의 장애에 대한 고려를 해주었습니다. basic_qos의 prefetch_count는 1로 설정하였는데, 각 워커의 작업의 수행시간이 매우 길기 때문에 1개를 초과하는 메세지를 수신했을 때의 이점이 없다고 생각했기 때문입니다.

생성자로 큐의 메세지를 수신받았을 때 수행할 콜백 함수를 받은 후 실제로 메세지를 수신받을 경우 해당 콜백 함수를 실행하는 구조입니다.

구현하고 테스트하는 도중 문제가 하나 발생했습니다. MQ와 연결된 커넥션은 주기적으로 하트비트를 보내주지 않으면 MQ 측에서 해당 서버에 문제가 발생했다고 판단하여 연결을 끊어버리는 것으로, 현재 시스템의 각 모듈이 담당하고 있는 로직은 모두 상당히 오랜 시간이 걸리는 작업이었기 때문에 이를 반드시 해결할 필요가 있었습니다.

- 대안 1. 작업이 완료될 때마다 새로운 커넥션 만들기
  - 일단 문제는 해결됩니다. 하지만 매번 네트워크 커넥션을 수립하는 것부터가 불필요하고 큰 오버헤드기 때문에 가능한 이 방안은 피하고 싶었습니다.
- 대안 2. 콜백 함수를 수행하는 동시에 멀티스레드로 하트비트를 송신하기
  - 그럴듯해 보였습니다. 하지만 의도한대로 동작하지 않고 다른 에러가 발생했습니다.
  - RabbitMQ는 스레드 당 하나의 커넥션을 유지시키기 때문에 다른 스레드에서는 접근이 불가능하기 때문에 발생하는 문제였습니다. 따라서 대안 2는 선택할 수 없었습니다.
- 대안 3. 콜백 함수를 멀티스레드로 수행하고 메인 스레드에서 하트비트를 송신하기
  - 그럼 반대로 콜백 함수를 멀티스레드로 수행하고 메인 스레드에서 하트비트를 송신하면 되지 않을까? 하는 생각에 다다랐고 정상적인 실행이 되었기 때문에 이를 채택했습니다.
  - 현재 시스템에서 수행하는 작업은 I/O Bound 작업이 대부분이라 파이썬의 GIL를 고려하여도 멀티스레드가 프로세스에 비해 이점이 있다고 판단하였습니다.

```python
class MessageQueueProducer:
    def __init__(self, target_queue: str, message_queue:queue.Queue):
        self.host = os.getenv('MQ_HOST', 'localhost')
        self.target_queue = target_queue
        self.message_queue = message_queue
        self.logger = get_logger('MessageQueueProducer')
        self.connection = None
        self.channel = None

    def start(self):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=self.host))
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=self.target_queue, durable=True)

        while True:
            if not self.message_queue.empty():
                message, need_dump = self.message_queue.get()
                self.send(message, need_dump=need_dump)
            else:
                self._send_heartbeat()

    def _send_heartbeat(self):
        interval = 30
        if self.connection.is_open:
            self.connection.process_data_events(time_limit=interval)

    def send(self, data, need_dump=False):
        self.logger.debug(f'sending {data}')
        if need_dump:
            data = pickle.dumps(data)

        self.channel.basic_publish(
            exchange='',
            routing_key=self.target_queue,
            body=data,
            properties=pika.BasicProperties(
                delivery_mode=pika.DeliveryMode.Persistent
            )
        )
```
이번에는 MQ와 연결되어 메세지를 생성하는 프로듀서 클래스입니다.

이는 전체 작업을 총괄하는 스케줄러가 사용하기도 하지만 크롤러와 클러스터의 작업 이후에 전처리 워커와 통신하는데도 사용됩니다.

스케줄러의 경우 주기적으로 메세지를 보내기만 하고 다른 작업을 수행하지 않기 때문에 문제가 없지만 크롤러의 경우 컨슈머와 마찬가지의 이유로 크롤링이 끝난 뒤 MQ에 메세지를 생성하려고 보면 커넥션이 끊어져 있는 문제가 발생했습니다.

컨슈머에서 해결한 것과 같이 메인 스레드에서 하트비트를 보내는 방향으로 생각했지만 이쪽은 생각할 부분이 더 있었습니다. 동적으로 데이터를 수신받아 MQ에 보내줘야 하기 때문에 스레드 간 데이터를 공유할 방법을 고안해야했다.

이미 IPC 사이에 MQ를 사용하고 있었으므로 동일하게 스레드 사이에도 MQ로 데이터 전달이 될 것이므로 이를 이용하여 해결하였습니다.

### API 서버 성능 개선
구현 완료된 API 서버의 성능 개선을 수행했습니다.

- Entity
  - FetchType이 Eager로 되어 있어 불필요한 조인이 발생하는 문제가 있었습니다.
  - FetchType을 Lazy로 변경하여 단일 쿼리로 발생하도록 수정하였습니다.
- Repository
  - Repository에서는 JPQL의 최적화 및 DB 레벨에서의 인덱스를 생성해두었습니다.
- Service
  - 연관 엔티티가 필요한 비즈니스 로직임에도 불구하고 Fetch Join을 사용하고 있지 않아 N+1 문제가 발생하고 있었습니다,.
  - Fetch Join을 적용시켜 단일 쿼리만 발생하도록 개선하였습니다.

## 화면 시안
### 메인 화면
![image](https://github.com/user-attachments/assets/204408ef-c04f-4c2d-9929-0ad7ef81b33e)

### 섹션 조회 화면
![image](https://github.com/user-attachments/assets/352f856a-85ae-4d6c-801a-805de6c16660)

### 클러스터 조회 화면
![image](https://github.com/user-attachments/assets/382c881e-a383-4b5b-8313-538cd8efec19)



