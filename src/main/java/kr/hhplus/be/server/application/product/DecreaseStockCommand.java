package kr.hhplus.be.server.application.product;

public record DecreaseStockCommand(
        Long productId,
        int size,
        int quantity
) {
    public static DecreaseStockCommand of(Long productId, int size, int quantity) {
        return new DecreaseStockCommand(productId, size, quantity);
    }
}
