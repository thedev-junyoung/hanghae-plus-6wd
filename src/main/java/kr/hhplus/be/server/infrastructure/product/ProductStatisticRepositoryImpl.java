package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.application.productstatistics.ProductSalesInfo;
import kr.hhplus.be.server.domain.productstatistics.ProductStatistics;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsId;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductStatisticRepositoryImpl implements ProductStatisticsRepository {

    private final ProductStatisticsJpaRepository jpaRepository;

    @Override
    public Optional<ProductStatistics> findByProductIdAndStatDate(Long productId, LocalDate statDate) {
        return jpaRepository.findById(new ProductStatisticsId(productId, statDate));
    }

    @Override
    public void save(ProductStatistics stats) {
        jpaRepository.save(stats);
    }

    @Override
    public List<ProductStatistics> saveAll(List<ProductStatistics> stats) {
        return jpaRepository.saveAll(stats);
    }

    @Override
    public List<ProductSalesInfo> findTopSellingProducts(LocalDate from, LocalDate to, int limit) {
        return jpaRepository.findByStatDateBetween(from, to).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ProductStatistics::getProductId,
                        java.util.stream.Collectors.summingInt(ProductStatistics::getSalesCount)
                ))
                .entrySet().stream()
                .map(entry -> new ProductSalesInfo(entry.getKey(), (long) entry.getValue()))
                .sorted(java.util.Comparator.comparing(ProductSalesInfo::salesCount).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<ProductStatistics> findById(ProductStatisticsId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void delete(ProductStatistics stats) {
        jpaRepository.delete(stats);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
