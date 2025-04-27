package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.coupon.ApplyCouponCommand;
import kr.hhplus.be.server.application.coupon.ApplyCouponResult;
import kr.hhplus.be.server.application.coupon.CouponUseCase;
import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.CouponType;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class OrderFacadeServiceTest {

    private ProductService productService;
    private OrderService orderService;
    private OrderEventService orderEventService;
    private CouponUseCase couponUseCase;
    private StockService stockService;

    private OrderFacadeService orderFacadeService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        orderService = mock(OrderService.class);
        orderEventService = mock(OrderEventService.class);
        couponUseCase = mock(CouponUseCase.class);
        stockService = mock(StockService.class);

        orderFacadeService = new OrderFacadeService(productService, orderService, orderEventService, couponUseCase, stockService);
    }

    @Test
    @DisplayName("쿠폰을 적용하여 주문을 생성하고 이벤트를 발행한다")
    void createOrder_withCoupon_success() {
        // given
        Long userId = 1L;
        Long productId = 1001L;
        int quantity = 2;
        int size = 270;
        long price = 5000;
        String couponCode = "DISCOUNT10";
        long discountAmount = 2000;

        CreateOrderCommand.OrderItemCommand itemCommand = new CreateOrderCommand.OrderItemCommand(productId, quantity, size);
        CreateOrderCommand command = new CreateOrderCommand(userId, List.of(itemCommand), couponCode);

        ProductDetailResult productResult = new ProductDetailResult(
                new ProductInfo(productId, "Test Product", price, 10)
        );

        ApplyCouponResult couponResult = new ApplyCouponResult(
                couponCode,
                CouponType.FIXED,
                2000,
                Money.wons(discountAmount)
        );

        Money originalTotal = Money.wons(price * quantity);
        Money discountedTotal = originalTotal.subtract(couponResult.discountAmount());

        when(productService.getProductDetail(any(GetProductDetailCommand.class))).thenReturn(productResult);
        when(couponUseCase.applyCoupon(any(ApplyCouponCommand.class))).thenReturn(couponResult);
        when(orderService.createOrder(eq(userId), anyList(), eq(discountedTotal)))
                .thenReturn(Order.create(userId,
                        List.of(OrderItem.of(productId, quantity, size, Money.wons(4000))), // 할인 적용된 가격
                        discountedTotal));

        // when
        OrderResult result = orderFacadeService.createOrder(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalAmount()).isEqualTo(discountedTotal.value());
        assertThat(result.items()).hasSize(1);
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);

        // verify interactions
        verify(productService).getProductDetail(any(GetProductDetailCommand.class));
        verify(couponUseCase).applyCoupon(any(ApplyCouponCommand.class));
        verify(orderService).createOrder(eq(userId), anyList(), eq(discountedTotal));
        verify(orderEventService).recordPaymentCompletedEvent(any(Order.class));
        verify(stockService).decrease(any(DecreaseStockCommand.class));
    }
}
