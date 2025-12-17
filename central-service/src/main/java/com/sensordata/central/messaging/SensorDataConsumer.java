package com.sensordata.central.messaging;

import com.sensordata.central.processing.AlarmLogger;
import com.sensordata.central.processing.ThresholdChecker;
import com.sensordata.central.domain.SensorDataEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataConsumer {

    private final ThresholdChecker thresholdChecker;
    private final AlarmLogger alarmLogger;

    @KafkaListener(topics = "${sensor-data.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(SensorDataEvent event) {
        log.debug("Consumed SensorDataEvent: {}", event);
        thresholdChecker.check(event).ifPresent(alarmLogger::logAlarm);
    }
}
