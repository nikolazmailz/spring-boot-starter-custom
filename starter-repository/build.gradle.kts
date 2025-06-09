dependencies {


    // database
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    runtimeOnly("org.postgresql:postgresql")

    // liquibase
    implementation("org.liquibase:liquibase-core")
}