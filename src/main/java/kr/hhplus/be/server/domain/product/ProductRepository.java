package kr.hhplus.be.server.domain.product;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository{
    Page<Product> findAll(Pageable pageable);

    Product save(Product domain);
    Optional<Product> findById(Long aLong);

}
