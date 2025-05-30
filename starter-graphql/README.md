### Application.yaml
```
employee:
  graphql-client:
    endpoint: "https://example.com/graphql"

```

```
├── starter-graphql/
│   ├── README.md
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── kotlin/
│       │   │   └── ru/
│       │   │       └── app/
│       │   │           ├── api/
│       │   │           │   ├── UserClient.kt
│       │   │           │   └── UserInfo.kt
│       │   │           ├── config/
│       │   │           │   ├── UserClientAutoConfiguration.kt
│       │   │           │   └── UserClientProperties.kt
│       │   │           └── infra/
│       │   │               ├── GraphQLResponse.kt
│       │   │               └── GraphQLUserClientImpl.kt
│       │   └── resources/
│       └── test/
│           ├── kotlin/
│           │   └── ru/
│           │       └── app/
│           │           └── infra/
│           │               └── GraphQLUserClientImplIT.kt
│           └── resources/
│               └── response.json

```