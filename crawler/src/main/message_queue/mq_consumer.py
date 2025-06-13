import threading

import pika, pickle, os

from src.main.utils import get_logger

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
