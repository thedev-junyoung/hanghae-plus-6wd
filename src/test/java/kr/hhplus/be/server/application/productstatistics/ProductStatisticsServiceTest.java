package kr.hhplus.be.server.application.productstatistics;

import kr.hhplus.be.server.domain.productstatistics.ProductStatistics;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStatisticsServiceTest {

    @Mock
    ProductStatisticsRepository repository;

    @InjectMocks
    ProductStatisticsService service;

    @Test
    @DisplayName("오늘자 통계가 존재하지 않으면 새로 생성 후 저장한다")
    void record_createsNewStatsIfNotExist() {
        // given
        Long productId = 1L;
        int quantity = 3;
        long amount = 10000;
        LocalDate today = LocalDate.now();

        when(repository.findByProductIdAndStatDate(productId, today)).thenReturn(Optional.empty());

        // when
        service.record(new RecordSalesCommand(productId, quantity, amount));

        // then
        verify(repository).save(argThat(stats ->
                stats.getProductId().equals(productId) &&
                        stats.getSalesCount() == quantity &&
                        stats.getSalesAmount() == amount * quantity
        ));
    }

    @Test
    @DisplayName("오늘자 통계가 존재하면 누적 후 저장한다")
    void record_updatesExistingStats() {
        // given
        Long productId = 1L;
        int quantity = 2;
        long amount = 5000;
        LocalDate today = LocalDate.now();

        ProductStatistics existing = ProductStatistics.create(productId, today);
        when(repository.findByProductIdAndStatDate(productId, today)).thenReturn(Optional.of(existing));

        // when
        service.record(new RecordSalesCommand(productId, quantity, amount));

        // then
        assertThat(existing.getSalesCount()).isEqualTo(quantity);
        assertThat(existing.getSalesAmount()).isEqualTo(quantity * amount);
        verify(repository).save(existing);
    }
}
