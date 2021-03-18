package fr.edjaz.microservices.composite.product

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.ProductServiceProperties
import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.RecommendationServiceProperties
import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.ReviewServiceProperties
import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.SwaggerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableConfigurationProperties(
    RecommendationServiceProperties::class,
    ReviewServiceProperties::class,
    ProductServiceProperties::class,
    SwaggerProperties::class
)
@EnableDiscoveryClient
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
