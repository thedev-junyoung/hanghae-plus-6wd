package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public void decrease(DecreaseStockCommand command) {
        ProductStock stock = productStockRepository.findByProductIdAndSizeForUpdate(command.productId(), command.size())
                .orElseThrow(() -> new ProductException.NotFoundException(command.productId()));
        if (stock.getStockQuantity() < command.quantity()) {
            throw new ProductException.InsufficientStockException();
        }

        stock.decreaseStock(command.quantity());

        productStockRepository.save(stock); // saveAndFlush 필요 없음
    }
}
