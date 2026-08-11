package com.stockflow.inventory;

import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.warehouse.Warehouse;
import com.stockflow.warehouse.WarehouseRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<Inventory> getAllTransactions() {
        return inventoryRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<Inventory> getRecentTransactions() {
        return inventoryRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public Inventory stockIn(StockInRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        productRepository.saveAndFlush(product);

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(request.getQuantity());
        inventory.setType("STOCK_IN");
        inventory.setRemarks(request.getRemarks());

        return inventoryRepository.saveAndFlush(inventory);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public Inventory stockOut(StockInRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if (product.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.saveAndFlush(product);

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(request.getQuantity());
        inventory.setType("STOCK_OUT");
        inventory.setRemarks(request.getRemarks());

        return inventoryRepository.saveAndFlush(inventory);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public List<Inventory> transferStock(StockTransferRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new RuntimeException("Source warehouse not found"));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new RuntimeException("Source and destination warehouses must be different");
        }

        if (product.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        Inventory outTransaction = new Inventory();
        outTransaction.setProduct(product);
        outTransaction.setWarehouse(fromWarehouse);
        outTransaction.setQuantity(request.getQuantity());
        outTransaction.setType("TRANSFER_OUT");
        outTransaction.setRemarks(request.getRemarks());
        inventoryRepository.saveAndFlush(outTransaction);

        Inventory inTransaction = new Inventory();
        inTransaction.setProduct(product);
        inTransaction.setWarehouse(toWarehouse);
        inTransaction.setQuantity(request.getQuantity());
        inTransaction.setType("TRANSFER_IN");
        inTransaction.setRemarks(request.getRemarks());

        return List.of(
                outTransaction,
                inventoryRepository.saveAndFlush(inTransaction));
    }

}
