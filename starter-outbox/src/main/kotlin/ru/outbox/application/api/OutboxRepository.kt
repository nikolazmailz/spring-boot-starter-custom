package ru.outbox.application.api

import ru.outbox.domain.OutboxRecord
import java.util.UUID

/**
 * Порт доступа к хранилищу outbox.
 * Реализация в infra/db через R2DBC.
 */
interface OutboxRepository {

    /**
     * Вставка новой записи (status = NEW).
     */
    suspend fun insert(record: OutboxRecord)

    /**
     * Забрать батч записей к обработке:
     * - выбрать по индексу (status=NEW AND available_at <= now()),
     * - заблокировать SELECT ... FOR UPDATE SKIP LOCKED,
     * - обновить статус на IN_PROGRESS,
     * - вернуть доменные записи.
     *
     * Важно: вся операция должна быть атомарной (в транзакции).
     */
    suspend fun lockBatchAndFetch(limit: Int): List<OutboxRecord>

    /**
     * Отметить запись как успешно отправленную.
     */
    suspend fun markSent(id: UUID)

    /**
     * Отметить запись как неуспешную (без ретраев).
     */
    suspend fun markFailed(id: UUID, errorMessage: String?)
}
