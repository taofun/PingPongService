package org.example.pingpong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PingService {
    private static final Logger logger = LoggerFactory.getLogger(PingService.class);
    private static final String PONG_SERVICE_URL = "http://localhost:8081/pong";
    private static final int RATE_LIMIT = 2;
    private static final AtomicInteger requestCount = new AtomicInteger(0);
    private static FileLock fileLock;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    public PingService(KafkaTemplate<String, String> kafkaTemplate,
                       ReactiveRedisTemplate<String, String> redisTemplate,
                       RedisScript<Long> rateLimitScript) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }


    static {
        try {
            Path lockFile = Paths.get("rateLimit.lock");
            if (!Files.exists(lockFile)) {
                Files.createFile(lockFile);
            }
            FileChannel fileChannel = FileChannel.open(lockFile, java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE);
            fileLock = fileChannel.tryLock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1000)
    public void sendPing() {
        if (isRateLimited()) {
            logger.info("Request not sent as being rate limited.");
            return;
        }
        HttpClient httpClient = HttpClient.create();
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        Mono<String> response = webClient.get()
                .uri(PONG_SERVICE_URL)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, clientResponse -> Mono.empty())
                .bodyToMono(String.class);
        response.subscribe(result -> {
            logger.info("Request sent & Pong Respond: {}", result);
            kafkaTemplate.send("ping-pong-log-topic", "Request sent & Pong Respond: " + result);
        }, error -> {
            logger.error("Error sending ping", error);
            kafkaTemplate.send("ping-pong-log-topic", "Error sending ping: " + error.getMessage());
        });
    }

    private boolean isRateLimited() {
        int currentCount = requestCount.incrementAndGet();
        if (currentCount > RATE_LIMIT) {
            requestCount.decrementAndGet();
            return true;
        }
        return false;
    }
}
