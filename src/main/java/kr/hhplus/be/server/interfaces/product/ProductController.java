package kr.hhplus.be.server.interfaces.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "상품 API")
public class ProductController implements ProductAPI {

    private final ProductUseCase productUseCase;
    private final ProductFacade productFacade;

    @GetMapping
    public ResponseEntity<CustomApiResponse<ProductResponse.ProductListResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        ProductRequest.ListRequest request = ProductRequest.ListRequest.of(page, size, sort);
        GetProductListCommand command = GetProductListCommand.fromRequest(request);
        ProductListResult result = productUseCase.getProductList(command);

        return ResponseEntity.ok(CustomApiResponse.success(
                ProductResponse.ProductListResponse.from(result)
        ));
    }


    @Override
    public ResponseEntity<CustomApiResponse<ProductResponse.ProductDetailResponse>> getProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int size
    ) {
        ProductRequest.DetailRequest request = ProductRequest.DetailRequest.of(productId, size);
        ProductDetailResult result = productUseCase.getProductDetail(request.toCommand());
        return ResponseEntity.ok(CustomApiResponse.success(ProductResponse.ProductDetailResponse.from(result)));
    }

    @Override
    public ResponseEntity<CustomApiResponse<List<ProductResponse.PopularProductResponse>>> getPopularProducts(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit
    ) {
        ProductRequest.PopularRequest request = ProductRequest.PopularRequest.of(days, limit);
        PopularProductCriteria criteria = PopularProductCriteria.of(request);
        List<PopularProductResult> result = productFacade.getPopularProducts(criteria);

        return ResponseEntity.ok(CustomApiResponse.success(
                result.stream()
                        .map(ProductResponse.PopularProductResponse::from)
                        .toList()
        ));
    }



}
