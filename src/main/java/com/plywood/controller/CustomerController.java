package com.plywood.controller;

import com.plywood.model.Customer;
import com.plywood.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    // Create new customer
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.createCustomer(customer);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get all customers
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get customer by id
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        return customer.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // Get active customers
    @GetMapping("/active")
    public ResponseEntity<List<Customer>> getActiveCustomers() {
        try {
            List<Customer> customers = customerService.getActiveCustomers();
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get customers by type
    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<Customer>> getCustomersByType(@PathVariable String customerType) {
        try {
            List<Customer> customers = customerService.getCustomersByType(customerType);
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Search customers
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String term) {
        try {
            List<Customer> customers = customerService.searchCustomers(term);
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update customer
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Delete customer
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Deactivate customer
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Customer> deactivateCustomer(@PathVariable Long id) {
        try {
            Customer customer = customerService.deactivateCustomer(id);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Activate customer
    @PostMapping("/{id}/activate")
    public ResponseEntity<Customer> activateCustomer(@PathVariable Long id) {
        try {
            Customer customer = customerService.activateCustomer(id);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Update outstanding balance
    @PostMapping("/{id}/outstanding")
    public ResponseEntity<Customer> updateOutstanding(@PathVariable Long id, 
                                                      @RequestParam Double amount) {
        try {
            Customer customer = customerService.updateOutstanding(id, amount);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Add payment
    @PostMapping("/{id}/payment")
    public ResponseEntity<Customer> addPayment(@PathVariable Long id, 
                                               @RequestParam Double amount) {
        try {
            Customer customer = customerService.addPayment(id, amount);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Record order
    @PostMapping("/{id}/order")
    public ResponseEntity<Customer> recordOrder(@PathVariable Long id, 
                                                @RequestParam Double amount) {
        try {
            Customer customer = customerService.recordOrder(id, amount);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get customers with outstanding
    @GetMapping("/with-outstanding")
    public ResponseEntity<List<Customer>> getCustomersWithOutstanding() {
        try {
            List<Customer> customers = customerService.getCustomersWithOutstanding();
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get top customers
    @GetMapping("/top")
    public ResponseEntity<List<Customer>> getTopCustomers() {
        try {
            List<Customer> customers = customerService.getTopCustomers();
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<CustomerService.CustomerStats> getStatistics() {
        try {
            CustomerService.CustomerStats stats = customerService.getCustomerStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}