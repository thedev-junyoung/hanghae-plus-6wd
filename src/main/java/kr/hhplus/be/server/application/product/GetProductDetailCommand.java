package kr.hhplus.be.server.application.product;

public record GetProductDetailCommand(
        Long productId,
        int size
) {
    public static GetProductDetailCommand of(Long productId, int size) {
        return new GetProductDetailCommand(productId, size);
    }

}