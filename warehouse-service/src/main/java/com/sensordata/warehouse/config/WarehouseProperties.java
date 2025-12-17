package com.sensordata.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "warehouse")
public record WarehouseProperties(String id) {
}
