import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties


plugins {
  java
  id("org.sonarqube") version "2.8"
  jacoco
  kotlin("jvm") version "1.3.61"
  kotlin("plugin.spring") version "1.3.61"
}


group = "fr.edjaz"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

ext {
    set("springfoxVersion", "3.0.0-SNAPSHOT")
    set("resilience4jVersion", "1.1.0")
    set("mapstructVersion", "1.3.1.Final")
    set("springCloudVersion", "Hoxton.RC2")
}

repositories {
    mavenCentral()
}


dependencies {
  "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")

  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}


tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }
}

jacoco {
    toolVersion = "0.8.5"
}


tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
  }
}

sonarqube {
  properties {
    val sonarProperties = Properties()
    sonarProperties.load(file("sonar-project.properties").inputStream())
    sonarProperties.forEach {
      property(it.key as String, it.value)
    }
  }
}




