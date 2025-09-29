package ru.outbox.infra.scheduler

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import ru.outbox.application.OutboxService
import ru.outbox.config.OutboxProperties

private val log = KotlinLogging.logger {}

//@Component
class PollingSchedulerAsync(
    private val service: OutboxService,
    private val props: OutboxProperties
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Scheduled(
        initialDelayString = "\${outbox.poll.initialDelay:PT1S}",
        fixedDelayString = "\${outbox.poll.fixedDelay:PT0.5S}"
    )
    fun tick() {
        if (!props.enabled || !props.poll.enabled) return

        scope.launch {
            try {
                val result = service.pollOnce(props.poll.batchSize)
                if (log.isDebugEnabled && result.locked > 0) {
                    log.debug { "Outbox tick (async): locked=${result.locked}, sent=${result.sent}, failed=${result.failed}" }
                }
            } catch (t: Throwable) {
                log.error(t) { "Outbox tick (async) failed" }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        // Корректно завершаем фоновые задачи при остановке приложения
        scope.cancel("Shutdown")
    }
}
