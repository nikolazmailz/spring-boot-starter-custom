package ru.app.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient


@Configuration
@EnableConfigurationProperties(FileInfoRemoteProperties::class)
class FileInfoWebClientConfig {

    @Bean
    @ConditionalOnMissingBean(name = ["fileInfoWebClient"])
    fun fileInfoWebClient(
        builder: WebClient.Builder,
        props: FileInfoRemoteProperties
    ): WebClient =
        builder
            .baseUrl(props.baseUrl)
            .build()
}

@ConfigurationProperties(prefix = "file-info.remote")
data class FileInfoRemoteProperties(
    var baseUrl: String = "",
    var uploadPath: String = "/upload"
)