package com.sensordata.central.config;

import com.sensordata.central.domain.SensorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "threshold")
public record ThresholdProperties(Map<SensorType, Map<String, Double>> thresholds) {
}
