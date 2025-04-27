package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceException;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import kr.hhplus.be.server.common.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService implements BalanceUseCase {


    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    public BalanceInfo charge(ChargeBalanceCommand command) {
        if (balanceHistoryRepository.existsByRequestId(command.requestId())) {
            log.warn("이미 처리된 충전 요청입니다: userId={}, requestId={}", command.userId(), command.requestId());
            Balance existing = balanceRepository.findByUserId(command.userId()).orElseThrow();
            System.out.println("existing = " + existing);
            return BalanceInfo.from(existing); // 이전 충전 결과 그대로 반환
        }

        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.charge(Money.wons(command.amount()));
        balanceRepository.save(balance);
        System.out.println("balance = " + balance);
        return BalanceInfo.from(balance);
    }



    @Override
    @Transactional(readOnly = true)
    public BalanceResult getBalance(Long userId) {
        Balance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new BalanceException.NotFoundException(userId));
        System.out.println("balance = " + balance);
        return BalanceResult.fromInfo(BalanceInfo.from(balance));
    }

    @Override
    public boolean decreaseBalance(DecreaseBalanceCommand command) {
        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.decrease(Money.wons(command.amount()));

        balanceRepository.save(balance);

        return true;
    }
}
