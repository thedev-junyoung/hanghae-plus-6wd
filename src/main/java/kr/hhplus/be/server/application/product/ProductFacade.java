package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.productstatistics.ProductStatisticsUseCase;
import kr.hhplus.be.server.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductUseCase productUseCase;
    private final ProductStatisticsUseCase statisticsUseCase;

    public List<PopularProductResult> getPopularProducts(PopularProductCriteria criteria) {
        return statisticsUseCase.getTopSellingProducts(criteria).stream()
                .map(info -> {
                    Product product = productUseCase.findProduct(info.productId());
                    return PopularProductResult.from(product, info.salesCount());
                })
                .toList();
    }

}
