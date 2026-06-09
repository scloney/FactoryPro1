package com.plywood.model;

import java.util.ArrayList;
import java.util.List;

public class Sheet {
    private int sheetNumber;
    private double width;
    private double height;
    private List<Rectangle> placedRectangles;
    private List<FreeSpace> freeSpaces;
    
    // Constructors
    public Sheet() {}
    
    public Sheet(int sheetNumber, double width, double height) {
        this.sheetNumber = sheetNumber;
        this.width = width;
        this.height = height;
        this.placedRectangles = new ArrayList<>();
        this.freeSpaces = new ArrayList<>();
        // Initialize with one free space covering entire sheet
        freeSpaces.add(new FreeSpace(0, 0, width, height));
    }
    
    public Sheet(int sheetNumber, double width, double height, 
                 List<Rectangle> placedRectangles, List<FreeSpace> freeSpaces) {
        this.sheetNumber = sheetNumber;
        this.width = width;
        this.height = height;
        this.placedRectangles = placedRectangles;
        this.freeSpaces = freeSpaces;
    }
    
    // Business methods
    public double getUsedArea() {
        if (placedRectangles == null) return 0;
        return placedRectangles.stream()
                .mapToDouble(Rectangle::getArea)
                .sum();
    }
    
    public double getUtilization() {
        double totalArea = width * height;
        if (totalArea == 0) return 0;
        return (getUsedArea() / totalArea) * 100;
    }
    
    // Getters and Setters
    public int getSheetNumber() {
        return sheetNumber;
    }
    
    public void setSheetNumber(int sheetNumber) {
        this.sheetNumber = sheetNumber;
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
    
    public List<Rectangle> getPlacedRectangles() {
        return placedRectangles;
    }
    
    public void setPlacedRectangles(List<Rectangle> placedRectangles) {
        this.placedRectangles = placedRectangles;
    }
    
    public List<FreeSpace> getFreeSpaces() {
        return freeSpaces;
    }
    
    public void setFreeSpaces(List<FreeSpace> freeSpaces) {
        this.freeSpaces = freeSpaces;
    }
}