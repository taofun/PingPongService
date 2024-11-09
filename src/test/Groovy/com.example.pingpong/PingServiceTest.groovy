import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class PingServiceTest {
    @Autowired
    private PingService pingService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @MockBean
    private RedisScript<Long> rateLimitScript;

    @BeforeEach
    void setUp() {
        Mockito.reset(kafkaTemplate, redisTemplate, rateLimitScript);
    }

    @Test
    void testSendPing_NotRateLimited_SuccessfulResponse() {
        // Given
        when(redisTemplate.execute(rateLimitScript, any(), any(), any())).thenReturn(Mono.just(1L));
        Mono<String> successfulResponse = Mono.just("World");
        Mockito.when(pingService.sendPingToPong()).thenReturn(successfulResponse);

        // When
        pingService.sendPing();

        // Then
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
        StepVerifier.create(successfulResponse)
                .expectNext("World")
                .verifyComplete();
    }

    @Test
    void testSendPing_RateLimited() {
        // Given
        when(redisTemplate.execute(rateLimitScript, any(), any(), any())).thenReturn(Mono.just(0L));

        // When
        pingService.sendPing();

        // Then
        verify(kafkaTemplate, times(1)).send(anyString(), "Request not sent as being rate limited.");
    }
}