package com.plywood.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

/**
 * Sales Order Item - Individual furniture item in the sales order
 */
@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonBackReference
    private SalesOrder salesOrder;
    
    @Column(nullable = false)
    private String description;
    
    // Size fields (furniture dimensions)
    private Double length;  // in feet
    private Double width;   // in feet
    private String size;    // formatted (e.g., "6' × 7'")
    private Double sqft;    // length × width
    
    // Pricing
    private Double quantity = 1.0;
    private String unit = "Pcs";
    private Double ratePerSqft;
    private Double unitPrice;  // sqft × ratePerSqft
    
    // Production tracking
    @Enumerated(EnumType.STRING)
    private ProductionStatus productionStatus = ProductionStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String productionNotes;
    
    // Constructors
    public SalesOrderItem() {}
    
    // Business Methods
    
    /**
     * Calculate total for this item
     */
    public Double getTotal() {
        if (quantity != null && unitPrice != null) {
            return quantity * unitPrice;
        }
        return 0.0;
    }
    
    /**
     * Calculate sqft from dimensions
     */
    public void calculateSqft() {
        if (length != null && width != null) {
            this.sqft = length * width;
            this.size = String.format("%.1f' × %.1f'", length, width);
        }
    }
    
    /**
     * Calculate unit price from rate
     */
    public void calculateUnitPrice() {
        if (sqft != null && ratePerSqft != null) {
            this.unitPrice = sqft * ratePerSqft;
        }
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public SalesOrder getSalesOrder() {
        return salesOrder;
    }
    
    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getLength() {
        return length;
    }
    
    public void setLength(Double length) {
        this.length = length;
        calculateSqft();
        calculateUnitPrice();
    }
    
    public Double getWidth() {
        return width;
    }
    
    public void setWidth(Double width) {
        this.width = width;
        calculateSqft();
        calculateUnitPrice();
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public Double getSqft() {
        return sqft;
    }
    
    public void setSqft(Double sqft) {
        this.sqft = sqft;
    }
    
    public Double getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Double getRatePerSqft() {
        return ratePerSqft;
    }
    
    public void setRatePerSqft(Double ratePerSqft) {
        this.ratePerSqft = ratePerSqft;
        calculateUnitPrice();
    }
    
    public Double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public ProductionStatus getProductionStatus() {
        return productionStatus;
    }
    
    public void setProductionStatus(ProductionStatus productionStatus) {
        this.productionStatus = productionStatus;
    }
    
    public String getProductionNotes() {
        return productionNotes;
    }
    
    public void setProductionNotes(String productionNotes) {
        this.productionNotes = productionNotes;
    }
    
    // Inner enum for production status
    public enum ProductionStatus {
        PENDING,      // Not started
        IN_PROGRESS,  // Being made
        COMPLETED     // Finished
    }
}