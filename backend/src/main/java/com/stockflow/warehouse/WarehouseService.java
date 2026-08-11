package com.stockflow.warehouse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Warehouse createWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
    public Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Warehouse updateWarehouse(Long id, Warehouse updatedWarehouse) {
        Warehouse warehouse = getWarehouse(id);
        warehouse.setName(updatedWarehouse.getName());
        warehouse.setLocation(updatedWarehouse.getLocation());
        return warehouseRepository.save(warehouse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Warehouse not found");
        }
        warehouseRepository.deleteById(id);
    }
}
