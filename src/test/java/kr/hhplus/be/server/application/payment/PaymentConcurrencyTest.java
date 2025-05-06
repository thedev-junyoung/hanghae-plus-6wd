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
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

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
