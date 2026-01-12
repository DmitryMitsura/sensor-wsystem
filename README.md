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

## Simulating Sensors

Temperature (UDP port 3344):
echo "sensor_id=t1; value=36" | nc -u localhost 3344

Humidity (UDP port 3355):
echo "sensor_id=h1; value=70" | nc -u localhost 3355

## Notes

- UDP is used intentionally; message loss is acceptable
- No retries or exactly-once guarantees
- Duplicate events and alarms are allowed

## Local run: multiple warehouse-service instances

The system allows running multiple `warehouse-service` instances locally, each representing a different warehouse, while using a single Kafka cluster and a single `central-service`.

First, start ZooKeeper in a dedicated terminal window:

    .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

Wait until ZooKeeper is fully started. Next, start Kafka in a second terminal window:

    .\bin\windows\kafka-server-start.bat .\config\server.properties

Kafka must be available on localhost:9092 before starting application services.

Build executable jars from the project root:

    .\gradlew.bat :warehouse-service:bootJar
    .\gradlew.bat :central-service:bootJar

Start the central-service in a new PowerShell terminal. Only one instance of central-service is required:

    $env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
    java -jar central-service\build\libs\central-service.jar

Start the first warehouse-service instance in PowerShell:
```powershell
$env:WAREHOUSE_ID="wh-1"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:UDP_TEMPERATURE_PORT="3344"
$env:UDP_HUMIDITY_PORT="3355"
java -jar warehouse-service\build\libs\warehouse-service.jar
```
Start the second one instance in new terminal (change warehouse id and UDP ports):
```powershell
$env:WAREHOUSE_ID="wh-2"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:UDP_TEMPERATURE_PORT="3346"
$env:UDP_HUMIDITY_PORT="3357"
java -jar warehouse-service\build\libs\warehouse-service.jar
```
To verify the setup, send UDP messages to different ports:

    $udp = New-Object System.Net.Sockets.UdpClient
    $bytes = [Text.Encoding]::UTF8.GetBytes("sensor_id=t1; value=36")
    $udp.Send($bytes, $bytes.Length, "localhost", 3344) | Out-Null
    $udp.Send($bytes, $bytes.Length, "localhost", 3346) | Out-Null
    $udp.Close()

If everything is configured correctly, the central-service logs will show alarm messages for both warehouseId=wh-1 and warehouseId=wh-2. You can check log for central service to see the next lines:

    ALARM: Alarm[event=SensorDataEvent(warehouseId=wh-1, sensorId=t1, sensorType=TEMPERATURE, value=36.0, receivedAt=2026-01-12T13:41:40.609103300Z), threshold=35.0]
    ALARM: Alarm[event=SensorDataEvent(warehouseId=wh-2, sensorId=t1, sensorType=TEMPERATURE, value=36.0, receivedAt=2026-01-12T13:41:40.638193400Z), threshold=35.0]

