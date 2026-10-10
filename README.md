# Smart Pantry Manager

- Author: Raymond Fainos Gomba
- Student number: 402411318
- Module: Mobile App Development 700, Richfield Graduate Institute of Technology

A Java Android app that helps reduce food waste. You record the ingredients you have at home, and the app suggests recipes you can cook using only those ingredients, with no shopping trip needed.

## Features

- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date) with input validation
- Pantry list: RecyclerView with a custom adapter; expired items shown in red, items expiring soon in orange
- Recipe collection: 18 recipes pre-loaded into the database on first run
- Suggested Recipes: strict matching, only recipes where every ingredient is in the pantry in at least the required quantity
- Almost There (bonus): a separate list of recipes missing exactly one ingredient
- Recipe detail: full ingredient list (ticked or crossed against your pantry) and method
- Settings: ignore expired items, show/hide Almost There, expiry highlight and warning days, clear pantry

## Try it (strict matching in 30 seconds)

1. Add these pantry items: Eggs 3 pcs, Milk 50 ml, Butter 1 tbsp, Salt 1 tsp.
2. Open Suggested Recipes: *Scrambled Eggs* appears.
3. Delete Salt and return: *Scrambled Eggs* disappears from the suggestions and appears under Almost there, missing salt.
4. Bonus: change Milk to 1 l and add Salt back. It still matches, because 1 l is converted to 1000 ml.

## Strict-matching rule

A recipe is suggested only if every ingredient is present in at least the required quantity. To cope with real-world input:

- Names are normalised: case, extra spaces, plurals (`tomatoes` = `tomato`, `berries` = `berry`) and common synonyms (`mealie meal` = `maize meal`)
- Quantities are converted to a base unit before comparing: mass to grams (g, kg), volume to millilitres (ml, l, tsp, tbsp, cup), count as pcs
- The same ingredient entered twice is added together
- Expired items can be excluded (Settings)

The logic lives in `java/com/smartpantry/manager/utils/RecipeMatcher.java` and is covered by unit tests in `test/`.

## Database choice: SQLite

The app uses SQLite through `SQLiteOpenHelper` (`DatabaseHelper.java`). Tables: `pantry_items`, `recipes` and `recipe_ingredients` (linked to recipes by a foreign key).

Why SQLite: pantry data is personal to one user on one device, so a local database is the simplest fit. It works offline, needs no account, server or hosting cost, is fast, and data persists between app launches. Settings are stored separately with SharedPreferences.

Trade-off: because the data lives only on the phone, there is no cloud backup and no sync between devices. Uninstalling the app or clearing its storage deletes the pantry. For a single-user pantry app this was an acceptable trade for simplicity and offline use; Firebase would be the next step if sync were needed.

## Known limitations

- Mass and volume cannot be compared. 500 g of flour does not satisfy a recipe needing 2 cups of flour, because converting would need each ingredient's density.
- Count and weight cannot be compared. 4 pcs of tomato does not satisfy a recipe needing 500 g of tomato.
- Synonyms are a fixed list. Common names are covered (e.g. scallion = spring onion), but unusual names may not match.
- Plural handling is rule-based. It covers normal English food words, not every irregular plural.
- Recipes are fixed. The 18 recipes are pre-loaded; users cannot add their own recipes.

## Project structure

This project keeps a flat layout; `build.gradle` points Gradle at these folders with `sourceSets`.

```
AndroidManifest.xml
build.gradle, settings.gradle
java/com/smartpantry/manager/
    MainActivity, AddEditItemActivity, SuggestedRecipesActivity,
    RecipeDetailActivity, SettingsActivity
    adapter/   PantryAdapter, RecipeAdapter
    database/  DatabaseHelper, RecipeSeeder
    model/     PantryItem, Recipe, RecipeIngredient
    utils/     RecipeMatcher, IngredientNormalizer, UnitConverter, ExpiryUtils, AppSettings
res/           layouts, menu, strings, theme
test/          RecipeMatcherTest (JUnit)
```

## Setup and run

Developed and tested with: Android Studio Quail 4 (2026.1.4) on Windows 11, Android Gradle Plugin 9.1.0, Gradle 9.7.1, Pixel 7 emulator. If you use an older Android Studio, accept its suggested Gradle plugin version when syncing.

1. Clone the repository:
   ```
   git clone https://github.com/rfgomba/SmartPantryManager.git
   ```
2. In Android Studio choose Open, select the project's `build.gradle` file, then Open as Project. Wait for Gradle sync to finish.
3. Create an emulator in Device Manager (for example Pixel 7) or connect a phone with USB debugging enabled.
4. Press Run.
5. To run the unit tests, right-click `test/com/smartpantry/manager/RecipeMatcherTest.java` and choose Run.

Requirements: minimum Android 7.0 (API 24). No internet connection, maps or location services are used.
