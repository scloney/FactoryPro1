package com.plywood.repository;

import com.plywood.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Supplier Repository
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findBySupplierCode(String supplierCode);
    
    List<Supplier> findByActive(boolean active);
    
    List<Supplier> findByNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String name, String companyName);
    
    @Query("SELECT s FROM Supplier s WHERE s.active = true ORDER BY s.name")
    List<Supplier> findAllActiveSuppliers();
    
    @Query("SELECT s FROM Supplier s WHERE s.outstandingBalance > s.creditLimit")
    List<Supplier> findSuppliersExceedingCreditLimit();
    
    @Query("SELECT s FROM Supplier s WHERE s.outstandingBalance > 0 ORDER BY s.outstandingBalance DESC")
    List<Supplier> findSuppliersWithOutstanding();
    
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.active = true")
    long countActiveSuppliers();
    
    @Query("SELECT SUM(s.outstandingBalance) FROM Supplier s WHERE s.active = true")
    Double getTotalOutstanding();
}