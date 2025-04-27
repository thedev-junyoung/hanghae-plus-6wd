package kr.hhplus.be.server.application.orderexport;

import kr.hhplus.be.server.infrastructure.external.ExternalPlatformClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderExportService implements OrderExportUseCase {

    private final ExternalPlatformClient platformClient;

    @Override
    public void export(OrderExportCommand command) {
        platformClient.sendOrder(command.payload());
    }
}
