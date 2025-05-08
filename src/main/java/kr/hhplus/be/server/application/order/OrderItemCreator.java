package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemCreator {

    private final ProductUseCase productService;
    private final StockService stockService;

    public List<OrderItem> createOrderItems(List<CreateOrderCommand.OrderItemCommand> commands) {
        List<OrderItem> result = new ArrayList<>();

        for (var item : commands) {
            // 재고 차감
            stockService.decrease(DecreaseStockCommand.of(item.productId(), item.size(), item.quantity()));

            // 상품 조회
            ProductDetailResult product = productService.getProductDetail(
                    GetProductDetailCommand.of(item.productId(), item.size())
            );

            Money unitPrice = Money.wons(product.product().price());

            result.add(OrderItem.of(item.productId(), item.quantity(), item.size(), unitPrice));
        }

        return result;
    }
}
