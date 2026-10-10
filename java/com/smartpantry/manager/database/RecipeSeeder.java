package com.smartpantry.manager.database;

import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-loads the recipe collection into SQLite the first time the app runs.
 * Recipes are only inserted when the recipes table is empty, so they are
 * never duplicated on later launches.
 */
public final class RecipeSeeder {

    private RecipeSeeder() {
        // Utility class: no instances
    }

    /**
     * Inserts the built-in recipes if none exist yet.
     * @return how many recipes were inserted (0 if already seeded)
     */
    public static int seedIfEmpty(DatabaseHelper db) {
        if (db.getRecipeCount() > 0) {
            return 0;
        }
        int inserted = 0;
        for (Recipe recipe : buildRecipes()) {
            if (db.addRecipe(recipe) != -1) {
                inserted++;
            }
        }
        return inserted;
    }

    /** The 18 built-in recipes. Ingredient units match the app's unit list. */
    static List<Recipe> buildRecipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(recipe("Scrambled Eggs", "Soft, creamy eggs ready in minutes.", 10,
                new String[]{"Beat the eggs with the milk and salt.",
                        "Melt the butter in a pan over low heat.",
                        "Add the eggs and stir gently until just set.",
                        "Serve immediately."},
                ing("egg", 3, "pcs"), ing("milk", 50, "ml"),
                ing("butter", 1, "tbsp"), ing("salt", 1, "tsp")));

        list.add(recipe("Cheese Omelette", "A fluffy omelette filled with melted cheese.", 10,
                new String[]{"Beat the eggs with the salt.",
                        "Melt the butter in a pan over medium heat.",
                        "Pour in the eggs and cook until almost set.",
                        "Sprinkle the cheese over one half, fold and serve."},
                ing("egg", 3, "pcs"), ing("cheese", 50, "g"),
                ing("butter", 1, "tbsp"), ing("salt", 1, "tsp")));

