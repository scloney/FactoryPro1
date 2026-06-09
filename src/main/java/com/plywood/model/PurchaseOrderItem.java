package com.plywood.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Purchase Order Item Entity
 */
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(length = 50)
    private String productCode;
    
    @Column(nullable = false, length = 200)
    private String productName;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 20)
    private String unit;
    
    @Column(nullable = false)
    private double quantity;
    
    @Column(nullable = false)
    private double receivedQuantity = 0.0;
    
    @Column(nullable = false)
    private double unitPrice;
    
    @Column(nullable = false)
    private double totalPrice;
    
    @Column(nullable = false)
    private double taxRate = 0.0;
    
    @Column(nullable = false)
    private double taxAmount = 0.0;
    
    @Column(length = 500)
    private String notes;
    
    // Constructors
    public PurchaseOrderItem() {}
    
    public PurchaseOrderItem(Long productId, String productName, double quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }
    
    // Business Methods
    public void calculateTotalPrice() {
        this.totalPrice = quantity * unitPrice;
        this.taxAmount = (totalPrice * taxRate) / 100.0;
    }
    
    @Transient
    public boolean isFullyReceived() {
        return receivedQuantity >= quantity;
    }
    
    @Transient
    public boolean isPartiallyReceived() {
        return receivedQuantity > 0 && receivedQuantity < quantity;
    }
    
    @Transient
    public double getPendingQuantity() {
        return Math.max(0, quantity - receivedQuantity);
    }
    
    @Transient
    public double getReceivedPercentage() {
        if (quantity == 0) return 0;
        return (receivedQuantity / quantity) * 100.0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { 
        this.quantity = quantity;
        calculateTotalPrice();
    }
    
    public double getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(double receivedQuantity) { 
        this.receivedQuantity = receivedQuantity; 
    }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { 
        this.taxRate = taxRate;
        calculateTotalPrice();
    }
    
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}