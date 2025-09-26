package ru.outbox.infra.db

/**
 * SQL-константы для работы с таблицей outbox (PostgreSQL).
 *
 * ВАЖНО: бронь батча выполнена атомарно в одном UPDATE через CTE.
 */
object OutboxSql {

    // Вставка новой записи (status = NEW)
    const val INSERT = """
        insert into outbox(
          id, aggregate_type, aggregate_id, event_type, destination, payload, headers,
          status, available_at, dedup_key, error_message, created_at, updated_at
        ) values (
          :id, :aggregateType, :aggregateId, :eventType, :destination, :payload, 
          cast(:headers as jsonb), :status, :availableAt, :dedupKey, null, now(), now()
        )
    """

    /**
     * Атомарная бронь + выборка батча:
     * 1) По индексу берём нужные id (NEW && available_at <= now()) с блокировкой (SKIP LOCKED).
     * 2) Обновляем их статус на IN_PROGRESS.
     * 3) Возвращаем полные строки (RETURNING o.*).
     *
     * Порядок по available_at обеспечивает fair-очерёдность.
     */
    const val LOCK_BATCH_AND_FETCH = """
        with cte as (
            select id
            from outbox
            where status = 'NEW'
              and available_at <= now()
            order by available_at
            limit :limit
            for update skip locked
        )
        update outbox o
        set status = 'IN_PROGRESS',
            updated_at = now(),
            error_message = null
        from cte
        where o.id = cte.id
        returning o.*
    """

    const val MARK_SENT = """
        update outbox
        set status = 'SENT',
            updated_at = now(),
            error_message = null
        where id = :id
    """

    const val MARK_FAILED = """
        update outbox
        set status = 'FAILED',
            updated_at = now(),
            error_message = :errorMessage
        where id = :id
    """

}