package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.application.product.ProductDetailResult;
import kr.hhplus.be.server.application.product.ProductInfo;
import kr.hhplus.be.server.application.product.ProductListResult;
import kr.hhplus.be.server.application.product.PopularProductResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class ProductResponse {

    public record ProductDTO(
            Long id,
            String name,
            Long price,
            int stockQuantity
    ) {
        public static ProductDTO from(ProductInfo info) {
            return new ProductDTO(
                    info.id(),
                    info.name(),
                    info.price(),
                    info.stockQuantity()
            );
        }

    }

    public record ProductListResponse(List<ProductDTO> products) {
        public static ProductListResponse from(ProductListResult result) {
            return new ProductListResponse(
                    result.products().stream()
                            .map(ProductDTO::from)
                            .toList()
            );
        }
    }

    public record ProductDetailResponse(ProductDTO product) {
        public static ProductDetailResponse from(ProductDetailResult result) {
            return new ProductDetailResponse(
                    ProductDTO.from(result.product())
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class PopularProductResponse {
        private final Long id;
        private final String name;
        private final long price;
        private final Long salesCount;

        public static PopularProductResponse from(PopularProductResult result) {
            return new PopularProductResponse(
                    result.id(),
                    result.name(),
                    result.price(),
                    result.salesCount()
            );
        }
    }
}
