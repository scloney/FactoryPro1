package com.plywood.model;

import java.util.List;

public class OptimizationResult {
    private List<Sheet> sheets;
    private int totalSheets;
    private double totalArea;
    private double usedArea;
    private double wasteArea;
    private double averageUtilization;
    private long optimizationTimeMs;
    
    // Constructors
    public OptimizationResult() {}
    
    public OptimizationResult(List<Sheet> sheets, int totalSheets, double totalArea, 
                             double usedArea, double wasteArea, double averageUtilization, 
                             long optimizationTimeMs) {
        this.sheets = sheets;
        this.totalSheets = totalSheets;
        this.totalArea = totalArea;
        this.usedArea = usedArea;
        this.wasteArea = wasteArea;
        this.averageUtilization = averageUtilization;
        this.optimizationTimeMs = optimizationTimeMs;
    }
    
    // Getters and Setters
    public List<Sheet> getSheets() {
        return sheets;
    }
    
    public void setSheets(List<Sheet> sheets) {
        this.sheets = sheets;
    }
    
    public int getTotalSheets() {
        return totalSheets;
    }
    
    public void setTotalSheets(int totalSheets) {
        this.totalSheets = totalSheets;
    }
    
    public double getTotalArea() {
        return totalArea;
    }
    
    public void setTotalArea(double totalArea) {
        this.totalArea = totalArea;
    }
    
    public double getUsedArea() {
        return usedArea;
    }
    
    public void setUsedArea(double usedArea) {
        this.usedArea = usedArea;
    }
    
    public double getWasteArea() {
        return wasteArea;
    }
    
    public void setWasteArea(double wasteArea) {
        this.wasteArea = wasteArea;
    }
    
    public double getAverageUtilization() {
        return averageUtilization;
    }
    
    public void setAverageUtilization(double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }
    
    public long getOptimizationTimeMs() {
        return optimizationTimeMs;
    }
    
    public void setOptimizationTimeMs(long optimizationTimeMs) {
        this.optimizationTimeMs = optimizationTimeMs;
    }
}