package com.stockflow.dashboard;

import com.stockflow.inventory.InventoryRepository;
import com.stockflow.product.ProductRepository;
import com.stockflow.security.RoleName;
import com.stockflow.supplier.SupplierRepository;
import com.stockflow.category.CategoryRepository;
import com.stockflow.user.UserRepository;
import com.stockflow.warehouse.WarehouseRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            UserRepository userRepository,
            InventoryRepository inventoryRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();
        RoleName currentRole = RoleName.from(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse(null));

        stats.put("totalProducts", productRepository.count());
        stats.put("totalWarehouses", warehouseRepository.count());
        stats.put("totalTransactions", inventoryRepository.count());
        stats.put("stockInCount", inventoryRepository.countByType("STOCK_IN"));
        stats.put("stockOutCount", inventoryRepository.countByType("STOCK_OUT"));
        stats.put("transferCount", inventoryRepository.countByTypeIn(List.of("TRANSFER_IN", "TRANSFER_OUT")));
        stats.put("recentTransactions", inventoryRepository.findTop10ByOrderByCreatedAtDesc());
        stats.put(
                "inventoryValue",
                productRepository.findAll()
                        .stream()
                        .map(product -> BigDecimal.valueOf(product.getPrice() * product.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        if (currentRole == RoleName.ADMIN || currentRole == RoleName.MANAGER) {
            stats.put("totalSuppliers", supplierRepository.count());
            stats.put("totalCategories", categoryRepository.count());
        }

        if (currentRole == RoleName.ADMIN) {
            stats.put("totalUsers", userRepository.count());
        }

        return stats;
    }
}
