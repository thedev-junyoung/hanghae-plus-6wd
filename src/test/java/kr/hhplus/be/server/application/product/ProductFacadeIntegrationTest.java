package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.productstatistics.ProductStatistics;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsId;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductFacadeIntegrationTest {

    @Autowired
    ProductFacade productFacade;


    @Test
    @DisplayName("인기 상품을 조회하면 판매량 기준으로 정렬된 상품 정보를 반환한다 (실제 DB 기반)")
    void getPopularProducts_success_withExistingData() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(7, 3); // 최근 7일, 5개까지

        // when
        List<PopularProductResult> results = productFacade.getPopularProducts(criteria);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).salesCount()).isGreaterThanOrEqualTo(results.get(results.size() - 1).salesCount());

        results.forEach(result -> {
            System.out.printf("상품명: %s, 판매량: %d\n", result.name(), result.salesCount());
        });
    }
}

