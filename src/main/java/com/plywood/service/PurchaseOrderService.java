package com.plywood.service;

import com.plywood.model.Product;
import com.plywood.model.PurchaseOrder;
import com.plywood.model.PurchaseOrderItem;
import com.plywood.model.Supplier;
import com.plywood.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Purchase Order Service - Handles purchase order operations
 */
@Service
@Transactional
public class PurchaseOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);
    
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private InventoryService inventoryService;
    
    // Create and Update Operations
    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
        logger.info("Creating purchase order: {}", purchaseOrder.getPoNumber());
        
        // Set supplier details
        Supplier supplier = supplierService.getSupplier(purchaseOrder.getSupplierId());
        if (supplier != null) {
            purchaseOrder.setSupplierName(supplier.getName());
            purchaseOrder.setSupplierCode(supplier.getSupplierCode());
            purchaseOrder.setPaymentTerms(supplier.getPaymentTerms());
        }
        // ✅ CRITICAL FIX: Set bidirectional relationship for items
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                item.setPurchaseOrder(purchaseOrder);
                item.calculateTotalPrice();
            }
        }
        purchaseOrder.calculateTotals();
        return purchaseOrderRepository.save(purchaseOrder);
    }
    
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrder purchaseOrder) {
        PurchaseOrder existing = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + id));
        
        purchaseOrder.setId(id);
        purchaseOrder.setCreatedDate(existing.getCreatedDate());
     // ✅ CRITICAL FIX: Set bidirectional relationship for items
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                item.setPurchaseOrder(purchaseOrder);
                item.calculateTotalPrice();
            }
        }
        purchaseOrder.calculateTotals();
        
        logger.info("Updating purchase order: {} (ID: {})", purchaseOrder.getPoNumber(), id);
        return purchaseOrderRepository.save(purchaseOrder);
    }
    
    public void deletePurchaseOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
        logger.info("Deleted purchase order ID: {}", id);
    }
    
    // Read Operations
    public PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id).orElse(null);
    }
    
    public PurchaseOrder getPurchaseOrderByNumber(String poNumber) {
        return purchaseOrderRepository.findByPoNumber(poNumber).orElse(null);
    }
    
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }
    
    public List<PurchaseOrder> getRecentPurchaseOrders() {
        return purchaseOrderRepository.findTop50ByOrderByPoDateDesc();
    }
    
    public List<PurchaseOrder> getPurchaseOrdersBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId);
    }
    
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByStatusOrderByPoDateDesc(status);
    }
    
    public List<PurchaseOrder> getOverduePurchaseOrders() {
        return purchaseOrderRepository.findOverduePurchaseOrders(LocalDate.now());
    }
    
    public List<PurchaseOrder> getPurchaseOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return purchaseOrderRepository.findByPoDateBetween(startDate, endDate);
    }
    
    // Status Management
    public PurchaseOrder approvePurchaseOrder(Long id, String approvedBy) {
        PurchaseOrder po = getPurchaseOrder(id);
        if (po == null) {
            throw new RuntimeException("Purchase order not found: " + id);
        }
        
        if (!"DRAFT".equals(po.getStatus()) && !"PENDING".equals(po.getStatus())) {
            throw new RuntimeException("Only DRAFT or PENDING orders can be approved");
        }
        
        po.setStatus("APPROVED");
        po.setApprovedBy(approvedBy);
        po.setApprovedDate(LocalDateTime.now());
        
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        logger.info("Approved purchase order: {} by {}", po.getPoNumber(), approvedBy);
        
        return saved;
    }
    
    public PurchaseOrder markAsOrdered(Long id) {
        PurchaseOrder po = getPurchaseOrder(id);
        if (po == null) {
            throw new RuntimeException("Purchase order not found: " + id);
        }
        
        if (!"APPROVED".equals(po.getStatus())) {
            throw new RuntimeException("Only APPROVED orders can be marked as ORDERED");
        }
        
        po.setStatus("ORDERED");
        
        // Update supplier outstanding
        supplierService.updateOutstandingBalance(po.getSupplierId(), po.getGrandTotal());
        
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        logger.info("Marked purchase order as ORDERED: {}", po.getPoNumber());
        
        return saved;
    }
    
    public PurchaseOrder receivePurchaseOrder(Long id, Map<Long, Double> receivedQuantities) {
        PurchaseOrder po = getPurchaseOrder(id);
        if (po == null) {
            throw new RuntimeException("Purchase order not found: " + id);
        }
        
        if (!"ORDERED".equals(po.getStatus())) {
            throw new RuntimeException("Only ORDERED purchase orders can be received");
        }
        
        // Update received quantities and add to inventory
        for (PurchaseOrderItem item : po.getItems()) {
            Double receivedQty = receivedQuantities.get(item.getId());
            if (receivedQty != null && receivedQty > 0) {
                item.setReceivedQuantity(item.getReceivedQuantity() + receivedQty);
                
                // Add to inventory
                inventoryService.addStock(
                    item.getProductId(),
                    receivedQty,
                    item.getUnitPrice(),
                    "PURCHASE",
                    po.getPoNumber(),
                    po.getSupplierName(),
                    "Received from PO: " + po.getPoNumber()
                );
            }
        }
        
        // Check if fully received
        boolean fullyReceived = po.getItems().stream()
            .allMatch(PurchaseOrderItem::isFullyReceived);
        
        if (fullyReceived) {
            po.setStatus("RECEIVED");
            po.setActualDeliveryDate(LocalDate.now());
        }
        
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        logger.info("Received items for purchase order: {}", po.getPoNumber());
        
        return saved;
    }
    
    public PurchaseOrder cancelPurchaseOrder(Long id, String reason) {
        PurchaseOrder po = getPurchaseOrder(id);
        if (po == null) {
            throw new RuntimeException("Purchase order not found: " + id);
        }
        
        if ("RECEIVED".equals(po.getStatus())) {
            throw new RuntimeException("Cannot cancel a RECEIVED purchase order");
        }
        
        po.setStatus("CANCELLED");
        po.setNotes((po.getNotes() != null ? po.getNotes() + "\n" : "") + 
                    "Cancelled: " + reason);
        
        // Reverse outstanding if already ordered
        if ("ORDERED".equals(po.getStatus())) {
            supplierService.clearOutstanding(po.getSupplierId(), po.getGrandTotal());
        }
        
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        logger.info("Cancelled purchase order: {} - Reason: {}", po.getPoNumber(), reason);
        
        return saved;
    }
    
    // Item Management
    public PurchaseOrder addItem(Long poId, PurchaseOrderItem item) {
        PurchaseOrder po = getPurchaseOrder(poId);
        if (po == null) {
            throw new RuntimeException("Purchase order not found: " + poId);
        }
        
        if (!"DRAFT".equals(po.getStatus())) {
            throw new RuntimeException("Can only add items to DRAFT purchase orders");
        }
        
        // Set product details
        Product product = inventoryService.getProduct(item.getProductId());
        if (product != null) {
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getName());
            item.setUnit(product.getUnit());
        }
        
        po.addItem(item);
        return purchaseOrderRepository.save(po);
    }
    
    // Statistics and Reports
    public Map<String, Object> getPurchaseOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOrders", purchaseOrderRepository.count());
        stats.put("draftCount", purchaseOrderRepository.countByStatus("DRAFT"));
        stats.put("pendingCount", purchaseOrderRepository.countByStatus("PENDING"));
        stats.put("approvedCount", purchaseOrderRepository.countByStatus("APPROVED"));
        stats.put("orderedCount", purchaseOrderRepository.countByStatus("ORDERED"));
        stats.put("receivedCount", purchaseOrderRepository.countByStatus("RECEIVED"));
        stats.put("cancelledCount", purchaseOrderRepository.countByStatus("CANCELLED"));
        
        stats.put("orderedAmount", purchaseOrderRepository.getTotalAmountByStatus("ORDERED"));
        stats.put("receivedAmount", purchaseOrderRepository.getTotalAmountByStatus("RECEIVED"));
        
        stats.put("overdueOrders", getOverduePurchaseOrders().size());
        
        return stats;
    }
    
    public double getTotalPurchaseAmount(LocalDate startDate, LocalDate endDate) {
        Double total = purchaseOrderRepository.getTotalPurchaseAmount(startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    // Validation
    public boolean isPoNumberUnique(String poNumber) {
        return purchaseOrderRepository.findByPoNumber(poNumber).isEmpty();
    }
    
    public boolean isPoNumberUnique(String poNumber, Long excludeId) {
        return purchaseOrderRepository.findByPoNumber(poNumber)
            .map(po -> po.getId().equals(excludeId))
            .orElse(true);
    }
}