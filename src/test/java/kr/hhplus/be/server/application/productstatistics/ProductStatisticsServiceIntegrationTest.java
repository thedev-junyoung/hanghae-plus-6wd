package kr.hhplus.be.server.application.productstatistics;

import kr.hhplus.be.server.application.product.PopularProductCriteria;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.productstatistics.ProductStatistics;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsId;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductStatisticsServiceIntegrationTest {

    @Autowired
    ProductStatisticsService service;

    @Autowired
    ProductStatisticsRepository repository;

    @Test
    @DisplayName("오늘자 통계가 없을 경우 새로 생성되어 저장된다")
    void record_createsNewStatistics() {
        // given
        Long productId = 10L; // 실제 존재하는 제품 ID (예: Reebok Classic Leather)
        LocalDate today = LocalDate.now();
        int quantity = 2;
        long unitAmount = 10000L;

        // 🧹 기존 통계가 있으면 삭제
        repository.findByProductIdAndStatDate(productId, today)
                .ifPresent(stat -> repository.delete(stat));

        // when
        service.record(new RecordSalesCommand(productId, quantity, unitAmount));

        // then
        ProductStatistics stats = repository.findByProductIdAndStatDate(productId, today)
                .orElseThrow(() -> new AssertionError("통계가 저장되지 않았습니다"));

        assertThat(stats.getSalesCount()).isEqualTo(2);
        assertThat(stats.getSalesAmount()).isEqualTo(20000L); // 2 * 10000
    }


    @Test
    @DisplayName("오늘자 통계가 존재하면 판매량과 금액이 누적된다")
    void record_accumulatesIfStatisticsExists() {
        // given
        Long productId = 9L; // Vans Old Skool
        LocalDate today = LocalDate.now();

        // clean up and setup
        repository.findByProductIdAndStatDate(productId, today).ifPresent(repository::delete);
        ProductStatistics existing = ProductStatistics.create(productId, today);
        existing.addSales(1, Money.wons(5000L));
        repository.save(existing);

        // when
        service.record(new RecordSalesCommand(productId, 2, 5000L));

        // then
        ProductStatistics stats = repository.findByProductIdAndStatDate(productId, today).orElseThrow();
        assertThat(stats.getSalesCount()).isEqualTo(3);
        assertThat(stats.getSalesAmount()).isEqualTo(15000L);
    }

    @Test
    @DisplayName("최근 3일간의 통계 기반으로 인기 상품 정렬 결과가 유효하다")
    void getTopSellingProducts_basedOnActualData() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(3, 5);

        // when
        Collection<ProductSalesInfo> results = service.getTopSellingProducts(criteria);
        List<ProductSalesInfo> resultList = new ArrayList<>(results);

        // then
        assertThat(resultList).isNotEmpty();
        assertThat(resultList.size()).isLessThanOrEqualTo(5);

        // 정렬 검증
        for (int i = 1; i < resultList.size(); i++) {
            assertThat(resultList.get(i - 1).salesCount())
                    .isGreaterThanOrEqualTo(resultList.get(i).salesCount());
        }

        // 기본 출력 확인
        resultList.forEach(result -> {
            System.out.printf("상품 ID: %d, 판매량: %d\n", result.productId(), result.salesCount());
            assertThat(result.salesCount()).isGreaterThan(0);
        });
    }


}
