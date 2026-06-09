package com.plywood.repository;

import com.plywood.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by product code
    Optional<Product> findByProductCode(String productCode);

    // Find by barcode value
    Optional<Product> findByBarcodeValue(String barcodeValue);

    // Find by active status
    List<Product> findByActive(boolean active);

    // Find active products ordered by name
    List<Product> findByActiveOrderByNameAsc(boolean active);

    // Find by category
    List<Product> findByCategory(String category);

    // Find active products by category
    List<Product> findByCategoryAndActive(String category, boolean active);

    // Find low stock products (currentStock <= minStockLevel)
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockLevel AND p.active = true ORDER BY p.currentStock ASC")
    List<Product> findLowStockProducts();

    // Find out of stock products
    @Query("SELECT p FROM Product p WHERE p.currentStock <= 0 AND p.active = true ORDER BY p.name ASC")
    List<Product> findOutOfStockProducts();

    // Find overstock products
    @Query("SELECT p FROM Product p WHERE p.currentStock > p.maxStockLevel AND p.active = true ORDER BY p.currentStock DESC")
    List<Product> findOverstockProducts();

    // Search products by name or product code
    @Query("SELECT p FROM Product p WHERE p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    // Get total inventory value
    @Query("SELECT SUM(p.currentStock * p.costPrice) FROM Product p WHERE p.active = true")
    Double getTotalInventoryValue();

    // Get total potential revenue
    @Query("SELECT SUM(p.currentStock * p.sellingPrice) FROM Product p WHERE p.active = true")
    Double getTotalPotentialRevenue();

    // Get category-wise inventory value
    @Query("SELECT p.category, SUM(p.currentStock * p.costPrice) FROM Product p WHERE p.active = true GROUP BY p.category")
    List<Object[]> getCategoryWiseInventoryValue();

    // Count products by stock status
    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock <= 0 AND p.active = true")
    Long countOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock > 0 AND p.currentStock <= p.minStockLevel AND p.active = true")
    Long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock > p.minStockLevel AND p.active = true")
    Long countInStockProducts();

    // Find by supplier
    List<Product> findBySupplierNameAndActive(String supplierName, boolean active);

    // Find by location
    List<Product> findByLocationAndActive(String location, boolean active);

    // Get all categories
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
    List<String> findAllCategories();

    // Get all suppliers
    @Query("SELECT DISTINCT p.supplierName FROM Product p WHERE p.active = true AND p.supplierName IS NOT NULL ORDER BY p.supplierName")
    List<String> findAllSuppliers();

    // Get all locations
    @Query("SELECT DISTINCT p.location FROM Product p WHERE p.active = true AND p.location IS NOT NULL ORDER BY p.location")
    List<String> findAllLocations();
    
    List<Product> findByCategoryAndActiveTrue(String category);
}