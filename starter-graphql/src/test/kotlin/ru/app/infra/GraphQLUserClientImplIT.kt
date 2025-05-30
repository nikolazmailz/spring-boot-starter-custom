package ru.app.infra

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.util.ResourceUtils
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import ru.app.api.UserInfo
import ru.app.infrastructure.GraphQLUserClientImpl

class GraphQLUserClientImplIT : ShouldSpec({

    val mockServer = MockWebServer()
    lateinit var client: GraphQLUserClientImpl

    beforeSpec {
        mockServer.start()
        val endpoint = mockServer.url("/graphql").toString()
        client = GraphQLUserClientImpl(
            webClient = WebClient.create(),
            endpoint = endpoint
        )
    }

    afterSpec {
        mockServer.shutdown()
    }

    should("return UserDto when user is found") {
        val login = "login@mail.com"

        val res = ResourceUtils.getFile(
            "classpath:response.json"
        ).readText(Charsets.UTF_8)

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(res)
        )

        val result = client.findByLogin(login)

        StepVerifier.create(result)
            .assertNext { user ->
                user shouldBe UserInfo(
                    id = "a7a3fd73-fd7a-11ef-befa-e43d1aa1a7a7",
                    lastName = "Пушкарёв",
                    middleName = "Викторович",
                    firstName = "Николай",
                    displayName = "Пушкарёв Николай Викторович",
                    email = "email@mail.com",
                    login = login
                )
            }
            .verifyComplete()
    }

    should("return empty Mono when user is not found") {
        val login = "notfound@mail.com"
        val responseBody = """
            {
              "data": {
                "employees": []
              }
            }
        """.trimIndent()
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        val result = client.findByLogin(login)

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }
})
