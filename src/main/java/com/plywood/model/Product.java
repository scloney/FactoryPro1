package com.plywood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Product Entity - Spring Boot 3.x Compatible
 * Uses Jakarta EE (jakarta.persistence) instead of javax.persistence
 */
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String productCode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 50)
    private String grade;
    
    @Column(length = 20)
    private String thickness;
    
    @Column(length = 20)
    private String size;
    
    @Column(nullable = false, length = 20)
    private String unit;
    
    @Column(unique = true, nullable = true, length = 100)
    private String barcodeValue;
    
    @Column(nullable = false)
    private double currentStock = 0.0;
    
    @Column(nullable = false)
    private double minStockLevel = 0.0;

    /** Reorder point — when currentStock falls below this, a purchase order should be raised. */
    @Column(nullable = false)
    private double reorderLevel = 0.0;
    
    @Column(nullable = false)
    private double maxStockLevel = 0.0;
    
    @Column(nullable = false)
    private double costPrice = 0.0;
    
    @Column(nullable = false)
    private double sellingPrice = 0.0;
    
    @Column(length = 200)
    private String supplierName;
    
    @Column(length = 50)
    private String supplierId;
    
    @Column(length = 100)
    private String location;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false, updatable = false,columnDefinition = "DATETIME")
    private LocalDateTime createdDate;
    
    @Column(nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime lastUpdated;
    
    // Constructors
    public Product() {
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.currentStock = 0.0;
    }
    
    public Product(String productCode, String name, String category, String unit) {
        this();
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.unit = unit;
    }
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
    
    // Business Methods
    @Transient
    public boolean isLowStock() {
        return currentStock <= minStockLevel;
    }

    /** True when stock has fallen to or below the reorder trigger point. */
    @Transient
    public boolean isReorderNeeded() {
        return currentStock <= reorderLevel && reorderLevel > 0;
    }

    /** True when stock is zero (critical — production may halt). */
    @Transient
    public boolean isCriticalStock() {
        return currentStock <= 0;
    }
    
    @Transient
    public boolean isOverStock() {
        return currentStock >= maxStockLevel;
    }
    
    @Transient
    public double getStockValue() {
        return currentStock * costPrice;
    }
    
    @Transient
    public double getPotentialRevenue() {
        return currentStock * sellingPrice;
    }
    
    @Transient
    public double getProfit() {
        return sellingPrice - costPrice;
    }
    
    @Transient
    public double getProfitMargin() {
        if (sellingPrice == 0) return 0;
        return ((sellingPrice - costPrice) / sellingPrice) * 100;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getThickness() { return thickness; }
    public void setThickness(String thickness) { this.thickness = thickness; }
    
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getBarcodeValue() { return barcodeValue; }
    public void setBarcodeValue(String barcodeValue) { this.barcodeValue = barcodeValue; }
    
    public double getCurrentStock() { return currentStock; }
    public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }
    
    public double getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(double minStockLevel) { this.minStockLevel = minStockLevel; }

    public double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(double reorderLevel) { this.reorderLevel = reorderLevel; }
    
    public double getMaxStockLevel() { return maxStockLevel; }
    public void setMaxStockLevel(double maxStockLevel) { this.maxStockLevel = maxStockLevel; }
    
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}