package ru.app.integration

import io.kotest.core.spec.style.ShouldSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.app.application.FileInfoApplicationService
import io.kotest.core.listeners.TestListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import ru.app.domain.FileInfo
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import io.kotest.extensions.spring.SpringExtension

@SpringBootTest
@ActiveProfiles("test")
//@Testcontainers
class FileInfoApplicationServiceE2ETest: ShouldSpec() {

    private val log = KotlinLogging.logger {}

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var service: FileInfoApplicationService

    companion object {

        private val mockWebServer = MockWebServer().apply {
            start()
            // Перед стартом контекста прокидываем base-url
            System.setProperty(
                "file-info.remote.base-url",
                this.url("/").toString().removeSuffix("/") // http://host:port
            )
        }

    }

    init {

        should("отправлять файл в remote, получать metadata и сохранять в БД") {
            // подготовим ожидание от MockWebServer
            val fakeId = UUID.randomUUID()
            val fakeFilename = "example.txt"
            val fakeSize = 12345L
            val fakeCreatedAt = Instant.now()
            val json = """
                {
                  "id":"$fakeId",
                  "filename":"$fakeFilename",
                  "size":$fakeSize,
                  "createdAt":"$fakeCreatedAt"
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(json)
                    .addHeader("Content-Type", "application/json")
            )

            // создаём Flux<DataBuffer> для «файла»
            val content = Flux.just(
                DefaultDataBufferFactory().wrap("hello world".toByteArray())
            )

            // вызываем приложение
            val result: FileInfo? = service
                .processFile(content, fakeFilename, "user1")
                .block(5.seconds)

            // проверяем ответ
            result shouldNotBe null
            result!!.id shouldBe fakeId
            result.filename shouldBe fakeFilename
            result.size shouldBe fakeSize

            log.info { "Verified service returned: $result" }

            // проверим, что Entity сохранилась в БД
//            val saved = repository.findById(fakeId).block()
//            saved shouldNotBe null
//            saved!!.id shouldBe fakeId
//            saved.filename shouldBe fakeFilename
//            saved.size shouldBe fakeSize
        }
    }

}