package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class StockServiceIntegrationTest {

    @Autowired
    StockService stockService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductStockRepository stockRepository;


    private Long productId;

    @BeforeEach
    void setUp() {
        Product product = Product.create("통합 테스트 상품", "Brand", Money.wons(10000), LocalDate.now().minusDays(1), null, null);
        product = productRepository.save(product);
        productId = product.getId();

        stockRepository.save(ProductStock.of(productId, 270, 10));
    }

    @Test
    @DisplayName("정상적으로 재고 차감이 이루어진다")
    void decrease_stock_successfully() {
        // given
        DecreaseStockCommand command = DecreaseStockCommand.of(productId, 270, 3);

        // when
        stockService.decrease(command);

        // then
        ProductStock stock = stockRepository.findByProductIdAndSize(productId, 270)
                .orElseThrow();
        assertThat(stock.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고 부족 시 예외가 발생한다")
    void decrease_stock_insufficient_throws_exception() {
        // given
        DecreaseStockCommand command = DecreaseStockCommand.of(productId, 270, 20); // 재고보다 많음

        // expect
        assertThatThrownBy(() -> stockService.decrease(command))
                .isInstanceOf(ProductException.InsufficientStockException.class);
    }
}

