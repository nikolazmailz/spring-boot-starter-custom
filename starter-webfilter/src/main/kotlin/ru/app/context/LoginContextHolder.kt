package ru.app.context

import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.app.filter.LoginHeaderWebFilter

object LoginContextHolder {
    /**
     * Получить логин из ReactorContext для Mono-потока.
     * Если контекст отсутствует — вернуть null (или можно дефолт).
     */
    fun getLogin(): Mono<String> =
        Mono.deferContextual { ctx ->
            Mono.justOrEmpty(ctx.getOrEmpty<String>(LoginHeaderWebFilter.LOGIN_CONTEXT_KEY))
        }

    /**
     * Получить логин из ReactorContext для Flux-потока.
     */
    fun getLoginFlux(): Flux<String> =
        Flux.deferContextual { ctx ->
            val value = ctx.getOrEmpty<String>(LoginHeaderWebFilter.LOGIN_CONTEXT_KEY)
            value.map { Flux.just(it) }.orElseGet { Flux.empty() }
        }.flatMap { it as Publisher<out String?>? }
}