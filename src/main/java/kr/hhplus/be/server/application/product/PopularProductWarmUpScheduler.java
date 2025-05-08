package kr.hhplus.be.server.application.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularProductWarmUpScheduler {

    private final ProductFacade productFacade;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void warmUpPopularProducts() {
        List.of(
                // 하루
//                new PopularProductCriteria(1, 5),

                new PopularProductCriteria(3, 5)
                // 일주일
//                new PopularProductCriteria(7, 24)

                // 한달
//                new PopularProductCriteria(30, 20)
        ).forEach(productFacade::getPopularProducts);

        log.info("인기 상품 웜업 완료");
    }
}