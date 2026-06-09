package com.plywood.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
public class Quotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String quotationNumber;
    
    @Column(nullable = false)
    private LocalDate date;
    
    // Link to Customer
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    // Customer details
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QuotationItem> items = new ArrayList<>();
    
    private Double taxRate = 0.0;
    private Double discount = 0.0;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // ===== APPROVAL FIELDS =====
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING";
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approval_method")
    private String approvalMethod;
    
    @Column(name = "approved_date")
    private LocalDate approvedDate;
    
    @Column(name = "approval_remarks", columnDefinition = "TEXT")
    private String approvalRemarks;
    
    @Column(name = "approval_image_path")
    private String approvalImagePath;
    
    // Company details
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    
    // Status
    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;
    
    // Valid until date
    private LocalDate validUntil;
    
    // Conversion tracking
    private Boolean convertedToOrder = false;
    private Long salesOrderId;
    
    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();
    
    private LocalDate modifiedDate;
    
    // Constructors
    public Quotation() {
        this.items = new ArrayList<>();
        this.date = LocalDate.now();
        this.taxRate = 0.0;
        this.discount = 0.0;
        this.approvalStatus = "PENDING";
    }
    
    // Business Methods
    public double getSubTotal() {
        return items.stream().mapToDouble(QuotationItem::getTotal).sum();
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
    
    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
    }
    
    public void removeItem(QuotationItem item) {
        items.remove(item);
        item.setQuotation(null);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDate.now();
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }
    
    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerAddress() {
        return customerAddress;
    }
    
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public List<QuotationItem> getItems() {
        return items;
    }
    
    public void setItems(List<QuotationItem> items) {
        this.items = items;
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
    
    // ===== APPROVAL GETTERS/SETTERS =====
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getApprovalMethod() {
        return approvalMethod;
    }
    
    public void setApprovalMethod(String approvalMethod) {
        this.approvalMethod = approvalMethod;
    }
    
    public LocalDate getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }
    
    public String getApprovalRemarks() {
        return approvalRemarks;
    }
    
    public void setApprovalRemarks(String approvalRemarks) {
        this.approvalRemarks = approvalRemarks;
    }
    
    public String getApprovalImagePath() {
        return approvalImagePath;
    }
    
    public void setApprovalImagePath(String approvalImagePath) {
        this.approvalImagePath = approvalImagePath;
    }
    
    // ===== OTHER GETTERS/SETTERS =====
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getCompanyAddress() {
        return companyAddress;
    }
    
    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }
    
    public String getCompanyPhone() {
        return companyPhone;
    }
    
    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }
    
    public String getCompanyEmail() {
        return companyEmail;
    }
    
    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }
    
    public QuotationStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuotationStatus status) {
        this.status = status;
    }
    
    public LocalDate getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }
    
    public Boolean getConvertedToOrder() {
        return convertedToOrder;
    }
    
    public void setConvertedToOrder(Boolean convertedToOrder) {
        this.convertedToOrder = convertedToOrder;
    }
    
    public Long getSalesOrderId() {
        return salesOrderId;
    }
    
    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
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
}