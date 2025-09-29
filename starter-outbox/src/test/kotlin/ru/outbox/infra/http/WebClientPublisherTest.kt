package ru.outbox.infra.http

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import ru.outbox.config.OutboxProperties
import ru.outbox.domain.Destination
import ru.outbox.domain.OutboxRecord

class WebClientPublisherTest : FunSpec({

    lateinit var server: MockWebServer
    lateinit var publisher: WebClientPublisher

    beforeTest {
        server = MockWebServer()
        server.start()

        val props = OutboxProperties(
            publisher = OutboxProperties.PublisherProperties(
                http = OutboxProperties.PublisherProperties.HttpProperties(
                    clients = listOf(
                        OutboxProperties.PublisherProperties.HttpProperties.HttpClient(
                            id = "test",
                            baseUrl = server.url("/").toString().trimEnd('/')
                        )
                    )
                )
            )
        )
        val registry = HttpClientRegistry(props)
        publisher = WebClientPublisher(registry)
    }

    afterTest {
        server.shutdown()
    }

    test("should send POST request and return success on 200") {
        server.enqueue(MockResponse().setResponseCode(200))

        val record = OutboxRecord(
            aggregateType = "Order",
            aggregateId = "123",
            eventType = "CREATED",
            destination = Destination.Http("test", "/send", "POST"),
            payload = """{"id":123}"""
        )

        runBlocking { publisher.publish(record) }

        val request = server.takeRequest()
        request.method shouldBe "POST"
        request.path shouldBe "/send"
        request.body.readUtf8() shouldBe """{"id":123}"""
    }

    test("should throw on non-2xx response") {
        server.enqueue(MockResponse().setResponseCode(500))

        val record = OutboxRecord(
            aggregateType = "Order",
            aggregateId = "123",
            eventType = "CREATED",
            destination = Destination.Http("test", "/fail", "POST"),
            payload = "{}"
        )

        shouldThrow<IllegalStateException> {
            runBlocking { publisher.publish(record) }
        }
    }

    test("should merge headers and override defaults") {
        server.enqueue(MockResponse().setResponseCode(200))

        val record = OutboxRecord(
            aggregateType = "User",
            aggregateId = "u1",
            eventType = "UPDATED",
            destination = Destination.Http("test", "/h", "POST"),
            payload = "{}",
            headers = mapOf("X-Custom" to "abc", "Content-Type" to "application/xml")
        )

        runBlocking { publisher.publish(record) }

        val request = server.takeRequest()
        request.getHeader("X-Custom") shouldBe "abc"
        request.getHeader("Content-Type") shouldBe "application/xml"
    }
})