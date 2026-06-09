package com.plywood.service;

import com.plywood.model.Customer;
import com.plywood.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    // Create new customer
    public Customer createCustomer(Customer customer) {
        customer.setCreatedDate(LocalDate.now());
        customer.setActive(true);
        customer.setOutstandingBalance(0.0);
        customer.setTotalPurchaseValue(0.0);
        customer.setTotalOrders(0);
        return customerRepository.save(customer);
    }
    
    // Update customer
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        
        customer.setCustomerName(customerDetails.getCustomerName());
        customer.setContactPerson(customerDetails.getContactPerson());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setCustomerPhone(customerDetails.getCustomerPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setState(customerDetails.getState());
        customer.setPincode(customerDetails.getPincode());
        customer.setCountry(customerDetails.getCountry());
        customer.setGstin(customerDetails.getGstin());
        customer.setPan(customerDetails.getPan());
        customer.setPaymentTerms(customerDetails.getPaymentTerms());
        customer.setCustomerType(customerDetails.getCustomerType());
        customer.setNotes(customerDetails.getNotes());
        customer.setActive(customerDetails.getActive());
        customer.setModifiedDate(LocalDate.now());
        
        return customerRepository.save(customer);
    }
    
    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    // Get customer by id
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    // Get active customers
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }
    
    // Get customers by type
    public List<Customer> getCustomersByType(String customerType) {
        return customerRepository.findByCustomerType(customerType);
    }
    
    // Search customers
    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.searchCustomers(searchTerm);
    }
    
    // Delete customer
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    
    // Deactivate customer
    public Customer deactivateCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customer.setActive(false);
        customer.setModifiedDate(LocalDate.now());
        return customerRepository.save(customer);
    }
    
    // Activate customer
    public Customer activateCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customer.setActive(true);
        customer.setModifiedDate(LocalDate.now());
        return customerRepository.save(customer);
    }
    
    // Update outstanding balance
    public Customer updateOutstanding(Long customerId, Double amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        customer.updateOutstanding(amount);
        customer.setModifiedDate(LocalDate.now());
        return customerRepository.save(customer);
    }
    
    // Add payment (reduce outstanding)
    public Customer addPayment(Long customerId, Double paymentAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        customer.updateOutstanding(-paymentAmount);
        customer.setModifiedDate(LocalDate.now());
        return customerRepository.save(customer);
    }
    
    // Record new order
    public Customer recordOrder(Long customerId, Double orderAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        customer.addOrder(orderAmount);
        customer.setModifiedDate(LocalDate.now());
        return customerRepository.save(customer);
    }
    
    // Get customers with outstanding
    public List<Customer> getCustomersWithOutstanding() {
        return customerRepository.findCustomersWithOutstanding();
    }
    
    // Get top customers
    public List<Customer> getTopCustomers() {
        return customerRepository.findTopCustomersByPurchaseValue();
    }
    
    // Get statistics
    public CustomerStats getCustomerStatistics() {
        Long totalCustomers = customerRepository.count();
        Long activeCustomers = customerRepository.countActiveCustomers();
        Double totalOutstanding = customerRepository.getTotalOutstandingAmount();
        Double totalPurchaseValue = customerRepository.getTotalPurchaseValue();
        
        return new CustomerStats(totalCustomers, activeCustomers, totalOutstanding, totalPurchaseValue);
    }
    
    // Inner class for statistics
    public static class CustomerStats {
        private Long totalCustomers;
        private Long activeCustomers;
        private Double totalOutstanding;
        private Double totalPurchaseValue;
        
        public CustomerStats(Long totalCustomers, Long activeCustomers, 
                           Double totalOutstanding, Double totalPurchaseValue) {
            this.totalCustomers = totalCustomers;
            this.activeCustomers = activeCustomers;
            this.totalOutstanding = totalOutstanding;
            this.totalPurchaseValue = totalPurchaseValue;
        }
        
        // Getters
        public Long getTotalCustomers() { return totalCustomers; }
        public Long getActiveCustomers() { return activeCustomers; }
        public Double getTotalOutstanding() { return totalOutstanding; }
        public Double getTotalPurchaseValue() { return totalPurchaseValue; }
    }
}