package com.sensordata.central.processing;

import com.sensordata.central.config.ThresholdProperties;
import com.sensordata.central.domain.SensorDataEvent;
import com.sensordata.central.domain.SensorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThresholdChecker {

    private final ThresholdProperties thresholdProperties;

    public Optional<Alarm> check(SensorDataEvent event) {
        double threshold = thresholdFor(event.getSensorType());
        if (event.getValue() > threshold) {
            return Optional.of(new Alarm(event, threshold));
        }
        return Optional.empty();
    }

    private double thresholdFor(SensorType type) {
        return switch (type) {
            case TEMPERATURE -> thresholdProperties.temperature();
            case HUMIDITY -> thresholdProperties.humidity();
        };
    }

    public record Alarm(SensorDataEvent event, double threshold) {
    }
}
