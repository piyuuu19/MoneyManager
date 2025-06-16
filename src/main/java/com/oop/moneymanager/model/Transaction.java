package com.oop.moneymanager.model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private final String id;
    private Type type;
    private double amount;
    private String categoryName;
    private String description;
    private LocalDate date;

    public Transaction(Type type, double amount, String categoryName, String description, LocalDate date) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.categoryName = categoryName;
        this.description = description;
        this.date = date;
    }

    // Getters and Setters
    public String getId() { return id; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategoryName() { return categoryName; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public void setType(Type type) { this.type = type; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(LocalDate date) { this.date = date; }
}