        list.add(recipe("French Toast", "Golden pan-fried bread soaked in sweet egg.", 15,
                new String[]{"Whisk the eggs, milk and sugar in a shallow dish.",
                        "Soak each slice of bread for a few seconds per side.",
                        "Fry in butter until golden on both sides."},
                ing("bread", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"),
                ing("sugar", 1, "tbsp"), ing("butter", 1, "tbsp")));

        list.add(recipe("Pancakes", "Classic breakfast pancakes.", 25,
                new String[]{"Mix the flour and sugar in a bowl.",
                        "Whisk in the milk and egg until smooth.",
                        "Melt a little butter in a pan.",
                        "Cook spoonfuls of batter until bubbles form, then flip."},
                ing("flour", 1, "cup"), ing("milk", 1, "cup"), ing("egg", 1, "pcs"),
                ing("sugar", 2, "tbsp"), ing("butter", 2, "tbsp")));

        list.add(recipe("Tomato Pasta", "Simple pasta in a fresh tomato sauce.", 25,
                new String[]{"Cook the pasta in salted water until tender.",
                        "Fry the chopped onion and garlic in olive oil.",
                        "Add the chopped tomatoes and simmer for 10 minutes.",
                        "Toss the drained pasta in the sauce."},
                ing("pasta", 250, "g"), ing("tomato", 4, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("olive oil", 2, "tbsp"), ing("salt", 1, "tsp")));

        list.add(recipe("Garlic Butter Pasta", "Buttery pasta with garlic and cheese.", 20,
                new String[]{"Cook the pasta until tender and drain.",
                        "Melt the butter and gently fry the crushed garlic.",
                        "Toss the pasta in the garlic butter.",
                        "Top with grated cheese and salt to taste."},
                ing("pasta", 250, "g"), ing("butter", 3, "tbsp"), ing("garlic", 3, "pcs"),
                ing("cheese", 50, "g"), ing("salt", 1, "tsp")));

        list.add(recipe("Egg Fried Rice", "A quick way to use leftover rice.", 20,
                new String[]{"Cook the rice and let it cool.",
                        "Fry the chopped onion in oil.",
                        "Push the onion aside and scramble the eggs in the pan.",
                        "Add the rice, peas and soy sauce and stir-fry for 3 minutes."},
                ing("rice", 1, "cup"), ing("egg", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("peas", 0.5, "cup"), ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")));

        list.add(recipe("Vegetable Stir-Fry", "Crunchy vegetables in soy and garlic.", 20,
                new String[]{"Slice the carrots, pepper and onion thinly.",
                        "Heat the oil in a pan until very hot.",
                        "Stir-fry the vegetables and garlic for 5 minutes.",
                        "Add the soy sauce and toss to coat."},
                ing("carrot", 2, "pcs"), ing("bell pepper", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")));

        list.add(recipe("Potato Wedges", "Crispy oven-baked wedges.", 45,
                new String[]{"Preheat the oven to 200 degrees C.",
                        "Cut the potatoes into wedges.",
                        "Toss with oil, salt and paprika.",
                        "Bake for 35 minutes, turning halfway."},
                ing("potato", 4, "pcs"), ing("oil", 2, "tbsp"),
                ing("salt", 1, "tsp"), ing("paprika", 1, "tsp")));

        list.add(recipe("Mashed Potatoes", "Smooth, buttery mash.", 30,
                new String[]{"Peel and boil the potatoes until soft.",
                        "Drain and mash.",
                        "Beat in the butter, warm milk and salt."},
                ing("potato", 5, "pcs"), ing("butter", 2, "tbsp"),
                ing("milk", 100, "ml"), ing("salt", 1, "tsp")));

        list.add(recipe("Tomato Soup", "Comforting soup from ripe tomatoes.", 35,
                new String[]{"Fry the chopped onion and garlic in butter.",
                        "Add the chopped tomatoes, sugar and salt.",
                        "Simmer for 20 minutes, then blend until smooth."},
                ing("tomato", 6, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"),
                ing("butter", 1, "tbsp"), ing("sugar", 1, "tsp"), ing("salt", 1, "tsp")));

        list.add(recipe("Chicken Stir-Fry", "Tender chicken with peppers and soy.", 25,
                new String[]{"Slice the chicken into strips.",
                        "Fry the chicken in oil until cooked through.",
                        "Add the sliced onion, pepper and garlic and cook for 4 minutes.",
                        "Stir in the soy sauce and serve."},
                ing("chicken breast", 2, "pcs"), ing("bell pepper", 1, "pcs"),
                ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"),
                ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")));

        list.add(recipe("Beef Bolognese", "Rich mince and tomato sauce with pasta.", 40,
                new String[]{"Fry the chopped onion and garlic in oil.",
                        "Add the mince and brown it.",
                        "Add the chopped tomatoes and simmer for 20 minutes.",
                        "Serve over cooked pasta."},
                ing("beef mince", 500, "g"), ing("pasta", 300, "g"), ing("tomato", 4, "pcs"),
                ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("oil", 1, "tbsp")));

        list.add(recipe("Banana Smoothie", "A thick, naturally sweet smoothie.", 5,
                new String[]{"Peel and slice the bananas.",
                        "Blend with the milk and honey until smooth."},
                ing("banana", 2, "pcs"), ing("milk", 1, "cup"), ing("honey", 1, "tbsp")));

        list.add(recipe("Greek Salad", "Fresh salad with feta and olive oil.", 10,
                new String[]{"Chop the tomatoes, cucumber and onion.",
                        "Crumble the feta over the vegetables.",
                        "Drizzle with olive oil."},
                ing("tomato", 3, "pcs"), ing("cucumber", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("feta cheese", 100, "g"), ing("olive oil", 2, "tbsp")));

        list.add(recipe("Cheese and Tomato Toastie", "A crispy toasted sandwich.", 10,
                new String[]{"Butter the outside of both slices of bread.",
                        "Fill with cheese and sliced tomato.",
                        "Toast in a pan until golden and the cheese melts."},
                ing("bread", 2, "pcs"), ing("cheese", 60, "g"),
                ing("butter", 1, "tbsp"), ing("tomato", 1, "pcs")));

        list.add(recipe("Pap and Chakalaka", "Stiff maize porridge with spicy vegetable relish.", 40,
                new String[]{"Cook the maize meal in salted boiling water, stirring, for 20 minutes.",
                        "Fry the chopped onion, grated carrots and pepper in oil.",
                        "Add the baked beans and simmer for 10 minutes.",
                        "Serve the chakalaka with the pap."},
                ing("maize meal", 2, "cup"), ing("carrot", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("bell pepper", 1, "pcs"), ing("baked beans", 400, "g"),
                ing("oil", 1, "tbsp"), ing("salt", 1, "tsp")));

        list.add(recipe("Oat Porridge", "A warm, filling breakfast.", 10,
                new String[]{"Bring the milk to a gentle simmer.",
                        "Stir in the oats and cook for 5 minutes.",
                        "Top with sliced banana and honey."},
                ing("oats", 1, "cup"), ing("milk", 2, "cup"),
                ing("banana", 1, "pcs"), ing("honey", 1, "tbsp")));

        return list;
    }

    // ---------- Small builders to keep the recipe list readable ----------

    private static Recipe recipe(String name, String description, int prepMinutes,
                                 String[] steps, RecipeIngredient... ingredients) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                joined.append(Recipe.STEP_SEPARATOR);
            }
            joined.append(steps[i]);
        }
        Recipe recipe = new Recipe(0, name, description, prepMinutes, joined.toString());
        for (RecipeIngredient ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }
        return recipe;
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
