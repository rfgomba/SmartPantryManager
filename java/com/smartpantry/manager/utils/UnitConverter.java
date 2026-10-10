package com.smartpantry.manager.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to a common base unit so different units can be compared:
 *  - count  : pcs
 *  - mass   : grams (g, kg)
 *  - volume : millilitres (ml, l, tsp, tbsp, cup)
 * Mass and volume cannot be compared with each other (that would need density),
 * so "500 g flour" will never be treated as "2 cup flour".
 */
public final class UnitConverter {

    public static final String COUNT = "count";
    public static final String MASS = "mass";
    public static final String VOLUME = "volume";

    private UnitConverter() {
        // Utility class: no instances
    }

    /** Alternative spellings mapped to the app's standard unit codes. */
    private static final Map<String, String> ALIASES = new HashMap<>();
    /** Standard unit code to its family (count, mass or volume). */
    private static final Map<String, String> FAMILY = new HashMap<>();
    /** Standard unit code to how many base units it equals. */
    private static final Map<String, Double> FACTOR = new HashMap<>();

    static {
        register("pcs", COUNT, 1, "pc", "piece", "pieces", "each", "whole", "item", "items");
        register("g", MASS, 1, "gram", "grams", "gr", "gm");
        register("kg", MASS, 1000, "kilogram", "kilograms", "kgs", "kilo", "kilos");
        register("ml", VOLUME, 1, "millilitre", "millilitres", "milliliter", "milliliters", "mls");
        register("l", VOLUME, 1000, "litre", "litres", "liter", "liters", "lt");
        register("tsp", VOLUME, 5, "teaspoon", "teaspoons", "tsps");
        register("tbsp", VOLUME, 15, "tablespoon", "tablespoons", "tbs", "tbsps");
        register("cup", VOLUME, 250, "cups");
    }

    private static void register(String code, String family, double factor, String... aliases) {
        FAMILY.put(code, family);
        FACTOR.put(code, factor);
        ALIASES.put(code, code);
        for (String alias : aliases) {
            ALIASES.put(alias, code);
        }
    }

    /** Returns the standard code for a unit, e.g. "Tablespoons" -> "tbsp". */
    public static String normalizeUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "pcs";
        }
        String key = unit.trim().toLowerCase(Locale.ROOT);
        String code = ALIASES.get(key);
        return code != null ? code : key;
    }

    /**
     * Returns the family a unit belongs to. Unknown units get their own
     * family ("unit:xyz") so they only match the exact same unit.
     */
    public static String familyOf(String unit) {
        String code = normalizeUnit(unit);
        String family = FAMILY.get(code);
        return family != null ? family : "unit:" + code;
    }

    /** Converts a quantity to its family's base unit (pcs, g or ml). */
    public static double toBase(double quantity, String unit) {
        Double factor = FACTOR.get(normalizeUnit(unit));
        return quantity * (factor != null ? factor : 1);
    }
}
