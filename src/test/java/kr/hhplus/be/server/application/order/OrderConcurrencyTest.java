package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.OrderRepository;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 상품 재고 차감을 포함한 주문 동시성 테스트 클래스.
 *
 * <p>여러 사용자가 동시에 동일 상품을 다량 주문할 때 발생할 수 있는 재고 초과 문제를 시뮬레이션한다.</p>
 *
 * <p>적용된 동시성 제어 방식:</p>
 * <ul>
 *   <li>재고 조회 시 `@Lock(PESSIMISTIC_WRITE)`을 사용하여 row-level 락</li>
 *   <li>수량 부족 시 예외 발생 → 트랜잭션 롤백</li>
 * </ul>
 *
 * <p>검증 포인트:</p>
 * <ul>
 *   <li>재고 10개 기준, 5개씩 주문 시 최대 2건만 성공</li>
 *   <li>최종 재고는 정확히 0</li>
 * </ul>
 */
@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long productId;

    private static final int INIT_STOCK = 10;
    private static final int ORDER_QTY = 5;
    private static final int CONCURRENCY = 3;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        // 트랜잭션 없이 바로 DB 반영됨
        Product product = Product.create(
                "Test Product", "TestBrand", Money.wons(10000),
                LocalDate.now().minusDays(1), null, null
        );
        product = productRepository.save(product);

        stockRepository.save(ProductStock.of(product.getId(), 270, INIT_STOCK));

        this.productId = product.getId();
    }

    @Test
    @DisplayName("동시에 여러 명이 주문하면 재고가 정확히 차감되어야 한다")
    void should_decrease_stock_correctly_when_multiple_orders_are_placed_concurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(CONCURRENCY);

        // 💡 테스트 전에 초기 주문 수 저장
        long beforeCount = orderRepository.count();
        ProductStock initStock = stockRepository.findByProductIdAndSize(productId, 270)
                .orElseThrow(() -> new IllegalStateException("재고 없음"));

        System.out.println("초기 재고: " + initStock.getStockQuantity());

        for (int i = 0; i < CONCURRENCY; i++) {
            long userId = 100L + i;
            executor.execute(() -> {
                try {
                    CreateOrderCommand command = new CreateOrderCommand(
                            userId,
                            List.of(new CreateOrderCommand.OrderItemCommand(productId, ORDER_QTY, 270)),
                            null
                    );
                    orderFacadeService.createOrder(command);
                } catch (Exception e) {
                    System.out.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 재고 확인
        ProductStock stock = stockRepository.findByProductIdAndSize(productId, 270)
                .orElseThrow(() -> new IllegalStateException("재고 없음"));

        // 최종 주문 수 측정
        long afterCount = orderRepository.count();
        long diff = afterCount - beforeCount;

        System.out.println("남은 재고: " + stock.getStockQuantity());
        System.out.println("신규 주문 수: " + diff);

        assertThat(stock.getStockQuantity()).isEqualTo(0);
        assertThat(diff).isEqualTo(2);
    }



}
