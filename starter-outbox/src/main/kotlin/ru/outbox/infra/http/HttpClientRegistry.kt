package ru.outbox.infra.http

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import mu.KotlinLogging
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import ru.outbox.config.OutboxProperties
import java.util.concurrent.TimeUnit

/**
 * Реестр именованных WebClient-ов:
 *  - создаёт WebClient для каждого outbox.publisher.http.clients[*]
 *  - применяет таймауты и дефолтные заголовки
 */
@Component
class HttpClientRegistry(
    props: OutboxProperties
) {
    private val log = KotlinLogging.logger {}

    private val clients: Map<String, WebClient>

    init {
        clients = props.publisher.http.clients.associate { cfg ->
            val connectMs = cfg.connectTimeout.toMillis().toInt().coerceAtLeast(1)
            val readMs = cfg.readTimeout.toMillis().toInt().coerceAtLeast(1)

            val httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectMs)
                .doOnConnected { conn ->
                    // read timeout через Netty handler
                    conn.addHandlerLast(ReadTimeoutHandler(readMs.toLong(), TimeUnit.MILLISECONDS))
                }

            val connector = ReactorClientHttpConnector(httpClient)

            val strategies = ExchangeStrategies.builder()
                .codecs { c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) } // 2MB на всякий
                .build()

            val builder = WebClient.builder()
                .baseUrl(cfg.baseUrl)
                .clientConnector(connector)
                .exchangeStrategies(strategies)

            // дефолтные заголовки из конфига клиента
            cfg.defaultHeaders.forEach { (k, v) -> builder.defaultHeader(k, v) }

            val webClient = builder.build()
            log.info { "Outbox HTTP client '${cfg.id}' configured (baseUrl=${cfg.baseUrl})" }
            cfg.id to webClient
        }
    }

    fun get(clientId: String): WebClient =
        clients[clientId]
            ?: error("Outbox HTTP client not configured: '$clientId'")
}
