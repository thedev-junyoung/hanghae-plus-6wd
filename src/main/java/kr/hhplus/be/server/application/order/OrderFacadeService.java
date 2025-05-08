package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.coupon.CouponUseCase;
import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacadeService {

    private final OrderItemCreator orderItemCreator;
    private final OrderUseCase orderService;
    private final OrderEventUseCase orderEventService;
    private final CouponUseCase couponUseCase;
    private final OrderCompensationService compensationService;

    public OrderResult createOrder(CreateOrderCommand command) {
        Order order = null;

        try {
            // 1. OrderItem 생성 (재고 차감 포함, 분산락 적용)
            List<OrderItem> orderItems = orderItemCreator.createOrderItems(command.items());

            // 2. 총액 계산 + 쿠폰 할인 적용
            Money discountedTotal = couponUseCase.calculateDiscountedTotal(command, orderItems);

            // 3. 주문 생성 및 저장 (TX 내부)
            order = orderService.createOrder(command.userId(), orderItems, discountedTotal);

            // 4. 이벤트 발행 (Outbox 또는 Domain Event 기반)
            orderEventService.recordPaymentCompletedEvent(order);

            return OrderResult.from(order);

        } catch (Exception e) {
            log.error("주문 실패 → 보상 트랜잭션 수행 시작", e);

            // 1. 재고 보상
            compensationService.compensateStock(command.items());

            // 2. 주문 생성이 된 경우 상태 변경
            if (order != null) {
                compensationService.markOrderAsFailed(order.getId());
            }

            throw e;
        }
    }
}
