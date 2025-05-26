import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    val kotlinVersion = "1.9.24"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot, WebFlux, R2DBC, Liquibase
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.liquibase:liquibase-core")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Spring JDBC для Liquibase
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // Драйвер PostgreSQL для JDBC-datasource
    runtimeOnly("org.postgresql:postgresql")


    // Kotlin stdlib
    implementation(kotlin("stdlib"))

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Spring Boot Test (без JUnit 4)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }

    // Kotest для тестов
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

    // Reactor Test
    testImplementation("io.projectreactor:reactor-test")

    // Mockk (если нужны моки)
    testImplementation("io.mockk:mockk:1.13.10")

    // Testcontainers для Postgres
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
    testImplementation("org.testcontainers:postgresql:1.19.7")

    // MockWebServer для мока HTTP-сервиса
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
