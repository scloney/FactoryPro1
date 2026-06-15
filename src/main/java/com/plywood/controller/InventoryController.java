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
    public String showReports(org.springframework.ui.Model model) {
        // ── Stock valuation (all active products) ────────────────────────
        List<Product> allActive = inventoryService.getActiveProducts();
        double totalStockValue = allActive.stream()
            .mapToDouble(p -> p.getCurrentStock() * p.getCostPrice()).sum();
        double totalSellingValue = allActive.stream()
            .mapToDouble(p -> p.getCurrentStock() * p.getSellingPrice()).sum();
        model.addAttribute("allProducts",       allActive);
        model.addAttribute("totalStockValue",   totalStockValue);
        model.addAttribute("totalSellingValue", totalSellingValue);
        model.addAttribute("totalProducts",     allActive.size());

        // ── Category breakdown ───────────────────────────────────────────
        model.addAttribute("categoryValues", inventoryService.getInventoryValueByCategory());

        // ── Low-stock & reorder alerts ───────────────────────────────────
        model.addAttribute("lowStockProducts",   inventoryService.getLowStockProducts());
        model.addAttribute("lowStockCount",      inventoryService.getLowStockCount());
        model.addAttribute("outOfStockCount",    inventoryService.getOutOfStockCount());

        // ── Monthly revenue (last 6 months from bills) ───────────────────
        com.plywood.repository.BillRepository billRepo =
            inventoryService.getBillRepository();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter mFmt =
            java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        java.util.List<String> monthLabels  = new java.util.ArrayList<>();
        java.util.List<Double> monthRevenue = new java.util.ArrayList<>();
        java.util.List<Long>   monthBills   = new java.util.ArrayList<>();
        java.util.List<com.plywood.model.Bill> allBills =
            billRepo.findAllByOrderByCreatedDateDesc();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate m  = today.minusMonths(i);
            java.time.LocalDate mS = m.withDayOfMonth(1);
            java.time.LocalDate mE = m.withDayOfMonth(m.lengthOfMonth());
            monthLabels.add(m.format(mFmt));
            double rev = allBills.stream()
                .filter(b -> b.getCreatedDate() != null
                          && !b.getCreatedDate().isBefore(mS)
                          && !b.getCreatedDate().isAfter(mE))
                .mapToDouble(com.plywood.model.Bill::getGrandTotal).sum();
            long cnt = allBills.stream()
                .filter(b -> b.getCreatedDate() != null
                          && !b.getCreatedDate().isBefore(mS)
                          && !b.getCreatedDate().isAfter(mE))
                .count();
            monthRevenue.add(rev);
            monthBills.add(cnt);
        }
        model.addAttribute("monthLabels",  monthLabels);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("monthBills",   monthBills);

        // ── Top products by stock value (top 10) ─────────────────────────
        java.util.List<Product> topByValue = allActive.stream()
            .sorted(java.util.Comparator.comparingDouble(
                (Product p) -> p.getCurrentStock() * p.getCostPrice()).reversed())
            .limit(10)
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("topByStockValue", topByValue);

        // ── Top selling products (by quantity sold in bills) ─────────────
        java.util.List<Object[]> topSelling = billRepo.findTopSellingProducts(org.springframework.data.domain.PageRequest.of(0, 8));
        model.addAttribute("topSellingProducts", topSelling);

        model.addAttribute("currentMonth",
            today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));

        return "inventory-reports";
    }

    @GetMapping("/export/csv")
    public void exportToCsv(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory.csv");
        
        List<Product> products = inventoryService.getActiveProducts();
        
        java.io.PrintWriter writer = response.getWriter();
        writer.println("Product Code,Product Name,Category,Grade,Thickness,Size,Current Stock,Unit,Cost Price,Selling Price,Stock Value");
        
        for (Product p : products) {
            writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%.2f,%.2f,%.2f",
                escapeCsv(p.getProductCode()),
                escapeCsv(p.getName()),
                escapeCsv(p.getCategory()),
                escapeCsv(p.getGrade()),
                escapeCsv(p.getThickness()),
                escapeCsv(p.getSize()),
                p.getCurrentStock(),
                escapeCsv(p.getUnit()),
                p.getCostPrice(),
                p.getSellingPrice(),
                p.getCurrentStock() * p.getCostPrice()
            ));
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
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
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("name").ascending());
            return ResponseEntity.ok(inventoryService.getAllProducts(pageable));
        }
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