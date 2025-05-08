package kr.hhplus.be.server.application.order;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@TestConfiguration
public class TestCompensationConfig {

    @Bean
    public OrderCompensationService stubOrderCompensationService() {
        return new OrderCompensationService(null, null) {
            @Override
            public void compensateStock(List<CreateOrderCommand.OrderItemCommand> items) {
                // no-op: 테스트용 stub
            }

            @Override
            public void markOrderAsFailed(String orderId) {
                // no-op: 테스트용 stub
            }
        };
    }
}
