package com.plywood.service;

import com.plywood.model.Supplier;
import com.plywood.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Supplier Service - Handles supplier operations
 */
@Service
@Transactional
public class SupplierService {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    // Create and Update Operations
    public Supplier createSupplier(Supplier supplier) {
        logger.info("Creating supplier: {}", supplier.getName());
        return supplierRepository.save(supplier);
    }
    
    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
        
        supplier.setId(id);
        supplier.setCreatedDate(existing.getCreatedDate());
        logger.info("Updating supplier: {} (ID: {})", supplier.getName(), id);
        return supplierRepository.save(supplier);
    }
    
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
        logger.info("Deleted supplier ID: {}", id);
    }
    
    // Read Operations
    public Supplier getSupplier(Long id) {
        return supplierRepository.findById(id).orElse(null);
    }
    
    public Supplier getSupplierByCode(String supplierCode) {
        return supplierRepository.findBySupplierCode(supplierCode).orElse(null);
    }
    
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByActive(true);
    }
    
    public List<Supplier> searchSuppliers(String searchTerm) {
        return supplierRepository.findByNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            searchTerm, searchTerm);
    }
    
    public List<Supplier> getSuppliersExceedingCreditLimit() {
        return supplierRepository.findSuppliersExceedingCreditLimit();
    }
    
    public List<Supplier> getSuppliersWithOutstanding() {
        return supplierRepository.findSuppliersWithOutstanding();
    }
    
    // Financial Operations
    public void updateOutstandingBalance(Long supplierId, double amount) {
        Supplier supplier = getSupplier(supplierId);
        if (supplier != null) {
            supplier.setOutstandingBalance(supplier.getOutstandingBalance() + amount);
            supplierRepository.save(supplier);
            logger.info("Updated outstanding balance for supplier {}: {}", 
                       supplierId, supplier.getOutstandingBalance());
        }
    }
    
    public void clearOutstanding(Long supplierId, double amount) {
        Supplier supplier = getSupplier(supplierId);
        if (supplier != null) {
            supplier.setOutstandingBalance(Math.max(0, supplier.getOutstandingBalance() - amount));
            supplierRepository.save(supplier);
            logger.info("Cleared outstanding for supplier {}: Remaining {}", 
                       supplierId, supplier.getOutstandingBalance());
        }
    }
    
    // Statistics
    public long getActiveSupplierCount() {
        return supplierRepository.countActiveSuppliers();
    }
    
    public double getTotalOutstanding() {
        Double total = supplierRepository.getTotalOutstanding();
        return total != null ? total : 0.0;
    }
    
    // Validation
    public boolean isSupplierCodeUnique(String supplierCode) {
        return supplierRepository.findBySupplierCode(supplierCode).isEmpty();
    }
    
    public boolean isSupplierCodeUnique(String supplierCode, Long excludeId) {
        return supplierRepository.findBySupplierCode(supplierCode)
            .map(s -> s.getId().equals(excludeId))
            .orElse(true);
    }
}