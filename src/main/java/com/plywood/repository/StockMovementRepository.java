package com.plywood.repository;

import com.plywood.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    // Find by product ID
    List<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId);
    
    // Find by movement type
    List<StockMovement> findByMovementTypeOrderByMovementDateDesc(String movementType);
    
    // Find by date range
    List<StockMovement> findByMovementDateBetweenOrderByMovementDateDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Find by product and date range
    List<StockMovement> findByProductIdAndMovementDateBetweenOrderByMovementDateDesc(
        Long productId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Find recent movements (top 50)
    List<StockMovement> findTop50ByOrderByMovementDateDesc();
    
    // Get total inward quantity for a product
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.productId = :productId AND sm.movementType = 'INWARD'")
    Double getTotalInwardQuantity(@Param("productId") Long productId);
    
    // Get total outward quantity for a product
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.productId = :productId AND sm.movementType = 'OUTWARD'")
    Double getTotalOutwardQuantity(@Param("productId") Long productId);
    
    // Get total value by movement type and date range
    @Query("SELECT SUM(sm.totalValue) FROM StockMovement sm WHERE sm.movementType = :movementType AND sm.movementDate BETWEEN :startDate AND :endDate")
    Double getTotalValueByTypeAndDateRange(
        @Param("movementType") String movementType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get movement statistics
    @Query("SELECT sm.movementType, COUNT(sm), SUM(sm.quantity), SUM(sm.totalValue) FROM StockMovement sm WHERE sm.movementDate BETWEEN :startDate AND :endDate GROUP BY sm.movementType")
    List<Object[]> getMovementStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find by reference number
    List<StockMovement> findByReferenceNumber(String referenceNumber);
    
    // Find by performed by
    List<StockMovement> findByPerformedByOrderByMovementDateDesc(String performedBy);
    
    // Get daily movement summary
    @Query("SELECT DATE(sm.movementDate), sm.movementType, SUM(sm.quantity), SUM(sm.totalValue) FROM StockMovement sm WHERE sm.movementDate BETWEEN :startDate AND :endDate GROUP BY DATE(sm.movementDate), sm.movementType ORDER BY DATE(sm.movementDate) DESC")
    List<Object[]> getDailyMovementSummary(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}