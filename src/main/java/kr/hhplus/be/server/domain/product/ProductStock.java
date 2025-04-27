package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "product_stock")
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private int size;

    private int stockQuantity;

    private LocalDateTime updatedAt;

    public ProductStock(Long productId, int size, int stockQuantity) {
        this.id = null;
        this.productId = productId;
        this.size = size;
        this.stockQuantity = stockQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductStock of(Long productId, int size, int stockQuantity) {
        return new ProductStock(productId, size, stockQuantity);
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseStock(int quantity) {
        if (!Policy.canDecrease(stockQuantity, quantity)) {
            throw new ProductException.InsufficientStockException("재고가 부족합니다. 현재: " + stockQuantity + ", 요청: " + quantity);
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable(int quantity) {
        return Policy.canDecrease(stockQuantity, quantity);
    }

    static class Policy {

        // 재고는 0 이상이어야 한다
        public static boolean canDecrease(int currentStock, int requestQuantity) {
            return currentStock >= requestQuantity;
        }

    }
}
