package fr.edjaz.springcloud.gateway

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = ["eureka.client.enabled=false", "spring.cloud.config.enabled=false", "spring.cloud.kubernetes.enabled= false"]
)
class GatewayApplicationTests {
    @Test
    fun contextLoads() {
    }
}
