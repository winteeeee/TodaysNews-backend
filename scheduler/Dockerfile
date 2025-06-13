FROM python:3.10

WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

ENV MQ_HOST=localhost

CMD ["python", "-m", "src.main.main"]
