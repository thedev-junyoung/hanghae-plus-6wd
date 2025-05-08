package kr.hhplus.be.server.interfaces.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.product.*;
import kr.hhplus.be.server.application.productstatistics.ProductSalesInfo;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import kr.hhplus.be.server.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @GetMapping("/popular/without-cache")
    public ResponseEntity<CustomApiResponse<List<ProductResponse.PopularProductResponse>>> getPopularProductsWithoutCache(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit
    ) {
        ProductRequest.PopularRequest request = ProductRequest.PopularRequest.of(days, limit);
        PopularProductCriteria criteria = PopularProductCriteria.of(request);

        // 캐싱 없이 바로 조회
        List<ProductSalesInfo> stats = productFacade.getPopularProductStatsOnly(criteria); // 별도 메서드 필요
        List<Long> productIds = stats.stream().map(ProductSalesInfo::productId).toList();
        Map<Long, Product> productMap = productUseCase.findProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<PopularProductResult> result = stats.stream()
                .map(info -> PopularProductResult.from(productMap.get(info.productId()), info.salesCount()))
                .toList();

        return ResponseEntity.ok(CustomApiResponse.success(
                result.stream()
                        .map(ProductResponse.PopularProductResponse::from)
                        .toList()
        ));
    }



}
