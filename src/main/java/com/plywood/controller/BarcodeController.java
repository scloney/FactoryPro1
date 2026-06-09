package com.plywood.controller;

import com.google.gson.JsonObject;
import com.plywood.model.Product;
import com.plywood.model.StockMovement;
import com.plywood.repository.ProductRepository;
import com.plywood.service.BarcodeService;
import com.plywood.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping
public class BarcodeController {

    private static final Logger log = LoggerFactory.getLogger(BarcodeController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BarcodeService barcodeService;

    @Autowired
    private InventoryService inventoryService;

    // ────────────────────────────────────────────────────────────────────────
    // API ENDPOINTS
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Endpoint 1: POST /api/barcode/generate/{productId}
     * Generates a barcode value, saves it to the product, and returns the barcode PNG image.
     * Aligns the barcode string value in header X-Barcode-Value.
     */
    @PostMapping("/api/barcode/generate/{productId}")
    @ResponseBody
    public ResponseEntity<?> generateBarcode(@PathVariable Long productId) {
        log.info("API request to generate barcode for product ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("Product ID {} not found for barcode generation", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = productOpt.get();
        String barcodeValue = barcodeService.generateProductBarcode(productId, product.getName());
        product.setBarcodeValue(barcodeValue);
        productRepository.save(product);

        byte[] barcodeImage = barcodeService.generateBarcodeImage(barcodeValue);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set("X-Barcode-Value", barcodeValue);
        // Expose X-Barcode-Value header to the frontend
        headers.add("Access-Control-Expose-Headers", "X-Barcode-Value");

        return new ResponseEntity<>(barcodeImage, headers, HttpStatus.OK);
    }

    /**
     * Endpoint 2: GET /api/barcode/image/{productId}
     * Returns the product barcode image as PNG.
     * Generates one if it doesn't already exist.
     */
    @GetMapping("/api/barcode/image/{productId}")
    @ResponseBody
    public ResponseEntity<?> getBarcodeImage(@PathVariable Long productId) {
        log.info("API request to fetch barcode image for product ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = productOpt.get();
        String barcodeValue = product.getBarcodeValue();
        if (barcodeValue == null || barcodeValue.trim().isEmpty()) {
            log.info("No barcode value found for product ID {}. Generating new barcode first.", productId);
            barcodeValue = barcodeService.generateProductBarcode(productId, product.getName());
            product.setBarcodeValue(barcodeValue);
            productRepository.save(product);
        }

        byte[] barcodeImage = barcodeService.generateBarcodeImage(barcodeValue);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set("X-Barcode-Value", barcodeValue);

        return new ResponseEntity<>(barcodeImage, headers, HttpStatus.OK);
    }

    /**
     * Endpoint 3: GET /api/barcode/qr/{productId}
     * Returns a product QR code PNG.
     * Content is JSON: {"id": productId, "name": productName, "unit": unit, "barcode": barcodeValue}
     */
    @GetMapping("/api/barcode/qr/{productId}")
    @ResponseBody
    public ResponseEntity<?> getQRCodeImage(@PathVariable Long productId) {
        log.info("API request to fetch QR code image for product ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = productOpt.get();
        String barcodeValue = product.getBarcodeValue();
        if (barcodeValue == null || barcodeValue.trim().isEmpty()) {
            barcodeValue = barcodeService.generateProductBarcode(productId, product.getName());
            product.setBarcodeValue(barcodeValue);
            productRepository.save(product);
        }

        // Construct JSON content
        JsonObject qrJson = new JsonObject();
        qrJson.addProperty("id", product.getId());
        qrJson.addProperty("name", product.getName());
        qrJson.addProperty("unit", product.getUnit());
        qrJson.addProperty("barcode", barcodeValue);

        String content = qrJson.toString();
        byte[] qrImage = barcodeService.generateQRCode(content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(qrImage, headers, HttpStatus.OK);
    }

    /**
     * Endpoint 4: POST /api/barcode/scan
     * Accepts multipart image file upload, decodes barcode/QR code, and returns product details.
     */
    @PostMapping("/api/barcode/scan")
    @ResponseBody
    public ResponseEntity<?> scanBarcodeImage(@RequestParam("file") MultipartFile file) {
        log.info("API request to decode and scan barcode from uploaded file: {}", file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Uploaded file is empty"));
            }

            String decodedValue = barcodeService.decodeBarcodeFromImage(file.getBytes());
            return findProductByDecodedValue(decodedValue);

        } catch (Exception e) {
            log.warn("Failed to decode barcode from image upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No barcode detected in image"));
        }
    }

    /**
     * Endpoint 4b: GET /api/barcode/scan
     * Helper endpoint that accepts barcodeValue query parameter instead of image upload.
     * Highly beneficial for frontend/camera scanning libraries.
     */
    @GetMapping("/api/barcode/scan")
    @ResponseBody
    public ResponseEntity<?> scanBarcodeValue(@RequestParam("barcodeValue") String barcodeValue) {
        log.info("API request to fetch product details from scanned barcode value: {}", barcodeValue);
        return findProductByDecodedValue(barcodeValue);
    }

    /**
     * Endpoint 5: POST /api/barcode/scan-and-update-stock
     * Accepts multipart file + stockChange (Integer) + type (String: IN or OUT) as request params.
     * Also supports optional client-side decoded barcodeValue string to avoid redundant image upload.
     */
    @PostMapping("/api/barcode/scan-and-update-stock")
    @ResponseBody
    public ResponseEntity<?> scanAndUpdateStock(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "barcodeValue", required = false) String barcodeValue,
            @RequestParam("stockChange") Integer stockChange,
            @RequestParam("type") String type) {

        log.info("API request to scan and update stock. Change: {}, Type: {}", stockChange, type);

        if (stockChange == null || stockChange <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Stock change must be a positive integer"));
        }

        String decodedValue;
        try {
            if (file != null && !file.isEmpty()) {
                decodedValue = barcodeService.decodeBarcodeFromImage(file.getBytes());
            } else if (barcodeValue != null && !barcodeValue.trim().isEmpty()) {
                decodedValue = barcodeValue.trim();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Either image file or decoded barcodeValue is required"));
            }
        } catch (Exception e) {
            log.warn("Barcode decode failed for stock update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No barcode detected in image"));
        }

        // Fetch product
        Product product = null;
        if (decodedValue.startsWith("PLY-")) {
            String[] parts = decodedValue.split("-");
            if (parts.length >= 2) {
                try {
                    Long productId = Long.parseLong(parts[1]);
                    product = productRepository.findById(productId).orElse(null);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (product == null) {
            product = productRepository.findByBarcodeValue(decodedValue).orElse(null);
        }

        if (product == null) {
            log.warn("Decoded value '{}' did not match any product", decodedValue);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        double previousStock = product.getCurrentStock();
        double newStock;

        try {
            if ("IN".equalsIgnoreCase(type)) {
                inventoryService.addStock(
                        product.getId(),
                        stockChange.doubleValue(),
                        product.getCostPrice(),
                        "PURCHASE",
                        "BARCODE_SCAN_IN",
                        "Barcode Stock Update",
                        "Added stock via mobile scan"
                );
                newStock = previousStock + stockChange;
            } else if ("OUT".equalsIgnoreCase(type)) {
                if (previousStock < stockChange) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Insufficient stock. Available: " + previousStock + ", Requested: " + stockChange));
                }
                inventoryService.removeStock(
                        product.getId(),
                        stockChange.doubleValue(),
                        product.getCostPrice(),
                        "SALE",
                        "BARCODE_SCAN_OUT",
                        "Barcode Stock Update",
                        "Subtracted stock via mobile scan"
                );
                newStock = previousStock - stockChange;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid stock update type. Must be IN or OUT"));
            }
        } catch (Exception e) {
            log.error("Stock update transaction failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("productName", product.getName());
        response.put("previousStock", previousStock);
        response.put("newStock", newStock);
        response.put("type", type.toUpperCase());
        response.put("message", "Successfully updated stock for " + product.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Priority 9: POST /api/barcode/generate-all
     * Bulk generate barcodeValues for all active products that lack one.
     */
    @PostMapping("/api/barcode/generate-all")
    @ResponseBody
    public ResponseEntity<?> generateAllBarcodes() {
        log.info("API request to bulk generate barcodes for all eligible products");
        List<Product> products = productRepository.findAll();
        int count = 0;

        for (Product product : products) {
            if (product.getBarcodeValue() == null || product.getBarcodeValue().trim().isEmpty()) {
                String barcodeValue = barcodeService.generateProductBarcode(product.getId(), product.getName());
                product.setBarcodeValue(barcodeValue);
                productRepository.save(product);
                count++;
            }
        }

        log.info("Bulk barcode generation complete. Barcodes generated: {}", count);
        return ResponseEntity.ok(Map.of(
                "generated", count,
                "message", "Generated barcodes for " + count + " products"
        ));
    }

    // ────────────────────────────────────────────────────────────────────────
    // PAGE VIEW CONTROLLER ENDPOINTS
    // ────────────────────────────────────────────────────────────────────────

    /**
     * GET /barcode/print
     * Returns Thymeleaf template with all products.
     */
    @GetMapping("/barcode/print")
    public String getBarcodePrintPage(Model model) {
        log.info("Loading barcode print template");
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "barcode-print";
    }

    /**
     * GET /barcode/scan
     * Returns Thymeleaf template for barcode scanning.
     */
    @GetMapping("/barcode/scan")
    public String getBarcodeScanPage() {
        log.info("Loading barcode scan template");
        return "barcode-scan";
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ────────────────────────────────────────────────────────────────────────

    private ResponseEntity<?> findProductByDecodedValue(String decodedValue) {
        Product product = null;

        // Try extracting from standard format PLY-{productId}-{timestamp}
        if (decodedValue.startsWith("PLY-")) {
            String[] parts = decodedValue.split("-");
            if (parts.length >= 2) {
                try {
                    Long productId = Long.parseLong(parts[1]);
                    product = productRepository.findById(productId).orElse(null);
                } catch (NumberFormatException ignored) {}
            }
        }

        // If not found by product ID format, query barcode value directly
        if (product == null) {
            product = productRepository.findByBarcodeValue(decodedValue).orElse(null);
        }

        if (product == null) {
            log.warn("Decoded barcode value '{}' does not match any product", decodedValue);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        log.info("Product found for barcode scan: {} (ID: {})", product.getName(), product.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("id", product.getId());
        response.put("name", product.getName());
        response.put("unit", product.getUnit());
        response.put("currentStock", product.getCurrentStock());
        response.put("barcodeValue", product.getBarcodeValue());

        return ResponseEntity.ok(response);
    }
}
