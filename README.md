# Plywood Management System

A comprehensive Java Spring Boot business management system for plywood and furniture businesses — covering quotations, billing, inventory, sales, purchasing, customers, suppliers, barcoding, and cutting optimization, with role-based authentication and a self-service customer portal.

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
- Quotation status workflow (send, accept, reject, approve, convert to sales order)
- PDF generation and WhatsApp sharing
- Customizable tax rates and discounts
- Notes and terms section

### 3. 🧾 Bill Maker
- Generate professional invoices with GST support
- Company and customer details with GSTIN
- Itemized billing with automatic calculations
- Payment terms and bank details
- Due date tracking
- Professional PDF invoice generation with WhatsApp sharing
- Colored headers and modern design

### 4. 📦 Inventory Management
- Product catalog with categories, units, and pricing
- Real-time stock levels with add/remove/adjust operations
- Low-stock alerts and active product tracking
- Stock movement history and audit trail
- Dashboard reports and CSV export

### 5. 👥 Customer Management
- Customer profiles with type segmentation
- Outstanding balance and payment tracking
- Order history per customer
- Top-customer and statistics views
- Search and filter by type or status

### 6. 🏭 Supplier Management
- Supplier directory with credit limits
- Outstanding balance tracking and clearing
- Search, filtering, and supplier statistics

### 7. 🛒 Purchase Orders
- Create and manage purchase orders linked to suppliers and products
- Status workflow (approve, mark ordered, receive, cancel)
- Overdue tracking and date-range queries
- Per-supplier and per-product order history

### 8. 📑 Sales Orders
- Convert quotations directly into sales orders
- Production status workflow (start production, mark ready, mark delivered, cancel)
- Payment tracking and order statistics
- Overdue and pending order views

### 9. 🔖 Barcode Generation & Scanning
- Generate barcodes and QR codes for products
- Bulk barcode generation for the full catalog
- Printable barcode sheets
- Scan-to-update stock workflow

### 10. 🔐 Authentication & Customer Portal
- Role-based login for staff/admin users
- Self-service customer registration, login, and dashboard
- Admin registration protected by a setup password

### 11. 💬 WhatsApp Integration
- Send generated quotation and bill PDFs directly to customers via WhatsApp Cloud API

## Technology Stack

- **Backend**: Spring Boot 3.2.1
- **Java Version**: 17
- **Database**: MySQL (via Spring Data JPA / Hibernate)
- **Security**: Spring Security (role-based access, CSRF protection)
- **Template Engine**: Thymeleaf
- **PDF Generation**: Apache PDFBox 2.0.30
- **Messaging**: Spring WebFlux (WhatsApp Cloud API integration)
- **Build Tool**: Maven
- **Frontend**: HTML5, CSS3, Vanilla JavaScript

## Project Structure

```
plywood-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/plywood/
│   │   │   ├── PlywoodManagementApplication.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java
│   │   │   ├── security/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── OptimizerController.java
│   │   │   │   ├── QuotationController.java
│   │   │   │   ├── BillController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── CustomerPortalController.java
│   │   │   │   ├── SupplierController.java
│   │   │   │   ├── PurchaseOrderController.java
│   │   │   │   ├── SalesOrderController.java
│   │   │   │   └── BarcodeController.java
│   │   │   ├── service/
│   │   │   │   ├── MaxRectOptimizerService.java
│   │   │   │   ├── OptimizerPdfService.java
│   │   │   │   ├── QuotationPdfService.java
│   │   │   │   ├── QuotationService.java
│   │   │   │   ├── BillPdfService.java
│   │   │   │   ├── BillService.java
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── SupplierService.java
│   │   │   │   ├── PurchaseOrderService.java
│   │   │   │   ├── SalesOrderService.java
│   │   │   │   ├── BarcodeService.java
│   │   │   │   ├── WhatsAppService.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── BillRepository.java
│   │   │   │   ├── QuotationRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── PurchaseOrderRepository.java
│   │   │   │   ├── SalesOrderRepository.java
│   │   │   │   ├── StockMovementRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── model/
│   │   │       ├── Rectangle.java
│   │   │       ├── Sheet.java
│   │   │       ├── FreeSpace.java
│   │   │       ├── OptimizationResult.java
│   │   │       ├── Quotation.java
│   │   │       ├── QuotationItem.java
│   │   │       ├── QuotationStatus.java
│   │   │       ├── Bill.java
│   │   │       ├── BillItem.java
│   │   │       ├── Product.java
│   │   │       ├── StockMovement.java
│   │   │       ├── Customer.java
│   │   │       ├── Supplier.java
│   │   │       ├── PurchaseOrder.java
│   │   │       ├── PurchaseOrderItem.java
│   │   │       ├── SalesOrder.java
│   │   │       ├── SalesOrderItem.java
│   │   │       ├── SalesOrderStatus.java
│   │   │       ├── PaymentStatus.java
│   │   │       └── User.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── index.html
│   │           ├── login.html / register.html
│   │           ├── optimizer.html
│   │           ├── quotation.html / quotations-list.html
│   │           ├── bill.html / bills-list.html
│   │           ├── Inventory.html / inventory-reports.html
│   │           ├── Customers.html
│   │           ├── suppliers.html
│   │           ├── purchase-orders.html
│   │           ├── Sales-orders-list.html
│   │           ├── barcode-print.html / barcode-scan.html
│   │           ├── customer-login.html / customer-register.html
│   │           └── customer-dashboard.html
│   └── test/
└── pom.xml
```

## Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL Server (running locally or remotely)

### Steps

1. **Clone or extract the project**
```bash
cd plywood-management-system
```

2. **Create the database**
```sql
CREATE DATABASE plywood_db;
```

3. **Configure the database connection**
Edit `src/main/resources/application.properties` with your MySQL credentials (defaults shown below assume a local MySQL instance with user `root`).

4. **Build the project**
```bash
mvn clean install
```

5. **Run the application**
```bash
mvn spring-boot:run
```

6. **Access the application**
Open your browser and navigate to: `http://localhost:8080`

Hibernate will automatically create/update the required tables on startup (`spring.jpa.hibernate.ddl-auto=update`).

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
8. Generate PDF quotation, send via WhatsApp, or convert to a sales order

### Bill Maker

1. Navigate to the Bill tool
2. Enter company details including GSTIN
3. Set invoice number, date, and due date
4. Fill in customer details with GSTIN
5. Add billable items
6. Configure GST rate and discounts
7. Add payment terms and bank details
8. Generate the invoice PDF and share via WhatsApp

### Inventory

1. Navigate to Inventory to view and manage the product catalog
2. Add, edit, or deactivate products with category, unit, and pricing details
3. Record stock additions, removals, and adjustments
4. View stock movement history and low-stock alerts
5. Export reports to CSV from the Inventory Reports page

### Customers & Suppliers

1. Navigate to Customers or Suppliers to manage directories
2. Track outstanding balances and record payments
3. View order/purchase history and statistics

### Purchase Orders & Sales Orders

1. Create purchase orders against suppliers and track them through approval, ordering, and receiving
2. Convert accepted quotations into sales orders
3. Track sales orders through production, ready, and delivered stages with payment updates

### Barcodes

1. Generate barcodes/QR codes for individual products or the entire catalog
2. Print barcode sheets from the Barcode Print page
3. Scan barcodes to quickly update stock levels

### Customer Portal

1. Customers can self-register and log in at `/register` and `/login`
2. The customer dashboard shows their orders and account information

## API Endpoints

### Optimizer
- `GET /optimizer` - Optimizer page
- `POST /optimizer/optimize` - Run optimization algorithm
- `POST /optimizer/download-pdf` - Generate PDF report

### Quotation
- `GET /quotation` - Quotation maker page
- `GET /quotations-list` - List quotations
- `POST /api/quotations` - Create quotation
- `GET /api/quotations` - List quotations (API)
- `GET /api/quotations/{id}` - Get quotation by ID
- `GET /api/quotations/number/{quotationNumber}` - Get by quotation number
- `GET /api/quotations/customer/{customerId}` - Quotations for a customer
- `GET /api/quotations/status/{status}` / `/pending` - Filter by status
- `PUT /api/quotations/{id}` - Update quotation
- `DELETE /api/quotations/{id}` - Delete quotation
- `POST /api/quotations/{id}/send` / `/accept` / `/reject` / `/approve` - Status transitions
- `POST /api/quotations/{id}/convert` - Convert to sales order
- `GET /api/quotations/{id}/pdf` / `POST /api/quotations/generate-pdf` - PDF generation
- `POST /api/quotations/{id}/send-whatsapp` - Share via WhatsApp
- `GET /api/quotations/statistics` - Statistics

### Bill
- `GET /bill` - Bill maker page
- `GET /bills` - List bills
- `POST /api/bills` - Create bill
- `GET /api/bills` / `GET /api/bills/{id}` - List / get bills
- `DELETE /api/bills/{id}` - Delete bill
- `GET /api/bills/statistics` - Statistics
- `POST /bill/generate-pdf` - Generate invoice PDF
- `POST /api/bills/{id}/send-whatsapp` - Share via WhatsApp

### Inventory
- `GET /inventory` / `/products` / `/movements` / `/reports` - Pages
- `GET /inventory/export/csv` - Export CSV report
- `POST /inventory/api/products` / `PUT /api/products/{id}` / `DELETE /api/products/{id}` - Manage products
- `GET /inventory/api/products` / `/active` / `/low-stock` / `/category/{category}` / `/{id}` - Query products
- `POST /inventory/api/stock/add` / `/remove` / `/adjust` - Stock operations
- `GET /inventory/api/movements` / `/movements/product/{productId}` - Stock movement history
- `GET /inventory/api/reports/dashboard` - Dashboard report

