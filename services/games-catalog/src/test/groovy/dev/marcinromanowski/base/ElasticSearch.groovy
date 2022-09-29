package dev.marcinromanowski.base

import groovy.transform.CompileStatic
import org.testcontainers.elasticsearch.ElasticsearchContainer

@CompileStatic
class ElasticSearch {

    static final String ELASTICSEARCH_USERNAME = "elastic";

    static void startContainer() {
        def elasticSearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:5.6.16")
                .withPassword(ElasticsearchContainer.ELASTICSEARCH_DEFAULT_PASSWORD)
        elasticSearchContainer.start()

        System.setProperty("spring.jpa.properties.hibernate.search.default.elasticsearch.host", "$elasticSearchContainer.httpHostAddress")
        System.setProperty("spring.jpa.properties.hibernate.search.default.elasticsearch.username", ELASTICSEARCH_USERNAME)
        System.setProperty("spring.jpa.properties.hibernate.search.default.elasticsearch.password", ElasticsearchContainer.ELASTICSEARCH_DEFAULT_PASSWORD)
    }

}
