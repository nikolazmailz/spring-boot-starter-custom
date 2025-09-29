package ru.outbox.infra.scheduler

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import ru.outbox.application.OutboxService
import ru.outbox.config.OutboxProperties

private val log = KotlinLogging.logger {}

//@Component
class PollingSchedulerBlocking(
    private val service: OutboxService,
    private val props: OutboxProperties
) {

    @Scheduled(
        initialDelayString = "\${outbox.poll.initialDelay:PT1S}",
        fixedDelayString = "\${outbox.poll.fixedDelay:PT0.5S}"
    )
    fun tick() = runBlocking {
        if (!props.enabled || !props.poll.enabled) return@runBlocking
        try {
            val result = service.pollOnce(props.poll.batchSize)
            if (log.isDebugEnabled && result.locked > 0) {
                log.debug { "Outbox tick (blocking): locked=${result.locked}, sent=${result.sent}, failed=${result.failed}" }
            }
        } catch (t: Throwable) {
            log.error(t) { "Outbox tick (blocking) failed" }
        }
    }
}
