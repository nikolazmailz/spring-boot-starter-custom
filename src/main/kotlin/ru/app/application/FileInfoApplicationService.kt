package ru.app.application

import mu.KotlinLogging
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.app.application.feature.FileInfoRemoteService
import ru.app.domain.FileInfo

@Service
class FileInfoApplicationService(
    private val fileInfoRemoteService: FileInfoRemoteService
) {

    private val log = KotlinLogging.logger {}

    fun processFile(
        content: Flux<DataBuffer>,
        filename: String,
        login: String
    ): Mono<FileInfo> =
        fileInfoRemoteService
            .sendFile(content, filename, login)
            .map { meta ->
                FileInfo(
                    id = meta.id,
                    filename = meta.filename,
                    login = login,
                    size = meta.size,
                    createdAt = meta.createdAt
                )
            }
            .doOnNext { fi ->
                log.info { "Processed FileInfo: $fi" }
            }
}