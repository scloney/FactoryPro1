package com.plywood.controller;

import com.plywood.model.OptimizationResult;
import com.plywood.model.Rectangle;
import com.plywood.service.MaxRectOptimizerService;
import com.plywood.service.OptimizerPdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/optimizer")
public class OptimizerController {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimizerController.class);
    
    @Autowired
    private MaxRectOptimizerService optimizerService;
    
    @Autowired
    private OptimizerPdfService pdfService;
    
    @GetMapping
    public String showOptimizer() {
        return "optimizer";
    }
    
    @PostMapping("/optimize")
    @ResponseBody
    public ResponseEntity<?> optimize(@RequestBody OptimizerRequest request) {
        try {
            logger.info("Received optimization request: sheetWidth={}, sheetHeight={}, rectangles={}", 
                       request.getSheetWidth(), request.getSheetHeight(), 
                       request.getRectangles() != null ? request.getRectangles().size() : 0);
            
            // Validate input
            if (request.getSheetWidth() <= 0 || request.getSheetHeight() <= 0) {
                logger.error("Invalid sheet dimensions");
                return ResponseEntity.badRequest().body("Invalid sheet dimensions");
            }
            
            if (request.getRectangles() == null || request.getRectangles().isEmpty()) {
                logger.error("No rectangles provided");
                return ResponseEntity.badRequest().body("No rectangles provided");
            }
            
            List<Rectangle> rectangles = new ArrayList<>();
            int id = 1;
            for (RectangleInput input : request.getRectangles()) {
                if (input.getWidth() <= 0 || input.getHeight() <= 0 || input.getQuantity() <= 0) {
                    logger.error("Invalid rectangle: width={}, height={}, quantity={}", 
                                input.getWidth(), input.getHeight(), input.getQuantity());
                    return ResponseEntity.badRequest().body("Invalid rectangle dimensions");
                }
                rectangles.add(new Rectangle(id++, input.getWidth(), input.getHeight(), input.getQuantity()));
            }
            
            OptimizationResult result = optimizerService.optimize(
                rectangles, 
                request.getSheetWidth(), 
                request.getSheetHeight(), 
                request.isAllowRotation()
            );
            
            logger.info("Optimization completed successfully: {} sheets used", result.getTotalSheets());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Optimization failed with error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Optimization failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestBody PdfRequest request) {
        try {
            logger.info("Generating PDF report");
            
            if (request.getResult() == null) {
                logger.error("No optimization result provided");
                return ResponseEntity.badRequest().build();
            }
            
            OptimizationResult result = request.getResult();
            byte[] pdfBytes = pdfService.generatePdf(result, request.getSheetWidth(), request.getSheetHeight());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "plywood-optimization.pdf");
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("PDF generation failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Request DTOs
    public static class OptimizerRequest {
        private double sheetWidth;
        private double sheetHeight;
        private List<RectangleInput> rectangles;
        private boolean allowRotation;
        
        public double getSheetWidth() { return sheetWidth; }
        public void setSheetWidth(double sheetWidth) { this.sheetWidth = sheetWidth; }
        public double getSheetHeight() { return sheetHeight; }
        public void setSheetHeight(double sheetHeight) { this.sheetHeight = sheetHeight; }
        public List<RectangleInput> getRectangles() { return rectangles; }
        public void setRectangles(List<RectangleInput> rectangles) { this.rectangles = rectangles; }
        public boolean isAllowRotation() { return allowRotation; }
        public void setAllowRotation(boolean allowRotation) { this.allowRotation = allowRotation; }
    }
    
    public static class RectangleInput {
        private double width;
        private double height;
        private int quantity;
        
        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }
        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    public static class PdfRequest {
        private OptimizationResult result;
        private double sheetWidth;
        private double sheetHeight;
        
        public OptimizationResult getResult() { return result; }
        public void setResult(OptimizationResult result) { this.result = result; }
        public double getSheetWidth() { return sheetWidth; }
        public void setSheetWidth(double sheetWidth) { this.sheetWidth = sheetWidth; }
        public double getSheetHeight() { return sheetHeight; }
        public void setSheetHeight(double sheetHeight) { this.sheetHeight = sheetHeight; }
    }
}