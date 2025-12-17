package com.sensordata.warehouse.kafka;

import com.sensordata.warehouse.domain.SensorDataEvent;
import com.sensordata.warehouse.config.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataPublisher {

    private final KafkaTopicProperties topicProperties;
    private final KafkaTemplate<String, SensorDataEvent> kafkaTemplate;

    public void publish(SensorDataEvent event) {
        // key ensures ordering of events per sensor within a warehouse
        String key = event.getWarehouseId() + ":" + event.getSensorId();

        kafkaTemplate.send(topicProperties.topic(), key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish SensorDataEvent: key={}, event={}", key, event, ex);
                    } else {
                        log.debug("Published SensorDataEvent: topic={}, key={}, offset={}",
                                topicProperties.topic(), key, result.getRecordMetadata().offset());
                    }
                });
    }
}
