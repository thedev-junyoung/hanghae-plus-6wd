package kr.hhplus.be.server.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.springframework.test.util.TestSocketUtils.findAvailableTcpPort;

@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        int port = findAvailableTcpPort();  // Spring Boot 제공 util
        redisServer = new RedisServer(port);
        redisServer.start();
        System.setProperty("spring.redis.port", String.valueOf(port)); // 동적으로 Redis port property 설정
        System.out.println("Embedded Redis 시작됨. 포트: " + port);
    }
    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
