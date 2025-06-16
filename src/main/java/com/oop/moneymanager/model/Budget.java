package com.oop.moneymanager.model;

import java.time.YearMonth;
import java.util.UUID;

public class Budget {
    private final String id;
    private String categoryName;
    private double amount;
    private YearMonth month;

    public Budget(String categoryName, double amount, YearMonth month) {
        this.id = UUID.randomUUID().toString();
        this.categoryName = categoryName;
        this.amount = amount;
        this.month = month;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getCategoryName() { return categoryName; }
    public double getAmount() { return amount; }
    public YearMonth getMonth() { return month; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setMonth(YearMonth month) { this.month = month; }
}
