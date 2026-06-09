package com.plywood.repository;

import com.plywood.model.Quotation;
import com.plywood.model.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    List<Quotation> findByCustomerId(Long customerId);

    List<Quotation> findByStatus(QuotationStatus status);

    List<Quotation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT q FROM Quotation q WHERE q.convertedToOrder = false AND q.status = 'SENT'")
    List<Quotation> findPendingQuotations();

    @Query("SELECT q FROM Quotation q WHERE q.validUntil < :today AND q.status != 'EXPIRED' AND q.status != 'CONVERTED'")
    List<Quotation> findExpiredQuotations(LocalDate today);

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.status = :status")
    Long countByStatus(QuotationStatus status);

    // Bug fix: was summing q.taxRate (a %) instead of actual item value
    @Query("SELECT COALESCE(SUM(i.quantity * i.unitPrice), 0.0) FROM Quotation q JOIN q.items i WHERE q.status = 'ACCEPTED'")
    Double getTotalAcceptedValue();

    // Customer portal: get all quotations by customer phone
    List<Quotation> findByCustomerPhoneOrderByCreatedDateDesc(String customerPhone);
}