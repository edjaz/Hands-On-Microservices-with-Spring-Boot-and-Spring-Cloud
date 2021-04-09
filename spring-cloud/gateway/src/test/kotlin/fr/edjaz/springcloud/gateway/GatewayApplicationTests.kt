package fr.edjaz.springcloud.gateway

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.kubernetes.enabled=false",
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.loadbalancer.enabled=false",
        "kubernetes.manifests.enabled=false",
        "kubernetes.informer.enabled=false",
    ]
)
class GatewayApplicationTests {
    @Test
    fun contextLoads() {
    }
}
