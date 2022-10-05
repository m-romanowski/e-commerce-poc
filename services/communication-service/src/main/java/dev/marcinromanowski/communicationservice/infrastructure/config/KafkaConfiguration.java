package dev.marcinromanowski.communicationservice.infrastructure.config;

import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Collections;
import java.util.Map;

@Configuration
class KafkaConfiguration {

    private static Map<String, Object> getKafkaConsumerProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> baseProperties = kafkaProperties.buildConsumerProperties();
        baseProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        baseProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        baseProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        baseProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        baseProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.name().toLowerCase());
        baseProperties.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        return Collections.unmodifiableMap(baseProperties);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        val consumerFactory = new DefaultKafkaConsumerFactory<>(getKafkaConsumerProperties(kafkaProperties));
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

}
