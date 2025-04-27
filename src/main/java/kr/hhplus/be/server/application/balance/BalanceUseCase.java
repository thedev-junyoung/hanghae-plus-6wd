package kr.hhplus.be.server.application.balance;

public interface BalanceUseCase {
    /**
     * 사용자 잔액을 충전합니다.
     */
    BalanceInfo charge(ChargeBalanceCommand command);

    /**
     * 사용자의 현재 잔액을 조회합니다.
     */
    BalanceResult getBalance(Long userId);

    /**
     * 결제 시 잔액을 차감합니다. (결제 서비스에서 호출)
     * @return 차감 성공 여부
     */
    boolean decreaseBalance(DecreaseBalanceCommand command);
}