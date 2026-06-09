package com.plywood.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String customerName;
    
    private String contactPerson;
    
    private String email;
    
    @Column(nullable = false)
    private String phone;

    private String customerPhone;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String country = "India";
    
    private String gstin;
    
    private String pan;
    
    private String paymentTerms = "Cash";
    
    private String customerType = "Retail"; // Retail, Wholesale, Distributor, Direct
    
    @Column(nullable = false)
    private Double outstandingBalance = 0.0;
    
    private Double totalPurchaseValue = 0.0;
    
    private Integer totalOrders = 0;
    
    private LocalDate lastOrderDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    private String notes;
    
    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();
    
    private LocalDate modifiedDate;

    // Constructors
    public Customer() {
    }

    public Customer(String customerName, String phone) {
        this.customerName = customerName;
        this.phone = phone;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public Double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Double getTotalPurchaseValue() {
        return totalPurchaseValue;
    }

    public void setTotalPurchaseValue(Double totalPurchaseValue) {
        this.totalPurchaseValue = totalPurchaseValue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public LocalDate getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDate lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    // Business Methods
    public void addOrder(Double orderAmount) {
        this.totalOrders++;
        this.totalPurchaseValue = (this.totalPurchaseValue == null ? 0.0 : this.totalPurchaseValue) + orderAmount;
        this.lastOrderDate = LocalDate.now();
    }

    public void updateOutstanding(Double amount) {
        this.outstandingBalance = (this.outstandingBalance == null ? 0.0 : this.outstandingBalance) + amount;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", active=" + active +
                '}';
    }
}