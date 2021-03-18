package fr.edjaz.springcloud.eurekaserver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.config.enabled=false", "management.health.rabbit.enabled=false"]
)
class EurekaServerApplicationTests {
    @Test
    fun contextLoads() {
    }

    @Value("\${app.eureka-username}")
    private val username: String? = null

    @Value("\${app.eureka-password}")
    private val password: String? = null

    @Autowired
    fun setTestRestTemplate(testRestTemplate: TestRestTemplate) {
        this.testRestTemplate = testRestTemplate.withBasicAuth(username, password)
    }

    private var testRestTemplate: TestRestTemplate? = null

    @Test
    fun catalogLoads() {
        val expectedResponseBody =
            "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}"
        val entity = testRestTemplate!!.getForEntity("/eureka/apps", String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals(expectedResponseBody, entity.body)
    }

    @Test
    fun healthy() {
        val expectedResponseBody = "{\"status\":\"UP\"}"
        val entity = testRestTemplate!!.getForEntity("/actuator/health", String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals(expectedResponseBody, entity.body)
    }
}
