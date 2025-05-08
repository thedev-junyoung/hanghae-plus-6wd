package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.productstatistics.ProductSalesInfo;
import kr.hhplus.be.server.application.productstatistics.ProductStatisticsUseCase;
import kr.hhplus.be.server.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductUseCase productUseCase;
    private final ProductStatisticsUseCase statisticsUseCase;

    @Cacheable(
        value = "popularProducts",
        key = "'popular:' + #criteria.days() + ':' + #criteria.limit()",
        sync = true
    )
    @Transactional(readOnly = true)
    public List<PopularProductResult> getPopularProducts(PopularProductCriteria criteria) {
        List<ProductSalesInfo> stats = statisticsUseCase.getTopSellingProducts(criteria);

        // 미리 productId 리스트 추출
        List<Long> productIds = stats.stream()
                .map(ProductSalesInfo::productId)
                .toList();

        // DB 한번에 조회
        Map<Long, Product> productMap = productUseCase.findProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 매핑
        return stats.stream()
                .map(info -> PopularProductResult.from(productMap.get(info.productId()), info.salesCount()))
                .toList();
    }

    public List<ProductSalesInfo> getPopularProductStatsOnly(PopularProductCriteria criteria) {
        return statisticsUseCase.getTopSellingProducts(criteria);
    }

}
