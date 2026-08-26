package com.stockflow.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    long countByType(InventoryType type);

    long countByTypeIn(Collection<InventoryType> types);

    List<Inventory> findTop10ByOrderByCreatedAtDesc();

}
