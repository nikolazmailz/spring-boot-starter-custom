dependencies {
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.springframework.boot:spring-boot-starter-json")

    // R2DBC + Postgres (исправлено: другая группа артефакта)
    api("org.postgresql:r2dbc-postgresql")
    implementation("io.r2dbc:r2dbc-pool")

    // Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.testcontainers:postgresql:1.20.3")
    testImplementation("org.testcontainers:r2dbc:1.20.3")
}

tasks.test { useJUnitPlatform() }
kotlin { jvmToolchain(21) }