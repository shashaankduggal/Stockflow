package com.stockflow.inventory;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getAllTransactions() {
        return inventoryService.getAllTransactions();
    }

    @PostMapping("/stock-in")
    public Inventory stockIn(@Valid @RequestBody StockInRequest request) {
        return inventoryService.stockIn(request);
    }

    @PostMapping("/stock-out")
    public Inventory stockOut(@Valid @RequestBody StockOutRequest request) {
        return inventoryService.stockOut(request);
    }

    @PostMapping("/transfer")
    public List<Inventory> transfer(@Valid @RequestBody StockTransferRequest request) {
        return inventoryService.transferStock(request);
    }
}
