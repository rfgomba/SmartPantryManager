package com.smartpantry.manager.model;

/**
 * One ingredient line inside a recipe, e.g. "2 pcs egg".
 * Maps to one row in the recipe_ingredients table.
 */
public class RecipeIngredient {

    private long id;
    private long recipeId;      // which recipe this line belongs to (foreign key)
    private String name;        // e.g. "egg"
    private double quantity;    // e.g. 2
    private String unit;        // e.g. "pcs"

    /** Constructor used when defining recipes before they are saved. */
    public RecipeIngredient(String name, double quantity, String unit) {
        this(0, 0, name, quantity, unit);
    }

    /** Constructor used when loading from the database. */
    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public long getRecipeId() {
        return recipeId;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    /** e.g. "2 pcs egg" or "0.5 cup milk" (whole numbers shown without ".0"). */
    public String getDisplayText() {
        String number = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        return number + " " + unit + " " + name;
    }
}
