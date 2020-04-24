package sk.pismaniacs.fiitcompany

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FiitCompanyApplication

fun main(args: Array<String>) {
	runApplication<FiitCompanyApplication>(*args)
}
