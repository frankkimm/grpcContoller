package de.kimmlingen.controller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["de.kimmlingen"])
class ControllerApplication

fun main(args: Array<String>) {
    runApplication<ControllerApplication>(*args)
}
