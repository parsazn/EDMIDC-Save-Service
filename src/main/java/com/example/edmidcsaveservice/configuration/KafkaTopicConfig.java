package com.example.edmidcsaveservice.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Getter
public class KafkaTopicConfig {
    @Value("${spring.kafka.topics}")
    private String topics;

    private String[] topicsArray;

    @PostConstruct
    public void init() {
        // Split the kafkaTopics string by comma and trim any whitespace
        topicsArray = Arrays.stream(topics.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }
}
