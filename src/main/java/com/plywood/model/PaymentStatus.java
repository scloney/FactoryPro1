package com.plywood.model;

/**
 * Payment Status for Sales Orders
 */
public enum PaymentStatus {
    UNPAID,      // No payment received
    PARTIAL,     // Partial payment received (advance)
    PAID         // Fully paid
}