package ru.app.filter

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class LoginHeaderWebFilter(
    private val headerName: String = "X-Login",
    private val defaultValue: String = "anonymous"
) : WebFilter {

    private val swaggerPaths = listOf("/swagger-ui", "/v3/api-docs", "/swagger-resources")
    private val actuatorPath = "/actuator"

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        if (isBypassPath(request)) {
            // Swagger и Actuator пропускаем без логина
            return chain.filter(exchange)
        }

        val login = request.headers.getFirst(headerName) ?: defaultValue

        // Добавляем логин в ReactorContext
        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put(LOGIN_CONTEXT_KEY, login) }
    }

    private fun isBypassPath(request: ServerHttpRequest): Boolean {
        val path = request.uri.path
        return swaggerPaths.any { path.startsWith(it) } || path.startsWith(actuatorPath)
    }

    companion object {
        const val LOGIN_CONTEXT_KEY = "LOGIN_CONTEXT_KEY"
    }
}