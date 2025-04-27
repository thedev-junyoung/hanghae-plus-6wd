package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductStock;
import kr.hhplus.be.server.domain.product.ProductStockRepository;
import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class StockServiceIntegrationTest {

    @Autowired StockService stockService;
    @Autowired ProductRepository productRepository;
    @Autowired ProductStockRepository stockRepository;

    private Long productId;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        Product product = Product.create("Test Product", "Brand", Money.wons(10000), LocalDate.now().minusDays(1), null, null);
        product = productRepository.save(product);
        productId = product.getId();

        stockRepository.save(ProductStock.of(productId, 270, 10));
    }

    @Test
    @DisplayName("동시에 여러 요청이 들어와도 재고가 음수가 되지 않는다")
    void concurrent_decrease_stock_safely() throws InterruptedException {
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    stockService.decrease(DecreaseStockCommand.of(productId, 270, 5));
                } catch (Exception e) {
                    System.out.println("실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ProductStock stock = stockRepository.findByProductIdAndSize(productId, 270)
                .orElseThrow();

        System.out.println("남은 재고: " + stock.getStockQuantity());
        assertThat(stock.getStockQuantity()).isZero(); // 정확히 2개 요청만 성공해야 함
    }
}
