package com.plywood.repository;

import com.plywood.model.SalesOrder;
import com.plywood.model.SalesOrderStatus;
import com.plywood.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    
    // Find by order number
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    
    // Find by customer
    List<SalesOrder> findByCustomerId(Long customerId);
    List<SalesOrder> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    
    // Find by quotation
    Optional<SalesOrder> findByQuotationId(Long quotationId);
    
    // Find by status
    List<SalesOrder> findByStatus(SalesOrderStatus status);
    List<SalesOrder> findByStatusOrderByOrderDateDesc(SalesOrderStatus status);
    
    // Find by payment status
    List<SalesOrder> findByPaymentStatus(PaymentStatus paymentStatus);
    
    // Find pending orders (not delivered or cancelled)
    @Query("SELECT s FROM SalesOrder s WHERE s.status IN ('PENDING', 'IN_PRODUCTION', 'READY') ORDER BY s.orderDate DESC")
    List<SalesOrder> findPendingOrders();
    
    // Find orders by date range
    List<SalesOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find overdue orders (expected delivery date passed but not delivered)
    @Query("SELECT s FROM SalesOrder s WHERE s.expectedDeliveryDate < ?1 AND s.status != 'DELIVERED' AND s.status != 'CANCELLED'")
    List<SalesOrder> findOverdueOrders(LocalDate currentDate);
    
    // Count by status
    long countByStatus(SalesOrderStatus status);
    
    // Get total revenue (delivered and paid orders)
    @Query("SELECT SUM(s.totalAmount) FROM SalesOrder s WHERE s.status = 'DELIVERED' AND s.paymentStatus = 'PAID'")
    Double getTotalRevenue();
    
    // Get pending payment amount
    @Query("SELECT SUM(s.totalAmount - COALESCE(s.paidAmount, 0)) FROM SalesOrder s WHERE s.paymentStatus IN ('UNPAID', 'PARTIAL')")
    Double getPendingPaymentAmount();
    
    // Recent orders
    List<SalesOrder> findTop10ByOrderByOrderDateDesc();
}