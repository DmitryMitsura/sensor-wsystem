package com.sensordata.warehouse.udp;

import com.sensordata.warehouse.domain.SensorType;
import com.sensordata.warehouse.processing.SensorDataProcessor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SensorDataUdpListener {

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

    @PostConstruct
    public void start() {
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
        thread.setDaemon(true);
        thread.start();

        log.info("UDP listener started: type={}, port={}", sensorType, port);
    }

    private void listenLoop() {
        byte[] buffer = new byte[2048];

        while (running.get()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();
                if (message.isBlank()) {
                    continue;
                }

                sensorDataProcessor.process(sensorType, message);
            } catch (Exception e) {
                if (!running.get()) {
                    break;
                }
                log.warn("Failed to process UDP message: type={}, port={}", sensorType, port, e);
            }
        }

        log.info("UDP listener stopped: type={}, port={}", sensorType, port);
    }

    @PreDestroy
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (socket != null) {
            socket.close();
        }
        if (thread != null) {
            try {
                thread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
