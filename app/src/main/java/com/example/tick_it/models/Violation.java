package com.example.tick_it.models;

import java.util.Date;

public class Violation {
    private String id;
    private String userId;
    private String licensePlate;
    private String violationType;
    private String description;
    private double fineAmount;
    private String status; // "pending", "paid", "disputed"
    private Date issueDate;
    private Date dueDate;
    private String enforcerName;
    private String location;

    public Violation() {
        // Default constructor required for Firestore
    }

    public Violation(String id, String userId, String licensePlate, String violationType,
                     String description, double fineAmount, String status, Date issueDate,
                     Date dueDate, String enforcerName, String location) {
        this.id = id;
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.violationType = violationType;
        this.description = description;
        this.fineAmount = fineAmount;
        this.status = status;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.enforcerName = enforcerName;
        this.location = location;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getEnforcerName() { return enforcerName; }
    public void setEnforcerName(String enforcerName) { this.enforcerName = enforcerName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}