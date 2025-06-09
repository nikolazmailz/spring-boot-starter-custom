package ru.app.filter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.app.context.LoginContextHolder


@RestController
class WhoamiController {
    @GetMapping("/whoami")
    fun whoami(): Mono<String> = LoginContextHolder.getLogin()
}