package kr.hhplus.be.server.domain.outbox;

public enum EventStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
