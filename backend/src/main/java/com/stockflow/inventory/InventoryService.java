package com.stockflow.inventory;

import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.warehouse.Warehouse;
import com.stockflow.warehouse.WarehouseRepository;
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

    public List<Inventory> getAllTransactions() {
        return inventoryRepository.findAll();
    }

    @Transactional
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

}
