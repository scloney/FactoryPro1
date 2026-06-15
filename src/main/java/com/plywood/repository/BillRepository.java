package com.plywood.repository;

import com.plywood.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);

    Optional<Bill> findBySourceQuotationNumber(String sourceQuotationNumber);

    List<Bill> findByStatus(String status);

    List<Bill> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Bill> findAllByOrderByCreatedDateDesc();

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    Long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(i.quantity * i.unitPrice), 0.0) FROM Bill b JOIN b.items i WHERE b.status = 'PAID'")
    Double getTotalPaidRevenue();

    @Query("SELECT COALESCE(SUM(i.quantity * i.unitPrice), 0.0) FROM Bill b JOIN b.items i")
    Double getTotalRevenue();

    // ── Dashboard: bills created within a date range ─────────────────────
    List<Bill> findByCreatedDateBetweenOrderByCreatedDateDesc(LocalDate start, LocalDate end);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.createdDate >= :start AND b.createdDate <= :end")
    Long countByCreatedDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Customer portal: get all bills by customer phone
    List<Bill> findByCustomerPhoneOrderByCreatedDateDesc(String customerPhone);

    @Query("SELECT i.description, SUM(i.quantity) FROM Bill b JOIN b.items i GROUP BY i.description ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
