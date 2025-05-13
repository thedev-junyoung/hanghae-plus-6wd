package kr.hhplus.be.server.application.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.config.TestCompensationConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>OrderConcurrencyTest</h2>
 *
 * <p>상품 재고 차감을 포함한 주문 도메인의 동시성 테스트 클래스.</p>
 *
 * <p>여러 사용자가 동시에 동일한 상품을 주문할 경우, 재고가 정확히 차감되는지 검증한다.
 * 이 테스트는 재고 초과 주문을 방지하고, 동시성 환경에서의 데이터 정합성을 확보하는 것이 목적이다.</p>
 *
 * <h3>💡 테스트 목적</h3>
 * <ul>
 *     <li>동시 주문 환경에서 분산락이 재고 초과를 방지하는지 검증</li>
 *     <li>2건까지만 주문이 성공하고, 이후 주문은 실패해야 함</li>
 *     <li>최종 재고는 정확히 0이어야 함</li>
 * </ul>
 *
 * <h3>동시성 제어 방식</h3>
 * <ul>
 *     <li>분산락: Redisson 기반 AOP 분산락(`DistributedLockAspect`)을 이용해 임계구역 보호</li>
 *     <li>락 범위: `stock:decrease:{productId}:{size}` 단위로 재고 차감 보호</li>
 *     <li>예외 발생 시 트랜잭션 롤백으로 재고 차감 무효화</li>
 * </ul>
 *
 * <h3>🧪 테스트 설정</h3>
 * <ul>
 *     <li>초기 재고 수량: 10개</li>
 *     <li>각 주문 수량: 5개</li>
 *     <li>3명의 사용자가 동시에 주문 → 최대 2건만 성공 가능</li>
 *     <li>보상 트랜잭션은 테스트에서 제외(stub으로 대체)</li>
 * </ul>
 *
 * <h3>테스트 환경 분리</h3>
 * <ul>
 *     <li>실제 보상 트랜잭션(@Profile("!test"))은 테스트에서 빈 등록 제외됨</li>
 *     <li>테스트 환경에서는 `TestCompensationConfig`로 보상 로직을 stub 처리</li>
 * </ul>
 */
@SpringBootTest
@Import(TestCompensationConfig.class)
@ActiveProfiles("test")
public class OrderConcurrencyTest {

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    private Long productId;

    private static final int INIT_STOCK = 10;
    private static final int ORDER_QTY = 5;
    private static final int CONCURRENCY = 3;

    @BeforeEach
    void setUp() {
        // 트랜잭션 없이 바로 DB 반영됨
        Product product = Product.create(
                "Test Product", "TestBrand", Money.wons(10000),
                LocalDate.now().minusDays(1), null, null
        );
        product = productRepository.save(product);

        stockRepository.save(ProductStock.of(product.getId(), 270, INIT_STOCK));

        em.clear(); // 1차 캐시 제거 (다른 쓰레드에서 DB만 조회하게 됨)

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

        em.clear(); // 영속성 컨텍스트 초기화
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
