package ru.app.filter

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import ru.app.TestApplication

@SpringBootTest(
    classes = [TestApplication::class],
    properties = [
        "starter.auth.login-header.header-name=Test-Login",
        "starter.auth.login-header.default-value=guest"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
class LoginHeaderAutoConfigurationTest(
    @Autowired
    val webTestClient: WebTestClient
): ShouldSpec() {


    init {
        should("inject custom properties and use header name from config") {
            webTestClient.get().uri("/whoami")
                .header("Test-Login", "springtest")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java)
                .value { it shouldBe "springtest" }
        }

        should("use custom default value from config if header is missing") {
            webTestClient.get().uri("/whoami")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java)
                .value { it shouldBe "guest" }
        }
    }

}



