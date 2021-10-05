package fr.edjaz.microservices.core.product

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableDiscoveryClient
class ProductServiceApplication {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Bean
    fun javaTimeModule(): Module {
        return JavaTimeModule()
    }
}

fun main(args: Array<String>) {
    val ctx = SpringApplication.run(ProductServiceApplication::class.java, *args)
    val mongodDbHost = ctx.environment.getProperty("spring.data.mongodb.host")
    val mongodDbPort = ctx.environment.getProperty("spring.data.mongodb.port")
    ProductServiceApplication.logger.info("Connected to MongoDb: $mongodDbHost:$mongodDbPort")
}
