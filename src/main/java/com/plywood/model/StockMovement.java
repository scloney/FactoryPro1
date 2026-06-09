package com.plywood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stock Movement Entity - Spring Boot 3.x Compatible
 */
@Entity
@Table(name = "stock_movements")
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(length = 50)
    private String productCode;
    
    @Column(length = 200)
    private String productName;
    
    @Column(nullable = false, length = 20)
    private String movementType;
    
    @Column(nullable = false, length = 50)
    private String transactionType;
    
    @Column(nullable = false)
    private double quantity;
    
    @Column(nullable = false)
    private double previousStock;
    
    @Column(nullable = false)
    private double newStock;
    
    @Column(nullable = false)
    private double unitPrice = 0.0;
    
    @Column(nullable = false)
    private double totalValue = 0.0;
    
    @Column(length = 100)
    private String referenceNumber;
    
    @Column(length = 200)
    private String partyName;
    
    @Column(length = 500)
    private String reason;
    
    @Column(length = 100)
    private String location;
    
    @Column(length = 100)
    private String performedBy;
    
    @Column(nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime movementDate;
    
    @Column(length = 1000)
    private String notes;
    
    public StockMovement() {
        this.movementDate = LocalDateTime.now();
    }
    
    public StockMovement(Long productId, String movementType, double quantity) {
        this();
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
    }
    
    @PrePersist
    protected void onCreate() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
        calculateTotalValue();
    }
    
    @PreUpdate
    protected void onUpdate() {
        calculateTotalValue();
    }
    
    public void calculateTotalValue() {
        this.totalValue = quantity * unitPrice;
    }
    
    @Transient
    public boolean isInward() {
        return "INWARD".equals(movementType);
    }
    
    @Transient
    public boolean isOutward() {
        return "OUTWARD".equals(movementType);
    }
    
    @Transient
    public boolean isAdjustment() {
        return "ADJUSTMENT".equals(movementType);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { 
        this.quantity = quantity;
        calculateTotalValue();
    }
    
    public double getPreviousStock() { return previousStock; }
    public void setPreviousStock(double previousStock) { this.previousStock = previousStock; }
    
    public double getNewStock() { return newStock; }
    public void setNewStock(double newStock) { this.newStock = newStock; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        calculateTotalValue();
    }
    
    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    
    public LocalDateTime getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}