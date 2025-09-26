package ru.outbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Минимальное тестовое приложение для поднятия Spring Boot контекста.
 * Используется в интеграционных тестах стартера (Kotest/Testcontainers).
 *
 * Включает:
 * - @SpringBootApplication для автоконфигурации
 * - @EnableScheduling, чтобы работал PollingScheduler
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["ru.outbox"])
class TestSpringBootApplication

fun main(args: Array<String>) {
    runApplication<TestSpringBootApplication>(*args)
}