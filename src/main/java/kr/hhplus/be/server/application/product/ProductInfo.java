package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;

public record ProductInfo(
        Long id,
        String name,
        Long price,
        int stockQuantity
) {
    public static ProductInfo from(Product product, int stockQuantity) {
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getPrice(),
                stockQuantity
        );
    }
}