### Customers (`/api/customers`)
- `POST /` / `GET /` / `GET /{id}` - Create / list / get
- `GET /active` / `/type/{customerType}` / `/search` - Filtered lookups
- `PUT /{id}` / `DELETE /{id}` - Update / delete
- `POST /{id}/activate` / `/deactivate` - Status toggling
- `POST /{id}/outstanding` / `/payment` / `/order` - Balance and order updates
- `GET /with-outstanding` / `/top` / `/statistics` - Reporting

### Suppliers (`/suppliers`)
- `GET /` - Suppliers page
- `POST /api` / `GET /api` / `GET /api/{id}` - Create / list / get
- `PUT /api/{id}` / `DELETE /api/{id}` - Update / delete
- `GET /api/active` / `/search` / `/exceeding-credit` / `/with-outstanding` - Filtered lookups
- `POST /api/{id}/update-outstanding` / `/clear-outstanding` - Balance updates
- `GET /api/stats` - Statistics

### Purchase Orders (`/purchase-orders`)
- `GET /` - Purchase orders page
- `POST /api` / `GET /api` / `GET /api/{id}` - Create / list / get
- `PUT /api/{id}` / `DELETE /api/{id}` - Update / delete
- `GET /api/number/{poNumber}` / `/product/{productId}` / `/supplier/{supplierId}` / `/status/{status}` / `/overdue` / `/date-range` / `/recent` - Filtered lookups
- `POST /api/{id}/approve` / `/mark-ordered` / `/receive` / `/cancel` - Status workflow
- `POST /api/{id}/items` - Add items
- `GET /api/stats` / `/total-amount` - Reporting

### Sales Orders
- `GET /sales-orders` - Sales orders page
- `GET /sales-order/new` / `/sales-order/{id}` - Detail pages
- `GET /api/sales-orders` / `/{id}` / `/number/{orderNumber}` / `/customer/{customerId}` / `/status/{status}` / `/pending` / `/overdue` - Lookups
- `POST /api/sales-orders` / `/from-quotation/{quotationId}` - Create
- `PUT /api/sales-orders/{id}` / `DELETE /api/sales-orders/{id}` - Update / delete
- `POST /api/sales-orders/{id}/start-production` / `/mark-ready` / `/mark-delivered` / `/cancel` / `/add-payment` - Workflow
- `GET /api/sales-orders/statistics` - Statistics

### Barcode
- `POST /api/barcode/generate/{productId}` / `/generate-all` - Generate barcodes
- `GET /api/barcode/image/{productId}` / `/qr/{productId}` - Barcode/QR images
- `GET /api/barcode/scan` / `POST /api/barcode/scan` - Scan lookup
- `POST /api/barcode/scan-and-update-stock` - Scan-to-stock workflow
- `GET /barcode/print` / `/barcode/scan` - Pages

### Authentication & Customer Portal
- `GET /login` / `GET|POST /register` - Staff login/registration
- `GET /customers/login` / `GET|POST /customers/register` / `GET /customers/dashboard` - Customer portal

## Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/plywood_db?useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=root

# Admin registration password (required to register a staff/admin account)
admin.registration.password=admin123

# WhatsApp Cloud API
whatsapp.api.url=https://graph.facebook.com/v19.0
whatsapp.phone.number.id=YOUR_PHONE_NUMBER_ID
whatsapp.access.token=YOUR_ACCESS_TOKEN
```

> **Note**: Update the WhatsApp credentials and database password before deploying to production, and consider setting `spring.jpa.hibernate.ddl-auto` to `validate` once your schema is stable.

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
- Currency formatting (Rs.)
- Date formatting
- Input sanitization to safely handle special characters and multi-line notes

## Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Check if port 8080 is available
- Verify Maven is installed: `mvn -version`
- Confirm MySQL is running and `plywood_db` exists, and that the credentials in `application.properties` are correct

### PDF generation fails
- Check console for error messages
- Ensure sufficient memory is allocated
- Verify all required fields are filled
- If the browser shows a "PDF generation failed" alert with a JSON error, the message includes the underlying cause (e.g. an HTTP status from Spring Security)

### Login / 403 errors on POST requests
- Spring Security CSRF protection applies to most form/JSON POST endpoints; ensure requests either include a CSRF token or target an endpoint exempted under `/api/**`, `/*/api/**`, or `/optimizer/**`

### WhatsApp sharing doesn't send
- Verify `whatsapp.api.url`, `whatsapp.phone.number.id`, and `whatsapp.access.token` are correctly configured in `application.properties`

### Optimization takes too long
- Reduce number of rectangles
- Simplify dimensions
- Consider pre-sorting similar sizes

## Future Enhancements

Potential features for future versions:
- Export to Excel for additional modules (beyond Inventory CSV)
- Email functionality for sending documents
- Multi-tenancy support
- Advanced reporting and analytics dashboards
- Automated low-stock reorder suggestions

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

**Java Version**: 17+
**Spring Boot**: 3.2.1