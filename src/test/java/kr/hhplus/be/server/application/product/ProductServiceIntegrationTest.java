package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductStockRepository productStockRepository;

    @Autowired
    ProductService productService;

    @BeforeEach
    void setUp() {
        // 테스트 시작 시 재고를 50으로 보정
        Long productId = 1L;
        int size = 270;
        productStockRepository.findByProductIdAndSize(productId, size).ifPresent(stock -> {
            int gap = 50 - stock.getStockQuantity();
            if (gap > 0) {
                stock.increaseStock(gap);
                productStockRepository.save(stock);
            } else if (gap < 0) {
                stock.decreaseStock(-gap);
                productStockRepository.save(stock);
            }
        });
    }

    @Test
    @DisplayName("상품 상세 조회 시 재고 정보도 함께 반환된다")
    void getProductDetail_shouldIncludeStockQuantity() {
        Long productId = 1L;
        int size = 270;

        ProductDetailResult result = productService.getProductDetail(
                new GetProductDetailCommand(productId, size)
        );

        assertThat(result.product().stockQuantity()).isEqualTo(50);
        assertThat(result.product().name()).isEqualTo("New Balance 993");
    }

    @Test
    @DisplayName("상품 목록 조회 시 재고 정보가 포함된다")
    void getProductList_shouldIncludeStock() {
        GetProductListCommand command = new GetProductListCommand(0, 5, null);

        ProductListResult result = productService.getProductList(command);

        assertThat(result.products()).isNotEmpty();
        assertThat(result.products().get(0).stockQuantity()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("상품 재고 차감이 성공하면 실제 수량이 줄어든다")
    void decreaseStock_shouldDeductStockQuantity() {
        Long productId = 1L;
        int size = 270;

        int before = productService.getProductDetail(
                new GetProductDetailCommand(productId, size)
        ).product().stockQuantity();

        boolean result = productService.decreaseStock(
                new DecreaseStockCommand(productId, size, 3)
        );

        int after = productService.getProductDetail(
                new GetProductDetailCommand(productId, size)
        ).product().stockQuantity();

        assertThat(result).isTrue();
        assertThat(after).isEqualTo(before - 3);
    }

    @Test
    @DisplayName("출시 전 상품은 재고 차감 시 예외가 발생한다")
    void decreaseStock_shouldFailIfProductNotReleased() {
        Long productId = 12L; // 출시일이 미래로 설정된 상품
        int size = 270;

        productStockRepository.findByProductIdAndSize(productId, size)
                .orElseGet(() -> productStockRepository.save(ProductStock.of(productId, size, 10)));

        assertThatThrownBy(() ->
                productService.decreaseStock(new DecreaseStockCommand(productId, size, 1)))
                .isInstanceOf(ProductException.NotReleasedException.class);
    }
}
