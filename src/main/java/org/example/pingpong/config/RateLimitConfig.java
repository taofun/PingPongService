package org.example.pingpong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RateLimitConfig {
    @Bean
    public RedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("local key = KEYS[1]\n" +
                "local current = tonumber(redis.call('get', key) or '0')\n" +
                "local limit = tonumber(ARGV[2])\n" +
                "if current + 1 > limit then\n" +
                "  return 0\n" +
                "else\n" +
                "  redis.call('INCR', key)\n" +
                "  return 1\n" +
                "end");
        return script;
    }
}
