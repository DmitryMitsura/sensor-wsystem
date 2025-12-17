package com.sensordata.central.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlarmLogger {

    public void logAlarm(ThresholdChecker.Alarm alarm) {
        var e = alarm.event();
        log.warn("ALARM! warehouseId={}, sensorId={}, type={}, value={}, threshold={}, receivedAt={}",
                e.getWarehouseId(),
                e.getSensorId(),
                e.getSensorType(),
                e.getValue(),
                alarm.threshold(),
                e.getReceivedAt());
    }
}
