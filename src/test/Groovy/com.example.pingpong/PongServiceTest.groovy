import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class PongServiceTest {
    @Autowired
    private PongService pongService;

    @Test
    void testHandleRequest_NotThrottled() {
        // Given
        ServerRequest request = null;
        when(pongService.isThrottled()).thenReturn(false);

        // When
        Mono<ServerResponse> responseMono = pongService.handleRequest(request);

        // Then
        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
                .expectNextMatches(response -> response.bodyToMono(String.class).block().equals("World"))
                .verifyComplete();
    }

    @Test
    void testHandleRequest_Throttled() {
        // Given
        ServerRequest request = null;
        when(pongService.isThrottled()).thenReturn(true);

        // When
        Mono<ServerResponse> responseMono = pongService.handleRequest(request);

        // Then
        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS))
                .verifyComplete();
    }
}