package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.coupon.ApplyCouponCommand;
import kr.hhplus.be.server.application.coupon.ApplyCouponResult;
import kr.hhplus.be.server.application.coupon.CouponUseCase;
import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.common.vo.Money;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

    private final ProductUseCase productService;
    private final OrderUseCase orderService;
    private final OrderEventUseCase orderEventService;
    private final CouponUseCase couponUseCase;
    private final StockService stockService;

    @Transactional
    public OrderResult createOrder(CreateOrderCommand command) {
        // 1. 초기화 - 전체 금액 및 주문 아이템 리스트 준비
        Money total = Money.wons(0L);
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. 각 상품에 대해 주문 상세 구성
        for (CreateOrderCommand.OrderItemCommand item : command.items()) {
            // 2-1. 재고 차감
            stockService.decrease(DecreaseStockCommand.of(item.productId(), item.size(), item.quantity()));

            // 2-2. 상품 상세 조회 (가격 포함)
            ProductDetailResult product = productService.getProductDetail(
                    GetProductDetailCommand.of(item.productId(), item.size())
            );
            // 2-3. 주문 상품 가격 계산
            Money itemPrice = Money.wons(product.product().price());
            Money itemTotal = itemPrice.multiply(item.quantity());

            // 2-4. 주문 아이템으로 추가
            orderItems.add(OrderItem.of(item.productId(), item.quantity(), item.size(), itemPrice));
            total = total.add(itemTotal);
        }
        // 3. 쿠폰 할인 적용
        if (command.hasCouponCode()) {
            ApplyCouponResult couponResult = couponUseCase.applyCoupon(
                    new ApplyCouponCommand(command.userId(), command.couponCode(), total)
            );
            total = total.subtract(couponResult.discountAmount());
        }


        // 4. 주문 생성 및 저장
        Order order = orderService.createOrder(command.userId(), orderItems, total);

        // 5. 결제 완료 이벤트 발행 (Outbox 패턴 기반 처리)
        orderEventService.recordPaymentCompletedEvent(order);

        // 6. 응답 객체 반환
        return OrderResult.from(order);
    }

}

