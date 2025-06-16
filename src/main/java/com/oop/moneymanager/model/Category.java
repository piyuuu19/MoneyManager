package com.oop.moneymanager.model;

import java.util.UUID;

public class Category {
    private String id;
    private String name;

    public Category(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
