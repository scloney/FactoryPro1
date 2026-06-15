package com.plywood.service;

import com.plywood.model.Bill;
import com.plywood.model.BillItem;
import com.plywood.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    /**
     * Persist a transient Bill (as received from the browser JSON payload).
     * Links each BillItem back to the parent Bill before saving.
     */
    @Transactional
    public Bill saveBill(Bill incoming) {
        // Set defaults if missing
        if (incoming.getCreatedDate() == null) {
            incoming.setCreatedDate(LocalDate.now());
        }
        if (incoming.getStatus() == null || incoming.getStatus().isBlank()) {
            incoming.setStatus("DRAFT");
        }
        if (incoming.getDate() == null) {
            incoming.setDate(LocalDate.now());
        }
        if (incoming.getDueDate() == null) {
            incoming.setDueDate(LocalDate.now().plusDays(30));
        }

        // If a bill with this number already exists, return existing (idempotent)
        if (incoming.getBillNumber() != null && !incoming.getBillNumber().isBlank()) {
            Optional<Bill> existing = billRepository.findByBillNumber(incoming.getBillNumber());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Link items to parent
        if (incoming.getItems() != null) {
            for (BillItem item : incoming.getItems()) {
                item.setBill(incoming);
            }
        }

        return billRepository.save(incoming);
    }

    public List<Bill> findAll() {
        return billRepository.findAllByOrderByCreatedDateDesc();
    }

    public org.springframework.data.domain.Page<Bill> findAll(org.springframework.data.domain.Pageable pageable) {
        return billRepository.findAll(pageable);
    }

    public Optional<Bill> findById(Long id) {
        return billRepository.findById(id);
    }

    @Transactional
    public void deleteBill(Long id) {
        billRepository.deleteById(id);
    }

    @Transactional
    public Bill updateStatus(Long id, String status) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        bill.setStatus(status.toUpperCase());
        return billRepository.save(bill);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total   = billRepository.count();
        long draft   = billRepository.countByStatus("DRAFT");
        long paid    = billRepository.countByStatus("PAID");
        long overdue = billRepository.countByStatus("OVERDUE");
        Double paidRevenue  = billRepository.getTotalPaidRevenue();
        Double totalRevenue = billRepository.getTotalRevenue();

        stats.put("totalBills",    total);
        stats.put("draftBills",    draft);
        stats.put("paidBills",     paid);
        stats.put("overdueBills",  overdue);
        stats.put("paidRevenue",   paidRevenue  != null ? paidRevenue  : 0.0);
        stats.put("totalRevenue",  totalRevenue != null ? totalRevenue : 0.0);
        return stats;
    }
}
