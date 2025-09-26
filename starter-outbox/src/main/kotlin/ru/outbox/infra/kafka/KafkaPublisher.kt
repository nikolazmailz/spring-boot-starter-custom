//package ru.outbox.infra.kafka
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import mu.KotlinLogging
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.common.header.internals.RecordHeaders
//import org.springframework.kafka.core.KafkaTemplate
//import ru.outbox.application.api.OutboxPublisher
//import ru.outbox.config.OutboxProperties
//import ru.outbox.domain.Destination
//import ru.outbox.domain.OutboxRecord
//import java.nio.charset.StandardCharsets
//
///**
// * Паблишер для Destination.Kafka.
// *
// * Как выбирается топик:
// *  - если в props.publisher.kafka.topics есть ключ равный dest.topic → берём значение из маппинга;
// *  - иначе используем dest.topic "как есть";
// *  - (fallback) если ни того, ни другого — пытаемся взять defaultTopic (но до него обычно не доходит).
// *
// * Ключ сообщения:
// *  - dedupKey ?: aggregateId
// *
// * Заголовки:
// *  - берём из OutboxRecord.headers и конвертируем в Kafka headers (bytes UTF-8).
// */
//class KafkaPublisher(
//    private val template: KafkaTemplate<String, String>,
//    private val kafkaProps: OutboxProperties.PublisherProperties.KafkaProperties
//) : OutboxPublisher {
//
//    private val log = KotlinLogging.logger {}
//
//    override fun supports(destination: Destination): Boolean =
//        destination is Destination.Kafka
//
//    override suspend fun publish(record: OutboxRecord) {
//        val dest = record.destination as? Destination.Kafka
//            ?: error("Unsupported destination type: ${record.destination}")
//
//        val topic = resolveTopic(dest)
//        val key = record.dedupKey ?: record.aggregateId
//        val headers = toRecordHeaders(record)
//
//        val producerRecord = ProducerRecord(
//            topic,
//            null,                // partition (null => решает брокер)
//            null,                // timestamp
//            key,                 // message key
//            record.payload,      // message value
//            headers
//        )
//
//        // Отправляем и ждём подтверждения брокера.
//        // Чтобы не тянуть kotlinx-coroutines-jdk8, ждём в IO-диспетчере.
//        val sendResult = withContext(Dispatchers.IO) {
//            template.send(producerRecord).get()
//        }
//
//        if (log.isDebugEnabled) {
//            log.debug {
//                "Kafka outbox sent id=${record.id} -> topic=$topic, partition=${sendResult.recordMetadata.partition()}, offset=${sendResult.recordMetadata.offset()}"
//            }
//        }
//    }
//
//    private fun resolveTopic(dest: Destination.Kafka): String {
//        val mapped = kafkaProps.topics[dest.topic]
//        if (!mapped.isNullOrBlank()) return mapped
//        if (dest.topic.isNotBlank()) return dest.topic
//        return kafkaProps.defaultTopic
//            ?: error("Kafka topic is not resolved: destination='${dest.topic}', defaultTopic is null")
//    }
//
//    private fun toRecordHeaders(record: OutboxRecord): RecordHeaders {
//        val headers = RecordHeaders()
//        record.headers.forEach { (k, v) ->
//            headers.add(k, v.toByteArray(StandardCharsets.UTF_8))
//        }
//        return headers
//    }
//}
