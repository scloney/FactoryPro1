package com.plywood.controller;

import com.plywood.model.Quotation;
import com.plywood.model.QuotationStatus;
import com.plywood.repository.QuotationRepository;
import com.plywood.service.QuotationService;
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
import java.time.LocalDate;
import java.util.List;
import java.io.File;
import java.util.Map;
import com.plywood.service.WhatsAppService;

@Controller
public class QuotationController {
    
    @Autowired
    private QuotationService quotationService;
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private WhatsAppService whatsAppService;
    
    // ============ VIEW CONTROLLERS ============
    
    @GetMapping("/quotation")
    public String showQuotation() {
        return "quotation";
    }
    
    @GetMapping("/quotations-list")
    public String quotationListPage() {
        return "quotations-list";
    }
    
    // ============ REST API ENDPOINTS ============
    
    @PostMapping("/api/quotations")
    @ResponseBody
    public ResponseEntity<Quotation> createQuotation(@RequestBody Quotation quotation) {
        try {
            Quotation saved = quotationService.createQuotation(quotation);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/quotations")
    @ResponseBody
    public ResponseEntity<List<Quotation>> getAllQuotations() {
        try {
            List<Quotation> quotations = quotationService.getAllQuotations();
            return new ResponseEntity<>(quotations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/quotations/{id}")
    @ResponseBody
    public ResponseEntity<Quotation> getQuotationById(@PathVariable Long id) {
        return quotationService.getQuotationById(id)
                .map(quotation -> new ResponseEntity<>(quotation, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/api/quotations/number/{quotationNumber}")
    @ResponseBody
    public ResponseEntity<Quotation> getQuotationByNumber(@PathVariable String quotationNumber) {
        return quotationService.getQuotationByNumber(quotationNumber)
                .map(quotation -> new ResponseEntity<>(quotation, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/api/quotations/customer/{customerId}")
    @ResponseBody
    public ResponseEntity<List<Quotation>> getQuotationsByCustomer(@PathVariable Long customerId) {
        try {
            List<Quotation> quotations = quotationService.getQuotationsByCustomer(customerId);
            return new ResponseEntity<>(quotations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/quotations/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Quotation>> getQuotationsByStatus(@PathVariable String status) {
        try {
            QuotationStatus quotationStatus = QuotationStatus.valueOf(status.toUpperCase());
            List<Quotation> quotations = quotationService.getQuotationsByStatus(quotationStatus);
            return new ResponseEntity<>(quotations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/quotations/pending")
    @ResponseBody
    public ResponseEntity<List<Quotation>> getPendingQuotations() {
        try {
            List<Quotation> quotations = quotationService.getPendingQuotations();
            return new ResponseEntity<>(quotations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/api/quotations/{id}")
    @ResponseBody
    public ResponseEntity<Quotation> updateQuotation(@PathVariable Long id, 
                                                     @RequestBody Quotation quotation) {
        try {
            Quotation updated = quotationService.updateQuotation(id, quotation);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/api/quotations/{id}")
    @ResponseBody
    public ResponseEntity<HttpStatus> deleteQuotation(@PathVariable Long id) {
        try {
            quotationService.deleteQuotation(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ============ STATUS MANAGEMENT ============
    
    @PostMapping("/api/quotations/{id}/send")
    @ResponseBody
    public ResponseEntity<Quotation> sendQuotation(@PathVariable Long id) {
        try {
            Quotation quotation = quotationService.sendQuotation(id);
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/api/quotations/{id}/accept")
    @ResponseBody
    public ResponseEntity<Quotation> acceptQuotation(@PathVariable Long id) {
        try {
            Quotation quotation = quotationService.acceptQuotation(id);
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/api/quotations/{id}/reject")
    @ResponseBody
    public ResponseEntity<Quotation> rejectQuotation(@PathVariable Long id) {
        try {
            Quotation quotation = quotationService.rejectQuotation(id);
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/api/quotations/{id}/convert")
    @ResponseBody
    public ResponseEntity<Quotation> convertToSalesOrder(@PathVariable Long id) {
        try {
            Quotation quotation = quotationService.convertToSalesOrder(id);
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    // ============ APPROVAL ENDPOINT ============
    
    @PostMapping("/api/quotations/{id}/approve")
    @ResponseBody
    public ResponseEntity<Quotation> approveQuotation(
            @PathVariable Long id,
            @RequestParam("approvedBy")      String approvedBy,
            @RequestParam("approvalMethod")  String approvalMethod,
            @RequestParam("approvedDate")    String approvedDate,
            @RequestParam(value = "approvalRemarks", required = false) String remarks,
            @RequestParam(value = "proofImage", required = false) MultipartFile proofImage) {
        try {
            Quotation quotation = quotationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));

            // Save approval details
            quotation.setApprovalStatus("APPROVED");
            quotation.setApprovedBy(approvedBy);
            quotation.setApprovalMethod(approvalMethod);
            quotation.setApprovedDate(LocalDate.parse(approvedDate));
            quotation.setApprovalRemarks(remarks);
            quotation.setStatus(QuotationStatus.ACCEPTED);

            // Save uploaded proof image
            if (proofImage != null && !proofImage.isEmpty()) {
                String filename = "approval_" + id + "_" + System.currentTimeMillis()
                                + getExtension(proofImage.getOriginalFilename());
                Path uploadPath = Paths.get("uploads/" + filename);
                Files.createDirectories(uploadPath.getParent());
                Files.write(uploadPath, proofImage.getBytes());
                quotation.setApprovalImagePath(filename);
            }

            quotation.setModifiedDate(LocalDate.now());
            Quotation saved = quotationRepository.save(quotation);
            return new ResponseEntity<>(saved, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot) : ".jpg";
    }
    
    // ============ PDF GENERATION ============
    
    @GetMapping("/api/quotations/{id}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdfFromId(@PathVariable Long id) {
        try {
            byte[] pdfBytes = quotationService.generatePdf(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quotation-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/api/quotations/generate-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdfFromData(@RequestBody Quotation quotation) {
        try {
            byte[] pdfBytes = quotationService.generatePdfFromData(quotation);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quotation-" + quotation.getQuotationNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ============ STATISTICS ============
    
    @GetMapping("/api/quotations/statistics")
    @ResponseBody
    public ResponseEntity<QuotationService.QuotationStats> getStatistics() {
        try {
            QuotationService.QuotationStats stats = quotationService.getStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/quotations/{id}/send-whatsapp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> sendQuotationWhatsApp(@PathVariable Long id) {
        try {
            Quotation quotation = quotationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Quotation not found"));
            String customerPhone = quotation.getCustomerPhone();
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                if (quotation.getCustomer() != null) {
                    customerPhone = quotation.getCustomer().getPhone();
                }
            }
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer phone number is missing"));
            }
            
            byte[] pdfBytes = quotationService.generatePdf(id);
            
            File tempFile = File.createTempFile("quotation-" + quotation.getQuotationNumber(), ".pdf");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }
            
            whatsAppService.sendQuotationPdf(customerPhone, tempFile, quotation.getQuotationNumber());
            
            if (!tempFile.delete()) {
                tempFile.deleteOnExit();
            }
            
            return ResponseEntity.ok(Map.of("message", "WhatsApp message sent successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }
}