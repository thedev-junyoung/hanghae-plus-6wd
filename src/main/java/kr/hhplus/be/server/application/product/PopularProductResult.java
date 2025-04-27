package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;


public record PopularProductResult(
        Long id,
        String name,
        long price,
        Long salesCount
) {
    public static PopularProductResult from(Product product, Long salesCount) {
        return new PopularProductResult(
                product.getId(),
                product.getName(),
                product.getPrice(),
                salesCount
        );
    }
}

