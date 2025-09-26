package ru.outbox.application.api

import ru.outbox.domain.Destination

/**
 * Реестр/маршрутизатор паблишеров по Destination.
 * В простом случае — находит первый OutboxPublisher.supports(dest) == true.
 */
interface PublisherRegistry {
    fun resolve(destination: Destination): OutboxPublisher
}
