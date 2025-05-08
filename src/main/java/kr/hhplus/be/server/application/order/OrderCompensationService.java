package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.product.IncreaseStockCommand;
import kr.hhplus.be.server.application.product.StockService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class OrderCompensationService {

    private final StockService stockService;
    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensateStock(List<CreateOrderCommand.OrderItemCommand> items) {
        log.info("[보상 트랜잭션] 재고 복구 시작");
        for (var item : items) {
            stockService.increase(IncreaseStockCommand.of(
                    item.productId(), item.size(), item.quantity()
            ));
        }
        log.info("[보상 트랜잭션] 재고 복구 완료");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderAsFailed(String orderId) {
        log.info("[보상 트랜잭션] 주문 상태 변경 시작 - 주문 ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("보상 대상 주문이 존재하지 않음: " + orderId));
        order.cancel(); // OrderStatus.CANCELLED 등으로 변경
        orderRepository.save(order);
        log.info("[보상 트랜잭션] 주문 상태 변경 완료 - 주문 ID: {}", orderId);
    }
}
