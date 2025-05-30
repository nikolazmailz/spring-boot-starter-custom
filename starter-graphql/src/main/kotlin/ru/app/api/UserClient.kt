package ru.app.api

import reactor.core.publisher.Mono

/**
 * Сервис для поиска сотрудника по логину (email).
 * Интерфейс не зависит от Spring и не привязан к инфраструктуре.
 */
interface UserClient {
    /**
     * Поиск сотрудника по логину (email).
     *
     * @param login логин/почта сотрудника
     * @return Mono с найденным сотрудником или пустой Mono, если не найден
     */
    fun findByLogin(login: String): Mono<UserInfo>
}
