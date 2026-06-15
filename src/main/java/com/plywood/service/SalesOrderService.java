package com.plywood.service;

import com.plywood.model.*;
import com.plywood.repository.CustomerRepository;
import com.plywood.repository.QuotationRepository;
import com.plywood.repository.SalesOrderRepository;
import com.plywood.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class SalesOrderService {
    
    @Autowired
    private SalesOrderRepository salesOrderRepository;
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillRepository billRepository;
    
    // ===== CREATE & CONVERT =====
    
    /**
     * Create sales order from approved quotation
     */
    @Transactional
    public SalesOrder createFromQuotation(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        // Validate quotation is approved
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new RuntimeException("Quotation must be ACCEPTED to convert to sales order");
        }
        
        // Check if already converted
        if (quotation.getConvertedToOrder()) {
            throw new RuntimeException("Quotation already converted to sales order");
        }
        
        // Create sales order
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderNumber(generateOrderNumber());
        salesOrder.setOrderDate(LocalDate.now());

        // Resolve customer — quotations can be created with only a free-text customerName
        // (no FK to the customers table), leaving getCustomer() null and causing a
        // "customer_id cannot be null" DB error. Fall back to a name lookup.
        Customer customer = quotation.getCustomer();
        if (customer == null && quotation.getCustomerName() != null) {
            customer = customerRepository.findByCustomerName(quotation.getCustomerName())
                    .orElse(null);
        }
        if (customer == null) {
            throw new RuntimeException(
                "Cannot create sales order: this quotation has no linked customer record. " +
                "Please edit the quotation and select a customer from the Customers list, then try again."
            );
        }
        salesOrder.setCustomer(customer);
        salesOrder.setQuotation(quotation);
        salesOrder.setTaxRate(quotation.getTaxRate());
        salesOrder.setDiscount(quotation.getDiscount());
        salesOrder.setNotes(quotation.getNotes());
        salesOrder.setStatus(SalesOrderStatus.PENDING);
        salesOrder.setPaymentStatus(PaymentStatus.UNPAID);
        
        // Copy items from quotation
        for (QuotationItem qItem : quotation.getItems()) {
            SalesOrderItem soItem = new SalesOrderItem();
            soItem.setDescription(qItem.getDescription());
            soItem.setLength(qItem.getLength());
            soItem.setWidth(qItem.getWidth());
            soItem.setSize(qItem.getSize());
            soItem.setSqft(qItem.getSqft());
            soItem.setQuantity(qItem.getQuantity());
            soItem.setUnit(qItem.getUnit());
            soItem.setRatePerSqft(qItem.getRatePerSqft());
            soItem.setUnitPrice(qItem.getUnitPrice());
            soItem.setProductionStatus(SalesOrderItem.ProductionStatus.PENDING);
            
            salesOrder.addItem(soItem);
        }
        
        // Calculate and set total
        salesOrder.setTotalAmount(salesOrder.getGrandTotal());
        
        // Save sales order
        SalesOrder saved = salesOrderRepository.save(salesOrder);
        
        // Mark quotation as converted
        quotation.setConvertedToOrder(true);
        quotation.setSalesOrderId(saved.getId());
        quotation.setStatus(QuotationStatus.CONVERTED);
        quotationRepository.save(quotation);
        
        return saved;
    }
    
    /**
     * Create new sales order manually (not from quotation)
     */
    @Transactional
    public SalesOrder createSalesOrder(SalesOrder salesOrder) {
        salesOrder.setOrderNumber(generateOrderNumber());
        salesOrder.setOrderDate(LocalDate.now());
        salesOrder.setStatus(SalesOrderStatus.PENDING);
        salesOrder.setPaymentStatus(PaymentStatus.UNPAID);

        // Wire items back-reference — @JsonBackReference strips salesOrder from
        // deserialized items, leaving sales_order_id NULL which violates the NOT NULL constraint.
        if (salesOrder.getItems() != null) {
            for (SalesOrderItem item : salesOrder.getItems()) {
                item.setSalesOrder(salesOrder);
                // Recalculate derived fields in case they were not sent from frontend
                if (item.getLength() != null && item.getWidth() != null) {
                    item.calculateSqft();
                    if (item.getRatePerSqft() != null) {
                        item.calculateUnitPrice();
                    }
                }
            }
        }

        salesOrder.setTotalAmount(salesOrder.getGrandTotal());
        return salesOrderRepository.save(salesOrder);
    }
    
    /**
     * Generate unique order number: SO-2026-001
     */
    private String generateOrderNumber() {
        int year = Year.now().getValue();
        // Use max ID + count to ensure uniqueness even after deletions.
        // count() alone causes duplicate key violations when orders are deleted.
        long count = salesOrderRepository.count() + 1;
        String candidate = String.format("SO-%d-%03d", year, count);
        // Increment suffix until unique (handles deletions / gaps)
        while (salesOrderRepository.findByOrderNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("SO-%d-%03d", year, count);
        }
        return candidate;
    }
    
    // ===== CRUD OPERATIONS =====
    
    public List<SalesOrder> getAllOrders() {
        return salesOrderRepository.findAll();
    }

    public org.springframework.data.domain.Page<SalesOrder> getAllOrders(org.springframework.data.domain.Pageable pageable) {
        return salesOrderRepository.findAll(pageable);
    }
    
    public Optional<SalesOrder> getOrderById(Long id) {
        return salesOrderRepository.findById(id);
    }
    
    public Optional<SalesOrder> getOrderByNumber(String orderNumber) {
        return salesOrderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<SalesOrder> getOrdersByCustomer(Long customerId) {
        return salesOrderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }
    
    public List<SalesOrder> getOrdersByStatus(SalesOrderStatus status) {
        return salesOrderRepository.findByStatusOrderByOrderDateDesc(status);
    }
    
    public List<SalesOrder> getPendingOrders() {
        return salesOrderRepository.findPendingOrders();
    }
    
    public List<SalesOrder> getOverdueOrders() {
        return salesOrderRepository.findOverdueOrders(LocalDate.now());
    }
    
    @Transactional
    public SalesOrder updateOrder(Long id, SalesOrder updatedOrder) {
        SalesOrder existing = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found"));
        
        existing.setExpectedDeliveryDate(updatedOrder.getExpectedDeliveryDate());
        existing.setNotes(updatedOrder.getNotes());
        existing.setModifiedDate(LocalDate.now());
        
        return salesOrderRepository.save(existing);
    }
    
    @Transactional
    public void deleteOrder(Long id) {
        salesOrderRepository.deleteById(id);
    }
    
    // ===== STATUS MANAGEMENT =====
    
    @Transactional
    public SalesOrder startProduction(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(SalesOrderStatus.IN_PRODUCTION);
        order.setModifiedDate(LocalDate.now());
        return salesOrderRepository.save(order);
    }
    
    @Transactional
    public SalesOrder markReady(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(SalesOrderStatus.READY);
        order.setModifiedDate(LocalDate.now());
        return salesOrderRepository.save(order);
    }
    
    @Transactional
    public SalesOrder markDelivered(Long id, LocalDate deliveryDate) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(SalesOrderStatus.DELIVERED);
        order.setActualDeliveryDate(deliveryDate != null ? deliveryDate : LocalDate.now());
        order.setModifiedDate(LocalDate.now());
        return salesOrderRepository.save(order);
    }
    
    @Transactional
    public SalesOrder cancelOrder(Long id, String reason) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Cancelled: " + reason);
        order.setModifiedDate(LocalDate.now());
        return salesOrderRepository.save(order);
    }
    
    // ===== PAYMENT MANAGEMENT =====
    
    @Transactional
    public SalesOrder addPayment(Long id, Double amount) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        double currentPaid = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
        order.setPaidAmount(currentPaid + amount);
        order.updatePaymentStatus();
        order.setModifiedDate(LocalDate.now());
        
        SalesOrder savedOrder = salesOrderRepository.save(order);
        
        // Sync with Bill
        if (order.getQuotation() != null && order.getQuotation().getQuotationNumber() != null) {
            billRepository.findBySourceQuotationNumber(order.getQuotation().getQuotationNumber())
                    .ifPresent(bill -> {
                        if (order.getPaymentStatus() == PaymentStatus.PAID) {
                            bill.setStatus("PAID");
                        } else if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                            bill.setStatus("DRAFT");
                        }
                        // PARTIAL stays unchanged
                        billRepository.save(bill);
                    });
        }
        
        return savedOrder;
    }
    
    // ===== STATISTICS =====
    
    public SalesOrderStats getStatistics() {
        SalesOrderStats stats = new SalesOrderStats();
        stats.totalOrders = salesOrderRepository.count();
        stats.pendingOrders = salesOrderRepository.countByStatus(SalesOrderStatus.PENDING);
        stats.inProductionOrders = salesOrderRepository.countByStatus(SalesOrderStatus.IN_PRODUCTION);
        stats.readyOrders = salesOrderRepository.countByStatus(SalesOrderStatus.READY);
        stats.deliveredOrders = salesOrderRepository.countByStatus(SalesOrderStatus.DELIVERED);
        stats.totalRevenue = salesOrderRepository.getTotalRevenue() != null ? salesOrderRepository.getTotalRevenue() : 0.0;
        stats.pendingPayments = salesOrderRepository.getPendingPaymentAmount() != null ? salesOrderRepository.getPendingPaymentAmount() : 0.0;
        return stats;
    }
    
    // Inner class for statistics
    public static class SalesOrderStats {
        public long totalOrders;
        public long pendingOrders;
        public long inProductionOrders;
        public long readyOrders;
        public long deliveredOrders;
        public double totalRevenue;
        public double pendingPayments;
    }
}