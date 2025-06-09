package ru.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "starter.auth.login-header")
data class LoginHeaderProperties(
    var headerName: String = "X-Login",
    var defaultValue: String = "anonymous"
)