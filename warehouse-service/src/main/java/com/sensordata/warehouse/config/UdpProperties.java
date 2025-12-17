package com.sensordata.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "udp")
public record UdpProperties(int temperaturePort, int humidityPort) {
}
