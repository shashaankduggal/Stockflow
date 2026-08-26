package com.stockflow.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.audit.AuditLog;
import com.stockflow.audit.AuditLogRepository;
import com.stockflow.audit.AuditLogService;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.warehouse.Warehouse;
import com.stockflow.warehouse.WarehouseRepository;
import com.stockflow.warehouse.WarehouseStock;
import com.stockflow.warehouse.WarehouseStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "staff@stockflow.com", roles = "STAFF")
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @SpyBean
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAllInBatch();
        auditLogRepository.deleteAllInBatch();
        warehouseStockRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        warehouseRepository.deleteAllInBatch();
        Mockito.reset(auditLogService);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(auditLogService);
    }

    @Test
    void stockInSuccess() throws Exception {
        Product product = createProduct("Widget", "W-001", "10.50");
        Warehouse warehouse = createWarehouse("Main", "Delhi");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "warehouseId", warehouse.getId(),
                                "quantity", 7,
                                "remarks", "Initial stock"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STOCK_IN"))
                .andExpect(jsonPath("$.quantity").value(7))
                .andExpect(jsonPath("$.warehouse.id").value(warehouse.getId()))
                .andExpect(jsonPath("$.product.id").value(product.getId()));

        assertThat(stockQuantity(product, warehouse)).isEqualTo(7);
        assertThat(inventoryRepository.count()).isEqualTo(1);
        AuditLog auditLog = auditLogRepository.findAll().getFirst();
        assertThat(auditLog.getUserName()).isEqualTo("staff@stockflow.com");
        assertThat(auditLog.getAction()).isEqualTo("STOCK_IN");
        assertThat(auditLog.getDetails()).contains("Widget", "Main", "7");
        assertThat(auditLog.getCreatedAt()).isNotNull();
    }

    @Test
    void stockOutSuccess() throws Exception {
        Product product = createProduct("Gadget", "G-001", "25.00");
        Warehouse warehouse = createWarehouse("Main", "Delhi");
        createStock(product, warehouse, 9);

        mockMvc.perform(post("/api/inventory/stock-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "warehouseId", warehouse.getId(),
                                "quantity", 4,
                                "remarks", "Dispatch"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STOCK_OUT"))
                .andExpect(jsonPath("$.quantity").value(4));

        assertThat(stockQuantity(product, warehouse)).isEqualTo(5);
        assertThat(inventoryRepository.count()).isEqualTo(1);
        assertThat(auditLogRepository.findAll().getFirst().getAction()).isEqualTo("STOCK_OUT");
    }

    @Test
    void insufficientStockReturnsBadRequest() throws Exception {
        Product product = createProduct("Cable", "C-001", "5.00");
        Warehouse warehouse = createWarehouse("Main", "Delhi");
        createStock(product, warehouse, 2);

        mockMvc.perform(post("/api/inventory/stock-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "warehouseId", warehouse.getId(),
                                "quantity", 3,
                                "remarks", "Too much"))))
                .andExpect(status().isBadRequest());

        assertThat(stockQuantity(product, warehouse)).isEqualTo(2);
        assertThat(inventoryRepository.count()).isZero();
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    void transferBetweenWarehouses() throws Exception {
        Product product = createProduct("Panel", "P-001", "99.99");
        Warehouse source = createWarehouse("North", "Delhi");
        Warehouse destination = createWarehouse("South", "Mumbai");
        createStock(product, source, 11);
        createStock(product, destination, 1);

        mockMvc.perform(post("/api/inventory/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "fromWarehouseId", source.getId(),
                                "toWarehouseId", destination.getId(),
                                "quantity", 6,
                                "remarks", "Rebalance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$[1].type").value("TRANSFER_IN"));

        assertThat(stockQuantity(product, source)).isEqualTo(5);
        assertThat(stockQuantity(product, destination)).isEqualTo(7);
        assertThat(inventoryRepository.findAll().stream().map(Inventory::getType))
                .containsExactlyInAnyOrder(InventoryType.TRANSFER_OUT, InventoryType.TRANSFER_IN);
        assertThat(auditLogRepository.findAll().getFirst().getAction()).isEqualTo("TRANSFER");
    }

    @Test
    void sameWarehouseTransferRejected() throws Exception {
        Product product = createProduct("Router", "R-001", "199.99");
        Warehouse warehouse = createWarehouse("Main", "Delhi");
        createStock(product, warehouse, 8);

        mockMvc.perform(post("/api/inventory/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "fromWarehouseId", warehouse.getId(),
                                "toWarehouseId", warehouse.getId(),
                                "quantity", 2,
                                "remarks", "Invalid"))))
                .andExpect(status().isBadRequest());

        assertThat(stockQuantity(product, warehouse)).isEqualTo(8);
        assertThat(inventoryRepository.count()).isZero();
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    void missingProductAndWarehouseReturnNotFound() throws Exception {
        Product product = createProduct("Mouse", "M-001", "15.00");
        Warehouse warehouse = createWarehouse("Main", "Delhi");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", 999999L,
                                "warehouseId", warehouse.getId(),
                                "quantity", 1,
                                "remarks", "Missing product"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/inventory/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "warehouseId", 999999L,
                                "quantity", 1,
                                "remarks", "Missing warehouse"))))
                .andExpect(status().isNotFound());

        assertThat(inventoryRepository.count()).isZero();
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    void transactionRollbackPreservesDataIntegrity() throws Exception {
        Product product = createProduct("Switch", "S-001", "49.99");
        Warehouse source = createWarehouse("North", "Delhi");
        Warehouse destination = createWarehouse("South", "Mumbai");
        createStock(product, source, 9);
        createStock(product, destination, 4);

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .logInventoryAction(eq("TRANSFER"), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> mockMvc.perform(post("/api/inventory/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "productId", product.getId(),
                                "fromWarehouseId", source.getId(),
                                "toWarehouseId", destination.getId(),
                                "quantity", 3,
                                "remarks", "Rollback")))))
                .hasCauseInstanceOf(RuntimeException.class);

        assertThat(stockQuantity(product, source)).isEqualTo(9);
        assertThat(stockQuantity(product, destination)).isEqualTo(4);
        assertThat(inventoryRepository.count()).isZero();
        assertThat(auditLogRepository.count()).isZero();
    }

    private Product createProduct(String name, String sku, String price) {
        return productRepository.save(new Product(null, name, sku, new BigDecimal(price)));
    }

    private Warehouse createWarehouse(String name, String location) {
        return warehouseRepository.save(new Warehouse(null, name, location));
    }

    private void createStock(Product product, Warehouse warehouse, int quantity) {
        warehouseStockRepository.save(new WarehouseStock(product, warehouse, quantity));
    }

    private int stockQuantity(Product product, Warehouse warehouse) {
        return warehouseStockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .map(WarehouseStock::getQuantity)
                .orElse(0);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
