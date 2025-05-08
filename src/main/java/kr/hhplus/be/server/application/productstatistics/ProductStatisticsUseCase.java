package kr.hhplus.be.server.application.productstatistics;

import kr.hhplus.be.server.application.product.PopularProductCriteria;

import java.util.Collection;
import java.util.List;

public interface ProductStatisticsUseCase {

    /**
     * 상품 판매 정보를 기록합니다.
     */
    void record(RecordSalesCommand command);

    /**
     * 최근 N일 간 가장 많이 팔린 상품을 조회합니다.
     */
    List<ProductSalesInfo> getTopSellingProducts(PopularProductCriteria criteria);
}
