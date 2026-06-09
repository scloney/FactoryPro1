package com.plywood.service;

import com.plywood.model.Quotation;
import com.plywood.model.QuotationItem;
import com.plywood.model.QuotationStatus;
import com.plywood.model.Customer;
import com.plywood.repository.QuotationRepository;
import com.plywood.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuotationService {
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private QuotationPdfService pdfService;
    
    // Create new quotation
    public Quotation createQuotation(Quotation quotation) {
        quotation.setCreatedDate(LocalDate.now());
        quotation.setStatus(QuotationStatus.DRAFT);
        
        // Link items to quotation
        if (quotation.getItems() != null) {
            for (QuotationItem item : quotation.getItems()) {
                item.setQuotation(quotation);
                // Calculate sqft and price if dimensions are provided
                if (item.getLength() != null && item.getWidth() != null) {
                    item.calculateSqft();
                    if (item.getRatePerSqft() != null) {
                        item.calculateUnitPrice();
                    }
                }
            }
        }
        
        return quotationRepository.save(quotation);
    }
    
    // Update quotation
    public Quotation updateQuotation(Long id, Quotation quotationDetails) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        
        quotation.setQuotationNumber(quotationDetails.getQuotationNumber());
        quotation.setDate(quotationDetails.getDate());
        quotation.setValidUntil(quotationDetails.getValidUntil());
        quotation.setCustomer(quotationDetails.getCustomer());
        quotation.setCustomerName(quotationDetails.getCustomerName());
        quotation.setCustomerAddress(quotationDetails.getCustomerAddress());
        quotation.setCustomerPhone(quotationDetails.getCustomerPhone());
        quotation.setCustomerEmail(quotationDetails.getCustomerEmail());
        quotation.setCompanyName(quotationDetails.getCompanyName());
        quotation.setCompanyAddress(quotationDetails.getCompanyAddress());
        quotation.setCompanyPhone(quotationDetails.getCompanyPhone());
        quotation.setCompanyEmail(quotationDetails.getCompanyEmail());
        quotation.setTaxRate(quotationDetails.getTaxRate());
        quotation.setDiscount(quotationDetails.getDiscount());
        quotation.setNotes(quotationDetails.getNotes());
        quotation.setModifiedDate(LocalDate.now());
        
        // Update items
        quotation.getItems().clear();
        if (quotationDetails.getItems() != null) {
            for (QuotationItem item : quotationDetails.getItems()) {
                item.setQuotation(quotation);
                if (item.getLength() != null && item.getWidth() != null) {
                    item.calculateSqft();
                    if (item.getRatePerSqft() != null) {
                        item.calculateUnitPrice();
                    }
                }
                quotation.getItems().add(item);
            }
        }
        
        return quotationRepository.save(quotation);
    }
    
    // Get all quotations
    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }
    
    // Get quotation by id
    public Optional<Quotation> getQuotationById(Long id) {
        return quotationRepository.findById(id);
    }
    
    // Get quotation by number
    public Optional<Quotation> getQuotationByNumber(String quotationNumber) {
        return quotationRepository.findByQuotationNumber(quotationNumber);
    }
    
    // Get quotations by customer
    public List<Quotation> getQuotationsByCustomer(Long customerId) {
        return quotationRepository.findByCustomerId(customerId);
    }
    
    // Get quotations by status
    public List<Quotation> getQuotationsByStatus(QuotationStatus status) {
        return quotationRepository.findByStatus(status);
    }
    
    // Get pending quotations
    public List<Quotation> getPendingQuotations() {
        return quotationRepository.findPendingQuotations();
    }
    
    // Delete quotation
    public void deleteQuotation(Long id) {
        quotationRepository.deleteById(id);
    }
    
    // Send quotation to customer
    public Quotation sendQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        quotation.setStatus(QuotationStatus.SENT);
        quotation.setModifiedDate(LocalDate.now());
        return quotationRepository.save(quotation);
    }
    
    // Accept quotation
    public Quotation acceptQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotation.setModifiedDate(LocalDate.now());
        return quotationRepository.save(quotation);
    }
    
    // Reject quotation
    public Quotation rejectQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        quotation.setStatus(QuotationStatus.REJECTED);
        quotation.setModifiedDate(LocalDate.now());
        return quotationRepository.save(quotation);
    }
    
    // Convert to sales order
    // NOTE: The actual sales order creation is handled by SalesOrderService.createFromQuotation().
    // This method is kept for the /api/quotations/{id}/convert endpoint but delegates properly.
    public Quotation convertToSalesOrder(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new RuntimeException("Only accepted quotations can be converted to sales orders");
        }
        
        if (Boolean.TRUE.equals(quotation.getConvertedToOrder())) {
            throw new RuntimeException("Quotation has already been converted to a sales order");
        }
        
        // Direct callers to use the correct endpoint via SalesOrderService
        throw new RuntimeException(
            "Use POST /api/sales-orders/from-quotation/" + id + " to create a sales order from this quotation"
        );
    }
    
    // Generate PDF
    public byte[] generatePdf(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        
        try {
            return pdfService.generatePdf(quotation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
    
    // Generate PDF from quotation object (without saving)
    public byte[] generatePdfFromData(Quotation quotation) {
        try {
            // Calculate sqft and prices for items
            if (quotation.getItems() != null) {
                for (QuotationItem item : quotation.getItems()) {
                    if (item.getLength() != null && item.getWidth() != null) {
                        item.calculateSqft();
                        if (item.getRatePerSqft() != null) {
                            item.calculateUnitPrice();
                        }
                    }
                }
            }
            return pdfService.generatePdf(quotation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
    
    // Get statistics
    public QuotationStats getStatistics() {
        Long totalQuotations = quotationRepository.count();
        Long draftQuotations = quotationRepository.countByStatus(QuotationStatus.DRAFT);
        Long sentQuotations = quotationRepository.countByStatus(QuotationStatus.SENT);
        Long acceptedQuotations = quotationRepository.countByStatus(QuotationStatus.ACCEPTED);
        Double totalAcceptedValue = quotationRepository.getTotalAcceptedValue();
        
        return new QuotationStats(totalQuotations, draftQuotations, sentQuotations, 
                                 acceptedQuotations, totalAcceptedValue);
    }
    
    // Mark expired quotations
    public void markExpiredQuotations() {
        List<Quotation> expiredQuotations = quotationRepository.findExpiredQuotations(LocalDate.now());
        for (Quotation quotation : expiredQuotations) {
            quotation.setStatus(QuotationStatus.EXPIRED);
            quotationRepository.save(quotation);
        }
    }
    
    // Inner class for statistics
    public static class QuotationStats {
        private Long totalQuotations;
        private Long draftQuotations;
        private Long sentQuotations;
        private Long acceptedQuotations;
        private Double totalAcceptedValue;
        
        public QuotationStats(Long totalQuotations, Long draftQuotations, 
                            Long sentQuotations, Long acceptedQuotations, 
                            Double totalAcceptedValue) {
            this.totalQuotations = totalQuotations;
            this.draftQuotations = draftQuotations;
            this.sentQuotations = sentQuotations;
            this.acceptedQuotations = acceptedQuotations;
            this.totalAcceptedValue = totalAcceptedValue;
        }
        
        // Getters
        public Long getTotalQuotations() { return totalQuotations; }
        public Long getDraftQuotations() { return draftQuotations; }
        public Long getSentQuotations() { return sentQuotations; }
        public Long getAcceptedQuotations() { return acceptedQuotations; }
        public Double getTotalAcceptedValue() { return totalAcceptedValue; }
    }
}