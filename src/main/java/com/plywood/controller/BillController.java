package com.plywood.controller;

import com.plywood.model.Bill;
import com.plywood.model.BillItem;
import com.plywood.model.Quotation;
import com.plywood.model.SalesOrder;
import com.plywood.model.PaymentStatus;
import com.plywood.repository.QuotationRepository;
import com.plywood.repository.SalesOrderRepository;
import com.plywood.service.BillPdfService;
import com.plywood.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.File;
import org.springframework.http.HttpStatus;
import com.plywood.service.WhatsAppService;

@Controller
@RequestMapping
public class BillController {

    @Autowired private BillPdfService      pdfService;
    @Autowired private BillService         billService;
    @Autowired private QuotationRepository quotationRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private WhatsAppService whatsAppService;

    // ===== THYMELEAF VIEWS =====

    /** Bill creation / edit form */
    @GetMapping("/bill")
    public String showBill(@RequestParam(name = "from", required = false) String fromQuotation,
                           Model model) {
        if (fromQuotation != null && !fromQuotation.isBlank()) {
            Optional<Quotation> optQ = quotationRepository.findByQuotationNumber(fromQuotation);
            if (optQ.isPresent()) {
                Quotation q = optQ.get();
                Bill bill = new Bill();

                bill.setBillNumber(q.getQuotationNumber().replaceFirst("^QT-", "INV-"));
                bill.setDate(LocalDate.now());
                bill.setDueDate(LocalDate.now().plusDays(30));

                bill.setCustomerName(q.getCustomerName());
                bill.setCustomerAddress(q.getCustomerAddress());
                bill.setCustomerPhone(q.getCustomerPhone());
                bill.setCustomerEmail(q.getCustomerEmail());

                bill.setCompanyName(q.getCompanyName());
                bill.setCompanyAddress(q.getCompanyAddress());
                bill.setCompanyPhone(q.getCompanyPhone());
                bill.setCompanyEmail(q.getCompanyEmail());

                bill.setTaxRate(q.getTaxRate() != null ? q.getTaxRate() : 0.0);
                bill.setDiscount(q.getDiscount() != null ? q.getDiscount() : 0.0);
                bill.setNotes(q.getNotes());
                bill.setSourceQuotationNumber(q.getQuotationNumber());

                // ── Inherit payment status from the linked Sales Order ──────
                // If a Sales Order already exists for this quotation and its
                // payment is PAID, pre-mark the bill as PAID so it doesn't
                // incorrectly start as DRAFT.
                if (q.getId() != null) {
                    salesOrderRepository.findByQuotationId(q.getId()).ifPresent(so -> {
                        if (PaymentStatus.PAID.equals(so.getPaymentStatus())) {
                            bill.setStatus("PAID");
                        } else if (PaymentStatus.PARTIAL.equals(so.getPaymentStatus())) {
                            // Partial payment → leave as DRAFT but note it
                            // (business decision: bill still needs to be issued)
                            bill.setStatus("DRAFT");
                        }
                    });
                }

                // Map QuotationItems → BillItems
                if (q.getItems() != null) {
                    q.getItems().forEach(qi -> {
                        BillItem bi = new BillItem();
                        bi.setDescription(qi.getDescription());
                        bi.setQuantity(qi.getQuantity());
                        bi.setUnit(qi.getUnit());
                        bi.setUnitPrice(qi.getUnitPrice());
                        bi.setBill(bill);
                        bill.getItems().add(bi);
                    });
                }

                model.addAttribute("bill", bill);
                model.addAttribute("sourceQuotation", q);
            }
        }
        return "bill";
    }

    /** Bills list page */
    @GetMapping("/bills")
    public String showBillsList() {
        return "bills-list";
    }

    // ===== REST API =====

    /** Save a bill (called by browser after PDF generation) */
    @PostMapping("/api/bills")
    @ResponseBody
    public ResponseEntity<Bill> saveBill(@RequestBody Bill bill) {
        try {
            Bill saved = billService.saveBill(bill);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** List all bills */
    @GetMapping("/api/bills")
    @ResponseBody
    public ResponseEntity<?> getAllBills(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdDate").descending());
            return ResponseEntity.ok(billService.findAll(pageable));
        }
        return ResponseEntity.ok(billService.findAll());
    }

    /** Get single bill */
    @GetMapping("/api/bills/{id}")
    @ResponseBody
    public ResponseEntity<Bill> getBill(@PathVariable Long id) {
        return billService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Delete a bill */
    @DeleteMapping("/api/bills/{id}")
    @ResponseBody
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }

    /** Update bill status (PAID / OVERDUE / DRAFT) */
    @PatchMapping("/api/bills/{id}/status")
    @ResponseBody
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id,
                                             @RequestParam String status) {
        try {
            Bill updated = billService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Statistics for dashboard cards */
    @GetMapping("/api/bills/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics() {
        return billService.getStatistics();
    }

    /** Generate PDF */
    @PostMapping("/bill/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody Bill bill) {
        try {
            byte[] pdfBytes = pdfService.generatePdf(bill);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + bill.getBillNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            String message = "PDF generation failed: " +
                    (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            byte[] errorBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorBytes);
        }
    }

    @PostMapping("/api/bills/{id}/send-whatsapp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> sendBillWhatsApp(@PathVariable Long id) {
        try {
            Bill bill = billService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));
            String customerPhone = bill.getCustomerPhone();
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer phone number is missing"));
            }
            
            byte[] pdfBytes = pdfService.generatePdf(bill);
            
            File tempFile = File.createTempFile("invoice-" + bill.getBillNumber(), ".pdf");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }
            
            whatsAppService.sendBillPdf(customerPhone, tempFile, bill.getBillNumber());
            
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