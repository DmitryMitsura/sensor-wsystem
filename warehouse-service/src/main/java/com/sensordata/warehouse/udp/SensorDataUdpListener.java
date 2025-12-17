package com.sensordata.warehouse.udp;

import com.sensordata.warehouse.domain.SensorType;
import com.sensordata.warehouse.processing.SensorDataProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

// Listens for incoming UDP packets and forwards raw messages for processing
@Slf4j
public class SensorDataUdpListener implements SmartLifecycle {

    private final int port;
    private final SensorType sensorType;
    private final SensorDataProcessor sensorDataProcessor;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket socket;
    private Thread thread;

    public SensorDataUdpListener(int port, SensorType sensorType, SensorDataProcessor sensorDataProcessor) {
        this.port = port;
        this.sensorType = sensorType;
        this.sensorDataProcessor = sensorDataProcessor;
    }

    @Override
    public void start() {
        log.error(">>> UDP LISTENER START CALLED <<<");
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            running.set(false);
            throw new IllegalStateException("Failed to bind UDP port " + port + " for " + sensorType, e);
        }

        thread = new Thread(this::listenLoop, "udp-listener-" + sensorType.name().toLowerCase());
        thread.start();

        log.info("UDP listener started: type={}, port={}", sensorType, port);
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void listenLoop() {
        byte[] buffer = new byte[2048];

        while (running.get()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                ).trim();

                if (!message.isBlank()) {
                    sensorDataProcessor.process(sensorType, message);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.warn("Failed to process UDP message: type={}, port={}", sensorType, port, e);
                }
            }
        }

        log.info("UDP listener stopped: type={}, port={}", sensorType, port);
    }
}
