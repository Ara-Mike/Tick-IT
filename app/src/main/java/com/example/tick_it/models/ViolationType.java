package com.example.tick_it.models;

public class ViolationType {
    private String id;
    private String violationName;
    private double fineAmount;
    private String description;
    private String category;
    private int points;
    private boolean isActive;

    // Default constructor (REQUIRED for Firestore)
    public ViolationType() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getViolationName() { return violationName; }
    public void setViolationName(String violationName) { this.violationName = violationName; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}