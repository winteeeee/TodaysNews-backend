from src.main.message_queue.mq_consumer import MessageQueueConsumer
from src.main.preprocessor.preprocessor import Preprocessor

if __name__ == '__main__':
    preprocessor = Preprocessor()
    consumer = MessageQueueConsumer(preprocessor.preprocess)
    consumer.start()
