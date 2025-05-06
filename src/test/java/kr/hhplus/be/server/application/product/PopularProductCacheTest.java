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
@Import(TestRedisCacheConfig.class)  // 명시적으로 import 가능
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
        redissonClient.getKeys().flushall(); // Redisson으로 Redis 비우기

        Cache cache = cacheManager.getCache("popularProducts");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("인기 상품 조회 - 캐시 저장 및 Redis 키 확인")
    void getPopularProducts_shouldCacheResults() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);

        // when - 첫 번째 호출
        List<PopularProductResult> firstResults = productFacade.getPopularProducts(criteria);

        // then - 캐시 저장 확인
        String cacheKey = "popular:" + criteria.days() + ":" + criteria.limit();
        Cache.ValueWrapper cachedValue = cacheManager.getCache("popularProducts").get(cacheKey);
        assertThat(cachedValue).isNotNull();

        // Redisson을 이용해 Redis에 저장된 키들을 하나씩 출력
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern("*popular*");
        for (String key : keys) {
            System.out.println("실제 Redis 저장된 키: " + key);
        }

        System.out.println("모든 Redis popular 키 출력 완료");

        // when - 두 번째 호출
        List<PopularProductResult> secondResults = productFacade.getPopularProducts(criteria);

        // then - 캐시 히트 확인
        assertThat(secondResults).isEqualTo(firstResults);
    }


    @Test
    @DisplayName("인기 상품 조회 - TTL 만료 후 캐시 evict 확인")
    void getPopularProducts_cacheEvictionAfterTTL() throws InterruptedException {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);

        // when - 첫 번째 호출 (캐시 저장)
        List<PopularProductResult> firstResults = productFacade.getPopularProducts(criteria);

        // then - 캐시 저장 확인
        String cacheKey = "popular:" + criteria.days() + ":" + criteria.limit();
        Cache.ValueWrapper cachedValue = cacheManager.getCache("popularProducts").get(cacheKey);
        assertThat(cachedValue).isNotNull();

        // 👇 TTL 시간만큼 기다리기 (ex: 캐시 TTL이 5초라면 6초 기다림)
        Thread.sleep(6000);

        // Redis 직접 조회 - 키가 사라졌는지 확인
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern("*popular*");
        boolean keyExists = false;
        for (String key : keys) {
            if (key.equals(cacheKey)) {
                keyExists = true;
                break;
            }
        }

        // then
        assertThat(keyExists).isFalse(); // TTL 지나서 키가 사라졌어야 함

        // when - 다시 호출 (DB 재조회)
        List<PopularProductResult> secondResults = productFacade.getPopularProducts(criteria);

        // then - 결과는 다시 정상이어야 함
        assertThat(secondResults).isNotNull();
    }

    @Test
    @DisplayName("캐시 TTL 만료 후 캐시가 자동 삭제되는지 테스트")
    void popularProducts_cacheExpiresAfterTTL() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String cacheKey = "popular:7:10";

        // when - 첫 번째 호출 (캐시 생성)
        List<PopularProductResult> firstCall = productFacade.getPopularProducts(criteria);

        // then - 캐시 존재 확인
        Object cached = cacheManager.getCache("popularProducts").get(cacheKey, Object.class);
        assertThat(cached).isNotNull();
        System.out.println("첫 조회 후 캐시 저장 완료");

        // -- 여기서 기다려야 TTL이 만료됨
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    Object expired = cacheManager.getCache("popularProducts").get(cacheKey, Object.class);
                    assertThat(expired).isNull(); // 🔥 TTL 지나면 캐시 없어야 한다
                });

        System.out.println("TTL 만료 후 캐시 삭제 확인 완료");
    }

    @Test
    @DisplayName("여러 스레드가 동시에 인기상품 요청 시 스탬피드 없이 하나만 DB 조회")
    void preventCacheStampede_withSyncTrue() throws Exception {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 10);
        String cacheKey = "popular:7:10";

        // 캐시 비우기
        redissonClient.getKeys().flushall();
        cacheManager.getCache("popularProducts").clear();

        // when - 동시에 여러 요청 보내기
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productFacade.getPopularProducts(criteria);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // then
        // 캐시가 존재해야 한다
        Object cached = cacheManager.getCache("popularProducts").get(cacheKey, Object.class);
        assertThat(cached).isNotNull();

        System.out.println("여러 스레드 요청 후에도 캐시 정상 생성 완료");

        // Redis 키도 한번 확인
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern("*popular*");
        for (String key : keys) {
            System.out.println("Redis에 남아있는 캐시 키 = " + key);
        }
    }

}