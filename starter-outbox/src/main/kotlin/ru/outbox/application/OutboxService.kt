package ru.outbox.application

import ru.outbox.domain.OutboxRecord

/**
 * Application use-cases для работы с outbox.
 */
interface OutboxService {

    /**
     * Поставить событие в outbox.
     * Обычно вызывается в общей БД-транзакции с изменением агрегата.
     */
    suspend fun enqueue(record: OutboxRecord)

    /**
     * Один цикл обработки outbox:
     * - забрать батч NEW (с бронью SKIP LOCKED → IN_PROGRESS),
     * - опубликовать по назначению,
     * - пометить SENT/FAILED.
     */
    suspend fun pollOnce(batchSize: Int): PollResult

    data class PollResult(
        val locked: Int,
        val sent: Int,
        val failed: Int
    )
}