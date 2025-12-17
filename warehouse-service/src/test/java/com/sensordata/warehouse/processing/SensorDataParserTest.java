package com.sensordata.warehouse.processing;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class SensorDataParserTest {

    private final SensorDataParser parser = new SensorDataParser();

    @Test
    void shouldParseValidMessage() {
        Optional<ParsedSensorData> result =
                parser.parse("sensor_id=t1; value=36");

        assertThat(result).isPresent();
        assertThat(result.get().sensorId()).isEqualTo("t1");
        assertThat(result.get().value()).isEqualTo(36.0);
    }

    @Test
    void shouldIgnoreUnknownFields() {
        Optional<ParsedSensorData> result =
                parser.parse("sensor_id=t1; value=36; unit=C");

        assertThat(result).isPresent();
    }

    @Test
    void shouldRejectInvalidMessage() {
        Optional<ParsedSensorData> result =
                parser.parse("sensor_id=t1; value=abc");

        assertThat(result).isEmpty();
    }
}
