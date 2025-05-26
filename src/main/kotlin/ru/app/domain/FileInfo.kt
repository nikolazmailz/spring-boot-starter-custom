package ru.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Сущность для хранения информации о файле.
 * Используется и в бизнес-логике, и для хранения в БД (R2DBC).
 */
@Table("file_info")
data class FileInfo(
    @Id
    val id: UUID? = null,
    val filename: String,
    val login: String,
    val size: Long,
    val createdAt: Instant
)