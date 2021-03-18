package fr.edjaz.microservices.core.recommendation

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableDiscoveryClient
class RecommendationServiceApplication {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }
}

fun main(args: Array<String>) {
    val ctx = SpringApplication.run(RecommendationServiceApplication::class.java, *args)
    val mongodDbHost = ctx.environment.getProperty("spring.data.mongodb.host")
    val mongodDbPort = ctx.environment.getProperty("spring.data.mongodb.port")
    RecommendationServiceApplication.logger.info("Connected to MongoDb: $mongodDbHost:$mongodDbPort")
}
