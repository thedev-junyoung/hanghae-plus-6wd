package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final ProductStockRepository productStockRepository;

    @DistributedLock(
            prefix = "stock:decrease:",
            key = "#command.productId + ':' + #command.size",
            waitTime = 5,
            leaseTime = 3
    )
    public void decrease(DecreaseStockCommand command) {
        log.info("[비즈니스 로직] 재고 차감 - 상품 ID: {}, 사이즈: {}, 수량: {}", command.productId(), command.size(), command.quantity());
        ProductStock stock = productStockRepository.findByProductIdAndSize(command.productId(), command.size())
                .orElseThrow(() -> new ProductException.NotFoundException(command.productId()));
        if (stock.getStockQuantity() < command.quantity()) {
            throw new ProductException.InsufficientStockException();
        }

        stock.decreaseStock(command.quantity());

        productStockRepository.save(stock);
        log.info("[비즈니스 로직] 재고 차감 완료 - 상품 ID: {}, 사이즈: {}, 수량: {}", command.productId(), command.size(), command.quantity());
    }

    @Transactional
    public void increase(IncreaseStockCommand command) {
        log.info("[비즈니스 로직] 재고 증가 - 상품 ID: {}, 사이즈: {}, 수량: {}", command.productId(), command.size(), command.quantity());
        ProductStock stock = productStockRepository.findByProductIdAndSize(command.productId(), command.size())
                .orElseThrow(() -> new ProductException.NotFoundException(command.productId()));

        stock.increaseStock(command.quantity());
        productStockRepository.save(stock);
        log.info("[비즈니스 로직] 재고 증가 완료 - 상품 ID: {}, 사이즈: {}, 수량: {}", command.productId(), command.size(), command.quantity());
    }
}
