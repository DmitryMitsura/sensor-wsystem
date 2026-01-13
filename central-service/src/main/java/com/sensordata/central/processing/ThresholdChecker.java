package com.sensordata.central.processing;

import com.sensordata.central.config.ThresholdProperties;
import com.sensordata.central.domain.SensorDataEvent;
import com.sensordata.central.domain.SensorType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Checks sensor values against configured thresholds which are defined per sensor type
@Component
@RequiredArgsConstructor
public class ThresholdChecker {

    private final ThresholdProperties thresholdProperties;
    private final Map<SensorType, Map<String, Double>> sensorThresholds = new HashMap<>();

    @PostConstruct
    private void parseThresholds() {
        Map<SensorType, Map<String, Double>> thresholds = thresholdProperties.thresholds();
        for (var typeEntry : thresholds.entrySet()) {
            SensorType sensorType = typeEntry.getKey();
            Map<String, Double> rules = typeEntry.getValue();

            Map<String, Double> sensorThreshold = new HashMap<>(); // store pairs <sensor_id, threshold>, NULL for default value
            for (var ruleEntry : rules.entrySet()) {
                String ruleKey = ruleEntry.getKey();
                Double threshold = ruleEntry.getValue();
                parseRule(sensorThreshold, ruleKey, threshold);
            }
            sensorThresholds.put(sensorType, sensorThreshold);
        }
    }

    private void parseRule(Map<String, Double> sensorThreshold, String ruleKey, Double threshold) {
        if (ruleKey.equals("default")) {
            sensorThreshold.putIfAbsent(null, threshold); // default value
        } else {
            Range range = getRangeBorders(ruleKey);
            for (int i = range.from; i <= range.to; i++)
                sensorThreshold.putIfAbsent(range.prefix + i, threshold);
        }
    }

    private Range getRangeBorders(String ruleKey) {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)(\\d+)-\\1(\\d+)");
        Matcher matcher = pattern.matcher(ruleKey);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range format: " + ruleKey);
        }

        int from = Integer.parseInt(matcher.group(2));
        int to = Integer.parseInt(matcher.group(3));

        return new Range(matcher.group(1), from, to);
    }

    private record Range(String prefix, int from, int to) {
    }

    public Optional<Alarm> check(SensorDataEvent event) {
        Optional<Double> threshold = thresholdFor(event.getSensorId(), event.getSensorType());
        if (threshold.isPresent() && event.getValue() > threshold.get()) {
            return Optional.of(new Alarm(event, threshold.get()));
        }
        return Optional.empty();
    }

    private Optional<Double> thresholdFor(String sensor_id, SensorType type) {
        Map<String, Double> sensorThreshold = sensorThresholds.get(type);
        if (sensorThreshold == null) {
            return Optional.empty();
        }

        Double value = sensorThreshold.get(sensor_id);
        if (value != null) {
            return Optional.of(value);
        }

        return Optional.ofNullable(sensorThreshold.get(null));
    }

    public record Alarm(SensorDataEvent event, double threshold) {
    }
}
