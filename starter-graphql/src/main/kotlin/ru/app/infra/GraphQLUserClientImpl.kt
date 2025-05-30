package ru.app.infrastructure

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.app.api.UserClient
import ru.app.api.UserInfo
import ru.app.infra.GraphQLResponse

/**
 * Клиент для работы с GraphQL-API сотрудников.
 * Выполняет запрос поиска сотрудника по логину (email).
 */
class GraphQLUserClientImpl(
    private val webClient: WebClient,
    private val endpoint: String
): UserClient {

    override fun findByLogin(login: String): Mono<UserInfo> {
        val query = """
            query GetUsers {
              employees(filter: {email: {eq: "$login"}}) {
                id
                lastName
                middleName
                firstName
                displayName
                email
                login
              }
            }
        """.trimIndent()

        val payload = mapOf("query" to query)

        return webClient.post()
            .uri(endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(GraphQLResponse::class.java)
            .flatMap { response ->
                val employees = response.data?.users
                if (employees.isNullOrEmpty()) {
                    Mono.empty()
                } else {
                    Mono.just(employees.first().toDto())
                }
            }
    }
    
    // Вспомогательные классы для парсинга ответа GraphQL

}
