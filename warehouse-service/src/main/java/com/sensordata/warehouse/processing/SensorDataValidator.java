package com.sensordata.warehouse.processing;

public class SensorDataValidator {

    public boolean isValid(ParsedSensorData data) {
        if (data == null) {
            return false;
        }
        if (data.sensorId() == null || data.sensorId().isBlank()) {
            return false;
        }
        return Double.isFinite(data.value());
    }
}
