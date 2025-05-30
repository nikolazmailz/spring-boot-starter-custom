package ru.app.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.app.api.UserClient
import ru.app.infrastructure.GraphQLUserClientImpl

/**
 * Автоконфигурация клиента поиска сотрудников по логину через GraphQL.
 */
@Configuration
@EnableConfigurationProperties(UserClientProperties::class)
@ConditionalOnProperty(
    prefix = "user.graphql-client",
    name = ["endpoint"],
    matchIfMissing = false
)
class UserClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun userWebClient(): WebClient =
        WebClient.builder().build()

    @Bean
    @ConditionalOnMissingBean
    fun graphQlEmployeeClient(
        webClient: WebClient,
        properties: UserClientProperties
    ): UserClient =
        GraphQLUserClientImpl(webClient, properties.endpoint)

//    @Bean
//    @ConditionalOnMissingBean(UserService::class)
//    fun employeeService(
//        graphQlUserClient: GraphQlUserClient
//    ): UserService =
//        UserServiceImpl(graphQlUserClient)
}