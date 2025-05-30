package ru.app.domain

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.UUID

interface FileInfoRepository: ReactiveCrudRepository<FileInfo, UUID> {
//    fun findByLogin(login: String): Flux<FileInfo>
//    fun findByFilename(filename: String): Flux<FileInfo>
}