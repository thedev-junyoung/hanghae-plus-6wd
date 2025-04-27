package kr.hhplus.be.server.application.order;


import java.util.List;

public record CreateOrderCommand(
        Long userId,
        List<OrderItemCommand> items,
        String couponCode
) {
    public boolean hasCouponCode() {
        return couponCode != null && !couponCode.isBlank();
    }

    public record OrderItemCommand(
            Long productId,
            int quantity,
            int size
    ) {
        public static OrderItemCommand of(Long productId, int quantity, int size) {
            return new OrderItemCommand(productId, quantity, size);
        }
    }
    public static CreateOrderCommand of(Long userId, List<OrderItemCommand> items, String couponCode) {
        return new CreateOrderCommand(userId, items, couponCode);
    }
}
