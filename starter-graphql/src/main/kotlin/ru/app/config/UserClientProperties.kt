package ru.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Настройки клиента для обращения к внешнему GraphQL сервису сотрудников.
 */
@ConfigurationProperties(prefix = "user.graphql-client")
data class UserClientProperties(
    /**
     * Endpoint внешнего GraphQL API (например, https://example.com/graphql).
     */
    val endpoint: String
)