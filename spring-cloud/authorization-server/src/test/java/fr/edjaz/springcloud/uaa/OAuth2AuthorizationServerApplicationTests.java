/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.edjaz.springcloud.uaa;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link OAuth2AuthorizationServerApplication}
 *
 * @author Josh Cummings
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"eureka.client.enabled=false","spring.cloud.config.enabled=false", "spring.cloud.kubernetes.enabled= false"})
@AutoConfigureMockMvc
class OAuth2AuthorizationServerApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void requestTokenWhenUsingPasswordGrantTypeThenOk()
			throws Exception {

		this.mvc.perform(post("/oauth/token")
			.param("grant_type", "password")
			.param("username", "dkahn")
			.param("password", "password")
			.header("Authorization", "Basic cmVhZGVyOnNlY3JldA=="))
				.andExpect(status().isOk());
	}

	@Test
	void requestJwkSetWhenUsingDefaultsThenOk()
			throws Exception {

		this.mvc.perform(get("/.well-known/jwks.json"))
				.andExpect(status().isOk());
	}

}
