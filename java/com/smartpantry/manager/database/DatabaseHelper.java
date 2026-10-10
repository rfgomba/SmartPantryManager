package com.smartpantry.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for the Smart Pantry Manager.
 * Tables:
 *  - pantry_items        : the user's ingredients (full CRUD)
 *  - recipes             : recipe name, description, prep time, steps
 *  - recipe_ingredients  : ingredient lines, linked to recipes by recipe_id
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    // Version 2 adds the recipe tables
    private static final int DATABASE_VERSION = 2;

    // ---------- Pantry table ----------
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // ---------- Recipe table ----------
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_DESC = "description";
    public static final String COL_RECIPE_PREP = "prep_minutes";
    public static final String COL_RECIPE_STEPS = "steps";

    // ---------- Recipe ingredients table ----------
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_EXPIRY + " TEXT)";

    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_NAME + " TEXT NOT NULL UNIQUE, "
                    + COL_RECIPE_DESC + " TEXT, "
                    + COL_RECIPE_PREP + " INTEGER, "
                    + COL_RECIPE_STEPS + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RI_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_RI_NAME + " TEXT NOT NULL, "
                    + COL_RI_QUANTITY + " REAL NOT NULL, "
                    + COL_RI_UNIT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES "
                    + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)";

    // Single shared instance so the whole app uses one database connection
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Turns on foreign key checks (SQLite has them off by default). */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Runs once on a fresh install: creates every table. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        createRecipeTables(db);
    }

    /**
     * Runs when an existing install has an older DATABASE_VERSION.
     * Only adds what is new, so the user's pantry items are NOT lost.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecipeTables(db);
        }
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENTS);
    }

    // =========================================================
    // PANTRY: CREATE
    // =========================================================

    /**
     * Saves a new pantry item.
     * @return the new row id, or -1 if the insert failed
     */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        long newId = db.insert(TABLE_PANTRY, null, toContentValues(item));
        item.setId(newId);
        return newId;
    }

    // =========================================================
    // PANTRY: READ
    // =========================================================

    /** Returns every pantry item, sorted alphabetically by name. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                items.add(cursorToPantryItem(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    /** Returns one pantry item by id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToPantryItem(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // =========================================================
    // PANTRY: UPDATE
    // =========================================================

    /**
     * Saves changes to an existing item (matched by its id).
     * @return number of rows changed (1 = success, 0 = item not found)
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, toContentValues(item),
                COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    // =========================================================
    // PANTRY: DELETE
    // =========================================================

    /**
     * Removes one item by id.
     * @return number of rows deleted (1 = success, 0 = item not found)
     */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Removes every pantry item (used by "Clear all pantry items" in Settings).
     * @return number of rows deleted
     */
    public int deleteAllPantryItems() {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, null, null);
    }

    // =========================================================
    // RECIPES
    // =========================================================

    /**
     * Saves a recipe and all its ingredient lines in one transaction:
     * either everything is saved, or nothing is (no half-saved recipes).
     * @return the new recipe id, or -1 if it failed
     */
    public long addRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_RECIPE_NAME, recipe.getName());
            values.put(COL_RECIPE_DESC, recipe.getDescription());
            values.put(COL_RECIPE_PREP, recipe.getPrepMinutes());
            values.put(COL_RECIPE_STEPS, recipe.getSteps());
            long recipeId = db.insertOrThrow(TABLE_RECIPES, null, values);

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ContentValues iv = new ContentValues();
                iv.put(COL_RI_RECIPE_ID, recipeId);
                iv.put(COL_RI_NAME, ingredient.getName());
                iv.put(COL_RI_QUANTITY, ingredient.getQuantity());
                iv.put(COL_RI_UNIT, ingredient.getUnit());
                ingredient.setId(db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, iv));
                ingredient.setRecipeId(recipeId);
            }

            db.setTransactionSuccessful();
            recipe.setId(recipeId);
            return recipeId;
        } catch (Exception e) {
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    /** Number of recipes stored (used to decide whether to seed on first run). */
    public int getRecipeCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /** Returns all recipes (A to Z), each with its ingredient list loaded. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_RECIPE_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                Recipe recipe = cursorToRecipe(cursor);
                loadIngredients(db, recipe);
                recipes.add(recipe);
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }

    /** Returns one recipe with its ingredients, or null if not found. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                Recipe recipe = cursorToRecipe(cursor);
                loadIngredients(db, recipe);
                return recipe;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /** Loads the ingredient lines that belong to one recipe. */
    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())}, null, null, COL_ID + " ASC");
        try {
            while (cursor.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT))));
            }
        } finally {
            cursor.close();
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    /** Converts a PantryItem into column/value pairs for insert and update. */
    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName().trim());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.hasExpiryDate() ? item.getExpiryDate() : null);
        return values;
    }

    /** Converts the current cursor row into a PantryItem object. */
    private PantryItem cursorToPantryItem(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY))
        );
    }

    /** Converts the current cursor row into a Recipe (without ingredients). */
    private Recipe cursorToRecipe(Cursor cursor) {
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_DESC)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECIPE_PREP)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS))
        );
    }
}
