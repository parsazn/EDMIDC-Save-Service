package com.example.edmidcsaveservice.consumer;

import com.example.edmidcsaveservice.configuration.KafkaTopicConfig;
import com.example.edmidcsaveservice.service.SavingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageConsumer {
    private final SavingService savingService;
    @Getter
    private final String[] topicsArray;
    public MessageConsumer(SavingService savingService , KafkaTopicConfig kafkaTopicConfig) {
        this.savingService = savingService;
        topicsArray = kafkaTopicConfig.getTopicsArray();
    }

    @KafkaListener(topics = "#{__listener.topicsArray}" , groupId = "group_id")
    public void listen(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode matrix = mapper.readValue(message, JsonNode.class);
            System.out.println("HashMap: " + message);
            String measurementName = matrix.get("measurementName").asText();
            String tag = matrix.get("tag").asText();
            savingService.save(matrix.get("result").toString() , tag , measurementName);
        } catch (IOException e) {
           throw new RuntimeException("Unable to consumer message : " + message + "\n due to : " + e);
        }

        System.out.println("Received Message in group foo: " + message);
    }

}
