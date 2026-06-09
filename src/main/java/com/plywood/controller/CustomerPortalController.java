package com.plywood.controller;

import com.plywood.model.Bill;
import com.plywood.model.Customer;
import com.plywood.model.Quotation;
import com.plywood.model.User;
import com.plywood.repository.BillRepository;
import com.plywood.repository.CustomerRepository;
import com.plywood.repository.QuotationRepository;
import com.plywood.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerPortalController {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final QuotationRepository quotationRepository;
    private final BillRepository billRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerPortalController(CustomerRepository customerRepository,
                                    UserRepository userRepository,
                                    QuotationRepository quotationRepository,
                                    BillRepository billRepository,
                                    PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.quotationRepository = quotationRepository;
        this.billRepository = billRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Login page ────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "customer-login";
    }

    // ── Registration page ─────────────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage() {
        return "customer-register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // 1. Phone must match a customer record
        Optional<Customer> customerOpt = customerRepository.findByPhone(phone);
        if (customerOpt.isEmpty()) {
            model.addAttribute("error", "Phone number not found in our system. Please contact your sales representative.");
            return "customer-register";
        }

        // 2. Passwords must match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("phone", phone);
            return "customer-register";
        }

        // 3. Password length check
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("phone", phone);
            return "customer-register";
        }

        // 4. Account must not already exist
        if (userRepository.findByUsername(phone).isPresent()) {
            model.addAttribute("error", "An account with this phone number already exists. Please login.");
            return "customer-register";
        }

        // 5. Create the user account
        Customer customer = customerOpt.get();
        User user = new User();
        user.setUsername(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("CUSTOMER");
        userRepository.save(user);

        return "redirect:/customer/login?registered=true&name=" + encodeParam(customer.getCustomerName());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String phone = principal.getName(); // username = phone number

        // Load customer profile
        customerRepository.findByPhone(phone).ifPresent(c -> {
            model.addAttribute("customer", c);
        });

        // Load quotations for this phone
        List<Quotation> quotations = quotationRepository.findByCustomerPhoneOrderByCreatedDateDesc(phone);
        model.addAttribute("quotations", quotations);

        // Load bills for this phone
        List<Bill> bills = billRepository.findByCustomerPhoneOrderByCreatedDateDesc(phone);
        model.addAttribute("bills", bills);

        // Summary stats
        long totalQuotations = quotations.size();
        long pendingQuotations = quotations.stream()
                .filter(q -> q.getStatus() != null &&
                        (q.getStatus().name().equals("DRAFT") || q.getStatus().name().equals("SENT")))
                .count();
        long totalBills = bills.size();
        long unpaidBills = bills.stream()
                .filter(b -> b.getStatus() != null && !b.getStatus().equals("PAID"))
                .count();
        double totalBillAmount = bills.stream().mapToDouble(Bill::getGrandTotal).sum();

        model.addAttribute("totalQuotations", totalQuotations);
        model.addAttribute("pendingQuotations", pendingQuotations);
        model.addAttribute("totalBills", totalBills);
        model.addAttribute("unpaidBills", unpaidBills);
        model.addAttribute("totalBillAmount", totalBillAmount);

        return "customer-dashboard";
    }

    private String encodeParam(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
