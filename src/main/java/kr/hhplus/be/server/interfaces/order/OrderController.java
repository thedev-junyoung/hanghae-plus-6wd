package kr.hhplus.be.server.interfaces.order;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.order.CreateOrderCommand;
import kr.hhplus.be.server.application.order.OrderFacadeService;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class OrderController implements OrderAPI {

    private final OrderFacadeService orderFacadeService;

    @Override
    public ResponseEntity<CustomApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        CreateOrderCommand command = request.toCommand();
        OrderResult result = orderFacadeService.createOrder(command);
        return ResponseEntity.ok(CustomApiResponse.success(OrderResponse.from(result)));
    }

}
