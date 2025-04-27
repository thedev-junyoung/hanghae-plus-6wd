package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;

import java.util.List;

public interface OrderUseCase {

    /**
     * 주문을 생성하고 저장소에 저장
     */
    Order createOrder(Long userId, List<OrderItem> items, Money totalAmount);

    /**
     * 결제를 시도하기 전에 해당 주문이 존재하고, 결제 가능한 상태인지 검증한 뒤 반환
     */
    Order getOrderForPayment(String orderId);

    /**
     * 주문을 결제 완료 상태로 변경
     */
    void markConfirmed(Order order);

    Order getOrderForPaymentWithLock(String orderId); // 주문을 잠금 처리하여 동시성 문제를 방지
}
