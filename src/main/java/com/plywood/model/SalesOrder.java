package com.plywood.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales Order - Confirmed order from approved quotation
 * Links: Customer → Quotation → Sales Order → Production → Bill
 */
@Entity
@Table(name = "sales_orders")
public class SalesOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;  // SO-2026-001
    
    @Column(nullable = false)
    private LocalDate orderDate;
    
    // Link to Customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Link to Quotation (the approved quotation this order came from)
    @ManyToOne
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;
    
    // Items in this order
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SalesOrderItem> items = new ArrayList<>();
    
    // Order Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesOrderStatus status = SalesOrderStatus.PENDING;
    
    // Delivery tracking
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    
    // Financial
    private Double totalAmount;
    private Double paidAmount = 0.0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    // Tax & Discount (copied from quotation)
    private Double taxRate = 0.0;
    private Double discount = 0.0;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Tracking
    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();
    
    private LocalDate modifiedDate;

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

    
    // Constructors
    public SalesOrder() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDate.now();
    }
    
    // Business Methods
    
    /**
     * Calculate subtotal from items
     */
    public double getSubTotal() {
        return items.stream().mapToDouble(SalesOrderItem::getTotal).sum();
    }
    
    /**
     * Calculate tax amount
     */
    public double getTaxAmount() {
        return getSubTotal() * (taxRate / 100);
    }
    
    /**
     * Calculate discount amount
     */
    public double getDiscountAmount() {
        return getSubTotal() * (discount / 100);
    }
    
    /**
     * Calculate grand total
     */
    public double getGrandTotal() {
        return getSubTotal() + getTaxAmount() - getDiscountAmount();
    }
    
    /**
     * Calculate remaining amount to be paid
     */
    public double getRemainingAmount() {
        return getGrandTotal() - (paidAmount != null ? paidAmount : 0.0);
    }
    
    /**
     * Add item to order
     */
    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }
    
    /**
     * Remove item from order
     */
    public void removeItem(SalesOrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
    }
    
    /**
     * Update payment status based on paid amount.
     * Uses stored totalAmount (consistent with what's shown in UI / invoices).
     * Falls back to dynamic grand total if totalAmount not yet set.
     */
    public void updatePaymentStatus() {
        double total = (totalAmount != null && totalAmount > 0) ? totalAmount : getGrandTotal();
        if (paidAmount == null || paidAmount == 0) {
            this.paymentStatus = PaymentStatus.UNPAID;
        } else if (paidAmount >= total) {
            this.paymentStatus = PaymentStatus.PAID;
        } else {
            this.paymentStatus = PaymentStatus.PARTIAL;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDate.now();
        this.updatedAt = java.time.LocalDateTime.now();
        updatePaymentStatus();
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Quotation getQuotation() {
        return quotation;
    }
    
    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }
    
    public List<SalesOrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<SalesOrderItem> items) {
        this.items = items;
    }
    
    public SalesOrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(SalesOrderStatus status) {
        this.status = status;
    }
    
    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }
    
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Double getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
        updatePaymentStatus();
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Double getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
    
    public Double getDiscount() {
        return discount;
    }
    
    public void setDiscount(Double discount) {
        this.discount = discount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDate getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}