package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.coupon.CouponUseCase;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderFacadeServiceTest {

    private OrderUseCase orderService;
    private OrderEventUseCase orderEventService;
    private CouponUseCase couponUseCase;
    private OrderItemCreator orderItemCreator;
    private OrderCompensationService compensationService;

    private OrderFacadeService orderFacadeService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderUseCase.class);
        orderEventService = mock(OrderEventUseCase.class);
        couponUseCase = mock(CouponUseCase.class);
        orderItemCreator = mock(OrderItemCreator.class);
        compensationService = mock(OrderCompensationService.class);

        orderFacadeService = new OrderFacadeService(
                orderItemCreator,
                orderService,
                orderEventService,
                couponUseCase,
                compensationService
        );
    }

    @Test
    @DisplayName("쿠폰을 적용하여 주문을 생성하고 이벤트를 발행한다")
    void createOrder_withCoupon_success() {
        // given
        Long userId = 1L;
        Long productId = 1001L;
        int quantity = 2;
        int size = 270;
        long unitPrice = 5000;
        String couponCode = "DISCOUNT10";

        CreateOrderCommand.OrderItemCommand itemCommand = new CreateOrderCommand.OrderItemCommand(productId, quantity, size);
        CreateOrderCommand command = new CreateOrderCommand(userId, List.of(itemCommand), couponCode);

        List<OrderItem> orderItems = List.of(OrderItem.of(productId, quantity, size, Money.wons(unitPrice)));
        Money originalTotal = Money.wons(unitPrice * quantity);
        Money discountedTotal = originalTotal.subtract(Money.wons(2000));

        Order order = Order.create(userId, orderItems, discountedTotal);

        when(orderItemCreator.createOrderItems(command.items())).thenReturn(orderItems);
        when(couponUseCase.calculateDiscountedTotal(command, orderItems)).thenReturn(discountedTotal);
        when(orderService.createOrder(userId, orderItems, discountedTotal)).thenReturn(order);

        // when
        OrderResult result = orderFacadeService.createOrder(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalAmount()).isEqualTo(discountedTotal.value());
        assertThat(result.items()).hasSize(1);
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);

        // verify
        verify(orderItemCreator).createOrderItems(command.items());
        verify(couponUseCase).calculateDiscountedTotal(command, orderItems);
        verify(orderService).createOrder(userId, orderItems, discountedTotal);
        verify(orderEventService).recordPaymentCompletedEvent(order);
        verifyNoInteractions(compensationService);
    }
}
