package kr.hhplus.be.server.domain.productstatistics;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class ProductStatisticsId implements Serializable {

    private Long productId;
    private LocalDate statDate;

    public ProductStatisticsId(Long productId, LocalDate statDate) {
        this.productId = productId;
        this.statDate = statDate;
    }
}
