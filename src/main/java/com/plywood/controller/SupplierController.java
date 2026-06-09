package com.plywood.controller;

import com.plywood.model.Supplier;
import com.plywood.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Supplier Controller - Handles supplier management requests
 */
@Controller
@RequestMapping("/suppliers")
public class SupplierController {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    
    @Autowired
    private SupplierService supplierService;
    
    // View Pages
    @GetMapping
    public String showSuppliers() {
        return "suppliers";
    }
    
    // API Endpoints
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createSupplier(@RequestBody Supplier supplier) {
        try {
            // Validate unique supplier code
            if (!supplierService.isSupplierCodeUnique(supplier.getSupplierCode())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Supplier code already exists"));
            }
            
            Supplier created = supplierService.createSupplier(supplier);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating supplier", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        try {
            // Validate unique supplier code (excluding current supplier)
            if (!supplierService.isSupplierCodeUnique(supplier.getSupplierCode(), id)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Supplier code already exists"));
            }
            
            Supplier updated = supplierService.updateSupplier(id, supplier);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating supplier", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error deleting supplier", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplier(id);
        if (supplier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(supplier);
    }
    
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }
    
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }
    
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Supplier>> searchSuppliers(@RequestParam String query) {
        return ResponseEntity.ok(supplierService.searchSuppliers(query));
    }
    
    @GetMapping("/api/exceeding-credit")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getSuppliersExceedingCreditLimit() {
        return ResponseEntity.ok(supplierService.getSuppliersExceedingCreditLimit());
    }
    
    @GetMapping("/api/with-outstanding")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getSuppliersWithOutstanding() {
        return ResponseEntity.ok(supplierService.getSuppliersWithOutstanding());
    }
    
    // Financial Operations
    @PostMapping("/api/{id}/update-outstanding")
    @ResponseBody
    public ResponseEntity<?> updateOutstanding(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            if (amount == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            supplierService.updateOutstandingBalance(id, amount);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error updating outstanding", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/clear-outstanding")
    @ResponseBody
    public ResponseEntity<?> clearOutstanding(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            if (amount == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            supplierService.clearOutstanding(id, amount);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error clearing outstanding", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Statistics
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
            "totalSuppliers", supplierService.getAllSuppliers().size(),
            "activeSuppliers", supplierService.getActiveSupplierCount(),
            "totalOutstanding", supplierService.getTotalOutstanding(),
            "exceedingCreditLimit", supplierService.getSuppliersExceedingCreditLimit().size()
        );
        return ResponseEntity.ok(stats);
    }
}