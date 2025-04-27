package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderFacadeServiceIntegrationTest {

    @Autowired
    OrderFacadeService orderFacadeService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("쿠폰 없이 단일 상품 주문이 성공한다")
    void createOrder_withoutCoupon_success() {
        // given
        Long userId = 100L;         // 데이터에 있는 userId
        Long productId = 1L;        // New Balance 993
        int quantity = 2;
        int size = 270;
        int price = 199000;

        CreateOrderCommand command = CreateOrderCommand.of(
                userId,
                List.of(new CreateOrderCommand.OrderItemCommand(productId, quantity, size)),
                null
        );

        // when
        OrderResult result = orderFacadeService.createOrder(command);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalAmount()).isEqualTo(price * quantity);
        assertThat(orderRepository.findById(result.orderId())).isPresent();
    }
    @Test
    @DisplayName("쿠폰을 적용한 주문이 성공한다")
    void createOrder_withCoupon_success() {
        // given
        Long userId = 100L;
        String couponCode = "WELCOME10";  // data.sql 에 존재
        Long productId = 2L;              // ASICS GEL-Kayano 14
        int price = 169000;

        CreateOrderCommand command = CreateOrderCommand.of(
                userId,
                List.of(new CreateOrderCommand.OrderItemCommand(productId, 1, 265)),
                couponCode
        );

        // when
        OrderResult result = orderFacadeService.createOrder(command);

        // then
        assertThat(result.totalAmount()).isLessThan(price); // 정확한 discount 계산은 로직 따라
        assertThat(orderRepository.findById(result.orderId())).isPresent();
    }
}
