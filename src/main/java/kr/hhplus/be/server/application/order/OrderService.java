package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderException;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    private final OrderRepository orderRepository;

    public Order createOrder(Long userId, List<OrderItem> items, Money totalAmount) {
        Order order = Order.create(userId, items, totalAmount);
        orderRepository.save(order);
        return order;
    }
    @Transactional(readOnly = true)
    public Order getOrderForPayment(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.NotFoundException(orderId));
        order.validatePayable();
        return order;
    }

    public void markConfirmed(Order order) {
        order.markConfirmed();
        orderRepository.save(order);
    }

    @Transactional
    public Order getOrderForPaymentWithLock(String orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderException.NotFoundException(orderId));
        order.validatePayable();
        return order;
    }
}

