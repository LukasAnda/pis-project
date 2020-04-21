package sk.pismaniacs.fiitcompany.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class TestController {
    @RequestMapping("/hello")
    fun hello(): String {
        return "world"
    }
}