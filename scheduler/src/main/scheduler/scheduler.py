import time
import schedule
import yaml

from src.main.message_queue.mq_producer import MessageQueueProducer
from src.main.utils import get_logger

class Scheduler:
    def __init__(self):
        with open('resource/scheduler_config.yml', 'r', encoding='UTF-8') as yml:
            self.conf = yaml.safe_load(yml)
        self.producer_crawler = MessageQueueProducer('crawler')
        self.producer_cluster = MessageQueueProducer('cluster')
        self.logger = get_logger('scheduler')

    def scheduling(self):
        self.logger.info('scheduler start')
        schedule.every(10).minutes.do(self._publish_crawler_message)
        schedule.every(1).hours.do(self._publish_cluster_message)
        schedule.run_all()

        while True:
            # schedule.run_pending()
            time.sleep(60)

    def _publish_crawler_message(self):
        self.logger.info('scheduler publish crawler message')
        press_list = self.conf['CRAWLER']['PRESS']
        for press in press_list:
            self.producer_crawler.send(press)

    def _publish_cluster_message(self):
        self.logger.info('scheduler publish cluster message')
        section_list = self.conf['CLUSTER']['SECTION']
        for section in section_list:
            self.producer_cluster.send(section)
