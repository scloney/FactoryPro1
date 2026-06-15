package com.plywood.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String billNumber;

    private LocalDate date;
    private LocalDate dueDate;

    // Customer
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private String customerGSTIN;

    // Line items
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BillItem> items = new ArrayList<>();

    private double taxRate  = 18.0;
    private double discount = 0.0;

    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Company
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String companyGSTIN;
    private String bankDetails;

    // Quotation traceability
    private String sourceQuotationNumber;

    // Lifecycle
    private String status = "DRAFT";   // DRAFT | PAID | OVERDUE

    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(length = 50)
    private String createdBy;

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    private java.time.LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
        if (this.createdDate == null) {
            this.createdDate = LocalDate.now();
        }
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                this.createdBy = auth.getName();
            } else {
                this.createdBy = "SYSTEM";
            }
        } catch (Exception e) {
            this.createdBy = "SYSTEM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters for audit fields
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }


    // ===== Constructors =====

    public Bill() {
        this.items       = new ArrayList<>();
        this.date        = LocalDate.now();
        this.dueDate     = LocalDate.now().plusDays(30);
        this.taxRate     = 18.0;
        this.discount    = 0.0;
        this.status      = "DRAFT";
        this.createdDate = LocalDate.now();
    }

    // ===== Business logic =====

    public double getSubTotal() {
        if (items == null || items.isEmpty()) return 0.0;
        return items.stream().mapToDouble(BillItem::getTotal).sum();
    }

    public double getTaxAmount() {
        return getSubTotal() * (taxRate / 100);
    }

    public double getDiscountAmount() {
        return getSubTotal() * (discount / 100);
    }

    public double getGrandTotal() {
        return getSubTotal() + getTaxAmount() - getDiscountAmount();
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerGSTIN() { return customerGSTIN; }
    public void setCustomerGSTIN(String customerGSTIN) { this.customerGSTIN = customerGSTIN; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getCompanyGSTIN() { return companyGSTIN; }
    public void setCompanyGSTIN(String companyGSTIN) { this.companyGSTIN = companyGSTIN; }

    public String getBankDetails() { return bankDetails; }
    public void setBankDetails(String bankDetails) { this.bankDetails = bankDetails; }

    public String getSourceQuotationNumber() { return sourceQuotationNumber; }
    public void setSourceQuotationNumber(String sourceQuotationNumber) { this.sourceQuotationNumber = sourceQuotationNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return "Bill{billNumber='" + billNumber + "', customer='" + customerName +
               "', status='" + status + "', grandTotal=" + getGrandTotal() + '}';
    }
}