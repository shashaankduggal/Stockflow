package com.stockflow.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {

    Optional<WarehouseStock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<WarehouseStock> findAllByOrderByWarehouseNameAscProductNameAsc();

    @Query("""
            select ws.product.id, coalesce(sum(ws.quantity), 0)
            from WarehouseStock ws
            group by ws.product.id
            """)
    List<Object[]> findTotalQuantitiesByProductId();
}
