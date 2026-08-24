package com.stockflow.warehouse;

import com.stockflow.inventory.Inventory;
import com.stockflow.inventory.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WarehouseStockBootstrap implements CommandLineRunner {

    private final WarehouseStockRepository warehouseStockRepository;
    private final InventoryRepository inventoryRepository;

    public WarehouseStockBootstrap(
            WarehouseStockRepository warehouseStockRepository,
            InventoryRepository inventoryRepository) {
        this.warehouseStockRepository = warehouseStockRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (warehouseStockRepository.count() > 0 || inventoryRepository.count() == 0) {
            return;
        }

        Map<String, WarehouseStock> seededStock = new LinkedHashMap<>();

        for (Inventory inventory : inventoryRepository.findAll()) {
            if (inventory.getProduct() == null || inventory.getWarehouse() == null || inventory.getQuantity() == null) {
                continue;
            }

            int delta = switch (String.valueOf(inventory.getType()).toUpperCase()) {
                case "STOCK_IN", "TRANSFER_IN" -> inventory.getQuantity();
                case "STOCK_OUT", "TRANSFER_OUT" -> -inventory.getQuantity();
                default -> 0;
            };

            if (delta == 0) {
                continue;
            }

            String key = inventory.getProduct().getId() + ":" + inventory.getWarehouse().getId();
            WarehouseStock warehouseStock = seededStock.computeIfAbsent(
                    key,
                    ignored -> new WarehouseStock(inventory.getProduct(), inventory.getWarehouse(), 0));

            warehouseStock.setQuantity(Math.max(warehouseStock.getQuantity() + delta, 0));
        }

        warehouseStockRepository.saveAll(seededStock.values());
    }
}
