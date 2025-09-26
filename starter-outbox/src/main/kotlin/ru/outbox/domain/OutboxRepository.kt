//package ru.outbox.domain
//
//import io.r2dbc.spi.Row
//import io.r2dbc.spi.RowMetadata
//import kotlinx.coroutines.reactive.awaitFirstOrNull
//import kotlinx.coroutines.reactor.awaitSingle
//import org.springframework.r2dbc.core.DatabaseClient
//import org.springframework.stereotype.Repository
//import java.time.OffsetDateTime
//import java.util.UUID
//
//@Repository
//class OutboxRepository(private val client: DatabaseClient) {
//
//    private fun map(row: Row, meta: RowMetadata) = OutboxRecord(
//        id = row.get("id", UUID::class.java)!!,
//        aggregateType = row.get("aggregate_type", String::class.java)!!,
//        aggregateId = row.get("aggregate_id", String::class.java)!!,
//        eventType = row.get("event_type", String::class.java)!!,
//        destination = enumValueOf(row.get("destination", String::class.java)!!),
//        payload = row.get("payload", String::class.java)!!,
//        headers = row.get("headers", String::class.java)!!,
//        status = enumValueOf(row.get("status", String::class.java)!!),
//        retries = row.get("retries", java.lang.Integer::class.java)!!.toInt(),
//        availableAt = row.get("available_at", OffsetDateTime::class.java)!!,
//        dedupKey = row.get("dedup_key", String::class.java),
//        createdAt = row.get("created_at", OffsetDateTime::class.java)!!,
//        updatedAt = row.get("updated_at", OffsetDateTime::class.java)!!
//    )
//
//    suspend fun insert(r: OutboxRecord) {
//        client.sql(
//            """
//            insert into outbox(
//              id, aggregate_type, aggregate_id, event_type, destination, payload, headers, status,
//              retries, available_at, dedup_key, created_at, updated_at
//            ) values (:id,:aggrType,:aggrId,:evt,:dest,:payload,:headers,:status,
//                      :retries,:availableAt,:dedupKey, now(), now())
//            """.trimIndent()
//        )
//            .bind("id", r.id)
//            .bind("aggrType", r.aggregateType)
//            .bind("aggrId", r.aggregateId)
//            .bind("evt", r.eventType)
//            .bind("dest", r.destination.name)
//            .bind("payload", r.payload)
//            .bind("headers", r.headers)
//            .bind("status", r.status.name)
//            .bind("retries", r.retries)
//            .bind("availableAt", r.availableAt)
////            .bind("dedupKey", r.dedupKey)
//            .fetch().rowsUpdated().awaitSingle()
//    }
//
//    /**
//     * Забираем пачку NEW/ERROR(перевыставленных) сообщений к обработке
//     * с блокировкой строк и SKIP LOCKED.
//     */
//    suspend fun lockBatchForProcessing(limit: Int): List<OutboxRecord> =
//        client.sql(
//            """
//            update outbox
//               set status = 'PROCESSING', updated_at = now()
//             where id in (
//               select id from outbox
//                where status in ('NEW','ERROR')
//                  and available_at <= now()
//                order by available_at
//                limit :limit
//                for update skip locked
//             )
//            returning *
//            """.trimIndent()
//        )
//            .bind("limit", limit)
//            .map(::map)
//            .all()
//            .collectList()
//            .awaitSingle()
//
//    suspend fun markSent(id: UUID) {
//        client.sql("update outbox set status='SENT', updated_at=now() where id=:id")
//            .bind("id", id).fetch().rowsUpdated().awaitSingle()
//    }
//
//    suspend fun markErrorAndReschedule(id: UUID, retries: Int, nextAt: OffsetDateTime) {
//        client.sql(
//            """
//            update outbox
//               set status='ERROR', retries=:retries, available_at=:nextAt, updated_at=now()
//             where id=:id
//            """.trimIndent()
//        )
//            .bind("id", id)
//            .bind("retries", retries)
//            .bind("nextAt", nextAt)
//            .fetch().rowsUpdated().awaitSingle()
//    }
//
//    suspend fun existsByDedupKey(dedupKey: String): Boolean =
//        client.sql("select 1 from outbox where dedup_key=:k limit 1")
//            .bind("k", dedupKey)
//            .map { _, _ -> 1 }.first().awaitFirstOrNull() != null
//}