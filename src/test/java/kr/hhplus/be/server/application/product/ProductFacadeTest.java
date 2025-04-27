package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.productstatistics.ProductStatisticsUseCase;
import kr.hhplus.be.server.application.productstatistics.ProductSalesInfo;
import kr.hhplus.be.server.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductFacadeTest {

    private ProductUseCase productUseCase;
    private ProductStatisticsUseCase statisticsUseCase;
    private ProductFacade productFacade;

    @BeforeEach
    void setUp() {
        productUseCase = mock(ProductUseCase.class);
        statisticsUseCase = mock(ProductStatisticsUseCase.class);
        productFacade = new ProductFacade(productUseCase, statisticsUseCase);
    }

    @Test
    @DisplayName("인기상품_정상조회")
    void popular_product() {
        // given
        PopularProductCriteria criteria = new PopularProductCriteria(3, 5);

        ProductSalesInfo info = new ProductSalesInfo(1L, 10L);
        when(statisticsUseCase.getTopSellingProducts(criteria))
                .thenReturn(List.of(info));

        Product mockProduct = mockProduct();
        when(productUseCase.findProduct(1L)).thenReturn(mockProduct);

        // when
        List<PopularProductResult> results = productFacade.getPopularProducts(criteria);

        // then
        assertThat(results).hasSize(1);
        PopularProductResult result = results.get(0);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("NIKE DUNK");
        assertThat(result.price()).isEqualTo(139000L);
        assertThat(result.salesCount()).isEqualTo(10);

        verify(statisticsUseCase).getTopSellingProducts(criteria);
        verify(productUseCase).findProduct(1L);
    }

    private Product mockProduct() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("NIKE DUNK");
        when(product.getPrice()).thenReturn(139000L);
        when(product.getReleaseDate()).thenReturn(LocalDate.now());
        when(product.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(product.getUpdatedAt()).thenReturn(LocalDateTime.now());
        return product;
    }
}
