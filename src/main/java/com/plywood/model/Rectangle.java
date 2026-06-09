package com.plywood.model;

public class Rectangle {
    private int id;
    private double width;
    private double height;
    private int quantity;
    private double x;  // Position on sheet
    private double y;  // Position on sheet
    private boolean rotated;
    
    // Constructors
    public Rectangle() {}
    
    public Rectangle(int id, double width, double height, int quantity) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.quantity = quantity;
        this.x = 0;
        this.y = 0;
        this.rotated = false;
    }
    
    public Rectangle(int id, double width, double height, int quantity, 
                     double x, double y, boolean rotated) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
        this.rotated = rotated;
    }
    
    // Business method
    public double getArea() {
        return width * height;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public boolean isRotated() {
        return rotated;
    }
    
    public void setRotated(boolean rotated) {
        this.rotated = rotated;
    }
}