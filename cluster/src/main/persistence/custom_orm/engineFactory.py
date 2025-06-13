import sqlalchemy, yaml, os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker


class EngineFactory:
    def __new__(cls):
        if not hasattr(cls, '_instance'):
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        self.__engine = None
        self.DB_USERNAME = os.getenv('DB_USERNAME', '')
        self.DB_PASSWORD = os.getenv('DB_PASSWORD', '')
        self.DB_HOST = os.getenv('DB_HOST', '192.168.0.6')
        self.DB_PORT = os.getenv('DB_PORT', '3306')
        self.DB_NAME = os.getenv('DB_NAME', 'todays_news')

        if self.__engine is None:
            self.__engine = create_engine(f'mysql+mysqlconnector://{self.DB_USERNAME}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}')

    def get_engine(self):
        return self.__engine

    def get_session(self) -> sqlalchemy.orm.session:
        db_session = sessionmaker(self.__engine)
        return db_session()
