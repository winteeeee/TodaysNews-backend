import queue
import threading

import yaml

from datetime import datetime, date

from src.main.message_queue.message import Message
from src.main.message_queue.mq_producer import MessageQueueProducer
from src.main.persistence.models import Article
from src.main.persistence.repository import ArticleRepository, SectionRepository
from .util import soupMaker, remove_tag, simply_ws, only_BMP_area
from src.main.utils import get_logger

CONFIG_PATH = 'src/main/resource/config.yml'

class Crawler:
    def __init__(self):
        self.conf = {}
        self.load_config()
        self.section_id = {}
        self.logger = get_logger('crawler')
        self.message_queue = queue.Queue()
        self.mq_producer = MessageQueueProducer('preprocessor', self.message_queue)
        producer_thread = threading.Thread(target=self.mq_producer.start, daemon=True)
        producer_thread.start()

    def crawling(self, press: str, t_date: date = None):
        if t_date is None:
            t_date = date.today()
        self.load_section()
        self.logger.info("crawling start")

        article_repository = ArticleRepository()
        limit = article_repository.find_by_press(press)
        if limit is None:
            limit = ''

        try:
            article_list = self._crawling(t_date, press, limit)
        except Exception as e:
            self.logger.debug(f"crawling error: {press}")

        if article_list:
            article_repository.insert(article_list)
            message = Message('preprocess', article_list)
            self.message_queue.put((message, True))

        self.logger.info("crawling finished")

    def load_config(self):
        with open(CONFIG_PATH, 'r', encoding='UTF-8') as yml:
            self.conf = yaml.safe_load(yml)

    def load_section(self):
        section_repository = SectionRepository()
        section_list = section_repository.find_all()
        for section in section_list:
            self.section_id[section.section_name] = section.section_id

    def _crawling(self, t_date: date, press: str, limit: str) -> (list, list):
        article_list = []

        code = self.conf['CODE'][press]
        self.logger.debug(str(press) + ' crawling start..')
        news_url_list = self.get_updated_url_list(code, limit, t_date)
        cur_article_list = self.get_news_list(news_url_list, press)
        article_list += cur_article_list

        return article_list

    def get_updated_url_list(self, code, limit, t_date: date) -> list:
        result = []

        loop = True
        req_page = 1
        try:
            while loop:
                page_url = self._make_page_url(code, t_date, req_page)
                page_soup = soupMaker.open_url(url=page_url, logger=self.logger)
                res_page = remove_tag(page_soup.find('div', class_='paging').find('strong'))

                if res_page == str(req_page):
                    url_list = self._find_article_url(page_soup)

                    for cur_url in url_list:
                        if cur_url > limit:
                            result.append(cur_url)
                        else:
                            loop = False
                            break

                    req_page += 1
                else:
                    loop = False
        except Exception as e:
            self.logger.debug(f"unknown error : {page_url}")

        result = sorted(list(set(result)), reverse=True)
        self.logger.debug('find ' + str(len(result)) + ' news')

        return result

    def get_news_list(self, url_list: list, press: str) -> list[Article]:
        article_list = []

        for url in url_list:
            article = self.get_article(url, press)
            if article is not None:
                article_list.append(article)

        return article_list

    def get_article(self, url, press) -> Article:
        soup = soupMaker.open_url(url, logger=self.logger)

        article = None
        try:
            title = only_BMP_area(remove_tag(soup.find('h2', id='title_area')))[:150]
            date_str = soup.find('span', class_='media_end_head_info_datestamp_time')['data-date-time']
            regdate = datetime(year=int(date_str[0:4]), month=int(date_str[5:7]), day=int(date_str[8:10]),
                               hour=int(date_str[11:13]), minute=int(date_str[14:16]), second=int(date_str[17:19]))
            img_url = soup.find('img', id='img1')['data-src']
            soup.find('span', class_='end_photo_org').decompose()
            content_soup = soup.find('div', id='newsct_article')
            strong_list = content_soup.find_all('strong')
            if len(strong_list) > 0:
                strong_list[0].decompose()
            content = only_BMP_area(simply_ws(remove_tag(content_soup)))
            if len(content) < 50 or len(content) > 10000:
                raise Exception
            writer = only_BMP_area(remove_tag(soup.find('span', class_='byline_s')))[:100]
            section_name = remove_tag(soup.find('em', class_='media_end_categorize_item'))

            article = Article(
                regdate=regdate,
                img_url=img_url,
                url=url,
                press=press,
                title=title,
                content=content,
                writer=writer,
                section_id=self.section_id[section_name],
            )
        except:
            pass

        return article

    def _find_article_url(self, soup) -> [str]:
        url_list = []
        targets = ['type06_headline', 'type06']

        for tg in targets:
            ul_tag = soup.find('ul', class_=tg)
            if ul_tag is not None:
                url_list += [attr['href'] for attr in ul_tag.select('a')]

        return url_list

    def _make_page_url(self, code: str, t_date: date, page: int) -> str:
        url = self.conf['HOME'] + code + \
              self.conf['DATE'] + t_date.strftime('%Y%m%d') + \
              self.conf['PAGE'] + str(page)
        return url
