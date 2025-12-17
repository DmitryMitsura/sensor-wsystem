package com.sensordata.warehouse.processing;

import com.sensordata.warehouse.domain.SensorDataEvent;
import com.sensordata.warehouse.domain.SensorType;
import com.sensordata.warehouse.config.WarehouseProperties;
import com.sensordata.warehouse.kafka.SensorDataPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataProcessor {

    private final WarehouseProperties warehouseProperties;
    private final SensorDataParser sensorDataParser;
    private final SensorDataValidator sensorDataValidator;
    private final SensorDataPublisher sensorDataPublisher;

    public void process(SensorType sensorType, String rawMessage) {
        var parsedOpt = sensorDataParser.parse(rawMessage);
        if (parsedOpt.isEmpty()) {
            log.warn("Invalid UDP message dropped: type={}, raw='{}'", sensorType, rawMessage);
            return;
        }

        var parsed = parsedOpt.get();
        if (!sensorDataValidator.isValid(parsed)) {
            log.warn("Invalid sensor data dropped: type={}, sensorId='{}', value={}, raw='{}'",
                    sensorType, parsed.sensorId(), parsed.value(), rawMessage);
            return;
        }

        var event = SensorDataEvent.builder()
                .warehouseId(warehouseProperties.id())
                .sensorId(parsed.sensorId())
                .sensorType(sensorType)
                .value(parsed.value())
                .receivedAt(Instant.now())
                .build();

        sensorDataPublisher.publish(event);
    }
}
