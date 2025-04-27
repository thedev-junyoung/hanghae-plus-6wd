package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import org.springframework.dao.OptimisticLockingFailureException;

public class BalanceException extends BusinessException {
    protected BalanceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static class NotFoundException extends BusinessException {

        public NotFoundException(Long userId) {
            super(ErrorCode.BALANCE_NOT_FOUND, "해당 유저(" + userId + ")의 잔액 정보가 존재하지 않습니다.");
        }
    }
    public static class MinimumChargeAmountException extends BusinessException {

        public MinimumChargeAmountException(long minimumChargeAmount) {
            super(
                    ErrorCode.INVALID_AMOUNT,
                    "최소 충전 금액은 " + minimumChargeAmount + "원 이상이어야 합니다."
            );
        }
    }
    public static class NotEnoughBalanceException extends BusinessException {

        public NotEnoughBalanceException() {
            super(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다.");
        }

        public NotEnoughBalanceException(String message) {
            super(ErrorCode.INSUFFICIENT_BALANCE, message);
        }
    }


    public static class ChargeConflictException extends Throwable {
        public ChargeConflictException(Long aLong, OptimisticLockingFailureException e) {
            super("충전 중 충돌 발생 - userId: " + aLong, e);
        }
    }
}
