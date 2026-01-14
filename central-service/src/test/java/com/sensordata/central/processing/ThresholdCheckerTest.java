package com.sensordata.central.processing;

import com.sensordata.central.config.ThresholdProperties;
import com.sensordata.central.domain.SensorDataEvent;
import com.sensordata.central.domain.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThresholdCheckerTest {

    @Test
    @DisplayName("Raises alarm when sensor value exceeds range-specific threshold")
    void shouldRaiseAlarmWhenValueExceedsSpecificThreshold() {
        ThresholdChecker checker = buildCheckerWithThresholds(Map.of(
                SensorType.TEMPERATURE, new LinkedHashMap<>(Map.of(
                        "default", 30.0,
                        "t1-t10", 35.0
                ))
        ));

        SensorDataEvent event = mockEvent("t1", SensorType.TEMPERATURE, 36.0);

        Optional<ThresholdChecker.Alarm> alarm = checker.check(event);

        assertTrue(alarm.isPresent());
        assertEquals(35.0, alarm.get().threshold(), 0.0001);
        assertSame(event, alarm.get().event());
    }

    @Test
    @DisplayName("Uses default threshold when sensorId does not match any range")
    void shouldUseDefaultThresholdWhenSensorIdNotMatched() {
        ThresholdChecker checker = buildCheckerWithThresholds(Map.of(
                SensorType.TEMPERATURE, new LinkedHashMap<>(Map.of(
                        "default", 30.0,
                        "t1-t10", 35.0
                ))
        ));

        SensorDataEvent event = mockEvent("t999", SensorType.TEMPERATURE, 31.0);

        Optional<ThresholdChecker.Alarm> alarm = checker.check(event);

        assertTrue(alarm.isPresent());
        assertEquals(30.0, alarm.get().threshold(), 0.0001);
    }

    @Test
    @DisplayName("Ignores event when no thresholds are configured for sensor type")
    void shouldIgnoreEventWhenTypeNotConfigured() {
        ThresholdChecker checker = buildCheckerWithThresholds(Map.of(
                SensorType.TEMPERATURE, new LinkedHashMap<>(Map.of(
                        "default", 30.0,
                        "t1-t10", 35.0
                ))
        ));

        SensorDataEvent event = mockEvent("h1", SensorType.HUMIDITY, 999.0);

        assertTrue(checker.check(event).isEmpty());
    }

    @Test
    @DisplayName("Applies first matching rule when threshold ranges overlap")
    void shouldUseFirstMatchingRuleWhenRangesOverlap() {
        LinkedHashMap<String, Double> rules = new LinkedHashMap<>();
        rules.put("default", 30.0);
        rules.put("t1-t10", 35.0);
        rules.put("t5-t15", 40.0);

        ThresholdChecker checker = buildCheckerWithThresholds(Map.of(
                SensorType.TEMPERATURE, rules
        ));

        SensorDataEvent event = mockEvent("t6", SensorType.TEMPERATURE, 36.0);
        Optional<ThresholdChecker.Alarm> alarm = checker.check(event);

        assertTrue(alarm.isPresent());
        assertEquals(35.0, alarm.get().threshold(), 0.0001);
    }

    @Test
    @DisplayName("Supports custom sensorId prefixes in threshold ranges")
    void shouldParseCustomPrefixRanges() {
        ThresholdChecker checker = buildCheckerWithThresholds(Map.of(
                SensorType.TEMPERATURE, new LinkedHashMap<>(Map.of(
                        "default", 30.0,
                        "temp11-temp20", 40.0
                ))
        ));

        SensorDataEvent event = mockEvent("temp12", SensorType.TEMPERATURE, 41.0);
        Optional<ThresholdChecker.Alarm> alarm = checker.check(event);

        assertTrue(alarm.isPresent());
        assertEquals(40.0, alarm.get().threshold(), 0.0001);
    }

    @Test
    @DisplayName("Fails fast when thresholds configuration is empty")
    void shouldThrowWhenThresholdsAreEmpty() {
        ThresholdProperties props = new ThresholdProperties(Map.of());
        ThresholdChecker checker = new ThresholdChecker(props);
        assertThrows(IllegalStateException.class, () -> invokePostConstruct(checker));
    }

    @Test
    @DisplayName("Fails fast when a threshold value is null")
    void shouldThrowWhenThresholdValueIsNull() {
        LinkedHashMap<String, Double> rules = new LinkedHashMap<>();
        rules.put("default", null);

        ThresholdProperties props = new ThresholdProperties(Map.of(
                SensorType.TEMPERATURE, rules
        ));
        ThresholdChecker checker = new ThresholdChecker(props);
        assertThrows(IllegalStateException.class, () -> invokePostConstruct(checker));
    }

    @Test
    @DisplayName("Fails fast when threshold range format is invalid")
    void shouldThrowWhenRangeFormatIsInvalid() {
        LinkedHashMap<String, Double> rules = new LinkedHashMap<>();
        rules.put("default", 30.0);
        rules.put("t1-xx", 35.0);

        ThresholdProperties props = new ThresholdProperties(Map.of(
                SensorType.TEMPERATURE, rules
        ));
        ThresholdChecker checker = new ThresholdChecker(props);
        assertThrows(IllegalArgumentException.class, () -> invokePostConstruct(checker));
    }

    private static ThresholdChecker buildCheckerWithThresholds(Map<SensorType, Map<String, Double>> thresholds) {
        ThresholdProperties props = new ThresholdProperties(thresholds);
        ThresholdChecker checker = new ThresholdChecker(props);
        invokePostConstruct(checker);
        return checker;
    }

    private static void invokePostConstruct(ThresholdChecker checker) {
        try {
            Method m = ThresholdChecker.class.getDeclaredMethod("parseThresholds");
            m.setAccessible(true);
            m.invoke(checker);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SensorDataEvent mockEvent(String sensorId, SensorType type, double value) {
        SensorDataEvent event = mock(SensorDataEvent.class);
        when(event.getSensorId()).thenReturn(sensorId);
        when(event.getSensorType()).thenReturn(type);
        when(event.getValue()).thenReturn(value);
        return event;
    }
}
