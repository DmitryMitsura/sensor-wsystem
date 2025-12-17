package com.sensordata.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sensor-data")
public record KafkaTopicProperties(String topic) {
}
