package kr.hhplus.be.server.application.product;

public record IncreaseStockCommand(Long productId, int size, int quantity) {
    public static IncreaseStockCommand of(Long productId, int size, int quantity) {
        return new IncreaseStockCommand(productId, size, quantity);
    }
}
