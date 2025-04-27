package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.BalanceChangeType;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Transactional
class BalanceHistoryServiceIntegrationTest {

    @Autowired
    BalanceHistoryService service;

    @Autowired
    BalanceHistoryRepository repository;

    @Test
    @DisplayName("recordHistory가 호출되면 DB에 히스토리가 저장된다")
    void recordHistory_persistsToDatabase() {
        // given
        Long userId = 100L;
        String reason = "충전 테스트";
        String requestId = "REQ-" + UUID.randomUUID();
        RecordBalanceHistoryCommand command = new RecordBalanceHistoryCommand(
                userId, 5000L, BalanceChangeType.CHARGE, reason, requestId
        );

        // when
        service.recordHistory(command);

        // then
        BalanceHistory history = repository.findAllByUserId(userId).get(0);
        assertThat(history.getUserId()).isEqualTo(userId);
        assertThat(history.getAmount()).isEqualTo(5000L);
        assertThat(history.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("동일한 requestId가 있는 경우 중복 저장되지 않는다")
    void recordHistory_shouldBeIdempotent() {
        // given
        long userId = 100L;
        String requestId = "REQ-HISTORY-IDEMPOTENT";
        String reason = "중복 저장 테스트";

        RecordBalanceHistoryCommand command = new RecordBalanceHistoryCommand(
                userId, 5000L, BalanceChangeType.CHARGE, reason, requestId
        );

        // when - 두 번 호출
        service.recordHistory(command);
        service.recordHistory(command); // 중복 호출

        // then - 한 번만 저장돼야 함
        long count = repository.findAllByUserId(userId).stream()
                .filter(h -> requestId.equals(h.getRequestId()))
                .count();

        assertThat(count).isEqualTo(1);
    }
}
