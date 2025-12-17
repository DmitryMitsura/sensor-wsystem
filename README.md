# Sensor Data Monitoring System

Simple event-driven system for processing sensor data via UDP and Kafka.

## Overview

The system consists of two independent services:

- **Warehouse Service**
    - Receives sensor data via UDP
    - Parses and validates raw messages
    - Publishes sensor events to Kafka

- **Central Service**
    - Consumes sensor events from Kafka
    - Checks values against thresholds
    - Logs alarms when thresholds are exceeded

Message delivery is best-effort. Occasional data loss is acceptable.

## Technology Stack

- Java 21
- Spring Boot
- Gradle
- Apache Kafka
- Lombok
- Docker / Docker Compose

## Message Format (UDP)

sensor_id=<id>; value=<number>

Example:
sensor_id=t1; value=36

Unknown fields are ignored.

## Running the System

docker-compose up

## Simulating Sensors

Temperature (UDP port 3344):
echo "sensor_id=t1; value=36" | nc -u localhost 3344

Humidity (UDP port 3355):
echo "sensor_id=h1; value=70" | nc -u localhost 3355

## Configuration

WAREHOUSE_ID=wh-1

## Notes

- UDP is used intentionally; message loss is acceptable
- No retries or exactly-once guarantees
- Duplicate events and alarms are allowed
