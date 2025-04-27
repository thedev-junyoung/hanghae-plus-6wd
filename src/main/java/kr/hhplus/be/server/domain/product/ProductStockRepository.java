package kr.hhplus.be.server.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductStockRepository {
    ProductStock save(ProductStock stock);

    List<ProductStock> findAllByProductId(Long productId);
    Optional<ProductStock> findByProductIdAndSize(Long productId, int size);

    Optional<ProductStock> findByProductIdAndSizeForUpdate(Long productId, int size);

    Optional<ProductStock> findByProductId(Long id);
}
