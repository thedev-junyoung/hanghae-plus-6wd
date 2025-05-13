package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceFacade;
import kr.hhplus.be.server.application.balance.ChargeBalanceCriteria;
import kr.hhplus.be.server.application.order.CreateOrderCommand;
import kr.hhplus.be.server.application.order.OrderFacadeService;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>PaymentConcurrencyTest</h2>
 *
 * <p>결제 요청의 동시성 제어를 검증하는 테스트 클래스.</p>
 *
 * <p>하나의 주문(orderId)에 대해 여러 요청자가 동시에 결제를 시도할 때,
 * Redis 기반 분산락을 통해 중복 결제를 방지하는 로직이 제대로 작동하는지 검증한다.</p>
 *
 * <h3>🛠 적용된 동시성 제어 방식</h3>
 * <ul>
 *   <li>Redisson 기반 분산락을 AOP로 적용: `@DistributedLock(key = "#command.orderId", prefix = "payment:order:")`</li>
 *   <li>결제 성공 기록, 잔액 차감, 후속 이벤트 발행을 하나의 트랜잭션 내에서 처리</li>
 * </ul>
 *
 * <h3>🧪 검증 포인트</h3>
 * <ul>
 *   <li>동시에 여러 결제 요청이 들어와도 **오직 1건만 성공**</li>
 *   <li>잔액은 정확히 1회만 차감 (10,000원 → 0원)</li>
 *   <li>나머지 요청은 예외 발생 (결제 중복 또는 잔액 부족)</li>
 * </ul>
 *
 * <h3>📌 추가 사항</h3>
 * <ul>
 *   <li>결제 성공 시, `PaymentCompletedEvent`를 발행하여 후속 처리(Event-Driven Architecture)</li>
 *   <li>Balance 차감 실패 또는 중복 결제 시, 예외를 통해 트랜잭션 전체 롤백</li>
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

    @Autowired
    private BalanceRepository balanceRepository;


    private final Long userId = System.currentTimeMillis();
    private String orderId;
    private final long PRICE = 10_000L;

    @BeforeEach
    void setUp() {
        balanceRepository.save(Balance.createNew(userId, Money.wons(0L)));
        balanceFacade.charge(ChargeBalanceCriteria.of(userId, PRICE, "초기 충전", "REQUEST-01"));

        Product product = productRepository.save(
                Product.create("테스트 상품", "브랜드", Money.wons(PRICE), LocalDate.now().minusDays(1), null, null)
        );
        stockRepository.save(ProductStock.of(product.getId(), 270, 10));

        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                List.of(new CreateOrderCommand.OrderItemCommand(product.getId(), 1, 270)),
                null
        );
        OrderResult result = orderFacadeService.createOrder(command);
        this.orderId = result.orderId();
    }

    @Test
    @DisplayName("동시에 여러 번 결제를 요청해도 1건만 성공하고 잔액은 정확히 10,000원만 차감되어야 한다")
    void should_allow_only_one_successful_payment_and_deduct_balance_exactly_once() throws InterruptedException {
        int CONCURRENCY = 5;
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(CONCURRENCY);

        List<PaymentResult> successes = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < CONCURRENCY; i++) {
            executor.execute(() -> {
                try {
                    RequestPaymentCommand command = new RequestPaymentCommand(orderId, userId, PRICE, "BALANCE");
                    PaymentResult result = paymentFacadeService.requestPayment(command);
                    System.out.println("[SUCCESS] " + result);
                    successes.add(result);
                } catch (Exception e) {
                    System.out.println("[FAILURE] " + e.getMessage());
                    failures.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Balance balance = balanceRepository.findByUserId(userId).orElseThrow();

        System.out.println("성공: " + successes.size());
        System.out.println("실패: " + failures.size());
        System.out.println("잔액: " + balance.getAmount());

        assertThat(successes).hasSize(1);
        assertThat(balance.getAmount()).isEqualTo(0L);
        assertThat(failures.size()).isEqualTo(CONCURRENCY - 1);
    }
}
