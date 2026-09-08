# srhr-ms-mock-service

`srhr-ms-mock-service` — централизованный заглушечный сервис для эмуляции внешних REST-интеграций SRHR.

Основное назначение — использование на DEV-стенде, где внешние интеграционные системы недоступны или их вызовы должны быть изолированы.

Сервис построен на базе WireMock и поставляется как Docker image.

---

# Architecture

```text
                      DEV
                       │
        ┌──────────────┴──────────────┐
        │                             │
 srhr-ms-paystub              другие SRHR-сервисы
        │                             │
        ├──────── REST ───────────────┤
        │                             │
        ▼                             ▼
               srhr-ms-mock-service
                      │
                      │ WireMock
                      │
          ┌───────────┼───────────┐
          │           │           │
         TSRM        EASUP     FileStorage
```

Приложение не должно знать, что взаимодействует с WireMock.

Для него `srhr-ms-mock-service` является обычной внешней HTTP-системой.

Пример конфигурации:

```yaml
tsrm:
  integration:
    service:
      url: http://srhr-ms-mock-service:8080

easup:
  integration:
    service:
      url: http://srhr-ms-mock-service:8080
```

---

# Project Structure

```text
srhr-ms-mock-service/
├── mappings/
│   ├── tsrm/
│   ├── easup/
│   ├── filestorage/
│   └── ...
│
├── __files/
│   ├── tsrm/
│   ├── easup/
│   ├── filestorage/
│   └── ...
│
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## `mappings`

Содержит описание HTTP-запросов и правил matching.

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

## `__files`

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

Путь в `bodyFileName` указывается относительно директории `__files`.

---

# Local Run

Собрать image:

```bash
docker compose build
```

Запустить:

```bash
docker compose up -d
```

Проверить контейнер:

```bash
docker ps
```

Остановить:

```bash
docker compose down
```

Пересобрать после изменения mappings:

```bash
docker compose up -d --build
```

По умолчанию сервис доступен локально по адресу:

```text
http://localhost:8089
```

---

# Example Request

Пример TSRM-запроса:

```bash
curl http://localhost:8089/functional-role/test-user/WEB
```

Ответ:

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

---

# Adding a New Mock

Для добавления новой REST-заглушки необходимо:

1. Определить внешнюю систему.
2. Добавить mapping.
3. Добавить response body.
4. Проверить запрос локально.
5. Пересобрать Docker image.

Пример структуры:

```text
mappings/
└── easup/
    └── get-paystub-pdf.json

__files/
└── easup/
    └── get-paystub-pdf-response.json
```

Mapping:

```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/paystub/.+"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "bodyFileName": "easup/get-paystub-pdf-response.json"
  }
}
```

---

# Naming Convention

Mappings рекомендуется называть по бизнес-операции:

```text
get-role.json
get-paystub-pdf.json
create-request.json
get-file.json
```

Response-файлы:

```text
get-role-response.json
get-paystub-pdf-response.json
create-request-response.json
```

Структура должна отражать внешнюю систему:

```text
mappings/<system>/<operation>.json
__files/<system>/<operation>-response.json
```

Пример:

```text
mappings/tsrm/get-role.json
__files/tsrm/get-role-response.json
```

---

# Request Matching

WireMock может выбирать ответ по:

- HTTP method;
- URL;
- URL pattern;
- query parameters;
- headers;
- JSON body;
- form parameters;
- cookies.

Пример matching по query parameter:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/employee",
    "queryParameters": {
      "id": {
        "equalTo": "123"
      }
    }
  }
}
```

Пример matching по body:

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/search",
    "bodyPatterns": [
      {
        "matchesJsonPath": "$[?(@.employeeId == '123')]"
      }
    ]
  }
}
```

---

# Different Responses

Для одного endpoint могут существовать несколько mappings.

Например:

```text
GET /employee/100
→ employee-100.json

