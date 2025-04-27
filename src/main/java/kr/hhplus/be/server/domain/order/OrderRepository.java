package kr.hhplus.be.server.domain.order;


import java.util.Collection;
import java.util.Optional;

public interface OrderRepository{
    /**
     * 주문을 저장하거나 업데이트한다.
     * 주문이 새로 생성되거나 변경된 경우 호출.
     */
    Order save(Order order);

    /**
     * 주문 ID로 주문을 조회한다.
     * 주문 상세 조회, 상태 확인 등에서 사용된다.
     */
    Optional<Order> findById(String orderId);

    long count();

    Collection<Order> findAll();

    Optional<Order> findByIdForUpdate(String orderId);

}
