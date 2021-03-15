import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI;

plugins {
	id ("org.springframework.boot")
	id ("io.spring.dependency-management")
	id ("java")
	id ("jacoco")
	id ("org.sonarqube")
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

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	compileOnly ("com.google.code.findbugs:jsr305:3.0.2")
	implementation ("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
	implementation ("org.springframework.cloud:spring-cloud-starter-config")
	implementation ("org.springframework.cloud:spring-cloud-starter-sleuth")
	implementation ("org.springframework.cloud:spring-cloud-starter-zipkin")
	implementation ("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation ("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	implementation ("org.springframework.retry:spring-retry")
	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.glassfish.jaxb:jaxb-runtime")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
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


