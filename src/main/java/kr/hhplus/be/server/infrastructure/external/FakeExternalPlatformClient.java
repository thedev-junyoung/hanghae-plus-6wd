package kr.hhplus.be.server.infrastructure.external;

import kr.hhplus.be.server.domain.orderexport.OrderExportPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FakeExternalPlatformClient implements ExternalPlatformClient {
    @Override
    public void sendOrder(OrderExportPayload payload) {
        log.info("[FAKE] 외부 플랫폼으로 주문 전송됨: {}", payload);
    }
}
