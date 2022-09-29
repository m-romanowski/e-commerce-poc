package dev.marcinromanowski.base

import groovy.transform.CompileStatic
import org.testcontainers.containers.PostgreSQLContainer

@CompileStatic
class PostgreSQL {

    static void startContainer() {
        def postgreSQLContainer = new PostgreSQLContainer("postgres:14.5")
        postgreSQLContainer.start()

        System.setProperty("spring.datasource.url", postgreSQLContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", postgreSQLContainer.username)
        System.setProperty("spring.datasource.password", postgreSQLContainer.password)
    }

}
