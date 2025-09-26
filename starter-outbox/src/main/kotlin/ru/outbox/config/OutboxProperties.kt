package ru.outbox.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "outbox")
data class OutboxProperties(
    @DefaultValue("true")
    val enabled: Boolean = true,
    val poll: PollProperties = PollProperties(),
    val publisher: PublisherProperties = PublisherProperties()
) {
    data class PollProperties(
        @DefaultValue("true")
        val enabled: Boolean = true,
        @DefaultValue("PT0.5S")
        val fixedDelay: Duration = Duration.ofMillis(500),
        @DefaultValue("PT1S")
        val initialDelay: Duration = Duration.ofSeconds(1),
        @DefaultValue("100")
        val batchSize: Int = 100
    )

    data class PublisherProperties(
        val kafka: KafkaProperties = KafkaProperties(),
        val http: HttpProperties = HttpProperties()
    ) {
        data class KafkaProperties(
            @DefaultValue("true")
            val enabled: Boolean = true,
            val defaultTopic: String? = null,
            val topics: Map<String, String> = emptyMap(),
            val templateBeanName: String? = null
        )
        data class HttpProperties(
            val clients: List<HttpClient> = emptyList()
        ) {
            data class HttpClient(
                val id: String,
                val baseUrl: String,
                @DefaultValue("PT2S")
                val connectTimeout: Duration = Duration.ofSeconds(2),
                @DefaultValue("PT3S")
                val readTimeout: Duration = Duration.ofSeconds(3),
                val defaultHeaders: Map<String, String> = emptyMap()
            )
        }
    }
}


//import org.springframework.boot.context.properties.ConfigurationProperties
//import java.time.Duration

//@ConfigurationProperties("outbox")
//data class OutboxProperties(
//    val enabled: Boolean = true,
//    val batchSize: Int = 100,
//    val pollInterval: Duration = Duration.ofSeconds(2),
//    val maxRetries: Int = 8,
//    val backoffBaseMillis: Long = 500, // экспон. backoff: base * 2^retries
//    val kafka: KafkaProps = KafkaProps(),
//    val webclientPropsRegistry: WebClientPropsRegistry = WebClientPropsRegistry()
//) {
//
//    data class KafkaProps(
//        val defaultTopic: String? = null,
//        val topicsByEventType: Map<String, String> = emptyMap() // eventType -> topic
//    )
//
//    data class WebClientPropsRegistry(
//        val services: Map<String, WebClientProps> = emptyMap() // serviceName -> cfg
//    )
//
//    data class WebClientProps(
//        val baseUrl: String,
//        val path: String = "/",
//        val authHeader: String? = null // e.g. "Bearer xxx"
//    )
//}
