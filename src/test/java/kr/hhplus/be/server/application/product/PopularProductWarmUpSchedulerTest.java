package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.config.TestRedisCacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(TestRedisCacheConfig.class)
class PopularProductWarmUpSchedulerTest {

    @Autowired
    PopularProductWarmUpScheduler scheduler;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 캐시 비우기
        cacheManager.getCache("popularProducts").clear();
    }

    @Test
    @DisplayName("웜업 스케줄러가 인기상품 캐시를 정상적으로 미리 채우는지 테스트")
    void warmUpPopularProducts_shouldPreloadCache() {
        // given
        String cacheKey = "popular:3:5"; // 3일 5개 기준
        Cache cache = cacheManager.getCache("popularProducts");

        // when
        scheduler.warmUpPopularProducts(); // 스케줄러 직접 호출

        // then
        assert cache != null;
        Object cached = cache.get(cacheKey, Object.class);
        assertThat(cached).isNotNull();

        System.out.println("캐시 키 = " + cacheKey + ", 값 = " + cached);
    }

}
