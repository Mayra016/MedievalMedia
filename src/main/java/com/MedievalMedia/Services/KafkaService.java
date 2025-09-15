package com.MedievalMedia.Services;

import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class KafkaService {

    private final Logger log = LoggerFactory.getLogger(KafkaService.class);
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topicName, String message) {
        CompletableFuture<SendResult<String, String>> future = this.kafkaTemplate.send(topicName, message);
        future.whenComplete((result, response) -> {
            if (response == null) {
                this.log.info("Message successfull sended to Kafka Server");
            } else {
                this.log.error("Error sending message to Kafka Server: " + response.getMessage());
            }
        });
        
    }
}
