package com.plywood.model;

/**
 * Sales Order Status Lifecycle
 */
public enum SalesOrderStatus {
    PENDING,           // Order created, waiting to start production
    IN_PRODUCTION,     // Being manufactured
    READY,             // Completed, ready for delivery
    DELIVERED,         // Delivered to customer
    CANCELLED          // Order cancelled
}