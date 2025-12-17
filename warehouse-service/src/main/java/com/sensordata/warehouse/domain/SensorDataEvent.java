package com.sensordata.warehouse.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@Getter
public class SensorDataEvent {
    String warehouseId;
    String sensorId;
    SensorType sensorType;
    double value;
    Instant receivedAt;
}
