package com.stockflow.product;

import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.warehouse.WarehouseStockRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public ProductService(ProductRepository productRepository, WarehouseStockRepository warehouseStockRepository) {
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<ProductResponse> getAllProducts() {
        return toResponses(productRepository.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return toResponses(productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProductResponse createProduct(ProductRequest request) {
        return toResponse(productRepository.save(toProduct(new Product(), request)), Map.of());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return toResponse(productRepository.save(toProduct(product, request)), currentStockByProductId());
    }

    private Product toProduct(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim());
        product.setPrice(request.getPrice().setScale(2, RoundingMode.HALF_UP));
        return product;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public ProductResponse getProduct(Long id) {

        return toResponse(
                productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found")),
                currentStockByProductId());
    }

    private List<ProductResponse> toResponses(List<Product> products) {
        Map<Long, Integer> stockByProductId = currentStockByProductId();
        return products.stream()
                .map(product -> toResponse(product, stockByProductId))
                .toList();
    }

    private ProductResponse toResponse(Product product, Map<Long, Integer> stockByProductId) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                stockByProductId.getOrDefault(product.getId(), 0));
    }

    private Map<Long, Integer> currentStockByProductId() {
        Map<Long, Integer> stockByProductId = new HashMap<>();
        for (Object[] row : warehouseStockRepository.findTotalQuantitiesByProductId()) {
            stockByProductId.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return stockByProductId;
    }
}
