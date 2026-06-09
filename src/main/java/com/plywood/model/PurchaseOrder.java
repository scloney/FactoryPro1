package com.plywood.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Entity
 */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String poNumber;
    
    @Column(nullable = false)
    private Long supplierId;
    
    @Column(length = 200)
    private String supplierName;
    
    @Column(length = 50)
    private String supplierCode;
    
    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate poDate;
    
    @Column(columnDefinition = "DATE")
    private LocalDate expectedDeliveryDate;
    
    @Column(columnDefinition = "DATE")
    private LocalDate actualDeliveryDate;
    
    @Column(nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, PENDING, APPROVED, ORDERED, RECEIVED, CANCELLED
    
    @Column(nullable = false)
    private double subtotal = 0.0;
    
    @Column(nullable = false)
    private double taxRate = 0.0;
    
    @Column(nullable = false)
    private double taxAmount = 0.0;
    
    @Column(nullable = false)
    private double discountPercent = 0.0;
    
    @Column(nullable = false)
    private double discountAmount = 0.0;
    
    @Column(nullable = false)
    private double shippingCharges = 0.0;
    
    @Column(nullable = false)
    private double otherCharges = 0.0;
    
    @Column(nullable = false)
    private double grandTotal = 0.0;
    
    @Column(length = 50)
    private String paymentTerms;
    
    @Column(length = 100)
    private String shippingAddress;
    
    @Column(length = 100)
    private String billingAddress;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(length = 1000)
    private String termsAndConditions;
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(length = 100)
    private String approvedBy;
    
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime approvedDate;
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdDate;
    
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime lastUpdated;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseOrderItem> items = new ArrayList<>();
    
    // Constructors
    public PurchaseOrder() {
        this.status = "DRAFT";
        this.poDate = LocalDate.now();
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public PurchaseOrder(String poNumber, Long supplierId) {
        this();
        this.poNumber = poNumber;
        this.supplierId = supplierId;
    }
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (poDate == null) {
            poDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
    
    // Business Methods
    public void calculateTotals() {
        this.subtotal = items.stream()
            .mapToDouble(PurchaseOrderItem::getTotalPrice)
            .sum();
        
        this.discountAmount = (subtotal * discountPercent) / 100.0;
        double afterDiscount = subtotal - discountAmount;
        
        this.taxAmount = (afterDiscount * taxRate) / 100.0;
        
        this.grandTotal = afterDiscount + taxAmount + shippingCharges + otherCharges;
    }
    
    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        calculateTotals();
    }
    
    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        calculateTotals();
    }
    
    @Transient
    public boolean isDraft() {
        return "DRAFT".equals(status);
    }
    
    @Transient
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    @Transient
    public boolean isReceived() {
        return "RECEIVED".equals(status);
    }
    
    @Transient
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    @Transient
    public boolean isOverdue() {
        if (expectedDeliveryDate == null || isReceived() || isCancelled()) {
            return false;
        }
        return LocalDate.now().isAfter(expectedDeliveryDate);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    
    public LocalDate getPoDate() { return poDate; }
    public void setPoDate(LocalDate poDate) { this.poDate = poDate; }
    
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { 
        this.expectedDeliveryDate = expectedDeliveryDate; 
    }
    
    public LocalDate getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) { 
        this.actualDeliveryDate = actualDeliveryDate; 
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { 
        this.taxRate = taxRate;
        calculateTotals();
    }
    
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { 
        this.discountPercent = discountPercent;
        calculateTotals();
    }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getShippingCharges() { return shippingCharges; }
    public void setShippingCharges(double shippingCharges) { 
        this.shippingCharges = shippingCharges;
        calculateTotals();
    }
    
    public double getOtherCharges() { return otherCharges; }
    public void setOtherCharges(double otherCharges) { 
        this.otherCharges = otherCharges;
        calculateTotals();
    }
    
    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { 
        this.termsAndConditions = termsAndConditions; 
    }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public List<PurchaseOrderItem> getItems() { return items; }
    public void setItems(List<PurchaseOrderItem> items) { 
        this.items = items;
        // CRITICAL FIX: Set bidirectional relationship for each item
        if (items != null) {
            for (PurchaseOrderItem item : items) {
                item.setPurchaseOrder(this);
                item.calculateTotalPrice();
            }
        }
        calculateTotals();
    }
}