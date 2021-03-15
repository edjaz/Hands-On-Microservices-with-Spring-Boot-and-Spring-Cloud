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
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.springframework.cloud:spring-cloud-config-server")
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
