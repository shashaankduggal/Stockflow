package com.stockflow.product;

import java.math.BigDecimal;

public class ProductResponse {

    private final Long id;
    private final String name;
    private final String sku;
    private final BigDecimal price;

    public ProductResponse(Long id, String name, String sku, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
