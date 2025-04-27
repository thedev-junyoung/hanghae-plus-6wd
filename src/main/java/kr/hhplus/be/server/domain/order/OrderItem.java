package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.vo.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int size;

    @Column(nullable = false)
    private long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 생성 메서드 (팩토리 패턴 유지)
    public static OrderItem of(Long productId, int quantity, int size, Money price) {
        OrderItem item = new OrderItem();
        item.productId = productId;
        item.quantity = quantity;
        item.size = size;
        item.price = price.value();
        return item;
    }

    public void initOrder(Order order) {
        this.order = order;
    }

    public Money calculateTotal() {
        return Money.from(price).multiply(quantity);
    }
}
