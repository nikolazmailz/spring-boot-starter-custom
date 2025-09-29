package ru.outbox.application

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import ru.outbox.application.api.OutboxRepository
import ru.outbox.application.api.PublisherRegistry
import ru.outbox.application.api.OutboxPublisher
import ru.outbox.domain.*

class OutboxServiceImplTest : FunSpec({

    val repository = mockk<OutboxRepository>()
    val registry = mockk<PublisherRegistry>()
    val publisher = mockk<OutboxPublisher>()

    val service = OutboxServiceImpl(repository, registry)

    test("enqueue should insert record into repository") {
        val record = OutboxRecord.forKafka(
            aggregateType = "Order",
            aggregateId = "123",
            eventType = "CREATED",
            topic = "orders",
            payload = """{"id":123}"""
        )

        coEvery { repository.insert(record) } just Runs

        runBlocking { service.enqueue(record) }

        coVerify { repository.insert(record) }
    }

    test("pollOnce with empty batch should return zeros") {
        coEvery { repository.lockBatchAndFetch(any()) } returns emptyList()

        val result = runBlocking { service.pollOnce(10) }

        result.locked shouldBe 0
        result.sent shouldBe 0
        result.failed shouldBe 0
    }

    test("pollOnce should publish successfully and mark sent") {
        val record = OutboxRecord.forKafka("Order", "123", "CREATED", "orders", "{}")

        coEvery { repository.lockBatchAndFetch(any()) } returns listOf(record)
        every { registry.resolve(record.destination) } returns publisher
        coEvery { publisher.publish(record) } just Runs
        coEvery { repository.markSent(record.id) } just Runs

        val result = runBlocking { service.pollOnce(10) }

        result.locked shouldBe 1
        result.sent shouldBe 1
        result.failed shouldBe 0
        coVerify { repository.markSent(record.id) }
    }

    test("pollOnce should mark failed when publisher throws") {
        val record = OutboxRecord.forKafka("Order", "123", "CREATED", "orders", "{}")

        coEvery { repository.lockBatchAndFetch(any()) } returns listOf(record)
        every { registry.resolve(record.destination) } returns publisher
        coEvery { publisher.publish(record) } throws RuntimeException("boom")
        coEvery { repository.markFailed(record.id, any()) } just Runs

        val result = runBlocking { service.pollOnce(10) }

        result.locked shouldBe 1
        result.sent shouldBe 0
        result.failed shouldBe 1
        coVerify { repository.markFailed(record.id, match { it?.contains("boom") == true }) }
    }

    test("pollOnce should mark failed when no publisher found") {
        val record = OutboxRecord.forKafka("Order", "123", "CREATED", "orders", "{}")

        coEvery { repository.lockBatchAndFetch(any()) } returns listOf(record)
        every { registry.resolve(record.destination) } throws IllegalStateException("No publisher")
        coEvery { repository.markFailed(record.id, any()) } just Runs

        val result = runBlocking { service.pollOnce(10) }

        result.locked shouldBe 1
        result.sent shouldBe 0
        result.failed shouldBe 1
    }
})
