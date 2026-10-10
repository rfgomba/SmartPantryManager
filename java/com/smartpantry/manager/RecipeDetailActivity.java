package com.smartpantry.manager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.utils.AppSettings;
import com.smartpantry.manager.utils.ExpiryUtils;
import com.smartpantry.manager.utils.RecipeMatcher;

import java.util.List;

/**
 * Recipe Detail screen. Receives a recipe id through the Intent and shows the
 * full ingredient list (marking what the pantry has or lacks) and the method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra key for the id of the recipe to show. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private static final int COLOR_HAVE = Color.parseColor("#2E7D32");    // green
    private static final int COLOR_MISSING = Color.parseColor("#C62828"); // red

    private DatabaseHelper db;
    private long recipeId;
    private MaterialToolbar toolbar;
    private TextView textDescription;
    private TextView textPrep;
    private TextView textStatus;
    private LinearLayout containerIngredients;
    private LinearLayout containerSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        db = DatabaseHelper.getInstance(this);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textDescription = findViewById(R.id.textDescription);
        textPrep = findViewById(R.id.textPrep);
        textStatus = findViewById(R.id.textStatus);
        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRecipe();
    }

    private void showRecipe() {
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.error_recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(recipe.getName());
        textDescription.setText(recipe.getDescription());
        textPrep.setText(getString(R.string.recipe_prep, recipe.getPrepMinutes()));

        // Check each ingredient against the current pantry
        RecipeMatcher matcher = new RecipeMatcher(db.getAllPantryItems(),
                AppSettings.isExcludeExpired(this), ExpiryUtils.today());
        RecipeMatcher.MatchResult result = matcher.check(recipe);

        if (result.isFullMatch()) {
            textStatus.setText(R.string.status_can_cook);
            textStatus.setTextColor(COLOR_HAVE);
        } else {
            textStatus.setText(getString(R.string.status_missing, result.getMissingCount()));
            textStatus.setTextColor(COLOR_MISSING);
        }

        // Ingredients: one row each, ticked or crossed
        containerIngredients.removeAllViews();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean have = matcher.hasEnough(ingredient);
            TextView row = makeRow(getString(
                    have ? R.string.ingredient_have : R.string.ingredient_missing,
                    ingredient.getDisplayText()));
            row.setTextColor(have ? COLOR_HAVE : COLOR_MISSING);
            containerIngredients.addView(row);
        }

        // Method: numbered steps
        containerSteps.removeAllViews();
        List<String> steps = recipe.getStepsList();
        for (int i = 0; i < steps.size(); i++) {
            containerSteps.addView(makeRow(getString(R.string.step_format, i + 1, steps.get(i))));
        }
    }

    /** Creates a simple text row for the ingredient and step lists */
    private TextView makeRow(String text) {
        TextView row = new TextView(this);
        row.setText(text);
        row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        row.setPadding(0, padding, 0, padding);
        return row;
    }
}
