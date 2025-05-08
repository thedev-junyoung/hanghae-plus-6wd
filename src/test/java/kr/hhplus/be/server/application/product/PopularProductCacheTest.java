package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.config.TestRedisCacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestRedisCacheConfig.class)
@ActiveProfiles("test")
class PopularProductCacheTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() {
        redissonClient.getKeys().flushall();
        Cache cache = cacheManager.getCache("popularProducts");
        if (cache != null) cache.clear();
    }

    @Test
    @DisplayName("인기 상품 조회 - 캐시 저장 및 Redis 직렬화된 JSON 확인")
    void getPopularProducts_shouldCacheResults() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String logicalCacheKey = "popular:" + criteria.days() + ":" + criteria.limit();
        String redisActualKey = "popularProducts::" + logicalCacheKey;

        // when - 첫 번째 호출
        List<PopularProductResult> firstResults = productFacade.getPopularProducts(criteria);

        // then - 캐시 저장 확인
        Cache.ValueWrapper cachedValue = cacheManager.getCache("popularProducts").get(logicalCacheKey);
        assertThat(cachedValue).isNotNull();

        // 🔍 Redis 실제 저장된 키와 직렬화된 값 출력
        String json = stringRedisTemplate.opsForValue().get(redisActualKey);
        System.out.println("Redis 저장 키 = " + redisActualKey);
        System.out.println("Redis 직렬화된 JSON = " + json);
        assertThat(json).contains("id", "name", "salesCount");

        // Redis 키 목록도 같이 출력
        redissonClient.getKeys().getKeysByPattern("*popular*")
                .forEach(key -> System.out.println("Redis 실제 키 = " + key));
    }

    @Test
    @DisplayName("인기 상품 조회 - TTL 만료 후 캐시 evict 확인")
    void getPopularProducts_cacheEvictionAfterTTL() throws InterruptedException {
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String key = "popular:" + criteria.days() + ":" + criteria.limit();

        productFacade.getPopularProducts(criteria);
        assertThat(cacheManager.getCache("popularProducts").get(key)).isNotNull();

        Thread.sleep(6000);

        boolean exists = false;
        for (String k : redissonClient.getKeys().getKeysByPattern("*popular*")) {
            if (k.equals("popularProducts::" + key)) {
                exists = true;
                break;
            }
        }

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("캐시 TTL 만료 후 자동 삭제 확인")
    void popularProducts_cacheExpiresAfterTTL() {
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String key = "popular:7:10";

        productFacade.getPopularProducts(criteria);
        assertThat(cacheManager.getCache("popularProducts").get(key)).isNotNull();

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(cacheManager.getCache("popularProducts").get(key)).isNull());
    }

    @Test
    @DisplayName("동시 요청 시 캐시 스탬피드 방지")
    void preventCacheStampede_withSyncTrue() throws InterruptedException {
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String key = "popular:7:10";

        redissonClient.getKeys().flushall();
        cacheManager.getCache("popularProducts").clear();

        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    productFacade.getPopularProducts(criteria);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(cacheManager.getCache("popularProducts").get(key)).isNotNull();

        System.out.println("캐시 정상 생성 확인");
    }
}
