package ru.outbox.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.outbox.application.api.OutboxRepository
import ru.outbox.application.api.PublisherRegistry
import ru.outbox.domain.OutboxRecord

private val log = KotlinLogging.logger {}

/**
 * Реализация сценариев outbox без ретраев/метрик.
 * - enqueue(): просто записывает в таблицу outbox (обычно вызывается в общей БД-транзакции домена).
 * - pollOnce(): забирает батч записей NEW, помечает IN_PROGRESS, публикует и фиксирует результат (SENT/FAILED).
 */
@Service
class OutboxServiceImpl(
    private val repository: OutboxRepository,
    private val registry: PublisherRegistry
) : OutboxService {

    /**
     * В транзакции приложения insert делается вместе с изменением агрегата (см. интерфейс вызывающей стороны).
     * Здесь — отдельная операция.
     */
    override suspend fun enqueue(record: OutboxRecord) {
        repository.insert(record)
        if (log.isDebugEnabled) {
            log.debug { "Enqueued outbox record id=${record.id}, dest=${record.destination}" }
        }
    }

    /**
     * Один цикл обработки:
     * 1) Берём батч с блокировкой (SELECT ... FOR UPDATE SKIP LOCKED) -> статус IN_PROGRESS.
     * 2) Публикуем по одному.
     * 3) Итог фиксируем: SENT или FAILED (без повторов).
     *
     * Вся выборка батча делается в транзакции (бронь). Публикация — ВНЕ той транзакции,
     * чтобы не держать блокировки долго (зависит от реализации репозитория).
     */
    @Transactional
    override suspend fun pollOnce(batchSize: Int): PollResult {
        // 1) Забираем батч; реализация репозитория сама помечает записи IN_PROGRESS в рамках транзакции.
        val batch: List<OutboxRecord> = repository.lockBatchAndFetch(limit = batchSize)
        if (batch.isEmpty()) return PollResult(locked = 0, sent = 0, failed = 0)

        // 2) Публикуем — уже после фиксации транзакции «броней».
        var sent = 0
        var failed = 0
        for (record in batch) {
            try {
                val publisher = registry.resolve(record.destination)
                publisher.publish(record)
                repository.markSent(record.id)
                sent++
            } catch (t: Throwable) {
                log.warn(t) { "Outbox publish failed id=${record.id}, dest=${record.destination}" }
                repository.markFailed(record.id, t.message)
                failed++
            }
        }
        return PollResult(
            locked = batch.size,
            sent = sent,
            failed = failed
        )
    }
}
