//package ru.outbox.config
//
//import org.springframework.boot.autoconfigure.AutoConfiguration
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.context.annotation.Bean
//
///**
// * Условная автоконфигурация KafkaPublisher — активируется только при наличии KafkaTemplate и outbox.publisher.kafka.enabled=true.
// */
//@AutoConfiguration
//@ConditionalOnClass(KafkaTemplate::class, ProducerRecord::class)
//@ConditionalOnBean(KafkaTemplate::class)
//@ConditionalOnProperty(prefix = "outbox.publisher.kafka", name = ["enabled"], havingValue = "true", matchIfMissing = true)
//class OutboxKafkaAutoConfiguration {
//
//    @Bean
//    fun kafkaPublisher(
//        template: KafkaTemplate<String, String>,
//        props: OutboxProperties
//    ): KafkaPublisher = KafkaPublisher(template, props.publisher.kafka)
//}