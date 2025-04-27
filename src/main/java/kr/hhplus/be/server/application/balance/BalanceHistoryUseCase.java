package kr.hhplus.be.server.application.balance;

public interface BalanceHistoryUseCase {
    void recordHistory(RecordBalanceHistoryCommand command);
}
