package com.smartpantry.manager.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns messy ingredient names into one consistent "key" so that, for example,
 * "Tomatoes", "tomato" and " Fresh TOMATO " all match each other.
 * Steps: lower-case, strip symbols, drop descriptive words (fresh, large...),
 * make each word singular, then map common synonyms to one name.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Utility class: no instances
    }

    /** Words that describe an ingredient but do not change what it is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "chopped", "sliced",
            "diced", "ripe", "raw", "organic", "whole"));

    /** Words ending in "s" that are already singular. */
    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus", "oats"));

    /** Irregular plurals. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Different names for the same ingredient (applied after singularising). */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");

        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("green pepper", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("maizemeal", "maize meal");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("clove of garlic", "garlic");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("feta", "feta cheese");
        SYNONYMS.put("pea", "peas");
        SYNONYMS.put("oat", "oats");
    }

    /** Returns the normalised matching key for an ingredient name. */
    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }
        String s = rawName.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace("'", "")
                .replaceAll("[^\\p{L}\\s]", " ")  // drop digits and symbols
                .replaceAll("\\s+", " ")
                .trim();

        StringBuilder result = new StringBuilder();
        for (String word : s.split(" ")) {
            if (word.isEmpty() || DESCRIPTORS.contains(word)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singularize(word));
        }

        String key = result.toString();
        String synonym = SYNONYMS.get(key);
        return synonym != null ? synonym : key;
    }

    /** Simple English plural-to-singular rules (good enough for food words). */
    static String singularize(String word) {
        if (word.length() <= 3 || KEEP_AS_IS.contains(word)) {
            return word;
        }
        if (IRREGULAR.containsKey(word)) {
            return IRREGULAR.get(word);
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";     // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);           // tomatoes -> tomato
        }
        if (word.endsWith("sses") || word.endsWith("ches")
                || word.endsWith("shes") || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);           // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                            // already singular
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);           // eggs -> egg
        }
        return word;
    }
}
