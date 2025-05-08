package kr.hhplus.be.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestRedissonConfig {

    @Bean
    @Primary
    public RedissonClient redissonClient(@Value("${spring.redis.port}") int port) {
        port = Integer.parseInt(System.getProperty("spring.redis.port", "6379"));
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:" + port);
        System.out.println("RedissonClient 시작됨. 포트: " + port);
        return Redisson.create(config);
    }
}
