package com.plywood.model;

public class FreeSpace {
    private double x;
    private double y;
    private double width;
    private double height;
    
    // Constructors
    public FreeSpace() {}
    
    public FreeSpace(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    // Business methods
    public double getArea() {
        return width * height;
    }
    
    public boolean canFit(double rectWidth, double rectHeight) {
        return rectWidth <= width && rectHeight <= height;
    }
    
    public boolean intersects(FreeSpace other) {
        if (other == null) return false;
        return !(x + width <= other.x || other.x + other.width <= x ||
                 y + height <= other.y || other.y + other.height <= y);
    }
    
    // Getters and Setters
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
}