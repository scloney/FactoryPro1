package com.plywood.repository;

import com.plywood.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Find by customer name
    Optional<Customer> findByCustomerName(String customerName);
    
    // Find by phone
    Optional<Customer> findByPhone(String phone);
    
    // Find by GSTIN
    Optional<Customer> findByGstin(String gstin);
    
    // Find all active customers
    List<Customer> findByActiveTrue();
    
    // Find all inactive customers
    List<Customer> findByActiveFalse();
    
    // Find by customer type
    List<Customer> findByCustomerType(String customerType);
    
    // Find active customers by type
    List<Customer> findByActiveTrueAndCustomerType(String customerType);
    
    // Find by city
    List<Customer> findByCity(String city);
    
    // Find by state
    List<Customer> findByState(String state);
    
    // Search customers by name or phone
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);
    
    // Find customers with outstanding balance
    @Query("SELECT c FROM Customer c WHERE c.outstandingBalance > 0 AND c.active = true")
    List<Customer> findCustomersWithOutstanding();
    
    // Find top customers by total purchase value
    @Query("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.totalPurchaseValue DESC")
    List<Customer> findTopCustomersByPurchaseValue();
    
    // Get total active customers count
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true")
    Long countActiveCustomers();
    
    // Get total outstanding amount
    @Query("SELECT COALESCE(SUM(c.outstandingBalance), 0.0) FROM Customer c WHERE c.active = true")
    Double getTotalOutstandingAmount();
    
    // Get total purchase value
    @Query("SELECT COALESCE(SUM(c.totalPurchaseValue), 0.0) FROM Customer c WHERE c.active = true")
    Double getTotalPurchaseValue();
    
    // Find customers with no orders
    @Query("SELECT c FROM Customer c WHERE c.totalOrders = 0 AND c.active = true")
    List<Customer> findCustomersWithNoOrders();
    
    // Find customers by outstanding range
    @Query("SELECT c FROM Customer c WHERE c.outstandingBalance BETWEEN :minAmount AND :maxAmount")
    List<Customer> findByOutstandingRange(@Param("minAmount") Double minAmount, 
                                          @Param("maxAmount") Double maxAmount);
}