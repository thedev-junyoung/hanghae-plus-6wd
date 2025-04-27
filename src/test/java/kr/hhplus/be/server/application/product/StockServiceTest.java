package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductStockRepository productStockRepository;

    @InjectMocks
    StockService stockService;

    @Test
    @DisplayName("재고 차감 성공")
    void decrease_success() {
        // given
        Long productId = 1L;
        int size = 270;
        int quantity = 3;

        ProductStock stock = ProductStock.of(productId, size, 10);

        when(productStockRepository.findByProductIdAndSizeForUpdate(productId, size))
                .thenReturn(Optional.of(stock));

        // when
        stockService.decrease(DecreaseStockCommand.of(productId, size, quantity));

        // then
        assertThat(stock.getStockQuantity()).isEqualTo(7); // 10 - 3
        verify(productStockRepository).save(stock);
    }

    @Test
    @DisplayName("재고 차감 실패 - 상품 없음")
    void decrease_fail_product_not_found() {
        // expect
        assertThatThrownBy(() ->
                stockService.decrease(DecreaseStockCommand.of(1L, 270, 1)))
                .isInstanceOf(ProductException.NotFoundException.class);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 없음")
    void decrease_fail_stock_not_found() {
        when(productStockRepository.findByProductIdAndSizeForUpdate(1L, 270))
                .thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() ->
                stockService.decrease(DecreaseStockCommand.of(1L, 270, 1)))
                .isInstanceOf(ProductException.NotFoundException.class);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 부족")
    void decrease_fail_insufficient() {
        // given
        Long productId = 1L;
        int size = 270;
        int quantity = 5;

        ProductStock stock = ProductStock.of(productId, size, 2); // 부족한 재고

        when(productStockRepository.findByProductIdAndSizeForUpdate(productId, size))
                .thenReturn(Optional.of(stock));

        // expect
        assertThatThrownBy(() ->
                stockService.decrease(DecreaseStockCommand.of(productId, size, quantity)))
                .isInstanceOf(ProductException.InsufficientStockException.class);
    }
}
