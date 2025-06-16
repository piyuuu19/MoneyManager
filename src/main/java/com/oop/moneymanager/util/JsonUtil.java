package com.oop.moneymanager.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.oop.moneymanager.model.Budget;
import com.oop.moneymanager.model.Category;
import com.oop.moneymanager.model.Transaction;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(YearMonth.class, new YearMonthAdapter())
            .setPrettyPrinting()
            .create();

    public static <T> void saveData(List<T> data, String filename) {
        if (data == null) data = new ArrayList<>();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
            System.out.println("Data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving to " + filename + ": " + e.getMessage());
        }
    }


    private static <T> List<T> loadData(String filename, Type type) {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(filename)) {
            List<T> data = gson.fromJson(reader, type);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading from " + filename + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<Transaction> loadTransactions() {
        return loadData("transactions.json", new TypeToken<ArrayList<Transaction>>() {}.getType());
    }

    public static List<Category> loadCategories() {
        Path path = Paths.get("categories.json");
        if (!Files.exists(path)) {
            List<Category> defaultCats = new ArrayList<>();
            defaultCats.add(new Category("Makanan & Minuman"));
            defaultCats.add(new Category("Transportasi"));
            defaultCats.add(new Category("Belanja"));
            defaultCats.add(new Category("Hiburan"));
            defaultCats.add(new Category("Gaji"));
            saveData(defaultCats, "categories.json");
            return defaultCats;
        }
        return loadData("categories.json", new TypeToken<ArrayList<Category>>() {}.getType());
    }


    public static List<Budget> loadBudgets() {
        return loadData("budgets.json", new TypeToken<ArrayList<Budget>>() {}.getType());
    }
}
