import pika, yaml, pickle, os

from src.main.utils import get_logger


class MessageQueueProducer:
    def __init__(self, target_queue: str):
        self.host = os.getenv('MQ_HOST', 'localhost')
        self.target_queue = target_queue
        self.logger = get_logger(f'MessageQueueProducer: {target_queue}')
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=self.host))
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=target_queue, durable=True)

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
