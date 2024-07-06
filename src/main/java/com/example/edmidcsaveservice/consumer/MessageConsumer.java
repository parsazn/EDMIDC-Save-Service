package com.example.edmidcsaveservice.consumer;

import com.example.edmidcsaveservice.service.SavingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@AllArgsConstructor
@Component
public class MessageConsumer {

    private final SavingService savingService;

    @KafkaListener(topics = "edmidc-bitcoin-cdc" , groupId = "group_iid")
    public void listen(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode matrix = mapper.readValue(message, JsonNode.class);
            System.out.println("HashMap: " + message);
            matrix.forEach(node ->
                savingService.save(node)
            );
        } catch (IOException e) {
           throw new RuntimeException("Unable to consumer message : " + message + "\n due to : " + e);
        }

        System.out.println("Received Message in group foo: " + message);
    }

}
