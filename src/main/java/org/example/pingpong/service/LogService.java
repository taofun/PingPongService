package org.example.pingpong.service;

import org.example.pingpong.repository.LogEntry;
import org.example.pingpong.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LogService {
    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @KafkaListener(topics = "ping-pong-log-topic", groupId = "ping-pong-group")
    public void consumeLog(String logMessage) {
        LogEntry logEntry = new LogEntry(logMessage);
        logRepository.save(logEntry).subscribe();
    }
}