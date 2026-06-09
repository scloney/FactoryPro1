package com.plywood.model;

public enum QuotationStatus {
    DRAFT,      // Being created
    SENT,       // Sent to customer
    ACCEPTED,   // Customer accepted
    REJECTED,   // Customer rejected
    EXPIRED,    // Past valid until date
    CONVERTED   // Converted to sales order
}