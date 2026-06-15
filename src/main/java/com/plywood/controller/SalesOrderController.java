package com.plywood.controller;

import com.plywood.model.SalesOrder;
import com.plywood.model.SalesOrderStatus;
import com.plywood.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sales Order Controller
 * Handles order creation, status tracking, and payment management
 */
@Controller
public class SalesOrderController {
    
    @Autowired
    private SalesOrderService salesOrderService;
    
    // ============ VIEW PAGES ============
    
    @GetMapping("/sales-orders")
    public String salesOrdersPage() {
        return "sales-orders-list";
    }
    
    /**
     * /sales-order/new — templates/sales-order-create.html does not exist.
     * Orders are always created from an approved quotation, so redirect there.
     */
    @GetMapping("/sales-order/new")
    public String newSalesOrderPage() {
        return "redirect:/quotations-list";
    }
    
    /**
     * /sales-order/{id} — templates/sales-order-details.html does not exist.
     * Redirect to the orders list so the user can see the order there.
     */
    @GetMapping("/sales-order/{id}")
    public String salesOrderDetailsPage(@PathVariable Long id) {
        return "redirect:/sales-orders";
    }
    
    // ============ REST API - CRUD ============
    
    /**
     * Get all sales orders
     */
    @GetMapping("/api/sales-orders")
    @ResponseBody
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            if (page != null && size != null) {
                org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("orderDate").descending());
                return new ResponseEntity<>(salesOrderService.getAllOrders(pageable), HttpStatus.OK);
            }
            List<SalesOrder> orders = salesOrderService.getAllOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get sales order by ID
     */
    @GetMapping("/api/sales-orders/{id}")
    @ResponseBody
    public ResponseEntity<SalesOrder> getOrderById(@PathVariable Long id) {
        return salesOrderService.getOrderById(id)
                .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Get sales order by order number
     */
    @GetMapping("/api/sales-orders/number/{orderNumber}")
    @ResponseBody
    public ResponseEntity<SalesOrder> getOrderByNumber(@PathVariable String orderNumber) {
        return salesOrderService.getOrderByNumber(orderNumber)
                .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Get orders by customer
     */
    @GetMapping("/api/sales-orders/customer/{customerId}")
    @ResponseBody
    public ResponseEntity<List<SalesOrder>> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            List<SalesOrder> orders = salesOrderService.getOrdersByCustomer(customerId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get orders by status
     */
    @GetMapping("/api/sales-orders/status/{status}")
    @ResponseBody
    public ResponseEntity<List<SalesOrder>> getOrdersByStatus(@PathVariable String status) {
        try {
            SalesOrderStatus orderStatus = SalesOrderStatus.valueOf(status.toUpperCase());
            List<SalesOrder> orders = salesOrderService.getOrdersByStatus(orderStatus);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get pending orders
     */
    @GetMapping("/api/sales-orders/pending")
    @ResponseBody
    public ResponseEntity<List<SalesOrder>> getPendingOrders() {
        try {
            List<SalesOrder> orders = salesOrderService.getPendingOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get overdue orders
     */
    @GetMapping("/api/sales-orders/overdue")
    @ResponseBody
    public ResponseEntity<List<SalesOrder>> getOverdueOrders() {
        try {
            List<SalesOrder> orders = salesOrderService.getOverdueOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Create sales order from quotation
     */
    @PostMapping("/api/sales-orders/from-quotation/{quotationId}")
    @ResponseBody
    public ResponseEntity<?> createFromQuotation(@PathVariable Long quotationId) {
        try {
            SalesOrder order = salesOrderService.createFromQuotation(quotationId);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Failed to create sales order"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Create sales order manually
     */
    @PostMapping("/api/sales-orders")
    @ResponseBody
    public ResponseEntity<SalesOrder> createOrder(@RequestBody SalesOrder salesOrder) {
        try {
            SalesOrder created = salesOrderService.createSalesOrder(salesOrder);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update sales order
     */
    @PutMapping("/api/sales-orders/{id}")
    @ResponseBody
    public ResponseEntity<SalesOrder> updateOrder(@PathVariable Long id, @RequestBody SalesOrder salesOrder) {
        try {
            SalesOrder updated = salesOrderService.updateOrder(id, salesOrder);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete sales order
     */
    @DeleteMapping("/api/sales-orders/{id}")
    @ResponseBody
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable Long id) {
        try {
            salesOrderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ============ STATUS MANAGEMENT ============
    
    /**
     * Start production
     */
    @PostMapping("/api/sales-orders/{id}/start-production")
    @ResponseBody
    public ResponseEntity<SalesOrder> startProduction(@PathVariable Long id) {
        try {
            SalesOrder order = salesOrderService.startProduction(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Mark as ready for delivery
     */
    @PostMapping("/api/sales-orders/{id}/mark-ready")
    @ResponseBody
    public ResponseEntity<SalesOrder> markReady(@PathVariable Long id) {
        try {
            SalesOrder order = salesOrderService.markReady(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Mark as delivered
     */
    @PostMapping("/api/sales-orders/{id}/mark-delivered")
    @ResponseBody
    public ResponseEntity<SalesOrder> markDelivered(
            @PathVariable Long id,
            @RequestParam(required = false) String deliveryDate) {
        try {
            LocalDate date = deliveryDate != null ? LocalDate.parse(deliveryDate) : LocalDate.now();
            SalesOrder order = salesOrderService.markDelivered(id, date);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/api/sales-orders/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "No reason provided") String reason) {
        try {
            SalesOrder order = salesOrderService.cancelOrder(id, reason);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
    // ============ PAYMENT MANAGEMENT ============
    
    /**
     * Add payment
     */
    @PostMapping("/api/sales-orders/{id}/add-payment")
    @ResponseBody
    public ResponseEntity<SalesOrder> addPayment(
            @PathVariable Long id,
            @RequestParam Double amount) {
        try {
            SalesOrder order = salesOrderService.addPayment(id, amount);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // ============ STATISTICS ============
    
    /**
     * Get statistics
     */
    @GetMapping("/api/sales-orders/statistics")
    @ResponseBody
    public ResponseEntity<SalesOrderService.SalesOrderStats> getStatistics() {
        try {
            SalesOrderService.SalesOrderStats stats = salesOrderService.getStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}