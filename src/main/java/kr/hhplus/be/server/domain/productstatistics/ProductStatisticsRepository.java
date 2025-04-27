package kr.hhplus.be.server.domain.productstatistics;

import kr.hhplus.be.server.application.productstatistics.ProductSalesInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductStatisticsRepository {

    /**
     * 특정 상품에 대한 특정 날짜의 통계를 조회한다.
     * 오늘자 통계가 있는지 확인하거나 기록을 누적할 때 사용.
     */
    Optional<ProductStatistics> findByProductIdAndStatDate(Long productId, LocalDate statDate);

    /**
     * 단일 통계를 저장하거나 업데이트한다.
     * 통계 객체가 새로 생성됐거나 누적값이 변경된 경우 호출.
     */
    void save(ProductStatistics stats);

    /**
     * 복수의 통계 데이터를 한 번에 저장한다.
     * 테스트 데이터 생성 또는 여러 날짜의 통계를 일괄 저장할 때 사용.
     */
    List<ProductStatistics> saveAll(List<ProductStatistics> stats);

    /**
     * 지정된 기간 내 판매량 상위 상품들을 조회한다.
     * 판매량 기반 인기 상품 목록 조회 기능에서 사용.
     */
    List<ProductSalesInfo> findTopSellingProducts(LocalDate from, LocalDate to, int limit);

    /**
     * 복합 키 (productId + statDate)를 통해 통계를 조회한다.
     * 상황에 따라 직접 ID로 조회가 필요한 경우에 사용.
     */
    Optional<ProductStatistics> findById(ProductStatisticsId id);

    /**
     * 통계 데이터 하나를 삭제한다.
     * 테스트 시 사전 정리하거나 특정 통계를 제거할 때 사용.
     */
    void delete(ProductStatistics stats);

    /**
     * 모든 통계 데이터를 삭제한다.
     * 테스트 세팅 초기화 목적 등으로 사용.
     */
    void deleteAll();
}
