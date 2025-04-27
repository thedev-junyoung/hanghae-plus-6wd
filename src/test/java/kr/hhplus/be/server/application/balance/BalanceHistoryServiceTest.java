package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.BalanceChangeType;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceHistoryServiceTest {

    @Mock
    BalanceHistoryRepository repository;

    @InjectMocks
    BalanceHistoryService service;

    @Test
    @DisplayName("잔액 변경 내역 기록 - 성공")
    void recordHistory_success() {
        // given
        RecordBalanceHistoryCommand command = new RecordBalanceHistoryCommand(
                100L, 2000L, BalanceChangeType.CHARGE, "테스트 충전", null
        );

        // when
        service.recordHistory(command);

        // then
        ArgumentCaptor<BalanceHistory> captor = ArgumentCaptor.forClass(BalanceHistory.class);
        verify(repository, times(1)).save(captor.capture());

        BalanceHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getAmount()).isEqualTo(2000L);
        assertThat(saved.getType()).isEqualTo(BalanceChangeType.CHARGE);
        assertThat(saved.getReason()).isEqualTo("테스트 충전");
    }
}
