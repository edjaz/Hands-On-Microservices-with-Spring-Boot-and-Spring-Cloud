package fr.edjaz.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors


@ComponentScan("fr.edjaz")
@EnableDiscoveryClient
@SpringBootApplication
class ReviewServiceApplication {
  companion object {
    @Suppress("JAVA_CLASS_ON_COMPANION")
    @JvmStatic
    val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
  }

  private var connectionPoolSize: Int

  @Autowired
  constructor(@Value("\${spring.datasource.maximum-pool-size:10}") connectionPoolSize: Int) {
    this.connectionPoolSize = connectionPoolSize
  }


  @Bean
  fun jdbcScheduler(): Scheduler? {
    logger.info("Creates a jdbcScheduler with connectionPoolSize = $connectionPoolSize")
    return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
  }


}

fun main(args: Array<String>) {
  val ctx = runApplication<ReviewServiceApplication>(*args)
  val mysqlUri = ctx.environment.getProperty("spring.datasource.url")
  ReviewServiceApplication.logger.info("Connected to MySQL: $mysqlUri")
}
