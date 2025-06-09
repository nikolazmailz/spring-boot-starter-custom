package ru.app.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.app.filter.LoginHeaderWebFilter

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(LoginHeaderWebFilter::class)
@EnableConfigurationProperties(LoginHeaderProperties::class)
class LoginHeaderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun loginHeaderWebFilter(properties: LoginHeaderProperties): LoginHeaderWebFilter =
        LoginHeaderWebFilter(
            headerName = properties.headerName,
            defaultValue = properties.defaultValue
        )
}