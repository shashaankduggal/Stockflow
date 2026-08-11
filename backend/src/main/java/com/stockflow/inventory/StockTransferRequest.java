package com.stockflow.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockTransferRequest {

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Source warehouse is required")
    private Long fromWarehouseId;

    @NotNull(message = "Destination warehouse is required")
    private Long toWarehouseId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    private String remarks;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(Long fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public Long getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(Long toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
