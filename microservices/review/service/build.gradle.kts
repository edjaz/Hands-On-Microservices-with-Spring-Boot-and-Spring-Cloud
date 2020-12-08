import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI;

plugins {
	id ("org.springframework.boot") version "2.2.11.RELEASE"
	id ("io.spring.dependency-management") version "1.0.10.RELEASE"
	id ("java")
	id ("jacoco")
	id ("org.sonarqube")
	id ("com.google.cloud.tools.jib") version "1.8.0"
  kotlin("jvm")
  kotlin("plugin.spring")
  kotlin("kapt")
}


group = "fr.edjaz.microservices.core.review"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

base {
	archivesBaseName = "review-service"
}

jib {
	from {
		image = "openjdk:12.0.2"
	}
	to {
		image = "edjaz/review/service"
	}
}



val developmentOnly by configurations.creating
configurations {
	runtimeClasspath {
		extendsFrom(developmentOnly)
	}
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()

	maven { url = URI("https://repo.spring.io/milestone") }
}



dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	compileOnly ("com.google.code.findbugs:jsr305:3.0.2")
/*
	compileOnly ("org.projectlombok:lombok")
	annotationProcessor ("org.projectlombok:lombok")
*/


	implementation (project(":api"))
	implementation (project(":microservices:review:api"))
	implementation (project(":util"))

	developmentOnly ("org.springframework.boot:spring-boot-devtools")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation ("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation ("org.springframework.cloud:spring-cloud-starter-kubernetes")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
	implementation("org.springframework.cloud:spring-cloud-starter-zipkin")
	implementation("org.springframework.retry:spring-retry")
	implementation("mysql:mysql-connector-java")
	implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")

	compileOnly ("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")
  kapt ("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")
	testAnnotationProcessor ("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }

	testImplementation("org.springframework.cloud:spring-cloud-stream-test-support")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.h2database:h2")


	testImplementation (project(":microservices:product:api"))


	//compile ("io.dekorate:kubernetes-spring-starter:0.9.9")
	//annotationProcessor ("io.dekorate:kubernetes-annotations:0.9.9")

	implementation("io.micrometer:micrometer-registry-prometheus")
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
