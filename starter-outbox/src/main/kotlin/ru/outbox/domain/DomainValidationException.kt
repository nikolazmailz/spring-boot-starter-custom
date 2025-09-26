package ru.outbox.domain

/**
 * Исключение на уровне домена для валидационных ошибок (если понадобится
 * отличать от инфраструктурных SQL/HTTP ошибок). В starter можно использовать
 * стандартные IllegalArgumentException, но этот тип помогает явно отделить слой.
 */
class DomainValidationException(message: String) : RuntimeException(message)