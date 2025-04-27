package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Product save(Product domain) {
        return jpaRepository.save(domain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
