package com.smartpantry.manager.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A recipe: name, short description, preparation steps and its ingredient list.
 * Maps to one row in the recipes table plus its rows in recipe_ingredients.
 */
public class Recipe {

    /** Steps are stored in one text column, separated by this character. */
    public static final String STEP_SEPARATOR = "|";

    private long id;
    private String name;
    private String description;
    private int prepMinutes;
    private String steps; // e.g. "Beat the eggs|Heat the pan|Cook for 3 minutes"
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String description, int prepMinutes, String steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public String getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /** Splits the stored steps text into a list, one entry per step. */
    public List<String> getStepsList() {
        if (steps == null || steps.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(steps.split("\\" + STEP_SEPARATOR));
    }
}
