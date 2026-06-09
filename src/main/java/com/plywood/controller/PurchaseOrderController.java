package com.plywood.controller;

import com.plywood.model.PurchaseOrder;
import com.plywood.model.PurchaseOrderItem;
import com.plywood.service.PurchaseOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Purchase Order Controller - Handles purchase order management requests
 */
@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    // View Pages
    @GetMapping
    public String showPurchaseOrders() {
        return "purchase-orders";
    }
    
    // API Endpoints - CRUD
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        try {
            // Validate unique PO number
            if (!purchaseOrderService.isPoNumberUnique(purchaseOrder.getPoNumber())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "PO number already exists"));
            }
            
            PurchaseOrder created = purchaseOrderService.createPurchaseOrder(purchaseOrder);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePurchaseOrder(@PathVariable Long id, 
                                                  @RequestBody PurchaseOrder purchaseOrder) {
        try {
            // Validate unique PO number (excluding current PO)
            if (!purchaseOrderService.isPoNumberUnique(purchaseOrder.getPoNumber(), id)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "PO number already exists"));
            }
            
            PurchaseOrder updated = purchaseOrderService.updatePurchaseOrder(id, purchaseOrder);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error deleting purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPurchaseOrder(@PathVariable Long id) {
        PurchaseOrder po = purchaseOrderService.getPurchaseOrder(id);
        if (po == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(po);
    }
    
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }
    
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getRecentPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getRecentPurchaseOrders());
    }
    
    @GetMapping("/api/supplier/{supplierId}")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrdersBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersBySupplier(supplierId));
    }
    
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersByStatus(status));
    }
    
    @GetMapping("/api/overdue")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getOverduePurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getOverduePurchaseOrders());
    }
    
    @GetMapping("/api/date-range")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersByDateRange(startDate, endDate));
    }
    
    // Status Management
    @PostMapping("/api/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approvePurchaseOrder(@PathVariable Long id, 
                                                   @RequestBody Map<String, String> request) {
        try {
            String approvedBy = request.get("approvedBy");
            if (approvedBy == null || approvedBy.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Approver name is required"));
            }
            PurchaseOrder approved = purchaseOrderService.approvePurchaseOrder(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            logger.error("Error approving purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/mark-ordered")
    @ResponseBody
    public ResponseEntity<?> markAsOrdered(@PathVariable Long id) {
        try {
            PurchaseOrder ordered = purchaseOrderService.markAsOrdered(id);
            return ResponseEntity.ok(ordered);
        } catch (Exception e) {
            logger.error("Error marking purchase order as ordered", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/receive")
    @ResponseBody
    public ResponseEntity<?> receivePurchaseOrder(@PathVariable Long id, 
                                                   @RequestBody Map<Long, Double> receivedQuantities) {
        try {
            PurchaseOrder received = purchaseOrderService.receivePurchaseOrder(id, receivedQuantities);
            return ResponseEntity.ok(received);
        } catch (Exception e) {
            logger.error("Error receiving purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelPurchaseOrder(@PathVariable Long id, 
                                                  @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cancellation reason is required"));
            }
            PurchaseOrder cancelled = purchaseOrderService.cancelPurchaseOrder(id, reason);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            logger.error("Error cancelling purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Item Management
    @PostMapping("/api/{id}/items")
    @ResponseBody
    public ResponseEntity<?> addItem(@PathVariable Long id, @RequestBody PurchaseOrderItem item) {
        try {
            PurchaseOrder updated = purchaseOrderService.addItem(id, item);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error adding item to purchase order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Statistics and Reports
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderStats());
    }
    
    @GetMapping("/api/total-amount")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTotalPurchaseAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        double total = purchaseOrderService.getTotalPurchaseAmount(startDate, endDate);
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }
}