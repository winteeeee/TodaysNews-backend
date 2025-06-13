from src.main.crawler import Crawler
from src.main.message_queue.mq_consumer import MessageQueueConsumer

if __name__ == '__main__':
    crawler = Crawler()
    consumer = MessageQueueConsumer(crawler.crawling)
    consumer.start()
