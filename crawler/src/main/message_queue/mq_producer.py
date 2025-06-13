import pika, pickle, os, queue

from src.main.utils import get_logger


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
