package com.plywood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Supplier Entity - Manages supplier information
 */
@Entity
@Table(name = "suppliers")
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String supplierCode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 200)
    private String companyName;
    
    @Column(length = 100)
    private String contactPerson;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 30)
    private String gstin;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 20)
    private String pincode;
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 50)
    private String paymentTerms;
    
    @Column(nullable = false)
    private int creditDays = 0;
    
    @Column(nullable = false)
    private double creditLimit = 0.0;
    
    @Column(nullable = false)
    private double outstandingBalance = 0.0;
    
    @Column(length = 100)
    private String bankName;
    
    @Column(length = 50)
    private String accountNumber;
    
    @Column(length = 20)
    private String ifscCode;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdDate;
    
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime lastUpdated;
    
    // Constructors
    public Supplier() {
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Supplier(String supplierCode, String name) {
        this();
        this.supplierCode = supplierCode;
        this.name = name;
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
    public boolean isCreditLimitExceeded() {
        return outstandingBalance > creditLimit;
    }
    
    @Transient
    public double getAvailableCredit() {
        return Math.max(0, creditLimit - outstandingBalance);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public int getCreditDays() { return creditDays; }
    public void setCreditDays(int creditDays) { this.creditDays = creditDays; }
    
    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }
    
    public double getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(double outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}