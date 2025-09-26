package ru.outbox.config

import ru.outbox.application.api.OutboxPublisher
import ru.outbox.application.api.PublisherRegistry
import ru.outbox.application.api.PublisherNotFoundException
import ru.outbox.domain.Destination

/**
 * Простой реестр: выбирает первый паблишер, который поддерживает Destination.
 */
class PublisherRegistryImpl(
    private val publishers: List<OutboxPublisher>
) : PublisherRegistry {
    override fun resolve(destination: Destination): OutboxPublisher =
        publishers.firstOrNull { it.supports(destination) }
            ?: throw PublisherNotFoundException(destination)
}
