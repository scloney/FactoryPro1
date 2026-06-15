package com.plywood.repository;

import com.plywood.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Purchase Order Repository
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    
    List<PurchaseOrder> findByStatus(String status);
    
    List<PurchaseOrder> findBySupplierIdAndStatus(Long supplierId, String status);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = :status ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByStatusOrderByPoDateDesc(@Param("status") String status);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.poDate BETWEEN :startDate AND :endDate ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByPoDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDeliveryDate < :today AND po.status NOT IN ('RECEIVED', 'CANCELLED') ORDER BY po.expectedDeliveryDate")
    List<PurchaseOrder> findOverduePurchaseOrders(@Param("today") LocalDate today);
    
    @Query("SELECT po FROM PurchaseOrder po ORDER BY po.poDate DESC")
    List<PurchaseOrder> findTop50ByOrderByPoDateDesc();
    
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(po.grandTotal) FROM PurchaseOrder po WHERE po.status = :status")
    Double getTotalAmountByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(po.grandTotal) FROM PurchaseOrder po WHERE po.poDate BETWEEN :startDate AND :endDate")
    Double getTotalPurchaseAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find POs that contain a line item for the given product, most recent first.
    // Used by Inventory > Add Stock to let the user pick the relevant PO for a product.
    @Query("SELECT DISTINCT po FROM PurchaseOrder po JOIN po.items i " +
           "WHERE i.productId = :productId " +
           "ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByProductId(@Param("productId") Long productId);
}