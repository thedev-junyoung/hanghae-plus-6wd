package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;

import java.time.LocalDate;

public record ProductResult(
        Long id,
        String name,
        Long price,
        LocalDate releaseDate,
        String description,
        String brand,
        int stockQuantity
) {
    public static ProductResult from(Product product, int stockQuantity) {
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getReleaseDate(),
                product.getDescription(),
                product.getBrand(),
                stockQuantity
        );
    }
}
