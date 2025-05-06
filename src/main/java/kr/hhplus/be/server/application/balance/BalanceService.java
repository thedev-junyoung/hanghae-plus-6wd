package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.DistributedLock;
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

    public BalanceInfo charge(ChargeBalanceCommand command) {


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
    @Transactional
    public boolean decreaseBalance(DecreaseBalanceCommand command) {
        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.decrease(Money.wons(command.amount()));

        balanceRepository.save(balance);

        return true;
    }
}
