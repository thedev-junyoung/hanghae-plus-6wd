package kr.hhplus.be.server.interfaces.order;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.application.order.OrderResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {

    @Schema(description = "주문 ID")
    private final String orderId;

    @Schema(description = "사용자 ID")
    private final Long userId;

    @Schema(description = "주문 항목 목록")
    private final List<OrderItemResponse> items;

    @Schema(description = "총 금액")
    private final long totalAmount;

    @Schema(description = "주문 상태")
    private final String status;

    public static OrderResponse from(OrderResult result) {
        List<OrderItemResponse> itemResponses = result.items().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                result.orderId(),
                result.userId(),
                itemResponses,
                result.totalAmount(),
                result.status().name()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private int quantity;
        private int size;
        private long price;

        public static OrderItemResponse from(OrderResult.OrderItemResult item) {
            return new OrderItemResponse(
                    item.productId(),
                    item.quantity(),
                    item.size(),
                    item.price()
            );
        }
    }
}
