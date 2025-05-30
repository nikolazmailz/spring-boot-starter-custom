package ru.app.api

/**
 * DTO сотрудника, используется как контракт во внешних API и сервисах.
 */
data class UserInfo(
    val id: String,
    val lastName: String,
    val middleName: String?,
    val firstName: String,
    val displayName: String,
    val email: String,
    val login: String
)