# Quick Start Guide

## Running the Application

### Option 1: Using Maven (Recommended)

```bash
cd plywood-management-system
mvn spring-boot:run
```

Then open: http://localhost:8080

### Option 2: Building JAR and Running

```bash
mvn clean package
java -jar target/plywood-management-system-1.0.0.jar
```

## First Time Usage

### 1. Plywood Optimizer - Try This First!

1. Click "Open Optimizer" from home page
2. Keep default sheet size (2440 x 1220 mm)
3. Add some rectangles:
   - Rectangle 1: 600 x 400 mm, Quantity: 5
   - Rectangle 2: 800 x 300 mm, Quantity: 3
   - Rectangle 3: 500 x 500 mm, Quantity: 2
4. Keep "Allow Rotation" checked
5. Click "Optimize"
6. See the visualization and click "Download PDF"

**Expected Result**: Should use 1-2 sheets with good utilization (>70%)

### 2. Quotation Maker - Sample Data

**Company Details:**
- Name: ABC Plywood Supplies
- Address: 123 Industrial Area, City
- Phone: +91-1234567890
- Email: info@abcplywood.com

**Quotation Details:**
- Quotation No: QT-2024-001
- Date: Today's date (auto-filled)

**Customer Details:**
- Name: XYZ Furniture
- Address: 456 Market Street, City
- Phone: +91-9876543210
- Email: xyz@furniture.com

**Items:**
- 18mm BWP Plywood, 10 sheets, ₹1,200 per sheet
- 12mm MR Plywood, 5 sheets, ₹800 per sheet
- Edge Banding, 50 meters, ₹25 per meter

**Additional:**
- Tax Rate: 18% (GST)
- Discount: 5%

Click "Generate PDF" to see the quotation!

### 3. Bill Maker - Sample Invoice

Use similar details as quotation, but add:
- Invoice No: INV-2024-001
- Due Date: 30 days from today
- Company GSTIN: 22AAAAA0000A1Z5
- Customer GSTIN: 22BBBBB0000B1Z6
- Bank Details: HDFC Bank, A/c: 12345678, IFSC: HDFC0001234
- Payment Terms: Net 30 days

## Common Use Cases

### Case 1: Furniture Manufacturer
**Problem**: Need to cut 20 different sized panels from plywood sheets

**Solution**:
1. Use Optimizer to find best cutting layout
2. Generate cutting PDF for workshop
3. Use saved material for quotation
4. Create invoice after job completion

### Case 2: Plywood Dealer
**Problem**: Need to provide quick quotes to customers

**Solution**:
1. Use Quotation Maker with standard pricing
2. Add company logo in PDF (coming soon)
3. Email PDF to customer
4. Convert to invoice when order confirmed

### Case 3: Carpentry Business
**Problem**: Track projects and billing

**Solution**:
1. Use Optimizer for each project
2. Create quotation before starting
3. Generate invoice on completion
4. Keep records organized by project number

## Tips & Tricks

### Optimizer
- Sort your rectangles by size before adding for better visualization
- Use rotation for irregular shapes
- Try different sheet sizes (common: 2440x1220, 2745x1220)
- Download PDF to keep cutting records

### Quotation
- Use consistent quotation numbering (QT-YYYY-NNN)
- Set standard tax rates for your region
- Save commonly used items in a spreadsheet
- Update prices regularly

### Bill
- Match invoice numbers to quotation numbers
- Always include GSTIN for GST compliance
- Set clear payment terms
- Include bank details for easy payment

## Keyboard Shortcuts

- Enter key: Submit forms
- Tab: Navigate between fields
- Ctrl+P: Print PDF (when viewing)

## Troubleshooting Quick Fixes

### "Port 8080 already in use"
Change port in application.properties or kill existing process

### "PDF not downloading"
Check browser's download settings and pop-up blocker

### "Optimization is slow"
Reduce number of rectangles or simplify dimensions

### "Form not submitting"
Check all required fields are filled

## Next Steps

1. Customize company details in each tool
2. Try real project data
3. Experiment with different sheet sizes
4. Explore PDF outputs
5. Integrate into your workflow

## Support

- Read the full README.md for detailed documentation
- Check application.properties for configuration
- Review code comments for technical details

## Performance Notes

- Optimizer handles up to 100 rectangles efficiently
- PDF generation is near-instantaneous
- No database means no data persistence between sessions
- All calculations happen in real-time

Enjoy using the Plywood Management System! 🪵
