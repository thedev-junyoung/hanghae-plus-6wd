package kr.hhplus.be.server.application.productstatistics;

public record RecordSalesCommand(
        Long productId,
        int quantity,
        long amount
) {}
