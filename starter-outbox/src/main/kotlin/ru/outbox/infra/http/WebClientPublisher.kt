package ru.outbox.infra.http

import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import ru.outbox.application.api.OutboxPublisher
import ru.outbox.domain.Destination
import ru.outbox.domain.OutboxRecord

/**
 * Реактивный паблишер для HTTP-назначений.
 * Поддерживает несколько сервисов через HttpClientRegistry.
 */
@Component
class WebClientPublisher(
    private val registry: HttpClientRegistry
) : OutboxPublisher {

    private val log = KotlinLogging.logger {}

    override fun supports(destination: Destination): Boolean =
        destination is Destination.Http

    override suspend fun publish(record: OutboxRecord) {
        val dest = record.destination as? Destination.Http
            ?: error("Unsupported destination type: ${record.destination}")

        val client: WebClient = registry.get(dest.clientId)
        val method = httpMethodOf(dest.method)

        // content-type берём из headers, иначе JSON по умолчанию
        val contentType = record.headers["Content-Type"]?.let { MediaType.parseMediaType(it) }
            ?: MediaType.APPLICATION_JSON

        val req = client.method(method)
            .uri(dest.path)
            .headers { h ->
                // смержим пользовательские заголовки поверх дефолтных
                record.headers.forEach { (k, v) -> h.set(k, v) }
                // если не задан Content-Type явно — проставим
                if (!h.containsKey("Content-Type")) {
                    h.contentType = contentType
                }
            }
            .body(BodyInserters.fromValue(record.payload))

        // отдаём запрос и ждём лишь статус (тело нам не нужно)
        val response = req.retrieve()
            .toBodilessEntity()
            .awaitSingle()

        if (response.statusCode.is2xxSuccessful) {
            if (log.isDebugEnabled) {
                log.debug { "HTTP outbox sent id=${record.id} -> ${dest.clientId}${dest.path} [${response.statusCode}]" }
            }
            return
        }

        // Ошибочный статус — считаем неуспехом
        throw IllegalStateException(
            "HTTP outbox failed id=${record.id}: status=${response.statusCode} dest=$dest"
        )
    }

    private fun httpMethodOf(method: String): HttpMethod =
        runCatching { HttpMethod.valueOf(method.uppercase()) }
            .getOrElse { HttpMethod.POST }
}
