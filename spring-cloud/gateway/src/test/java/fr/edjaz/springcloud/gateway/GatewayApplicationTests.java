package fr.edjaz.springcloud.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false","spring.cloud.config.enabled=false", "spring.cloud.kubernetes.enabled= false"})
public class GatewayApplicationTests {

	@Test
	public void contextLoads() {
	}

}
