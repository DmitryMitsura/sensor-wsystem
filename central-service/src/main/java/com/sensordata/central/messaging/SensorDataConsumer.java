package com.sensordata.central.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensordata.central.domain.SensorDataEvent;
import com.sensordata.central.processing.ThresholdChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataConsumer {

    private final ThresholdChecker thresholdChecker;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${sensor-data.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String message) {
        try {
            SensorDataEvent event =
                    objectMapper.readValue(message, SensorDataEvent.class);

            thresholdChecker.check(event)
                    .ifPresent(alarm -> log.warn("ALARM: {}", alarm));

        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize SensorDataEvent: {}", message, e);
        }
    }
}
