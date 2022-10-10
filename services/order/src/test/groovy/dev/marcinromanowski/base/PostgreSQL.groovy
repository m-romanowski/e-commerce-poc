package dev.marcinromanowski.base

import groovy.transform.CompileStatic
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer

@CompileStatic
class PostgreSQL {

    static final short DEFAULT_PORT = 5432
    static final String DEFAULT_USERNAME = "test"
    static final String DEFAULT_PASSWORD = "test"
    static final String DEFAULT_DB_NAME = "test"
    static final String DEFAULT_SERVICE_NAME = "tests-postgresql"

    static void startContainer(Network network) {
        def container = new PostgreSQLContainer("postgres:14.5")
                .withUsername(DEFAULT_USERNAME)
                .withPassword(DEFAULT_PASSWORD)
                .withDatabaseName(DEFAULT_DB_NAME)
                .withNetwork(network)
                .withNetworkAliases(DEFAULT_SERVICE_NAME) as PostgreSQLContainer
        container.start()

        System.setProperty("spring.r2dbc.url", container.jdbcUrl.replace("jdbc", "r2dbc"))
        System.setProperty("spring.r2dbc.username", container.username)
        System.setProperty("spring.r2dbc.password", container.password)
    }

    static String getNetworkJdbcUrl() {
        return "jdbc:postgresql://$DEFAULT_SERVICE_NAME:$DEFAULT_PORT/$DEFAULT_DB_NAME"
    }

}
