package kr.hhplus.be.server.infrastructure.product;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.product.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductStockJpaRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findByProductIdAndSize(Long productId, int size);
    Optional<ProductStock> findByProductId(Long productId); // 구현 목적에 따라 다르게 쿼리 가능

    List<ProductStock> findAllByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ProductStock ps WHERE ps.productId = :productId AND ps.size = :size")
    Optional<ProductStock> findByProductIdAndSizeForUpdate(@Param("productId") Long productId, @Param("size") int size);



}
