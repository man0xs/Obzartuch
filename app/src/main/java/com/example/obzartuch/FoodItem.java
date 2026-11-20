package com.example.obzartuch;

public class FoodItem {
    private final String name;
    private final int iconResId;
    private final int protein;
    private final int kcal;
    private final int sugar;
    private final boolean healthy;

    public FoodItem(String name, int iconResId, int protein, int kcal, int sugar, boolean healthy) {
        this.name = name;
        this.iconResId = iconResId;
        this.protein = protein;
        this.kcal = kcal;
        this.sugar = sugar;
        this.healthy = healthy;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getProtein() {
        return protein;
    }

    public int getKcal() {
        return kcal;
    }

    public int getSugar() {
        return sugar;
    }

    public boolean isHealthy() {
        return healthy;
    }
}