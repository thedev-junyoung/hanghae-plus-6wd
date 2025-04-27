package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.application.product.GetProductDetailCommand;

public class ProductRequest {

    public record ListRequest(
            int page,
            int size,
            String sort
    ) {
        public static ListRequest of(int page, int size, String sort) {
            return new ListRequest(page, size, sort);
        }
    }

    public record DetailRequest(Long productId, int size) {
        public static DetailRequest of(Long productId, int size) {
            return new DetailRequest(productId, size);
        }

        public GetProductDetailCommand toCommand() {
            return new GetProductDetailCommand(productId,size);
        }
    }

    public record PopularRequest(Integer days, Integer limit) {
        public static PopularRequest of(Integer days, Integer limit) {
            return new PopularRequest(days, limit);
        }
    }
}
