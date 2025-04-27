package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceFacade;
import kr.hhplus.be.server.application.balance.ChargeBalanceCriteria;
import kr.hhplus.be.server.application.order.CreateOrderCommand;
import kr.hhplus.be.server.application.order.OrderFacadeService;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductStock;
import kr.hhplus.be.server.domain.product.ProductStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * 결제 요청의 중복 처리를 방지하는 동시성 테스트 클래스.
 *
 * <p>하나의 주문에 대해 여러 명이 동시에 결제를 요청하는 시나리오를 시뮬레이션한다.</p>
 *
 * <p>적용된 동시성 제어 방식:</p>
 * <ul>
 *   <li>주문 객체 조회 시 `@Lock(PESSIMISTIC_WRITE)`으로 선점</li>
 *   <li>잔액 차감, 결제 저장, 주문 상태 변경을 하나의 트랜잭션 내에서 처리</li>
 * </ul>
 *
 * <p>검증 포인트:</p>
 * <ul>
 *   <li>결제 요청이 동시에 들어와도 오직 1건만 성공</li>
 *   <li>나머지는 중복 처리로 인해 예외 발생</li>
 * </ul>
 */

@SpringBootTest
public class PaymentConcurrencyTest {

    @Autowired
    private PaymentFacadeService paymentFacadeService;

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository stockRepository;

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private OrderRepository orderRepository;

    private final Long userId = 777L;
    private String orderId;

    private final long PRICE = 10_000L;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        // 1. 잔액 세팅
        balanceFacade.charge(ChargeBalanceCriteria.of(userId, PRICE, "초기 충전", "REQUEST-01"));

        // 2. 상품 및 재고 세팅
        Product product = productRepository.save(Product.create("테스트 상품", "브랜드", Money.wons(PRICE), LocalDate.now().minusDays(1), null, null));
        stockRepository.save(ProductStock.of(product.getId(), 270, 10));

        // 3. 주문 생성
        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                List.of(new CreateOrderCommand.OrderItemCommand(product.getId(), 1, 270)),
                null
        );
        OrderResult result = orderFacadeService.createOrder(command);
        this.orderId = result.orderId();
    }

    @Test
    @DisplayName("동시에 여러 번 결제를 요청해도 1건만 성공해야 한다")
    void should_allow_only_one_successful_payment_when_concurrent_requests_are_made() throws InterruptedException {
        int CONCURRENCY = 5;
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(CONCURRENCY);

        List<PaymentResult> successes = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < CONCURRENCY; i++) {
            executor.execute(() -> {
                try {
                    RequestPaymentCommand command = new RequestPaymentCommand(orderId, userId , PRICE, "BALANCE");
                    PaymentResult result = paymentFacadeService.requestPayment(command);
                    successes.add(result);
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("성공한 결제 수: " + successes.size());
        System.out.println("실패한 결제 수: " + failures.size());
        assertThat(successes).hasSize(1);
    }
}
