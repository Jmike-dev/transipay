package lab.transipay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TransipayApplication

fun main(args: Array<String>) {
    runApplication<TransipayApplication>(*args)
}