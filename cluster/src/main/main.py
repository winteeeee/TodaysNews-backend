from src.main.cluster.cluster_maker import ClusterMaker
from src.main.message_queue.mq_consumer import MessageQueueConsumer

if __name__ == '__main__':
    # TODO Dockerfile 작성
    cluster_maker = ClusterMaker()
    consumer = MessageQueueConsumer(cluster_maker.clustering)
    consumer.start()