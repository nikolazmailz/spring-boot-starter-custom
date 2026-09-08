## WireMock может выбирать ответ по:

- HTTP method;
- URL;
- URL pattern;
- query parameters;
- headers;
- JSON body;
- form parameters;
- cookies.

examples:
`match по HTTP method + точному URL path.`
```json
{
  "request": {
    "method": "GET",
    "urlPath": "/users/123"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "id": 123
    }
  }
}
```

`URL pattern` /users/1
```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/users/[0-9]+"
  },
  "response": {
    "status": 200
  }
}
```

`queryParameters` - GET /employees?department=IT&active=true
```json
{
  "request": {
    "method": "GET",
    "urlPath": "/employees",
    "queryParameters": {
      "department": {
        "equalTo": "IT"
      },
      "active": {
        "equalTo": "true"
      }
    }
  },
  "response": {
    "status": 200
  }
}
```

`headers`
```json
{
  "request": {
    "method": "GET",
    "urlPath": "/profile",
    "headers": {
      "X-User-Login": {
        "equalTo": "test-user"
      },
      "X-Channel": {
        "matches": "WEB|MOBILE"
      }
    }
  },
  "response": {
    "status": 200
  }
}
```

`body` для запроса
```json
{
  "employeeId": "123",
  "name": "Test"
}
```
можно
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
  },
  "response": {
    "status": 200
  }
}
```

Можно матчить весь JSON целиком:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/search",
    "bodyPatterns": [
      {
        "equalToJson": {
          "employeeId": "123",
          "active": true
        },
        "ignoreArrayOrder": true,
        "ignoreExtraElements": true
      }
    ]
  },
  "response": {
    "status": 200
  }
}
```
ignoreExtraElements=true означает, что в реальном body могут быть дополнительные поля.