GET /employee/200
→ employee-200.json
```

Можно также использовать разные ответы в зависимости от:

```text
path
query
headers
request body
```

---

# Error Responses

WireMock позволяет эмулировать ошибки внешних систем.

Пример `500 Internal Server Error`:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/external/service"
  },
  "response": {
    "status": 500,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "code": "500",
      "message": "Mock external service error"
    }
  }
}
```

---

# Delays

Можно эмулировать медленный внешний сервис.

```json
{
  "response": {
    "status": 200,
    "fixedDelayMilliseconds": 5000
  }
}
```

Это удобно для проверки:

```text
timeouts
retry
circuit breaker
fallback
```

---

# Stateful Scenarios

WireMock поддерживает stateful scenarios.

Например:

```text
request #1 → 500
request #2 → 500
request #3 → 200
```

Это позволяет тестировать retry-механизмы и восстановление внешних систем.

Пример:

```json
{
  "scenarioName": "TSRM retry",
  "requiredScenarioState": "Started",
  "newScenarioState": "SecondAttempt",
  "request": {
    "method": "GET",
    "urlPathPattern": "/functional-role/.*"
  },
  "response": {
    "status": 500
  }
}
```

Следующий mapping может обрабатывать состояние `SecondAttempt`.

---

# WireMock Admin API

WireMock предоставляет Admin API для диагностики.

## Загруженные mappings

```bash
curl http://localhost:8089/__admin/mappings
```

## Полученные запросы

```bash
curl http://localhost:8089/__admin/requests
```

## Сброс истории запросов

```bash
curl -X DELETE http://localhost:8089/__admin/requests
```

Admin API особенно полезен, если сервис получает:

```text
404 No response could be served
```

Через `/__admin/requests` можно увидеть фактический запрос, который пришёл в WireMock.

---

# Troubleshooting

## `404 No response could be served`

Проверить загруженные mappings:

```bash
curl http://localhost:8089/__admin/mappings
```

Проверить реальные запросы:

```bash
curl http://localhost:8089/__admin/requests
```

Чаще всего проблема связана с:

```text
неверным URL
query parameters
headers
HTTP method
body matching
```

## Изменения mapping не применились

Если mappings находятся внутри Docker image:

```bash
docker compose up -d --build
```

## Проверить логи

```bash
docker logs -f srhr-ms-mock-service
```

WireMock запускается в verbose-режиме, поэтому в логах отображается информация о matching запросов.

---

# Docker

## Dockerfile

```dockerfile
FROM wiremock/wiremock:3.13.2

COPY mappings /home/wiremock/mappings
COPY __files /home/wiremock/__files

ENTRYPOINT ["/docker-entrypoint.sh", "--verbose"]
```

## docker-compose.yml

```yaml
services:

  srhr-ms-mock-service:
    build:
      context: .
      dockerfile: Dockerfile

    container_name: srhr-ms-mock-service

    ports:
      - "8089:8080"

    restart: unless-stopped
```

---

# DEV Deployment

На DEV-стенде `srhr-ms-mock-service` запускается как отдельный сервис.

Внешние URL SRHR-сервисов перенаправляются на него через environment/configuration.

Пример:

```yaml
TSRM_INTEGRATION_SERVICE_URL: http://srhr-ms-mock-service:8080
EASUP_INTEGRATION_SERVICE_URL: http://srhr-ms-mock-service:8080
```

Таким образом можно запускать сервисы SRHR без доступа к реальным внешним интеграционным системам.

---

# Current Scope

Текущая версия `srhr-ms-mock-service` предназначена для эмуляции REST-интеграций.

Следующими итерациями могут быть добавлены:

- дополнительные REST-системы;
- сложные stateful scenarios;
- управление сценариями;
- динамические ответы;
- Kafka-интеграции;
- UI/API для переключения режимов заглушек.

Kafka-интеграции не входят в текущий scope и будут рассматриваться отдельно.