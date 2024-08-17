package com.example.edmidcsaveservice.consumer;

import com.example.edmidcsaveservice.configuration.KafkaConsumerConfig;
import com.example.edmidcsaveservice.configuration.KafkaTopicConfig;
import com.example.edmidcsaveservice.service.SavingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class MessageConsumer {
    private final SavingService savingService;
    @Getter
    private final String[] topicsArray;
    @Getter
    private final String groupId;

    public MessageConsumer(SavingService savingService, KafkaTopicConfig kafkaTopicConfig, KafkaConsumerConfig kafkaConsumerConfig) {
        this.savingService = savingService;
        this.topicsArray = kafkaTopicConfig.getTopicsArray();
        this.groupId = kafkaConsumerConfig.getGroupId();
    }

    @KafkaListener(topics = "#{__listener.topicsArray}", groupId = "#{__listener.groupId}")
    public void listen(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode matrix = mapper.readValue(message, JsonNode.class);
            System.out.println("HashMap: " + message);
            String measurementName = matrix.get("measurementName").asText();
            String tag = matrix.get("tag").asText();
            long created = matrix.get("created").asLong(new Date().getTime());
            savingService.save(matrix.get("result").toString(), tag, measurementName, created);
        } catch (IOException e) {
            throw new RuntimeException("Unable to consumer message : " + message + "\n due to : " + e);
        }

        System.out.println("Received Message in group foo: " + message);
    }

}
