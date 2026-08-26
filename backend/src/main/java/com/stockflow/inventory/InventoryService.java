package com.stockflow.inventory;

import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.BadRequestException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.warehouse.Warehouse;
import com.stockflow.warehouse.WarehouseRepository;
import com.stockflow.warehouse.WarehouseStock;
import com.stockflow.warehouse.WarehouseStockRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final AuditLogService auditLogService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            WarehouseStockRepository warehouseStockRepository,
            AuditLogService auditLogService) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.auditLogService = auditLogService;
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
        WarehouseStock warehouseStock = getWarehouseStock(request.getProductId(), request.getWarehouseId());
        warehouseStock.setQuantity(warehouseStock.getQuantity() + request.getQuantity());
        warehouseStockRepository.saveAndFlush(warehouseStock);
        auditLogService.logInventoryAction(InventoryType.STOCK_IN.name(), warehouseStock.getProduct(), null,
            warehouseStock.getWarehouse(), request.getQuantity(), request.getRemarks());
        return inventoryRepository.saveAndFlush(
                createTransaction(
                        warehouseStock.getProduct(),
                        warehouseStock.getWarehouse(),
                        request.getQuantity(),
                        InventoryType.STOCK_IN,
                        request.getRemarks()));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public Inventory stockOut(StockInRequest request) {
        WarehouseStock warehouseStock = getWarehouseStock(request.getProductId(), request.getWarehouseId());
        if (warehouseStock.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        warehouseStock.setQuantity(warehouseStock.getQuantity() - request.getQuantity());
        warehouseStockRepository.saveAndFlush(warehouseStock);
        auditLogService.logInventoryAction(InventoryType.STOCK_OUT.name(), warehouseStock.getProduct(),
            warehouseStock.getWarehouse(), null, request.getQuantity(), request.getRemarks());
        return inventoryRepository.saveAndFlush(
                createTransaction(
                        warehouseStock.getProduct(),
                        warehouseStock.getWarehouse(),
                        request.getQuantity(),
                        InventoryType.STOCK_OUT,
                        request.getRemarks()));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public List<Inventory> transferStock(StockTransferRequest request) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new BadRequestException("Source and destination warehouses must be different");
        }

        WarehouseStock sourceStock = getWarehouseStock(request.getProductId(), request.getFromWarehouseId());
        WarehouseStock destinationStock = getWarehouseStock(request.getProductId(), request.getToWarehouseId());
        if (sourceStock.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        sourceStock.setQuantity(sourceStock.getQuantity() - request.getQuantity());
        destinationStock.setQuantity(destinationStock.getQuantity() + request.getQuantity());
        warehouseStockRepository.saveAll(List.of(sourceStock, destinationStock));
        auditLogService.logInventoryAction("TRANSFER", sourceStock.getProduct(), sourceStock.getWarehouse(),
            destinationStock.getWarehouse(), request.getQuantity(), request.getRemarks());

        Inventory outTransaction = inventoryRepository.saveAndFlush(
                createTransaction(
                        sourceStock.getProduct(),
                        sourceStock.getWarehouse(),
                        request.getQuantity(),
                        InventoryType.TRANSFER_OUT,
                        request.getRemarks()));

        Inventory inTransaction = inventoryRepository.saveAndFlush(
                createTransaction(
                        destinationStock.getProduct(),
                        destinationStock.getWarehouse(),
                        request.getQuantity(),
                        InventoryType.TRANSFER_IN,
                        request.getRemarks()));

        return List.of(
                outTransaction,
                inTransaction);
    }

    private WarehouseStock getWarehouseStock(Long productId, Long warehouseId) {
        return warehouseStockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
                return warehouseStockRepository.save(new WarehouseStock(product, warehouse, 0));
                });
    }

    private Inventory createTransaction(
            Product product,
            Warehouse warehouse,
            Integer quantity,
            InventoryType type,
            String remarks) {

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(quantity);
        inventory.setType(type);
        inventory.setRemarks(remarks);
        return inventory;
    }
}
