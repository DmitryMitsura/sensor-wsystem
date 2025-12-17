package com.sensordata.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CentralApplication {
    public static void main(String[] args) {
        SpringApplication.run(CentralApplication.class, args);
    }
}
