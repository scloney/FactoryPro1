package com.plywood.service;

import com.plywood.model.Product;
import com.plywood.model.StockMovement;
import com.plywood.repository.ProductRepository;
import com.plywood.repository.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Service - Spring Boot 3.x Compatible
 * Handles all inventory operations using JPA
 */
@Service
@Transactional
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private com.plywood.repository.BillRepository billRepository;

    public com.plywood.repository.BillRepository getBillRepository() {
        return billRepository;
    }

    public org.springframework.data.domain.Page<Product> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    // Product Operations
    public Product createProduct(Product product) {
        logger.info("Creating product: {}", product.getName());
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        product.setId(id);
        product.setCreatedDate(existing.getCreatedDate());
        logger.info("Updating product: {} (ID: {})", product.getName(), id);
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        logger.info("Deleted product ID: {}", id);
    }
    
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public Product getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode).orElse(null);
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getActiveProducts() {
        return productRepository.findByActive(true);
    }
    
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }
    
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm);
    }
    
    // Stock Movement Operations
    public StockMovement addStock(Long productId, double quantity, double unitPrice, 
                                  String transactionType, String referenceNumber, 
                                  String partyName, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setProductCode(product.getProductCode());
        movement.setProductName(product.getName());
        movement.setMovementType("INWARD");
        movement.setTransactionType(transactionType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(product.getCurrentStock());
        movement.setNewStock(product.getCurrentStock() + quantity);
        movement.setUnitPrice(unitPrice);
        movement.setTotalValue(quantity * unitPrice);
        movement.setReferenceNumber(referenceNumber);
        movement.setPartyName(partyName);
        movement.setNotes(notes);
        movement.setLocation(product.getLocation());
        movement.setMovementDate(LocalDateTime.now());
        
        // Update product stock
        product.setCurrentStock(product.getCurrentStock() + quantity);
        productRepository.save(product);
        
        StockMovement saved = stockMovementRepository.save(movement);
        logger.info("Added stock: {} units of {} (ID: {})", quantity, product.getName(), productId);
        
        return saved;
    }
    
    public StockMovement removeStock(Long productId, double quantity, double unitPrice,
                                     String transactionType, String referenceNumber,
                                     String partyName, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        if (product.getCurrentStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getCurrentStock() + 
                                     ", Requested: " + quantity);
        }
        
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setProductCode(product.getProductCode());
        movement.setProductName(product.getName());
        movement.setMovementType("OUTWARD");
        movement.setTransactionType(transactionType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(product.getCurrentStock());
        movement.setNewStock(product.getCurrentStock() - quantity);
        movement.setUnitPrice(unitPrice);
        movement.setTotalValue(quantity * unitPrice);
        movement.setReferenceNumber(referenceNumber);
        movement.setPartyName(partyName);
        movement.setNotes(notes);
        movement.setLocation(product.getLocation());
        movement.setMovementDate(LocalDateTime.now());
        
        // Update product stock
        product.setCurrentStock(product.getCurrentStock() - quantity);
        productRepository.save(product);
        
        StockMovement saved = stockMovementRepository.save(movement);
        logger.info("Removed stock: {} units of {} (ID: {})", quantity, product.getName(), productId);
        
        return saved;
    }
    
    public StockMovement adjustStock(Long productId, double newQuantity, String reason, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        double previousStock = product.getCurrentStock();
        double difference = newQuantity - previousStock;
        
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setProductCode(product.getProductCode());
        movement.setProductName(product.getName());
        movement.setMovementType("ADJUSTMENT");
        movement.setTransactionType("ADJUSTMENT");
        movement.setQuantity(Math.abs(difference));
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newQuantity);
        movement.setReason(reason);
        movement.setNotes(notes);
        movement.setLocation(product.getLocation());
        movement.setMovementDate(LocalDateTime.now());
        
        // Update product stock
        product.setCurrentStock(newQuantity);
        productRepository.save(product);
        
        StockMovement saved = stockMovementRepository.save(movement);
        logger.info("Adjusted stock: {} → {} for {} (ID: {})", previousStock, newQuantity, 
                   product.getName(), productId);
        
        return saved;
    }
    
    public List<StockMovement> getProductMovements(Long productId) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId);
    }
    
    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findTop50ByOrderByMovementDateDesc();
    }
    
    public List<StockMovement> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return stockMovementRepository.findByMovementDateBetweenOrderByMovementDateDesc(startDate, endDate);
    }
    
    // Reports and Analytics
    public double getTotalInventoryValue() {
        Double value = productRepository.getTotalInventoryValue();
        return value != null ? value : 0.0;
    }
    
    public double getTotalPotentialRevenue() {
        Double revenue = productRepository.getTotalPotentialRevenue();
        return revenue != null ? revenue : 0.0;
    }
    
    public Map<String, Double> getInventoryValueByCategory() {
        List<Product> products = productRepository.findByActive(true);
        return products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    Collectors.summingDouble(Product::getStockValue)
                ));
    }
    
    public long getLowStockCount() {
        return productRepository.countLowStockProducts();
    }
    
    public long getOutOfStockCount() {
        return productRepository.countOutOfStockProducts();
    }
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("activeProducts", productRepository.findByActive(true).size());
        stats.put("lowStockCount", getLowStockCount());
        stats.put("outOfStockCount", getOutOfStockCount());
        stats.put("totalInventoryValue", getTotalInventoryValue());
        stats.put("potentialRevenue", getTotalPotentialRevenue());
        stats.put("categoryValues", getInventoryValueByCategory());
        return stats;
    }
}