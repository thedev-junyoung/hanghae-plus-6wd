package kr.hhplus.be.server.domain.orderexport;

import kr.hhplus.be.server.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class OrderExportPayload {
    private final String orderId;
    private final Long userId;
    private final List<OrderItemPayload> items;
    private final long totalAmount;

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class OrderItemPayload {
        private final Long productId;
        private final int quantity;
        private final int size;
        private final long price;
    }

    public static OrderExportPayload from(Order order) {
        return OrderExportPayload.of(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(item -> OrderItemPayload.of(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getSize(),
                                item.getPrice()
                        ))
                        .toList(),
                order.getTotalAmount()
        );
    }
}
