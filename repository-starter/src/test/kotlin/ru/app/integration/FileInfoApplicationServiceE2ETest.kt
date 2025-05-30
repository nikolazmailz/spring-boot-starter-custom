package ru.app.integration

import io.kotest.core.spec.style.ShouldSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.app.application.FileInfoApplicationService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import ru.app.domain.FileInfo
import java.time.Instant
import java.util.UUID
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import io.kotest.extensions.spring.SpringExtension
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.app.TestApplication
import ru.app.domain.FileInfoRepository

@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "logging.level.liquibase=TRACE",
        "logging.level.org.springframework.boot.autoconfigure.liquibase=TRACE"
    ]
)
@ActiveProfiles("test")
@Testcontainers
class FileInfoApplicationServiceE2ETest: ShouldSpec() {

    private val log = KotlinLogging.logger {}

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var service: FileInfoApplicationService

    @Autowired
    private lateinit var repository: FileInfoRepository

    companion object {

        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("postgresql")
            withPassword("postgresql")
            start()
        }

        private val mockWebServer = MockWebServer().apply {
            start()
            // Перед стартом контекста прокидываем base-url
            System.setProperty(
                "file-info.remote.base-url",
                this.url("/").toString().removeSuffix("/") // http://host:port
            )
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // R2DBC
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)

            // JDBC-datasource (для Liquibase)
            registry.add("spring.liquibase.enabled") {
                "true"
            }
            registry.add("spring.liquibase.url") {
                "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.liquibase.user", postgres::getUsername)
            registry.add("spring.liquibase.password", postgres::getPassword)

            // Мастер-чейндж-лог
            registry.add("spring.liquibase.change-log") {
                "classpath:db/changelog/db.changelog-master.yaml"
            }

        }

    }

    init {

        // очищаем перед каждым тестом
        beforeTest {
            repository.deleteAll()
        }

        should("возвращать все записи через findAll") {
            val a = FileInfo(UUID.randomUUID(), "a.txt", "u1", 10, Instant.now()).setAsNew()
            val b = FileInfo(UUID.randomUUID(), "b.txt", "u2", 20, Instant.now()).setAsNew()

            repository.saveAll(listOf(a, b)).collectList().block()

            val list = repository.findAll().collectList().block()
            list!!.size shouldBe 2
            list.map { it.filename }.toSet() shouldBe setOf("a.txt", "b.txt")
        }

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
            val content: Flux<DataBuffer> = Flux.just(
                DefaultDataBufferFactory().wrap("hello world".toByteArray())
            )

            // вызываем приложение
            val result: FileInfo? = service.processFile(content, fakeFilename, "user1").block()

            // проверяем ответ
            result shouldNotBe null
            result!!.id shouldBe fakeId
            result.filename shouldBe fakeFilename
            result.size shouldBe fakeSize

            log.info { "Verified service returned: $result" }

            // проверим, что Entity сохранилась в БД
            val saved = repository.findById(fakeId).block()
            saved shouldNotBe null
            saved!!.id shouldBe fakeId
            saved.filename shouldBe fakeFilename
            saved.size shouldBe fakeSize
        }
    }
}