package com.smartpantry.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.utils.IngredientNormalizer;
import com.smartpantry.manager.utils.RecipeMatcher;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the strict-matching rule. Run with right-click > Run 'RecipeMatcherTest'.
 */
public class RecipeMatcherTest {

    private static final String TODAY = "2026-10-08";

    /** Scrambled eggs: 3 egg, 50 ml milk, 1 tbsp butter, 1 tsp salt. */
    private Recipe scrambledEggs() {
        Recipe r = new Recipe(1, "Scrambled Eggs", "", 10, "Cook");
        r.addIngredient(new RecipeIngredient("egg", 3, "pcs"));
        r.addIngredient(new RecipeIngredient("milk", 50, "ml"));
        r.addIngredient(new RecipeIngredient("butter", 1, "tbsp"));
        r.addIngredient(new RecipeIngredient("salt", 1, "tsp"));
        return r;
    }

    private PantryItem item(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private List<RecipeMatcher.MatchResult> suggest(List<PantryItem> pantry) {
        return new RecipeMatcher(pantry, true, TODAY)
                .getSuggestions(Collections.singletonList(scrambledEggs()));
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "pcs"), item("milk", 50, "ml"),
                item("butter", 1, "tbsp"), item("salt", 1, "tsp"));
        assertEquals(1, suggest(pantry).size());
    }

    @Test
    public void oneIngredientMissing_isNotSuggested() {
        // 3 of 4 ingredients: salt is missing
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "pcs"), item("milk", 50, "ml"),
                item("butter", 1, "tbsp"));
        assertEquals(0, suggest(pantry).size());
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        // Only 2 eggs, recipe needs 3
        List<PantryItem> pantry = Arrays.asList(item("egg", 2, "pcs"), item("milk", 50, "ml"),
                item("butter", 1, "tbsp"), item("salt", 1, "tsp"));
        assertEquals(0, suggest(pantry).size());
    }

    @Test
    public void pluralAndCapitalNames_stillMatch() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item(" MILK ", 1, "l"),
                item("Butter", 2, "tbsp"), item("salt", 1, "tsp"));
        assertEquals(1, suggest(pantry).size());
    }

    @Test
    public void differentUnitsSameFamily_areConverted() {
        // 1 l milk = 1000 ml (needs 50 ml); 1 tbsp salt = 15 ml (needs 1 tsp = 5 ml)
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "pcs"), item("milk", 1, "l"),
                item("butter", 1, "tbsp"), item("salt", 1, "tbsp"));
        assertEquals(1, suggest(pantry).size());
    }

    @Test
    public void incompatibleUnits_doNotMatch() {
        // Butter in grams cannot be compared with tablespoons
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "pcs"), item("milk", 50, "ml"),
                item("butter", 500, "g"), item("salt", 1, "tsp"));
        assertEquals(0, suggest(pantry).size());
    }

    @Test
    public void duplicateEntries_areAddedTogether() {
        // 2 eggs + 1 egg = 3 eggs
        List<PantryItem> pantry = Arrays.asList(item("egg", 2, "pcs"), item("eggs", 1, "pcs"),
                item("milk", 50, "ml"), item("butter", 1, "tbsp"), item("salt", 1, "tsp"));
        assertEquals(1, suggest(pantry).size());
    }

    @Test
    public void expiredItem_isIgnored() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("egg", 3, "pcs", "2026-10-01"), // expired before TODAY
                item("milk", 50, "ml"), item("butter", 1, "tbsp"), item("salt", 1, "tsp"));
        assertEquals(0, suggest(pantry).size());
    }

    @Test
    public void almostThere_listsRecipesMissingExactlyOne() {
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "pcs"), item("milk", 50, "ml"),
                item("butter", 1, "tbsp"));
        RecipeMatcher matcher = new RecipeMatcher(pantry, true, TODAY);
        List<RecipeMatcher.MatchResult> almost =
                matcher.getAlmostThere(Collections.singletonList(scrambledEggs()));
        assertEquals(1, almost.size());
        assertEquals("salt", almost.get(0).getMissing().get(0).getName());
    }

    @Test
    public void normalizer_handlesPluralsAndSynonyms() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomatoes"));
        assertEquals("tomato", IngredientNormalizer.normalize("fresh tomato"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals("peach", IngredientNormalizer.normalize("Peaches"));
        assertEquals("bell pepper", IngredientNormalizer.normalize("green peppers"));
        assertEquals("oats", IngredientNormalizer.normalize("oat"));
        assertTrue(IngredientNormalizer.normalize("cheese").equals(
                IngredientNormalizer.normalize("Cheeses")));
        assertFalse(IngredientNormalizer.normalize("onion").equals(
                IngredientNormalizer.normalize("spring onion")));
    }
}
