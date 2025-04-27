package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductListResult getProductList(GetProductListCommand command) {
        PageRequest pageRequest = PageRequest.of(command.page(), command.size(), command.getSort());

        var productPage = productRepository.findAll(pageRequest);

        List<ProductInfo> infos = productPage.getContent().stream()
                .map(product -> {
                    List<ProductStock> stocks = productStockRepository.findAllByProductId(product.getId());
                    int totalStock = stocks.stream()
                            .mapToInt(ProductStock::getStockQuantity)
                            .sum();
                    return ProductInfo.from(product, totalStock);
                })
                .toList();
        return ProductListResult.from(infos);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResult getProductDetail(GetProductDetailCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductException.NotFoundException(command.productId()));

        int stock = productStockRepository.findByProductIdAndSize(product.getId(), command.size())
                .map(ProductStock::getStockQuantity)
                .orElse(0);

        return ProductDetailResult.fromDomain(product, stock);
    }

    @Override
    @Transactional
    public boolean decreaseStock(DecreaseStockCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductException.NotFoundException(command.productId()));
        // 락 걸린 재고 조회
        ProductStock stock = productStockRepository.findByProductIdAndSizeForUpdate(command.productId(), command.size())
                .orElseThrow(ProductException.InsufficientStockException::new);

        product.validateOrderable(stock.getStockQuantity());
        stock.decreaseStock(command.quantity());
        productStockRepository.save(stock);
        return true;
    }

    @Override
    public Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException.NotFoundException(productId));
    }
}

