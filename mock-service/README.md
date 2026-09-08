# mock-service

Заглушечный сервис для эмуляции внешних REST-интеграций  на DEV-стенде и при локальной разработке.

Сервис построен на базе WireMock и запускается в Docker.

## Назначение

`mock-service` используется вместо реальных внешних интеграционных систем.

Пример:

```text
ms-paystub
      |
      | REST
      v
mock-service
      |
      +-- TSRM
      +-- EASUP
      +-- FileStorage
      +-- другие REST-интеграции
```

На DEV-стенде URL внешних интеграций могут быть перенаправлены на `mock-service`.

Например:

```text
TSRM_INTEGRATION_SERVICE_URL=http://mock-service:8080
EASUP_INTEGRATION_SERVICE_URL=http://mock-service:8080
```

## Структура проекта

```text
mock-service/
├── mappings/
│   ├── tsrm/
│   ├── easup/
│   └── ...
│
├── __files/
│   ├── tsrm/
│   ├── easup/
│   └── ...
│
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### mappings

Содержит описание запросов, которые должен обрабатывать WireMock.

Пример:

```text
mappings/tsrm/get-role.json
```

```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/functional-role/[^/]+/[^/]+"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "bodyFileName": "tsrm/get-role-response.json"
  }
}
```

### __files

Содержит тела ответов.

Пример:

```text
__files/tsrm/get-role-response.json
```

```json
{
  "roles": [
    {
      "roleId": "admin",
      "attributeIds": [
        "attr1",
        "attr2",
        "attr3"
      ]
    },
    {
      "roleId": "user"
    }
  ]
}
```

## Запуск

Собрать Docker image:

```bash
docker compose build
```

Запустить сервис:

```bash
docker compose up -d
```

Проверить:

```bash
docker ps
```

Остановить:

```bash
docker compose down
```

Пересобрать и перезапустить после изменения mappings:

```bash
docker compose down
docker compose build
docker compose up -d
```

Либо:

```bash
docker compose up -d --build
```

## Локальный адрес

По умолчанию сервис доступен по адресу:

```text
http://localhost:8089
```

## Проверка заглушки

Пример запроса TSRM:

```bash
curl http://localhost:8089/functional-role/test-user/WEB
```

Пример ответа:

```json
{
  "roles": [
    {
      "roleId": "admin",
      "attributeIds": [
        "attr1",
        "attr2",
        "attr3"
      ]
    },
    {
      "roleId": "user"
    }
  ]
}
```

## WireMock Admin API

Посмотреть все загруженные mappings:

```bash
curl http://localhost:8089/__admin/mappings
```

Посмотреть запросы, которые пришли в WireMock:

```bash
curl http://localhost:8089/__admin/requests
```

Это удобно для диагностики проблем с matching запросов.

## Добавление новой заглушки

Для новой REST-интеграции необходимо:

1. Создать mapping:

```text
mappings/<system>/<request-name>.json
```

2. Создать файл ответа:

```text
__files/<system>/<response-name>.json
```

3. В mapping указать:

```json
"bodyFileName": "<system>/<response-name>.json"
```

4. Пересобрать Docker image.

Пример:

```text
mappings/
└── easup/
    └── get-paystub.json

__files/
└── easup/
    └── get-paystub-response.json
```

## Возможности WireMock

В заглушках могут использоваться:

* разные ответы в зависимости от URL;
* query parameters;
* headers;
* request body;
* HTTP 4xx / 5xx;
* задержки ответа;
* эмуляция timeout;
* разные ответы для разных входных данных;
* stateful scenarios;
* последовательности ответов.

Например:

```text
первый запрос  -> 500
второй запрос  -> 500
третий запрос  -> 200
```

Такие сценарии будут добавляться по мере необходимости.

## Dockerfile

Пример:

```dockerfile
FROM wiremock/wiremock:3.13.2

COPY mappings /home/wiremock/mappings
COPY __files /home/wiremock/__files

ENTRYPOINT ["/docker-entrypoint.sh", "--verbose"]
```

## docker-compose.yml

```yaml
services:
  mock-service:
    build:
      context: .
      dockerfile: Dockerfile

    container_name: mock-service

    ports:
      - "8089:8080"

    restart: unless-stopped
```

## DEV

На DEV-стенде `mock-service` запускается как отдельный сервис.

Приложения, для которых необходимо замокировать внешние интеграции, должны использовать URL `mock-service` вместо URL реальных систем.

REST-интеграции добавляются в текущий сервис постепенно.

Эмуляция Kafka-интеграций будет реализована отдельной итерацией.
