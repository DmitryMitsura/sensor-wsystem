package com.sensordata.warehouse.config;

import com.sensordata.warehouse.domain.SensorType;
import com.sensordata.warehouse.processing.SensorDataProcessor;
import com.sensordata.warehouse.udp.SensorDataUdpListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UdpListenerConfiguration {

    @Bean
    public SensorDataUdpListener temperatureUdpListener(UdpProperties udpProperties, SensorDataProcessor sensorDataProcessor) {
        return new SensorDataUdpListener(
                udpProperties.temperaturePort(),
                SensorType.TEMPERATURE,
                sensorDataProcessor);
    }

    @Bean
    public SensorDataUdpListener humidityUdpListener(UdpProperties udpProperties, SensorDataProcessor sensorDataProcessor) {
        return new SensorDataUdpListener(
                udpProperties.humidityPort(),
                SensorType.HUMIDITY,
                sensorDataProcessor);
    }
}
