package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.interfaces.product.ProductRequest;
import org.springframework.data.domain.Sort;

public record GetProductListCommand(
        int page,
        int size,
        String sort
) {
    public static GetProductListCommand fromRequest(ProductRequest.ListRequest request) {
        return new GetProductListCommand(request.page(), request.size(), request.sort());
    }

    public Sort getSort() {
        if (sort == null || sort.isEmpty()) return Sort.unsorted();

        String[] parts = sort.split(",");
        String field = parts[0];
        String direction = (parts.length > 1) ? parts[1] : "asc";

        return direction.equals("desc")
                ? Sort.by(Sort.Order.desc(field))
                : Sort.by(Sort.Order.asc(field));
    }
}

