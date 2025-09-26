//package ru.outbox.infra
//
//import com.example.outbox.config.OutboxWorkerProps
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import org.slf4j.LoggerFactory
//import org.springframework.context.annotation.Configuration
//import org.springframework.scheduling.annotation.EnableScheduling
//import org.springframework.scheduling.annotation.Scheduled
//import ru.outbox.domain.OutboxRepository
//
//@Configuration
//@EnableScheduling
//class OutboxWorker(
//    private val repo: OutboxRepository,
//    private val publisher: DestinationPublisher,
//    private val props: OutboxWorkerProps
//) {
//    private val log = LoggerFactory.getLogger(javaClass)
//    private val scope = CoroutineScope(Dispatchers.Default)
//
//    @Scheduled(fixedDelayString = "\${outbox.worker.fixedDelayMs:1000}")
//    fun tick() {
//        if (!props.enabled) return
//        scope.launch {
//            val batch = repo.lockBatchAndMarkProcessing(props.batchSize)
//            if (batch.isEmpty()) return@launch
//            for (e in batch) {
//                runCatching {
//                    publisher.publish(e.destination, e.payload)
//                    repo.markSent(e.id)
//                }.onFailure { ex ->
//                    log.warn("Publish failed for outbox=${e.id}: ${ex.message}")
//                    scope.launch {
//                        repo.markErrorWithBackoff(e.id, props.maxRetries, props.retryBackoffSeconds, ex.message ?: "error")
//                    }
//                }
//            }
//        }
//    }
//}
