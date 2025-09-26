```
БД и драйвер. Postgres + R2DBC?
Дефолт: PostgreSQL (r2dbc-postgresql).

Вариант outbox. Polling (поллер) vs CDC/Debezium.
Дефолт: поллер с FOR UPDATE SKIP LOCKED.

Куда публикуем. Kafka? HTTP (WebClient) в один/несколько сервисов?
Дефолт: Kafka (опционально) + HTTP (несколько клиентов) — оба через реестр паблишеров, автоконфиг по classpath/props.

[//]: # (Семантика доставки. At-least-once? дедупликация на стороне потребителя или в outbox?)

[//]: # (Дефолт: at-least-once, дедуп по dedup_key + уникальный индекс.)

[//]: # (Ретраи/бэкофф. Параметры: max-retries, base-delay, multiplier.)

[//]: # (Дефолт: экспоненциальный бэкофф, max-retries=10, base-delay=1s, multiplier=2.0.)

Шедулинг. Частота, конкурентность, размер батча.
Дефолт: poll-interval=500ms, batch-size=100, concurrency=4.

Миграции. Liquibase подключаем?
Дефолт: да, если Liquibase в classpath — отдадим changelog; иначе приложим SQL.

[//]: # (Метрики/трейсинг. Micrometer/OTel?)

[//]: # (Дефолт: включим Micrometer таймеры/счётчики, OTel — если в classpath.)
```




```swift

outbox-starter/
  src/main/kotlin/ru/outbox/
    domain/
      model/
        OutboxRecord.kt          // сущность outbox
        Destination.kt           // value-object: kafka:<topic> | http:<clientId>:/path
        OutboxStatus.kt          // NEW, IN_PROGRESS, SENT, FAILED
      // доменная логика минимальна: без ретраев и бэкоффа

    api/                         // порты (интерфейсы)
      OutboxRepository.kt        // порт к БД: insert(), lockBatchAndFetch(), markSent(), markFailed()
      OutboxPublisher.kt         // порт публикации единичного события
      PublisherRegistry.kt       // выбор паблишера по Destination
      OutboxService.kt           // use-case API: enqueue(), pollOnce()

    application/                 // интеракторы/сценарии
      OutboxServiceImpl.kt       // реализация use-case: enqueue(), pollOnce()
      PollingScheduler.kt        // @Scheduled fixedDelay: вызывает service.pollOnce()
      // Внутри pollOnce(): одна транзакция на «бронь» (SELECT ... FOR UPDATE SKIP LOCKED) и обработку батча
      // Параллелизма нет или локальный (coroutines) — без метрик

    infra/
      db/
        R2dbcOutboxRepository.kt // адаптер порт-репозитория под Postgres R2DBC
        OutboxSql.kt             // SQL: insert, select for update skip locked, updates
      http/
        WebClientPublisher.kt    // отправка в несколько HTTP сервисов, отбор по clientId
        HttpClientRegistry.kt    // фабрика WebClient по конфигу outbox.publisher.http.clients
      kafka/
        KafkaPublisher.kt        // отправка в Kafka по topic из destination
      scheduler/
        // (опционально) утилиты для сериализации/логирования ошибок публикации
      // Никаких метрик/OTel

    config/
      OutboxProperties.kt        // ТОЛЬКО poll и publisher.*, без retry/metrics
      OutboxAutoConfiguration.kt // ядро: свойства, repo, registry, publishers, service, scheduler
      OutboxKafkaAutoConfiguration.kt // условная конфигурация KafkaPublisher при наличии Kafka
      OutboxLiquibaseAutoConfiguration.kt // регистрирует changelog, если есть Liquibase

  src/main/resources/
    META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    db/changelog/outbox-changelog.yaml   // миграция таблицы (из предыдущего шага)
    sql/outbox-postgres.sql              // альтернативный SQL без Liquibase

  src/test/kotlin/ru/outbox/
    integration/
      OutboxFlowIT.kt            // сценарий: enqueue -> pollOnce -> публикация в Kafka/HTTP -> статус SENT
      HttpPublisherIT.kt         // с MockWebServer
    unit/
      OutboxServiceImplTest.kt   // поведение без ретраев (FAILED при ошибке)
      PublisherRegistryTest.kt
      R2dbcOutboxRepositoryTest.kt

```