package ru.app.application.feature

import java.time.Instant
import java.util.UUID

/**
 * Модель данных, возвращаемая внешним сервисом:
 * название файла, его размер, UUID и время создания на стороне remote.
 */
data class FileInfoMetadata(
    val id: UUID,
    val filename: String,
    val size: Long,
    val createdAt: Instant
)