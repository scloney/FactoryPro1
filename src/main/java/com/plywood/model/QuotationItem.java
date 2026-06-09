package com.plywood.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "quotation_items")

public class QuotationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "quotation_id", nullable = false)
    @JsonBackReference
    private Quotation quotation;
    
    @Column(nullable = false)
    private String description;
    
    // Size fields (for furniture dimensions)
    private Double length;  // in feet (e.g., 6)
    private Double width;   // in feet (e.g., 7)
    private String size;    // formatted size (e.g., "6' × 7'")
    
    // Square feet (auto-calculated)
    private Double sqft;    // length × width
    
    // Pricing
    private Double quantity = 1.0;
    private String unit = "Pcs";
    private Double ratePerSqft;  // Rate per square foot
    private Double unitPrice;    // Final price per unit (sqft × ratePerSqft)
    
    // Constructors
    public QuotationItem() {}
    
    public QuotationItem(String description, Double quantity, String unit, Double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }
    
    // Business Methods
    
    /**
     * Calculate square feet from length and width
     */
    public void calculateSqft() {
        if (length != null && width != null) {
            this.sqft = length * width;
            this.size = String.format("%.1f' × %.1f'", length, width);
        }
    }
    
    /**
     * Calculate unit price from rate per sqft
     */
    public void calculateUnitPrice() {
        if (sqft != null && ratePerSqft != null) {
            this.unitPrice = sqft * ratePerSqft;
        }
    }
    
    /**
     * Get total amount for this item
     */
    public Double getTotal() {
        if (quantity != null && unitPrice != null) {
            return quantity * unitPrice;
        }
        return 0.0;
    }
    
    /**
     * Auto-calculate sqft and price when setting dimensions
     */
    public void setDimensions(Double length, Double width, Double ratePerSqft) {
        this.length = length;
        this.width = width;
        this.ratePerSqft = ratePerSqft;
        calculateSqft();
        calculateUnitPrice();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Quotation getQuotation() {
        return quotation;
    }
    
    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
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
    
    @Override
    public String toString() {
        return "QuotationItem{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", size='" + size + '\'' +
                ", sqft=" + sqft +
                ", quantity=" + quantity +
                ", ratePerSqft=" + ratePerSqft +
                ", unitPrice=" + unitPrice +
                ", total=" + getTotal() +
                '}';
    }
}