package kr.hhplus.be.server.infrastructure.external;

import kr.hhplus.be.server.domain.orderexport.OrderExportPayload;

public interface ExternalPlatformClient {
    void sendOrder(OrderExportPayload payload);
}
