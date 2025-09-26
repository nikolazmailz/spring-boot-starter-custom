package ru.outbox.infra.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import ru.outbox.application.api.OutboxRepository
import ru.outbox.domain.Destination
import ru.outbox.domain.OutboxRecord
import ru.outbox.domain.OutboxStatus
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class R2dbcOutboxRepository(
    private val client: DatabaseClient,
    private val objectMapper: ObjectMapper
) : OutboxRepository {

    private val log = KotlinLogging.logger {}

    override suspend fun insert(record: OutboxRecord) {
        client.sql(OutboxSql.INSERT)
            .bind("id", record.id)
            .bind("aggregateType", record.aggregateType)
            .bind("aggregateId", record.aggregateId)
            .bind("eventType", record.eventType)
            .bind("destination", record.destination.toString())
            .bind("payload", record.payload)
            .bind("headers", serializeHeaders(record.headers)) // jsonb
            .bind("status", record.status.name)
            .bind("availableAt", record.availableAt)
//            .bind("dedupKey", record.dedupKey)
            .fetch().rowsUpdated()
            .awaitSingle()
            .also { updated ->
                if (updated.toInt() != 1) {
                    log.warn { "Unexpected rowsUpdated on insert: $updated for id=${record.id}" }
                }
            }
    }

    /**
     * ВАЖНО: метод ожидается вызванным в рамках @Transactional (корутинная транзакция R2DBC).
     * Возвращаем список уже помеченных IN_PROGRESS строк.
     */
    override suspend fun lockBatchAndFetch(limit: Int): List<OutboxRecord> {
        if (limit <= 0) return emptyList()

        return client.sql(OutboxSql.LOCK_BATCH_AND_FETCH)
            .bind("limit", limit)
            .map { row, _ -> mapRow(row) }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun markSent(id: UUID) {
        client.sql(OutboxSql.MARK_SENT)
            .bind("id", id)
            .fetch().rowsUpdated()
            .awaitSingle()
    }

    override suspend fun markFailed(id: UUID, errorMessage: String?) {
        client.sql(OutboxSql.MARK_FAILED)
            .bind("id", id)
            .bind("errorMessage", errorMessage ?: "")
            .fetch().rowsUpdated()
            .awaitSingle()
    }

    // ---------- helpers ----------

    private fun serializeHeaders(headers: Map<String, String>): String =
        objectMapper.writeValueAsString(headers)

    /**
     * Маппер строки БД -> доменная модель.
     * Аккуратно читаем JSONB headers: драйвер может вернуть String/Json/Map.
     */
    @Suppress("UNCHECKED_CAST")
    private fun mapRow(row: io.r2dbc.spi.Row): OutboxRecord {
        val id: UUID = row.get("id", UUID::class.java)!!
        val aggregateType: String = row.get("aggregate_type", String::class.java)!!
        val aggregateId: String = row.get("aggregate_id", String::class.java)!!
        val eventType: String = row.get("event_type", String::class.java)!!
        val destinationRaw: String = row.get("destination", String::class.java)!!
        val payload: String = row.get("payload", String::class.java)!!

        val headers: Map<String, String> = when (val raw = row.get("headers")) {
            null -> emptyMap()
            is String -> readHeaders(raw)
            is Map<*, *> -> raw as Map<String, String>
            else -> {
                // Например, io.r2dbc.postgresql.codec.Json
                val text = raw.toString()
                readHeaders(text)
            }
        }

        val status: OutboxStatus = row.get("status", String::class.java)!!
            .let { OutboxStatus.valueOf(it) }

        val availableAt: OffsetDateTime =
            row.get("available_at", OffsetDateTime::class.java)!!

//        val dedupKey: String? = row.get("dedup_key", String::class.java)
        val errorMessage: String? = row.get("error_message", String::class.java)
        val createdAt: OffsetDateTime? = row.get("created_at", OffsetDateTime::class.java)
        val updatedAt: OffsetDateTime? = row.get("updated_at", OffsetDateTime::class.java)

        return OutboxRecord(
            id = id,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = eventType,
            destination = Destination.parse(destinationRaw),
            payload = payload,
            headers = headers,
            status = status,
            availableAt = availableAt,
//            dedupKey = dedupKey,
            errorMessage = errorMessage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun readHeaders(json: String): Map<String, String> =
        if (json.isBlank() || json == "{}") emptyMap() else objectMapper.readValue(json)
}
