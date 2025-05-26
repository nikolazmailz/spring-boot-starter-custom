package ru.app.infrastructure

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.app.application.feature.FileInfoMetadata
import ru.app.application.feature.FileInfoRemoteService
import ru.app.config.FileInfoRemoteProperties
import org.springframework.web.reactive.function.BodyInserters

private val log = KotlinLogging.logger {}

@Service
class FileInfoRemoteServiceImpl(
    @Qualifier("fileInfoWebClient")
    private val webClient: WebClient,
    private val props: FileInfoRemoteProperties
) : FileInfoRemoteService {

    override fun sendFile(
        content: Flux<DataBuffer>,
        filename: String,
        login: String
    ): Mono<FileInfoMetadata> {
        val builder = MultipartBodyBuilder().apply {
            asyncPart("file", content, org.springframework.core.io.buffer.DataBuffer::class.java)
                .filename(filename)
        }

        log.debug { "Uploading file='$filename' for login='$login' to ${props.baseUrl}${props.uploadPath}" }

        return webClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path(props.uploadPath)
                    .queryParam("type", "FILE")
                    .build()
            }
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(FileInfoMetadata::class.java)
            .doOnNext { meta ->
                log.info { "Received metadata from remote: $meta" }
            }
    }
}