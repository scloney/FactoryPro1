package com.plywood.service;

import com.plywood.model.Bill;
import com.plywood.model.BillItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class BillPdfService {
    
    private static final Logger logger = LoggerFactory.getLogger(BillPdfService.class);
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    public byte[] generatePdf(Bill bill) throws IOException {
        logger.info("Generating invoice PDF for bill: {}", bill.getBillNumber());
        PDDocument document = new PDDocument();
        
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                
                // Header with colored background
                contentStream.setNonStrokingColor(new Color(41, 128, 185));
                contentStream.addRect(0, yPosition - 5, 595, 40);
                contentStream.fill();
                
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 26);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition + 5);
                contentStream.showText("INVOICE");
                contentStream.endText();
                
                contentStream.setNonStrokingColor(Color.BLACK);
                yPosition -= 50;
                
                // Company details
                if (bill.getCompanyName() != null && !bill.getCompanyName().isEmpty()) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(bill.getCompanyName());
                    contentStream.endText();
                    yPosition -= 18;
                    
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    if (bill.getCompanyAddress() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText(bill.getCompanyAddress());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                    if (bill.getCompanyPhone() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("Phone: " + bill.getCompanyPhone());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                    if (bill.getCompanyEmail() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("Email: " + bill.getCompanyEmail());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                    if (bill.getCompanyGSTIN() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("GSTIN: " + bill.getCompanyGSTIN());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                }
                
                yPosition -= 15;
                
                // Bill details box
                float boxY = yPosition;
                contentStream.setStrokingColor(new Color(200, 200, 200));
                contentStream.setLineWidth(1f);
                contentStream.addRect(350, boxY - 55, 195, 60);
                contentStream.stroke();
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(360, boxY - 15);
                contentStream.showText("Invoice No:");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(440, boxY - 15);
                contentStream.showText(bill.getBillNumber() != null ? bill.getBillNumber() : "");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(360, boxY - 30);
                contentStream.showText("Date:");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(440, boxY - 30);
                contentStream.showText(bill.getDate() != null ? bill.getDate().format(dateFormatter) : "");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(360, boxY - 45);
                contentStream.showText("Due Date:");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(440, boxY - 45);
                contentStream.showText(bill.getDueDate() != null ? bill.getDueDate().format(dateFormatter) : "");
                contentStream.endText();
                
                yPosition -= 70;
                
                // Customer details
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Bill To:");
                contentStream.endText();
                yPosition -= 15;
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                if (bill.getCustomerName() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(bill.getCustomerName());
                    contentStream.endText();
                    yPosition -= 12;
                }
                if (bill.getCustomerAddress() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(bill.getCustomerAddress());
                    contentStream.endText();
                    yPosition -= 12;
                }
                if (bill.getCustomerPhone() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Phone: " + bill.getCustomerPhone());
                    contentStream.endText();
                    yPosition -= 12;
                }
                if (bill.getCustomerEmail() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Email: " + bill.getCustomerEmail());
                    contentStream.endText();
                    yPosition -= 12;
                }
                if (bill.getCustomerGSTIN() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("GSTIN: " + bill.getCustomerGSTIN());
                    contentStream.endText();
                    yPosition -= 12;
                }
                
                yPosition -= 20;
                
                // Items table
                drawTableHeader(contentStream, yPosition);
                yPosition -= 20;
                
                if (bill.getItems() != null) {
                    int itemNumber = 1;
                    for (BillItem item : bill.getItems()) {
                        drawTableRow(contentStream, yPosition, itemNumber++, item);
                        yPosition -= 18;
                    }
                }
                
                yPosition -= 10;
                
                // Totals section
                float totalsX = 380;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Subtotal:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX + 120, yPosition);
                contentStream.showText("Rs. " + df.format(bill.getSubTotal()));
                contentStream.endText();
                yPosition -= 15;
                
                if (bill.getDiscount() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX, yPosition);
                    contentStream.showText("Discount (" + bill.getDiscount() + "%):");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX + 120, yPosition);
                    contentStream.showText("- Rs. " + df.format(bill.getDiscountAmount()));
                    contentStream.endText();
                    yPosition -= 15;
                }
                
                if (bill.getTaxRate() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX, yPosition);
                    contentStream.showText("GST (" + bill.getTaxRate() + "%):");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX + 120, yPosition);
                    contentStream.showText("Rs. " + df.format(bill.getTaxAmount()));
                    contentStream.endText();
                    yPosition -= 15;
                }
                
                // Grand total box
                contentStream.setNonStrokingColor(new Color(52, 152, 219));
                contentStream.addRect(totalsX - 10, yPosition - 25, 175, 30);
                contentStream.fill();
                
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX, yPosition - 8);
                contentStream.showText("Grand Total:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX + 120, yPosition - 8);
                contentStream.showText("Rs. " + df.format(bill.getGrandTotal()));
                contentStream.endText();
                
                contentStream.setNonStrokingColor(Color.BLACK);
                yPosition -= 50;
                
                // Payment terms and bank details
                if (bill.getPaymentTerms() != null || bill.getBankDetails() != null) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Payment Information:");
                    contentStream.endText();
                    yPosition -= 15;
                    
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    if (bill.getPaymentTerms() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("Terms: " + bill.getPaymentTerms());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                    if (bill.getBankDetails() != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("Bank Details: " + bill.getBankDetails());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                }
                
                // Notes
                if (bill.getNotes() != null && !bill.getNotes().isEmpty()) {
                    yPosition -= 15;
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Notes:");
                    contentStream.endText();
                    yPosition -= 15;
                    
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(bill.getNotes());
                    contentStream.endText();
                }

                // Source quotation footer
                if (bill.getSourceQuotationNumber() != null && !bill.getSourceQuotationNumber().isBlank()) {
                    yPosition -= 30;
                    contentStream.setFont(PDType1Font.HELVETICA, 8);
                    contentStream.setNonStrokingColor(new Color(150, 150, 150));
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Generated from Quotation: " + bill.getSourceQuotationNumber());
                    contentStream.endText();
                    contentStream.setNonStrokingColor(Color.BLACK);
                }
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            logger.info("PDF generated successfully");
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to generate PDF", e);
            throw e;
        } finally {
            document.close();
        }
    }
    
    private void drawTableHeader(PDPageContentStream contentStream, float yPosition) throws IOException {
        contentStream.setNonStrokingColor(new Color(52, 152, 219));
        contentStream.addRect(50, yPosition - 5, 495, 20);
        contentStream.fill();
        
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        
        contentStream.beginText(); contentStream.newLineAtOffset(55, yPosition + 2); contentStream.showText("No."); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(85, yPosition + 2); contentStream.showText("Description"); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(310, yPosition + 2); contentStream.showText("Qty"); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(360, yPosition + 2); contentStream.showText("Unit"); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(410, yPosition + 2); contentStream.showText("Price"); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(485, yPosition + 2); contentStream.showText("Total"); contentStream.endText();
        
        contentStream.setNonStrokingColor(Color.BLACK);
    }
    
    private void drawTableRow(PDPageContentStream contentStream, float yPosition, int itemNumber, BillItem item) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        
        contentStream.beginText(); contentStream.newLineAtOffset(55, yPosition); contentStream.showText(String.valueOf(itemNumber)); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(85, yPosition); contentStream.showText(item.getDescription() != null ? item.getDescription() : ""); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(310, yPosition); contentStream.showText(String.valueOf(item.getQuantity())); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(360, yPosition); contentStream.showText(item.getUnit() != null ? item.getUnit() : ""); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(410, yPosition); contentStream.showText("Rs. " + df.format(item.getUnitPrice())); contentStream.endText();
        contentStream.beginText(); contentStream.newLineAtOffset(485, yPosition); contentStream.showText("Rs. " + df.format(item.getTotal())); contentStream.endText();
        
        contentStream.setStrokingColor(new Color(220, 220, 220));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, yPosition - 5);
        contentStream.lineTo(545, yPosition - 5);
        contentStream.stroke();
        contentStream.setStrokingColor(Color.BLACK);
    }
}