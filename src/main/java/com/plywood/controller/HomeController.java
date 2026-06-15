package com.plywood.controller;

import com.plywood.model.Bill;
import com.plywood.model.Product;
import com.plywood.model.Quotation;
import com.plywood.model.QuotationStatus;
import com.plywood.model.SalesOrder;
import com.plywood.model.SalesOrderStatus;
import com.plywood.model.User;
import com.plywood.repository.BillRepository;
import com.plywood.repository.CustomerRepository;
import com.plywood.repository.ProductRepository;
import com.plywood.repository.QuotationRepository;
import com.plywood.repository.SalesOrderRepository;
import com.plywood.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class HomeController {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final BillRepository       billRepository;
    private final QuotationRepository  quotationRepository;
    private final ProductRepository    productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository   customerRepository;

    @Value("${admin.registration.password:admin123}")
    private String adminRegistrationPassword;

    public HomeController(UserRepository       userRepository,
                          PasswordEncoder      passwordEncoder,
                          BillRepository       billRepository,
                          QuotationRepository  quotationRepository,
                          ProductRepository    productRepository,
                          SalesOrderRepository salesOrderRepository,
                          CustomerRepository   customerRepository) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.billRepository       = billRepository;
        this.quotationRepository  = quotationRepository;
        this.productRepository    = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository   = customerRepository;
    }

    // ── Dashboard ────────────────────────────────────────────────────────
    @GetMapping("/")
    public String home(Model model) {

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // ── Current month label ──────────────────────────────────────────
        model.addAttribute("currentMonth",
            today.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // ── Revenue this month (from bills created in current month) ─────
        List<Bill> allBills = billRepository.findAllByOrderByCreatedDateDesc();
        double revenueThisMonth = allBills.stream()
            .filter(b -> b.getCreatedDate() != null
                      && !b.getCreatedDate().isBefore(monthStart)
                      && !b.getCreatedDate().isAfter(today))
            .mapToDouble(Bill::getGrandTotal)
            .sum();
        long billsThisMonth = allBills.stream()
            .filter(b -> b.getCreatedDate() != null
                      && !b.getCreatedDate().isBefore(monthStart)
                      && !b.getCreatedDate().isAfter(today))
            .count();
        model.addAttribute("revenueThisMonth", revenueThisMonth);
        model.addAttribute("billsThisMonth",   billsThisMonth);

        // ── Pending quotations (SENT, not converted) ─────────────────────
        long pendingQuotations = quotationRepository.countByStatus(QuotationStatus.SENT);
        model.addAttribute("pendingQuotations", pendingQuotations);

        // ── Active (non-terminal) sales orders ───────────────────────────
        long pendingSalesOrders =
            salesOrderRepository.countByStatus(SalesOrderStatus.PENDING)
          + salesOrderRepository.countByStatus(SalesOrderStatus.IN_PRODUCTION)
          + salesOrderRepository.countByStatus(SalesOrderStatus.READY);
        model.addAttribute("pendingSalesOrders", pendingSalesOrders);

        // ── Product / inventory stats ────────────────────────────────────
        long totalProducts = productRepository.countByActive(true);
        model.addAttribute("totalProducts", totalProducts);

        long lowStockCount  = productRepository.countReorderNeeded();
        long outOfStockCount= productRepository.countOutOfStockProducts();
        model.addAttribute("lowStockCount",   lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        Double invValue = productRepository.getTotalInventoryValue();
        model.addAttribute("totalInventoryValue", invValue != null ? invValue : 0.0);

        // ── Active customers ─────────────────────────────────────────────
        long activeCustomers = customerRepository.countActiveCustomers();
        model.addAttribute("activeCustomers", activeCustomers);

        // ── 6-month revenue trend (bar chart) ───────────────────────────
        List<String> trendLabels  = new ArrayList<>();
        List<Double> trendRevenue = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yy");
        for (int i = 5; i >= 0; i--) {
            LocalDate m   = today.minusMonths(i);
            LocalDate mS  = m.withDayOfMonth(1);
            LocalDate mE  = m.withDayOfMonth(m.lengthOfMonth());
            trendLabels.add(m.format(monthFmt));
            double rev = allBills.stream()
                .filter(b -> b.getCreatedDate() != null
                          && !b.getCreatedDate().isBefore(mS)
                          && !b.getCreatedDate().isAfter(mE))
                .mapToDouble(Bill::getGrandTotal)
                .sum();
            trendRevenue.add(rev);
        }
        model.addAttribute("trendLabels",  trendLabels);
        model.addAttribute("trendRevenue", trendRevenue);

        // ── Quotation status donut ───────────────────────────────────────
        List<String> qLabels = new ArrayList<>();
        List<Long>   qData   = new ArrayList<>();
        for (QuotationStatus qs : QuotationStatus.values()) {
            long cnt = quotationRepository.countByStatus(qs);
            if (cnt > 0) {
                qLabels.add(capitalize(qs.name()));
                qData.add(cnt);
            }
        }
        model.addAttribute("quotationStatusLabels", qLabels);
        model.addAttribute("quotationStatusData",   qData);

        // ── Low-stock alerts (top 8) ─────────────────────────────────────
        List<Product> lowStockProducts = productRepository.findProductsBelowReorderLevel();
        List<Map<String, Object>> lowStockAlerts = new ArrayList<>();
        for (Product p : lowStockProducts) {
            if (lowStockAlerts.size() >= 8) break;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name",    p.getName());
            item.put("code",    p.getProductCode());
            item.put("current", p.getCurrentStock());
            item.put("minimum", p.getReorderLevel());
            item.put("unit",    p.getUnit());
            // pct: how full the bar should be (capped 0–100)
            double pct = p.getReorderLevel() > 0
                ? Math.min(100.0, (p.getCurrentStock() / p.getReorderLevel()) * 100.0)
                : 0.0;
            item.put("pct", Math.round(pct));
            lowStockAlerts.add(item);
        }
        model.addAttribute("lowStockAlerts", lowStockAlerts);

        // ── Recent activity feed (mixed: bills + quotations + orders) ────
        List<Map<String, Object>> recentActivity = new ArrayList<>();

        // Bills (last 5)
        allBills.stream().limit(5).forEach(b -> {
            Map<String, Object> ev = new LinkedHashMap<>();
            ev.put("icon",   "🧾");
            ev.put("label",  "Bill " + b.getBillNumber()
                + (b.getCustomerName() != null ? " · " + b.getCustomerName() : ""));
            ev.put("date",   formatDate(b.getCreatedDate()));
            ev.put("status", b.getStatus());
            ev.put("color",  billColor(b.getStatus()));
            ev.put("amount", b.getGrandTotal());
            recentActivity.add(ev);
        });

        // Quotations (last 5)
        quotationRepository.findAll().stream()
            .sorted(Comparator.comparing(
                Quotation::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .forEach(q -> {
                Map<String, Object> ev = new LinkedHashMap<>();
                ev.put("icon",   "📋");
                ev.put("label",  "Quotation " + q.getQuotationNumber()
                    + (q.getCustomerName() != null ? " · " + q.getCustomerName() : ""));
                ev.put("date",   formatDate(q.getCreatedDate()));
                ev.put("status", q.getStatus() != null ? q.getStatus().name() : "DRAFT");
                ev.put("color",  quotationColor(q.getStatus()));
                ev.put("amount", q.getGrandTotal());
                recentActivity.add(ev);
            });

        // Sales orders (last 5)
        salesOrderRepository.findTop10ByOrderByOrderDateDesc().stream()
            .limit(5)
            .forEach(so -> {
                Map<String, Object> ev = new LinkedHashMap<>();
                ev.put("icon",   "🛒");
                ev.put("label",  "Order " + so.getOrderNumber()
                    + (so.getCustomer() != null ? " · " + so.getCustomer().getCustomerName() : ""));
                ev.put("date",   formatDate(so.getOrderDate()));
                ev.put("status", so.getStatus() != null ? so.getStatus().name() : "PENDING");
                ev.put("color",  salesOrderColor(so.getStatus()));
                ev.put("amount", so.getTotalAmount() != null ? so.getTotalAmount() : 0.0);
                recentActivity.add(ev);
            });

        // Sort mixed list by date descending, take top 10
        recentActivity.sort((a, b2) -> b2.get("date").toString().compareTo(a.get("date").toString()));
        model.addAttribute("recentActivity",
            recentActivity.size() > 10 ? recentActivity.subList(0, 10) : recentActivity);

        return "index";
    }

    // ── Auth routes ──────────────────────────────────────────────────────
    @GetMapping("/customers")
    public String customers() { return "Customers"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String adminPassword,
                                      @RequestParam(defaultValue = "STAFF") String role) {
        if (!adminRegistrationPassword.equals(adminPassword)) {
            return "redirect:/register?error=invalid_admin";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error=exists";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role.toUpperCase());
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private String formatDate(LocalDate d) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private String billColor(String status) {
        if (status == null) return "#888";
        return switch (status.toUpperCase()) {
            case "PAID"    -> "#22c55e";
            case "OVERDUE" -> "#ef4444";
            default        -> "#888";   // DRAFT
        };
    }

    private String quotationColor(QuotationStatus status) {
        if (status == null) return "#888";
        return switch (status) {
            case ACCEPTED  -> "#22c55e";
            case REJECTED  -> "#ef4444";
            case SENT      -> "#3b82f6";
            case CONVERTED -> "#a855f7";
            case EXPIRED   -> "#888";
            default        -> "#eab308"; // DRAFT
        };
    }

    private String salesOrderColor(SalesOrderStatus status) {
        if (status == null) return "#888";
        return switch (status) {
            case DELIVERED    -> "#22c55e";
            case CANCELLED    -> "#ef4444";
            case IN_PRODUCTION-> "#f97316";
            case READY        -> "#3b82f6";
            default           -> "#eab308"; // PENDING
        };
    }
}
