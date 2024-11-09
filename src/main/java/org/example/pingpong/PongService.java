package org.example.pingpong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class PongService {
    private static final Logger logger = LoggerFactory.getLogger(PongService.class);
    private static final int THROTTLING_LIMIT = 1;
    private static int requestCount = 0;

    public Mono<ServerResponse> handleRequest(ServerRequest request) {
        if (isThrottled()) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        logger.info("Received request from ping service.");
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("World");
    }

    private boolean isThrottled() {
        if (requestCount >= THROTTLING_LIMIT) {
            return true;
        }
        requestCount++;
        return false;
    }
}
