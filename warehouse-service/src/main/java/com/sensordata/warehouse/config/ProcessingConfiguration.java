package com.sensordata.warehouse.config;

import com.sensordata.warehouse.processing.SensorDataParser;
import com.sensordata.warehouse.processing.SensorDataValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessingConfiguration {

    @Bean
    public SensorDataParser sensorDataParser() {
        return new SensorDataParser();
    }

    @Bean
    public SensorDataValidator sensorDataValidator() {
        return new SensorDataValidator();
    }
}
