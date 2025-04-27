package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;


public interface ProductUseCase {

    /**
     * 페이징된 상품 리스트를 조회한다. 재고 정보 포함.
     */
    ProductListResult getProductList(GetProductListCommand command);

    /**
     * 단일 상품 상세 정보를 조회합니다.
     */
    ProductDetailResult getProductDetail(GetProductDetailCommand command);
    /**
     * 상품 재고 차감
     */
    boolean decreaseStock(DecreaseStockCommand command);

    /**
     *
     */
    Product findProduct(Long productId); // Info 생성을 위한 raw entity
}
