package com.smartpantry.manager.model;

/**
 * Model class for one ingredient in the user's pantry.
 * Each object maps to one row in the pantry_items database table.
 */
public class PantryItem {

    private long id;            // Database primary key (0 = not saved yet)
    private String name;        // e.g. "Tomato"
    private double quantity;    // e.g. 4 or 0.5
    private String unit;        // e.g. "pcs", "g", "ml", "cup"
    private String expiryDate;  // Optional, format yyyy-MM-dd, null if not set

    /** Constructor for a new item that has not been saved to the database yet. */
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    /** Constructor for an item loaded from the database (id is known). */
    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // ---------- Getters ----------

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    // ---------- Setters ----------

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    // ---------- Helpers ----------

    /** True if the user entered an expiry date for this item. */
    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.trim().isEmpty();
    }

    /**
     * Quantity and unit for display, e.g. "4 pcs" or "0.5 kg".
     * Whole numbers are shown without ".0".
     */
    public String getDisplayQuantity() {
        String number = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        return number + " " + unit;
    }
}
