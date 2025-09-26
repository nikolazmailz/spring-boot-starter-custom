//package ru.outbox.config
//
//import jakarta.sql.DataSource
//import org.springframework.boot.autoconfigure.AutoConfiguration
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.context.annotation.Bean
//import liquibase.integration.spring.SpringLiquibase
//
///**
// * Регистрирует Liquibase changelog стартера, если:
// *  - liquibase-core на classpath,
// *  - есть JDBC DataSource (для Liquibase),
// *  - outbox.enabled=true
// *
// * Если у приложения уже есть свой SpringLiquibase — этот бин не создаётся.
// */
//@AutoConfiguration
//@ConditionalOnClass(SpringLiquibase::class)
//@ConditionalOnBean(DataSource::class)
//@ConditionalOnProperty(prefix = "outbox", name = ["enabled"], havingValue = "true", matchIfMissing = true)
//class OutboxLiquibaseAutoConfiguration {
//
//    @Bean
//    @ConditionalOnMissingBean(name = ["outboxSpringLiquibase"])
//    fun outboxSpringLiquibase(dataSource: DataSource): SpringLiquibase =
//        SpringLiquibase().apply {
//            this.dataSource = dataSource
//            this.changeLog = "classpath:db/changelog/outbox-changelog.yaml"
//            // contexts, defaultSchema и т.п. — по необходимости
//        }
//}
