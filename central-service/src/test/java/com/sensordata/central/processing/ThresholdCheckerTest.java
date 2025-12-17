package com.sensordata.central.processing;

import com.sensordata.central.domain.SensorDataEvent;
import com.sensordata.central.domain.SensorType;
import com.sensordata.central.config.ThresholdProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ThresholdCheckerTest {

    private final ThresholdProperties properties =
            new ThresholdProperties(35.0, 70.0);

    private final ThresholdChecker checker =
            new ThresholdChecker(properties);

    @Test
    void shouldNotTriggerAlarmWhenBelowThreshold() {
        SensorDataEvent event = SensorDataEvent.builder()
                .warehouseId("wh-1")
                .sensorId("t1")
                .sensorType(SensorType.TEMPERATURE)
                .value(30.0)
                .receivedAt(Instant.now())
                .build();

        assertThat(checker.check(event)).isEmpty();
    }

    @Test
    void shouldTriggerAlarmWhenAboveThreshold() {
        SensorDataEvent event = SensorDataEvent.builder()
                .warehouseId("wh-1")
                .sensorId("t1")
                .sensorType(SensorType.TEMPERATURE)
                .value(36.0)
                .receivedAt(Instant.now())
                .build();

        var alarmOpt = checker.check(event);

        assertThat(alarmOpt).isPresent();
        assertThat(alarmOpt.get().event()).isEqualTo(event);
        assertThat(alarmOpt.get().threshold()).isEqualTo(35.0);
    }
}
