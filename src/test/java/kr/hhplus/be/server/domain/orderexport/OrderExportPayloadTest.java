package kr.hhplus.be.server.domain.orderexport;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderExportPayloadTest {

    @Test
    @DisplayName("Order → OrderExportPayload 변환 성공")
    void convert_order_to_export_payload_success() {
        // given
        Long userId = 42L;
        long price = 10000L;
        int quantity = 2;
        int size = 270;

        OrderItem orderItem = OrderItem.of(123L, quantity, size, Money.wons(price));
        Order order = Order.create(userId, List.of(orderItem), Money.wons(price * quantity));

        // when
        OrderExportPayload payload = OrderExportPayload.from(order);

        // then
        assertThat(payload).isNotNull();
        assertThat(payload.getUserId()).isEqualTo(userId);
        assertThat(payload.getTotalAmount()).isEqualTo(price * quantity);

        assertThat(payload.getItems()).hasSize(1);
        OrderExportPayload.OrderItemPayload itemPayload = payload.getItems().get(0);
        assertThat(itemPayload.getProductId()).isEqualTo(123L);
        assertThat(itemPayload.getQuantity()).isEqualTo(quantity);
        assertThat(itemPayload.getSize()).isEqualTo(size);
        assertThat(itemPayload.getPrice()).isEqualTo(price);
    }
}
