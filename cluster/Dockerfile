FROM python:3.10

WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

ENV MQ_HOST=localhost
ENV MQ_QUEUE=crawler
ENV DB_USERNAME=''
ENV DB_PASSWORD=''
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=todays_news

CMD ["python", "-m", "src.main.main"]
