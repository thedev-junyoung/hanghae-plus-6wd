package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.product.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    ProductStockRepository productStockRepository;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("상품 목록을 조회할 수 있다")
    void getProductList_success() {
        // given
        Product product = Product.create("Jordan 1", "Nike", Money.wons(200_000),
                LocalDate.of(2024, 1, 1), "image.jpg", "best seller");

        when(productRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        when(productStockRepository.findAllByProductId(product.getId()))
                .thenReturn(List.of(
                        ProductStock.of(product.getId(), 270, 5),
                        ProductStock.of(product.getId(), 280, 3)
                ));

        // when
        ProductListResult result = productService.getProductList(new GetProductListCommand(0, 10, null));

        // then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().get(0).name()).isEqualTo("Jordan 1");
    }



    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail_success() {
        // given
        Product product = Product.create( "Jordan 1", "Nike", Money.wons(200_000),LocalDate.of(2024, 1, 1), "image.jpg", "best seller");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        ProductDetailResult result = productService.getProductDetail(new GetProductDetailCommand(1L, 260));

        // then
        assertThat(result.product().name()).isEqualTo("Jordan 1");
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품")
    void getProductDetail_fail() {
        // given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductDetail(new GetProductDetailCommand(99L,260)))
                .isInstanceOf(ProductException.NotFoundException.class);
    }


}
