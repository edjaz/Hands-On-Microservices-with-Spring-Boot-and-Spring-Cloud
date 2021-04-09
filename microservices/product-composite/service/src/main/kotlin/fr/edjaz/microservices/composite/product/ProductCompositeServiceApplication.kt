package fr.edjaz.microservices.composite.product

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.SwaggerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.reactive.function.client.WebClient
import reactivefeign.spring.config.EnableReactiveFeignClients

@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableConfigurationProperties(
    SwaggerProperties::class
)
@EnableDiscoveryClient
@EnableReactiveFeignClients
@EnableFeignClients
class ProductCompositeServiceApplication {
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
