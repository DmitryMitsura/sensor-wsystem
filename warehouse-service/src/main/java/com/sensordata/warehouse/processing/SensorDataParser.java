package com.sensordata.warehouse.processing;

import org.springframework.stereotype.Component;

import java.util.Optional;

// Parses raw UDP message into structured sensor data
// Expected format: "sensor_id=...; value=..."
@Component
public class SensorDataParser {

    private static final String SENSOR_ID = "sensor_id";
    private static final String VALUE = "value";

    public Optional<ParsedSensorData> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String sensorId = null;
        Double value = null;

        // Parse key-value pairs from raw message; unknown fields are ignored
        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] pair = trimmed.split("=", 2);
            if (pair.length != 2) {
                return Optional.empty();
            }

            switch (pair[0].trim()) {
                case SENSOR_ID -> sensorId = pair[1].trim();
                case VALUE -> {
                    try {
                        value = Double.parseDouble(pair[1].trim());
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                }
            }
        }

        // Validate presence of mandatory fields
        if (sensorId == null || value == null) {
            return Optional.empty();
        }

        return Optional.of(new ParsedSensorData(sensorId, value));
    }
}
