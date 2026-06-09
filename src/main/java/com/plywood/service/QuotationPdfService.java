package com.plywood.service;

import com.plywood.model.Quotation;
import com.plywood.model.QuotationItem;
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
public class QuotationPdfService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuotationPdfService.class);
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    // PANDIT FURNITURE Colors
    private static final Color RED_COLOR = new Color(234, 0, 0);  // Coral red like in image
    private static final Color BLACK_COLOR = new Color(0, 0, 0);
    private static final Color GRAY_COLOR = new Color(100, 100, 100);
    private static final Color LINE_COLOR = new Color(80, 80, 80);
    
    public byte[] generatePdf(Quotation quotation) throws IOException {
        logger.info("Generating PANDIT FURNITURE quotation PDF for: {}", quotation.getQuotationNumber());
        PDDocument document = new PDDocument();
        
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                float pageWidth = page.getMediaBox().getWidth();
                float margin = 50;
                
                // ============ TOP HEADER ============
                
                // Company Name in RED
                contentStream.setNonStrokingColor(RED_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 28);
                String companyName = quotation.getCompanyName() != null ? quotation.getCompanyName() : "PANDIT FURNITURE";
                float companyNameWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(companyName) / 1000 * 28;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - companyNameWidth) / 2, yPosition);
                contentStream.showText(companyName);
                contentStream.endText();
                yPosition -= 20;
                
                // Tagline
                contentStream.setNonStrokingColor(BLACK_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                String tagline = "ALL TYPE OF FURNITURE WORK, HOME DECORE AND ALL OTHER WOODEN";
                float taglineWidth = PDType1Font.HELVETICA.getStringWidth(tagline) / 1000 * 9;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - taglineWidth) / 2, yPosition);
                contentStream.showText(tagline);
                contentStream.endText();
                yPosition -= 12;
                
                // Skilled work line
                String skilledLine = "SKILLED WORK WE DO.";
                float skilledWidth = PDType1Font.HELVETICA.getStringWidth(skilledLine) / 1000 * 9;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - skilledWidth) / 2, yPosition);
                contentStream.showText(skilledLine);
                contentStream.endText();
                yPosition -= 15;
                
                // Top line
                contentStream.setStrokingColor(LINE_COLOR);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                yPosition -= 15;
                
                // Email and Mobile
                contentStream.setNonStrokingColor(BLACK_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                
                String email = quotation.getCompanyEmail() != null ? quotation.getCompanyEmail() : "vijaysandit5770@gmail.com";
                String mobile = quotation.getCompanyPhone() != null ? quotation.getCompanyPhone() : "9371044197";
                String contactLine = "Email id: " + email + "     Mob: " + mobile;
                float contactWidth = PDType1Font.HELVETICA.getStringWidth(contactLine) / 1000 * 9;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - contactWidth) / 2, yPosition);
                contentStream.showText(contactLine);
                contentStream.endText();
                yPosition -= 15;
                
                // Bottom line
                contentStream.setStrokingColor(LINE_COLOR);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                yPosition -= 30;
                
                // ============ QUOTATION TITLE ============
                contentStream.setNonStrokingColor(BLACK_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                String quotationTitle = "QUOTATION";
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(quotationTitle) / 1000 * 20;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
                contentStream.showText(quotationTitle);
                contentStream.endText();
                
                // Date on right
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                String dateStr = "Date: " + (quotation.getDate() != null ? quotation.getDate().format(dateFormatter) : "");
                contentStream.beginText();
                contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition + 2);
                contentStream.showText(dateStr);
                contentStream.endText();
                yPosition -= 30;
                
                // ============ TO SECTION ============
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("To");
                contentStream.endText();
                yPosition -= 15;
                
                // Customer details
                if (quotation.getCustomerName() != null) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(quotation.getCustomerName());
                    contentStream.endText();
                    yPosition -= 13;
                }
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                if (quotation.getCustomerAddress() != null && !quotation.getCustomerAddress().isEmpty()) {
                    String[] addressLines = quotation.getCustomerAddress().split(",");
                    for (String line : addressLines) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(line.trim());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                }
                
                if (quotation.getCustomerPhone() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Mob: " + quotation.getCustomerPhone());
                    contentStream.endText();
                    yPosition -= 12;
                }
                
                yPosition -= 20;
                
                // ============ ITEMS TABLE ============
                drawTableHeader(contentStream, yPosition, margin, pageWidth);
                yPosition -= 20;
                
                if (quotation.getItems() != null && quotation.getItems().size() > 0) {
                    int itemNumber = 1;
                    for (QuotationItem item : quotation.getItems()) {
                        drawTableRow(contentStream, yPosition, margin, itemNumber++, item);
                        yPosition -= 18;
                        
                        // Check if need new page
                        if (yPosition < 200) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            PDPageContentStream newStream = new PDPageContentStream(document, page);
                            yPosition = 750;
                            drawTableHeader(newStream, yPosition, margin, pageWidth);
                            yPosition -= 20;
                        }
                    }
                }
                
                yPosition -= 10;
                
                // ============ TOTALS ============
                float totalsX = pageWidth - margin - 180;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                
                // Subtotal
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Subtotal:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX + 100, yPosition);
                contentStream.showText("Rs. " + df.format(quotation.getSubTotal()));
                contentStream.endText();
                yPosition -= 15;
                
                // Tax
                if (quotation.getTaxRate() != null && quotation.getTaxRate() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX, yPosition);
                    contentStream.showText("Tax (" + quotation.getTaxRate() + "%):");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX + 100, yPosition);
                    contentStream.showText("Rs. " + df.format(quotation.getTaxAmount()));
                    contentStream.endText();
                    yPosition -= 15;
                }
                
                // Discount
                if (quotation.getDiscount() != null && quotation.getDiscount() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX, yPosition);
                    contentStream.showText("Discount (" + quotation.getDiscount() + "%):");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(totalsX + 100, yPosition);
                    contentStream.showText("- Rs. " + df.format(quotation.getDiscountAmount()));
                    contentStream.endText();
                    yPosition -= 15;
                }
                
                // Grand Total
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Grand Total:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(totalsX + 100, yPosition);
                contentStream.showText("Rs. " + df.format(quotation.getGrandTotal()));
                contentStream.endText();
                
                // ============ NOTES / TERMS ============
                yPosition = 180; // Fixed position for notes
                
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Notes:");
                contentStream.endText();
                yPosition -= 12;
                
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                String[] defaultNotes = {
                    "> Quotation deadline 1 month.",
                    "> 50% Advance has to be given.",
                    "> Mandatory to pay for material either work will stop.",
                    "> Incase of increase in length-width, additional rate will have to be given.",
                    "> As per the quotation, after the work is completed the remaining till has to be paid."
                };
                
                String[] notes = quotation.getNotes() != null && !quotation.getNotes().isEmpty() 
                    ? quotation.getNotes().split("\\n") 
                    : defaultNotes;
                
                for (String note : notes) {
                    if (note.trim().isEmpty()) continue;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 10, yPosition);
                    contentStream.showText(note.trim());
                    contentStream.endText();
                    yPosition -= 10;
                }
                
                // ============ BOTTOM COMPANY NAME ============
                yPosition = 50;
                contentStream.setNonStrokingColor(RED_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                String bottomName = companyName;
                float bottomWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(bottomName) / 1000 * 12;
                contentStream.beginText();
                contentStream.newLineAtOffset(pageWidth - margin - bottomWidth, yPosition);
                contentStream.showText(bottomName);
                contentStream.endText();
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
    
    private void drawTableHeader(PDPageContentStream contentStream, float yPosition, float margin, float pageWidth) throws IOException {
        contentStream.setNonStrokingColor(BLACK_COLOR);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
        
        float x = margin;
        
        // Sr No
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Sr");
        contentStream.endText();
        x += 30;
        
        // Description
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Description");
        contentStream.endText();
        x += 150;
        
        // Size
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Size");
        contentStream.endText();
        x += 55;
        
        // Sq Ft
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Sq Ft");
        contentStream.endText();
        x += 45;
        
        // Rate
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Rate");
        contentStream.endText();
        x += 50;
        
        // Qty
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Qty");
        contentStream.endText();
        x += 40;
        
        // Amount
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText("Amount");
        contentStream.endText();
        
        // Line under header
        contentStream.setStrokingColor(LINE_COLOR);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(margin, yPosition - 5);
        contentStream.lineTo(pageWidth - margin, yPosition - 5);
        contentStream.stroke();
    }
    
    private void drawTableRow(PDPageContentStream contentStream, float yPosition, float margin, int itemNumber, QuotationItem item) throws IOException {
        contentStream.setNonStrokingColor(BLACK_COLOR);
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        
        float x = margin;
        
        // Sr No
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(String.valueOf(itemNumber));
        contentStream.endText();
        x += 30;
        
        // Description
        String desc = item.getDescription() != null ? item.getDescription() : "";
        if (desc.length() > 30) {
            desc = desc.substring(0, 27) + "...";
        }
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(desc);
        contentStream.endText();
        x += 150;
        
        // Size
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(item.getSize() != null ? item.getSize() : "-");
        contentStream.endText();
        x += 55;
        
        // Sq Ft
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(item.getSqft() != null ? String.format("%.2f", item.getSqft()) : "-");
        contentStream.endText();
        x += 45;
        
        // Rate
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(item.getRatePerSqft() != null ? df.format(item.getRatePerSqft()) : "-");
        contentStream.endText();
        x += 50;
        
        // Qty
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(item.getQuantity() != null ? String.format("%.0f", item.getQuantity()) : "1");
        contentStream.endText();
        x += 40;
        
        // Amount
        contentStream.beginText();
        contentStream.newLineAtOffset(x, yPosition);
        contentStream.showText(df.format(item.getTotal()));
        contentStream.endText();
    }
}