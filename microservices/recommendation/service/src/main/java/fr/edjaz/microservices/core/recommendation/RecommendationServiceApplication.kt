package fr.edjaz.microservices.core.recommendation


import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableDiscoveryClient
class RecommendationServiceApplication


fun main(args: Array<String>) {
  val LOG = LoggerFactory.getLogger(RecommendationServiceApplication::class.java)
  val ctx = SpringApplication.run(RecommendationServiceApplication::class.java, *args)
  val mongodDbHost = ctx.environment.getProperty("spring.data.mongodb.host")
  val mongodDbPort = ctx.environment.getProperty("spring.data.mongodb.port")
  LOG.info("Connected to MongoDb: $mongodDbHost:$mongodDbPort")
}
