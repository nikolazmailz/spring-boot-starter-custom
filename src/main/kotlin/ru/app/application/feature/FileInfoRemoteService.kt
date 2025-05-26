package ru.app.application.feature

import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FileInfoRemoteService {
    /**
     * Отправляет поток DataBuffer'ов на внешний сервис
     * и возвращает FileInfoMetadata.
     */
    fun sendFile(
        content: Flux<DataBuffer>,
        filename: String,
        login: String
    ): Mono<FileInfoMetadata>
}