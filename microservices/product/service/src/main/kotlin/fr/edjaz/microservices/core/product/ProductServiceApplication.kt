package fr.edjaz.microservices.core.product

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("fr.edjaz")
class ProductServiceApplication

fun main(args: Array<String>) {

  val LOG = LoggerFactory.getLogger(ProductServiceApplication::class.java)
  val ctx = SpringApplication.run(ProductServiceApplication::class.java, *args)
  val mongodDbHost = ctx.environment.getProperty("spring.data.mongodb.host")
  val mongodDbPort = ctx.environment.getProperty("spring.data.mongodb.port")
  LOG.info("Connected to MongoDb: $mongodDbHost:$mongodDbPort")
}

