package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceException;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import kr.hhplus.be.server.common.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryTracingAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService implements BalanceUseCase {


    private final BalanceRepository balanceRepository;
    private final ApplicationEventPublisher eventPublisher;


    public BalanceInfo charge(ChargeBalanceCommand command) {
        log.info("[비즈니스 로직 시작: 잔액 충전] userId={}, amount={}", command.userId(), command.amount());

        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.charge(Money.wons(command.amount()));

        log.info("[DB 저장 직전] userId={}, newAmount={}", command.userId(), balance.getAmount());

        balanceRepository.save(balance);

        log.info("[비즈니스 로직 끝] 잔액 충전 완료 : userId={}, amount={}", command.userId(), balance.getAmount());
        eventPublisher.publishEvent(BalanceChargedEvent.from(command));

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
//    @DistributedLock(key = "#command.orderId", prefix = "payment:order:")
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean decreaseBalance(DecreaseBalanceCommand command) {
        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.decrease(Money.wons(command.amount()));

        balanceRepository.save(balance);

        return true;
    }
}
