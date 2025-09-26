package ru.outbox.domain

sealed interface Destination {
    val kind: String

    data class Kafka(val topic: String) : Destination {
        init {
            require(topic.isNotBlank()) { "Kafka topic must not be blank" }
        }

        override val kind: String = "kafka"
        override fun toString(): String = "kafka:$topic"
    }

    data class Http(val clientId: String, val path: String, val method: String) : Destination {
        init {
            require(clientId.isNotBlank()) { "Http clientId must not be blank" }
            require(path.isNotBlank()) { "Http path must not be blank" }
            require(path.startsWith("/")) { "Http path must start with '/'" }
        }
//        override fun me
        override val kind: String = "http"
        override fun toString(): String = "http:$clientId:$path"
    }

    companion object {
        /**
         * Парсер строкового вида destination (см. toString()).
         * Примеры:
         *  - "kafka:users.events"
         *  - "http:notifications:/send"
         */
        fun parse(raw: String): Destination {
            require(raw.isNotBlank()) { "Destination string must not be blank" }
            val parts = raw.split(':', limit = 3)
            require(parts.isNotEmpty()) { "Invalid destination format" }
            return when (parts[0]) {
                "kafka" -> {
                    require(parts.size == 2) { "Kafka destination must be 'kafka:<topic>'" }
                    Kafka(parts[1])
                }

                "http" -> {
                    require(parts.size == 3) { "Http destination must be 'http:<clientId>:/path'" }
                    Http(parts[1], parts[2])
                }

                else -> error("Unknown destination kind '${parts[0]}'")
            }
        }
    }
}
