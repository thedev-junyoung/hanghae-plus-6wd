package kr.hhplus.be.server.application.orderexport;

import kr.hhplus.be.server.domain.orderexport.OrderExportPayload;

public record OrderExportCommand(
        OrderExportPayload payload
) {

}
