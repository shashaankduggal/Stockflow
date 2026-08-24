package com.stockflow.product;

import com.stockflow.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Product updateProduct(Long id, Product updatedProduct) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(updatedProduct.getName());
        product.setSku(updatedProduct.getSku());
        product.setPrice(updatedProduct.getPrice());

        return productRepository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public Product getProduct(Long id) {

        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
