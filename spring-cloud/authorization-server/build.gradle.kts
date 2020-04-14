import java.net.URI;


plugins {
	id ("org.springframework.boot") version "2.2.6.RELEASE"
	id ("io.spring.dependency-management") version "1.0.9.RELEASE"
	id ("java")
	id ("jacoco")
	id ("org.sonarqube")
	id ("com.google.cloud.tools.jib") version "1.8.0"
  kotlin("jvm")
  kotlin("plugin.spring")
}

group = "fr.edjaz.springcloud"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11


repositories {
	mavenCentral()
  maven { url = URI("https://repo.spring.io/milestone") }
}

jib {
	from {
		image = "openjdk:12.0.2"
	}
	to {
		image = "edjaz/auth-server"
	}
}



dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	compileOnly ("com.google.code.findbugs:jsr305:3.0.2")
	implementation ("org.springframework.boot:spring-boot-starter-web")
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("org.springframework.boot:spring-boot-starter-security")

	implementation ("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
	implementation ("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation ("org.springframework.cloud:spring-cloud-starter-config")
	implementation ("org.springframework.cloud:spring-cloud-starter-sleuth")
	implementation ("org.springframework.cloud:spring-cloud-starter-zipkin")
	implementation ("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation ("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	implementation ("org.springframework.retry:spring-retry")

	implementation ("javax.xml.bind:jaxb-api")
	implementation ("com.sun.xml.bind:jaxb-core")
	implementation ("com.sun.xml.bind:jaxb-impl")
	implementation ("com.nimbusds:nimbus-jose-jwt:6.7")

	implementation("io.micrometer:micrometer-registry-prometheus")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}


sonarqube {
	properties {
		property ("sonar.sources", "src/main/")
		property ("sonar.tests", "src/test/")
	}
}



