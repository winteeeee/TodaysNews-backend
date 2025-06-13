FROM python:3.10

WORKDIR /app
COPY requirements.txt .
RUN apt-get update && apt-get install -y openjdk-17-jdk
RUN pip install -r requirements.txt

COPY . .

ENV MQ_HOST=localhost
ENV MQ_QUEUE=preprocessor
ENV DB_USERNAME=''
ENV DB_PASSWORD=''
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=todays_news
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

CMD ["python", "-m", "src.main.main"]
