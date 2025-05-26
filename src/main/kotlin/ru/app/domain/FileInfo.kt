package ru.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Transient

/**
 * Сущность для хранения информации о файле.
 * Используется и в бизнес-логике, и для хранения в БД (R2DBC).
 */
@Table("file_info")
data class FileInfo(
    private val id: UUID,
    val filename: String,
    val login: String,
    val size: Long,
    val createdAt: Instant
): Persistable<UUID> {

    @Transient
    private var isNew: Boolean = false

    @Id
    override fun getId(): UUID {
        return id
    }

    override fun isNew() = isNew

    fun setAsNew(): FileInfo {
        isNew = true
        return this
    }
}