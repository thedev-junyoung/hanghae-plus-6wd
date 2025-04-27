package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.interfaces.product.ProductRequest;

public record PopularProductCriteria(int days, int limit) {
    public static PopularProductCriteria defaultSetting() {
        return new PopularProductCriteria(3, 5);
    }
    public static PopularProductCriteria of(ProductRequest.PopularRequest request) {
        return new PopularProductCriteria(
                request.days() != null ? request.days() : defaultSetting().days(),
                request.limit() != null ? request.limit() : defaultSetting().limit()
        );
    }
}
