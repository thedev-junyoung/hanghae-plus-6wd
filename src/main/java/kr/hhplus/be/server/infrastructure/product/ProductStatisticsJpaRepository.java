package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.productstatistics.ProductStatistics;
import kr.hhplus.be.server.domain.productstatistics.ProductStatisticsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductStatisticsJpaRepository extends JpaRepository<ProductStatistics, Long> {
    Optional<ProductStatistics> findById(ProductStatisticsId id);

    @Query("""
        SELECT ps
        FROM ProductStatistics ps
        WHERE ps.id.statDate BETWEEN :from AND :to
        """)
    List<ProductStatistics> findByStatDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
