# Plywood Management System

A comprehensive Java Spring Boot application for plywood business management with three powerful tools:

## Features

### 1. 🪵 Plywood Optimizer
- **MaxRect Bin Packing Algorithm**: Intelligent optimization for cutting plywood sheets
- **Visual Layout Display**: Real-time visualization of piece placement on sheets
- **Rotation Support**: Option to rotate pieces for better space utilization
- **PDF Report Generation**: Downloadable cutting layout reports with statistics
- **Waste Minimization**: Calculates optimal layouts to reduce material waste
- **Utilization Metrics**: Shows percentage utilization for each sheet

### 2. 📋 Quotation Maker
- Create professional quotations with company branding
- Add multiple items with quantities and pricing
- Automatic calculations (subtotal, tax, discount, grand total)
- PDF generation for client sharing
- Customizable tax rates and discounts
- Notes and terms section

### 3. 🧾 Bill Maker
- Generate professional invoices with GST support
- Company and customer details with GSTIN
- Itemized billing with automatic calculations
- Payment terms and bank details
- Due date tracking
- Professional PDF invoice generation
- Colored headers and modern design

## Technology Stack

- **Backend**: Spring Boot 3.2.1
- **Java Version**: 17
- **Template Engine**: Thymeleaf
- **PDF Generation**: Apache PDFBox 2.0.30
- **Build Tool**: Maven
- **Frontend**: HTML5, CSS3, Vanilla JavaScript

## Project Structure

```
plywood-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/plywood/
│   │   │   ├── PlywoodManagementApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── OptimizerController.java
│   │   │   │   ├── QuotationController.java
│   │   │   │   └── BillController.java
│   │   │   ├── service/
│   │   │   │   ├── MaxRectOptimizerService.java
│   │   │   │   ├── OptimizerPdfService.java
│   │   │   │   ├── QuotationPdfService.java
│   │   │   │   └── BillPdfService.java
│   │   │   └── model/
│   │   │       ├── Rectangle.java
│   │   │       ├── Sheet.java
│   │   │       ├── FreeSpace.java
│   │   │       ├── OptimizationResult.java
│   │   │       ├── Quotation.java
│   │   │       ├── QuotationItem.java
│   │   │       └── Bill.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── index.html
│   │           ├── optimizer.html
│   │           ├── quotation.html
│   │           └── bill.html
│   └── test/
└── pom.xml
```

## Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps

1. **Clone or extract the project**
```bash
cd plywood-management-system
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
Open your browser and navigate to: `http://localhost:8080`

## Usage Guide

### Plywood Optimizer

1. Navigate to the Optimizer tool
2. Set sheet dimensions (default: 2440mm x 1220mm)
3. Add rectangles to cut with dimensions and quantities
4. Choose whether to allow rotation
5. Click "Optimize" to see the layout
6. Download PDF report with visualizations

**Algorithm**: The MaxRect (Maximal Rectangles) algorithm uses the Best Short Side Fit (BSSF) heuristic:
- Sorts rectangles by area (largest first)
- Places each rectangle in the free space with minimum leftover
- Splits free spaces efficiently after placement
- Supports rotation for better utilization

### Quotation Maker

1. Navigate to the Quotation tool
2. Fill in company details
3. Enter quotation number and date
4. Add customer information
5. Add items with descriptions, quantities, and prices
6. Set tax rate and discount (if applicable)
7. Add notes or terms
8. Generate PDF quotation

### Bill Maker

1. Navigate to the Bill tool
2. Enter company details including GSTIN
3. Set invoice number, date, and due date
4. Fill in customer details with GSTIN
5. Add billable items
6. Configure GST rate and discounts
7. Add payment terms and bank details
8. Generate professional invoice PDF

## API Endpoints

### Optimizer
- `GET /optimizer` - Optimizer page
- `POST /optimizer/optimize` - Run optimization algorithm
- `POST /optimizer/download-pdf` - Generate PDF report

### Quotation
- `GET /quotation` - Quotation maker page
- `POST /quotation/generate-pdf` - Generate quotation PDF

### Bill
- `GET /bill` - Bill maker page
- `POST /bill/generate-pdf` - Generate invoice PDF

## Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Features in Detail

### MaxRect Algorithm
The optimizer implements the MaxRect bin packing algorithm with:
- **Best Short Side Fit (BSSF)**: Selects positions minimizing leftover space
- **Free Space Management**: Efficiently tracks and splits available areas
- **Overlap Prevention**: Ensures no pieces overlap
- **Rotation Support**: Can rotate pieces 90° for better fit
- **Multi-sheet Support**: Automatically uses additional sheets when needed

### PDF Generation
All PDFs are generated server-side using Apache PDFBox:
- High-quality vector graphics
- Professional layouts
- Customizable fonts and colors
- Automatic page breaks
- Currency formatting (₹)
- Date formatting

## Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Check if port 8080 is available
- Verify Maven is installed: `mvn -version`

### PDF generation fails
- Check console for error messages
- Ensure sufficient memory is allocated
- Verify all required fields are filled

### Optimization takes too long
- Reduce number of rectangles
- Simplify dimensions
- Consider pre-sorting similar sizes

## Future Enhancements

Potential features for future versions:
- Database integration for saving projects
- User authentication and multi-tenancy
- Export to Excel/CSV
- Email functionality for sending documents
- Material cost calculations
- Inventory management
- Customer relationship management (CRM)
- Advanced reporting and analytics

## License

This project is provided as-is for educational and commercial use.

## Support

For issues, questions, or contributions:
- Check the documentation
- Review the code comments
- Test with sample data

## Credits

- Spring Boot Framework
- Apache PDFBox
- MaxRect Algorithm by Jukka Jylänki

---

**Version**: 1.0.0  
**Last Updated**: January 2024  
**Java Version**: 17+  
**Spring Boot**: 3.2.1
