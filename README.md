


```
file-info-starter/                  # Главный Gradle-модуль (spring boot starter)
│
├── build.gradle.kts                # Gradle build file
├── settings.gradle.kts
├── README.md
│
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── ru/app/
│   │   │       ├── api/                  # Внешние API-контракты (DTO)
│   │   │       ├── domain/               # Бизнес-логика (Entities, Service interfaces, Use Cases)
│   │   │       ├── infrastructure/       # Работа с БД, WebClient, адаптеры
│   │   │       ├── config/               # Конфигурация и AutoConfiguration
│   │   │       └── FileInfoAutoConfiguration.kt # Главная точка старта авто-конфига
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   └── db/
│   │   │       └── changelog/
│   │   │           └── 01-init-tables.xml    # Liquibase миграция
│
│   ├── test/
│   │   ├── kotlin/
│   │   │   └── ru/app/
│   │   │       ├── api/
│   │   │       ├── domain/
│   │   │       ├── infrastructure/
│   │   │       └── integration/          # Интеграционные тесты (Testcontainers, Kotest)
│   │   └── resources/
│   │       └── application-test.yml
│
└── src/
└── autoconfigure/
└── main/
└── resources/
└── META-INF/
└── spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

