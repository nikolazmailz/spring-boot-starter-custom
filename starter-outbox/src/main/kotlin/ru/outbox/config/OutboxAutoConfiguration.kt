package ru.outbox.config

import ru.outbox.application.PollingSchedulerAsync
import ru.outbox.application.PollingSchedulerBlocking
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import ru.outbox.application.OutboxServiceImpl
import ru.outbox.application.api.OutboxRepository
import ru.outbox.application.OutboxService
import ru.outbox.application.api.PublisherRegistry
import ru.outbox.infra.db.R2dbcOutboxRepository
import ru.outbox.infra.http.HttpClientRegistry
import ru.outbox.infra.http.WebClientPublisher
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Базовая автоконфигурация: свойства, репозиторий, сервис, реестр паблишеров и HTTP-паблишер.
 * Включает @EnableScheduling — чтобы @Scheduled из шедулера работал.
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties::class)
@ConditionalOnProperty(prefix = "outbox", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Import(HttpClientRegistry::class) // создаст реестр клиентов HTTP
class OutboxAutoConfiguration {

    // ---------- Repository ----------
    @Bean
    @ConditionalOnMissingBean(OutboxRepository::class)
    fun outboxRepository(client: DatabaseClient, objectMapper: ObjectMapper): OutboxRepository =
        R2dbcOutboxRepository(client, objectMapper)

    // ---------- Publishers ----------
    @Bean
    @ConditionalOnMissingBean(WebClientPublisher::class)
    fun webClientPublisher(registry: HttpClientRegistry): WebClientPublisher =
        WebClientPublisher(registry)

    @Bean
    @ConditionalOnMissingBean(PublisherRegistry::class)
    fun publisherRegistry(publishers: List<ru.outbox.application.api.OutboxPublisher>): PublisherRegistry =
        PublisherRegistryImpl(publishers)

    // ---------- Service ----------
    @Bean
    @ConditionalOnMissingBean(OutboxService::class)
    fun outboxService(
        repository: OutboxRepository,
        registry: PublisherRegistry
    ): OutboxService = OutboxServiceImpl(repository, registry)

    // ---------- Scheduler ----------
    // Планировщик у нас уже оформлен как @Component (PollingScheduler*).
    // Если хочешь полностью конфигурировать из автоконфига — можно создать бин тут.
    // Оставим компонентный вариант, чтобы не плодить дубли — и он будет работать благодаря @EnableScheduling.

    // ---------- Scheduler ----------
    @Bean
    @ConditionalOnProperty(prefix = "outbox.poll", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(PollingSchedulerAsync::class)
    fun outboxPollingScheduler(
        service: OutboxService,
        props: OutboxProperties
    ): PollingSchedulerAsync = PollingSchedulerAsync(service, props)
}


//class OutboxAutoConfiguration {
//}
