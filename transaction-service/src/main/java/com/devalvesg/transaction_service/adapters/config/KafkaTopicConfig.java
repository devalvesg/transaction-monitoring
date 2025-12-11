package com.devalvesg.transaction_service.adapters.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topics.transactions-created}")
    private String transactionsCreatedTopic;

    @Value("${spring.kafka.topics.transactions-updated}")
    private String transactionsUpdatedTopic;

    @Bean
    public NewTopic transactionsCreatedTopic() {
        return TopicBuilder
                .name(transactionsCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsUpdatedTopic() {
        return TopicBuilder
                .name(transactionsUpdatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
