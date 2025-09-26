package ru.outbox.application.api

import ru.outbox.domain.Destination
import ru.outbox.domain.OutboxRecord

/**
 * Порт публикации записи в конкретный транспорт (Kafka, HTTP).
 * Реализации живут в infra/{kafka|http}.
 */
interface OutboxPublisher {

    /**
     * Говорит, умеет ли паблишер обработать данный Destination.
     * Например, KafkaPublisher -> только Destination.Kafka.
     */
    fun supports(destination: Destination): Boolean

    /**
     * Публикация события.
     * Должна бросать исключение при неуспехе — сервис отметит FAILED.
     */
    suspend fun publish(record: OutboxRecord)
}
