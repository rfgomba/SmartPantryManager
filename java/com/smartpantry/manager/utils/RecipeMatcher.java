package com.smartpantry.manager.utils;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule (core business logic).
 *
 * A recipe is "suggested" ONLY if EVERY ingredient it needs is in the pantry
 * in AT LEAST the required quantity. Names are normalised (plural/singular,
 * synonyms) and quantities are converted to a common unit before comparing.
 *
 */
public class RecipeMatcher {

    /** Small allowance for decimal rounding, e.g. 0.1 + 0.2. */
    private static final double TOLERANCE = 0.0001;

    /** Result of checking one recipe against the pantry. */
    public static class MatchResult {
        private final Recipe recipe;
        private final List<RecipeIngredient> missing;

        MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        /** Ingredients the pantry lacks, or has too little of. */
        public List<RecipeIngredient> getMissing() {
            return missing;
        }

        public int getMissingCount() {
            return missing.size();
        }

        /** True only when nothing is missing (strict match). */
        public boolean isFullMatch() {
            return missing.isEmpty();
        }
    }

    /**
     * Pantry totals: ingredient key -> (unit family -> total in base units).
     * e.g. "egg" -> {count: 6}, "milk" -> {volume: 1500}
     */
    private final Map<String, Map<String, Double>> pantryTotals = new HashMap<>();

    /**
     * @param pantry        the user's current pantry items
     * @param ignoreExpired if true, expired items do not count as available
     * @param today         today's date as yyyy-MM-dd (passed in so tests can control it)
     */
    public RecipeMatcher(List<PantryItem> pantry, boolean ignoreExpired, String today) {
        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            if (ignoreExpired && ExpiryUtils.isExpired(item.getExpiryDate(), today)) {
                continue;
            }
            String key = IngredientNormalizer.normalize(item.getName());
            String family = UnitConverter.familyOf(item.getUnit());
            double amount = UnitConverter.toBase(item.getQuantity(), item.getUnit());

            Map<String, Double> byFamily = pantryTotals.get(key);
            if (byFamily == null) {
                byFamily = new HashMap<>();
                pantryTotals.put(key, byFamily);
            }
            // Same ingredient entered twice (e.g. 2 eggs + 4 eggs) is added together
            Double current = byFamily.get(family);
            byFamily.put(family, (current == null ? 0 : current) + amount);
        }
    }

    /** True if the pantry holds at least the required amount of this ingredient. */
    public boolean hasEnough(RecipeIngredient needed) {
        Map<String, Double> byFamily = pantryTotals.get(IngredientNormalizer.normalize(needed.getName()));
        if (byFamily == null) {
            return false; // not in the pantry at all
        }
        Double available = byFamily.get(UnitConverter.familyOf(needed.getUnit()));
        if (available == null) {
            return false; // in the pantry, but in a unit that cannot be compared
        }
        double required = UnitConverter.toBase(needed.getQuantity(), needed.getUnit());
        return available + TOLERANCE >= required;
    }

    /** Checks one recipe and lists anything missing. */
    public MatchResult check(Recipe recipe) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasEnough(ingredient)) {
                missing.add(ingredient);
            }
        }
        return new MatchResult(recipe, missing);
    }

    /** STRICT suggestions: only recipes with zero missing ingredients. */
    public List<MatchResult> getSuggestions(List<Recipe> recipes) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = check(recipe);
            if (match.isFullMatch()) {
                result.add(match);
            }
        }
        return result;
    }

    /** Optional extra list: recipes missing exactly one ingredient (kept separate). */
    public List<MatchResult> getAlmostThere(List<Recipe> recipes) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = check(recipe);
            if (match.getMissingCount() == 1) {
                result.add(match);
            }
        }
        return result;
    }
}
