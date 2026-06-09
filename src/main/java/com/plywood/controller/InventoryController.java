package com.plywood.controller;

import com.plywood.model.Product;
import com.plywood.model.StockMovement;
import com.plywood.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventory Controller - Spring Boot 3.x Compatible
 * Handles HTTP requests for inventory management
 */
@Controller
@RequestMapping("/inventory")
public class InventoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    @Autowired
    private InventoryService inventoryService;
    
    // View Pages
    @GetMapping
    public String showInventory() {
        return "inventory";
    }
    
    @GetMapping("/products")
    public String showProducts() {
        return "inventory-products";
    }
    
    @GetMapping("/movements")
    public String showMovements() {
        return "inventory-movements";
    }
    
    @GetMapping("/reports")
    public String showReports() {
        return "inventory-reports";
    }
    
    // Product API Endpoints
    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product created = inventoryService.createProduct(product);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = inventoryService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating product", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            inventoryService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error deleting product", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = inventoryService.getProduct(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }
    
    @GetMapping("/api/products/active")
    @ResponseBody
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(inventoryService.getActiveProducts());
    }
    
    @GetMapping("/api/products/low-stock")
    @ResponseBody
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }
    
    @GetMapping("/api/products/category/{category}")
    @ResponseBody
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(inventoryService.getProductsByCategory(category));
    }
    
    // Stock Movement API Endpoints
    @PostMapping("/api/stock/add")
    @ResponseBody
    public ResponseEntity<?> addStock(@RequestBody StockMovementRequest request) {
        try {
            StockMovement movement = inventoryService.addStock(
                request.getProductId(),
                request.getQuantity(),
                request.getUnitPrice(),
                request.getTransactionType(),
                request.getReferenceNumber(),
                request.getPartyName(),
                request.getNotes()
            );
            return ResponseEntity.ok(movement);
        } catch (Exception e) {
            logger.error("Error adding stock", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/stock/remove")
    @ResponseBody
    public ResponseEntity<?> removeStock(@RequestBody StockMovementRequest request) {
        try {
            StockMovement movement = inventoryService.removeStock(
                request.getProductId(),
                request.getQuantity(),
                request.getUnitPrice(),
                request.getTransactionType(),
                request.getReferenceNumber(),
                request.getPartyName(),
                request.getNotes()
            );
            return ResponseEntity.ok(movement);
        } catch (Exception e) {
            logger.error("Error removing stock", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/stock/adjust")
    @ResponseBody
    public ResponseEntity<?> adjustStock(@RequestBody StockAdjustmentRequest request) {
        try {
            StockMovement movement = inventoryService.adjustStock(
                request.getProductId(),
                request.getNewQuantity(),
                request.getReason(),
                request.getNotes()
            );
            return ResponseEntity.ok(movement);
        } catch (Exception e) {
            logger.error("Error adjusting stock", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/movements")
    @ResponseBody
    public ResponseEntity<List<StockMovement>> getAllMovements() {
        return ResponseEntity.ok(inventoryService.getAllMovements());
    }
    
    @GetMapping("/api/movements/product/{productId}")
    @ResponseBody
    public ResponseEntity<List<StockMovement>> getProductMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductMovements(productId));
    }
    
    // Reports API Endpoints
    @GetMapping("/api/reports/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalProducts", inventoryService.getActiveProducts().size());
        dashboard.put("lowStockCount", inventoryService.getLowStockCount());
        dashboard.put("outOfStockCount", inventoryService.getOutOfStockCount());
        dashboard.put("totalInventoryValue", inventoryService.getTotalInventoryValue());
        dashboard.put("potentialRevenue", inventoryService.getTotalPotentialRevenue());
        dashboard.put("categoryValues", inventoryService.getInventoryValueByCategory());
        dashboard.put("lowStockProducts", inventoryService.getLowStockProducts());
        return ResponseEntity.ok(dashboard);
    }
    
    // Request DTOs
    public static class StockMovementRequest {
        private Long productId;
        private double quantity;
        private double unitPrice;
        private String transactionType;
        private String referenceNumber;
        private String partyName;
        private String notes;
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        
        public String getPartyName() { return partyName; }
        public void setPartyName(String partyName) { this.partyName = partyName; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class StockAdjustmentRequest {
        private Long productId;
        private double newQuantity;
        private String reason;
        private String notes;
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public double getNewQuantity() { return newQuantity; }
        public void setNewQuantity(double newQuantity) { this.newQuantity = newQuantity; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}