package kr.hhplus.be.server.application.product;

import java.util.List;

public record ProductListResult(
        List<ProductInfo> products
) {
    public static ProductListResult from(List<ProductInfo> infos) {
        return new ProductListResult(infos);
    }
}
