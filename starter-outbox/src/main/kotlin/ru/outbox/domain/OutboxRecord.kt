package ru.outbox.domain

import java.time.OffsetDateTime
import java.util.UUID

data class OutboxRecord(
    val id: UUID = UUID.randomUUID(),
    val aggregateType: String,        // тип агрегата (e.g. "User", "Order")
    val aggregateId: String,          // ID агрегата (строка для универсальности)
    val eventType: String,            // тип доменного события (e.g. "UserRegistered")
    val destination: Destination,     // KAFKA или HTTP
    val payload: String,              // JSON
//    val headers: String = """{}""",   // JSON Map<String,String>
    val headers: Map<String, String> = emptyMap(),
    val status: OutboxStatus = OutboxStatus.NEW,
//    val retries: Int = 0,
    val availableAt: OffsetDateTime = OffsetDateTime.now(),
//    val dedupKey: String? = null,     // для идемпотентности
    val createdAt: OffsetDateTime? = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime? = OffsetDateTime.now(),
    val errorMessage: String? = null // todo add to migration
) {

    companion object {
        fun forKafka(
            aggregateType: String,
            aggregateId: String,
            eventType: String,
            topic: String,
            payload: String,
            headers: Map<String, String> = emptyMap(),
            dedupKey: String? = null,
        ): OutboxRecord = OutboxRecord(
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = eventType,
            destination = Destination.Kafka(topic),
            payload = payload,
            headers = headers,
//            dedupKey = dedupKey
        )

        fun forWebClient(
            aggregateType: String,
            aggregateId: String,
            eventType: String,
            clientId: String,
            path: String,
            payload: String,
            headers: Map<String, String> = emptyMap(),
            dedupKey: String? = null,
        ): OutboxRecord = OutboxRecord(
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = eventType,
            destination = Destination.Http(clientId, path),
            payload = payload,
            headers = headers,
//            dedupKey = dedupKey
        )
    }

    fun markInProgress(now: OffsetDateTime = OffsetDateTime.now()): OutboxRecord =
        copy(status = OutboxStatus.PROCESSING, updatedAt = now, errorMessage = null)

    fun markSent(now: OffsetDateTime = OffsetDateTime.now()): OutboxRecord =
        copy(status = OutboxStatus.SENT, updatedAt = now, errorMessage = null)

    fun markFailed(message: String?, now: OffsetDateTime = OffsetDateTime.now()): OutboxRecord =
        copy(status = OutboxStatus.ERROR, updatedAt = now, errorMessage = message?.take(MAX_ERROR_MESSAGE))

    /** Ограничение для хранения в TEXT, чтобы не захламлять БД бесконечными stacktrace. */
    private val MAX_ERROR_MESSAGE: Int = 8000

}

enum class OutboxStatus { NEW, PROCESSING, SENT, ERROR }
//enum class Destination { KAFKA, HTTP }
