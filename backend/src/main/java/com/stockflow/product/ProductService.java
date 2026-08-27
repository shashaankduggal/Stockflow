package com.stockflow.product;

import com.stockflow.exception.DuplicateResourceException;
import com.stockflow.exception.ProductNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        ensureSkuAvailable(request.getSku(), null);
        return toResponse(productRepository.save(toProduct(new Product(), request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        ensureSkuAvailable(request.getSku(), id);

        return toResponse(productRepository.save(toProduct(product, request)));
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
            throw new ProductNotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public ProductResponse getProduct(Long id) {

        return toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found")));
    }

    private List<ProductResponse> toResponses(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice());
    }

    private void ensureSkuAvailable(String sku, Long productId) {
        productRepository.findBySkuIgnoreCase(sku.trim()).ifPresent(existing -> {
            if (productId == null || !existing.getId().equals(productId)) {
                throw new DuplicateResourceException("A product with that SKU already exists");
            }
        });
    }
}
