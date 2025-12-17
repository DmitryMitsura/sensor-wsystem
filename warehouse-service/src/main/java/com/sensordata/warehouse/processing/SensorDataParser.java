package com.sensordata.warehouse.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SensorDataParser {

    public Optional<ParsedSensorData> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        Map<String, String> kv = new HashMap<>();
        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] pair = trimmed.split("=", 2);
            if (pair.length != 2) {
                return Optional.empty();
            }
            kv.put(pair[0].trim(), pair[1].trim());
        }

        String sensorId = kv.get("sensor_id");
        String valueStr = kv.get("value");
        if (sensorId == null || valueStr == null) {
            return Optional.empty();
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return Optional.of(new ParsedSensorData(sensorId, value));
    }
}
