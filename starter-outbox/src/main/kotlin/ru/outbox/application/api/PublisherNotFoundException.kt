package ru.outbox.application.api

import ru.outbox.domain.Destination

class PublisherNotFoundException(destination: Destination) :
    IllegalStateException("No publisher found for destination: $destination")