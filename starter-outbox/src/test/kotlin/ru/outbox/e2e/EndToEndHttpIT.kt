package ru.outbox.e2e

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.outbox.application.OutboxService
import ru.outbox.domain.Destination
import ru.outbox.domain.OutboxRecord
import ru.outbox.domain.OutboxStatus
import ru.outbox.TestSpringBootApplication
import java.util.*
import io.kotest.matchers.nulls.shouldBeNull

@Testcontainers
@SpringBootTest(classes = [TestSpringBootApplication::class])
class EndToEndHttpIT(
    @Autowired private val service: OutboxService,
    @Autowired private val db: DatabaseClient
) : FunSpec({

    test("enqueue -> pollOnce -> HTTP 200 -> SENT") {
        // given
        val id = UUID.randomUUID()
        val record = OutboxRecord(
            id = id,
            aggregateType = "Order",
            aggregateId = "o-1",
            eventType = "CREATED",
            destination = Destination.Http("test", "/send", "POST"),
            payload = """{"id":1}""",
            headers = mapOf("X-Trace" to "abc")
        )
        // HTTP мокаем успешный ответ
        server.enqueue(MockResponse().setResponseCode(200))

        // when
        kotlinx.coroutines.runBlocking {
            service.enqueue(record)
            service.pollOnce(batchSize = 10)
        }

        // then: запрос ушел
        val req = server.takeRequest()
        req.method shouldBe "POST"
        req.path shouldBe "/send"
        req.getHeader("X-Trace") shouldBe "abc"
        req.body.readUtf8() shouldBe """{"id":1}"""

        // then: статус в БД SENT
        val row = db.sql("select status, error_message from outbox where id = :id")
            .bind("id", id)
            .map { r, _ -> r.get("status", String::class.java)!! to r.get("error_message", String::class.java) }
            .one().block()!!

        row.first shouldBe OutboxStatus.SENT.name
        row.second.shouldBeNull()
    }

}) {
    companion object {
        // ---------- Testcontainers Postgres ----------
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        // ---------- MockWebServer ----------
        @JvmStatic
        lateinit var server: MockWebServer

        init {
            server = MockWebServer()
            server.start()
        }

        @JvmStatic
        @org.junit.jupiter.api.AfterAll
        fun shutdown() {
            server.shutdown()
        }

        // ---------- Spring properties ----------
        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            // JDBC для Liquibase
            reg.add("spring.datasource.url") { postgres.jdbcUrl }
            reg.add("spring.datasource.username") { postgres.username }
            reg.add("spring.datasource.password") { postgres.password }

            // R2DBC для репозитория
            val r2dbcUrl = postgres.jdbcUrl.replace("jdbc:", "r2dbc:")
            reg.add("spring.r2dbc.url") { r2dbcUrl }
            reg.add("spring.r2dbc.username") { postgres.username }
            reg.add("spring.r2dbc.password") { postgres.password }

            // Отключаем планировщик
            reg.add("outbox.poll.enabled") { "false" }

            // HTTP клиент "test" с baseUrl на мок-сервер
            reg.add("outbox.publisher.http.clients[0].id") { "test" }
            reg.add("outbox.publisher.http.clients[0].baseUrl") {
                server.url("/").toString().trimEnd('/')
            }
        }
    }
}
