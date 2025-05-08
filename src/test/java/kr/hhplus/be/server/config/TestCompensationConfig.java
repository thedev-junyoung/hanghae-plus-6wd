package kr.hhplus.be.server.config;

import kr.hhplus.be.server.application.order.CreateOrderCommand;
import kr.hhplus.be.server.application.order.OrderCompensationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("test")
public class TestCompensationConfig {

    @Bean
    @Primary
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
