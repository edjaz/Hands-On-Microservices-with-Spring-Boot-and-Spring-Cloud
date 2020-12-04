package fr.edjaz.microservices.composite.product;

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
@ComponentScan("fr.edjaz")
@EnableConfigurationProperties({
        ServicesProperties.RecommendationServiceProperties.class,
        ServicesProperties.ReviewServiceProperties.class,
        ServicesProperties.ProductServiceProperties.class,
        ServicesProperties.SwaggerProperties.class
})
@EnableDiscoveryClient
public class ProductCompositeServiceApplication {

	@Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        final WebClient.Builder builder = WebClient.builder();
        return builder;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }
}
