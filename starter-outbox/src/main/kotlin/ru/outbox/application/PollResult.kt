package ru.outbox.application

data class PollResult(
    val locked: Int,
    val sent: Int,
    val failed: Int
)