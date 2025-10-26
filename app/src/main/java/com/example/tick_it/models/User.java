package com.example.tick_it.models;

public class User {
    private String uid;
    private String email;
    private String role;
    private String name;

    public User() {}

    public User(String uid, String email, String role, String name) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}