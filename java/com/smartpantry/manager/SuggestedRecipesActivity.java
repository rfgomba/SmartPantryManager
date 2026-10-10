package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.adapter.RecipeAdapter;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.utils.AppSettings;
import com.smartpantry.manager.utils.ExpiryUtils;
import com.smartpantry.manager.utils.RecipeMatcher;

import java.util.List;

/**
 * Suggested Recipes screen. Runs the strict-matching rule against the
 * current pantry and lists ONLY recipes the user can make right now.
 * An optional, clearly separated "Almost there" list shows recipes
 * missing exactly one ingredient.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper db;
    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostThereAdapter;
    private TextView textHeaderReady;
    private TextView textNoMatches;
    private TextView textHeaderAlmost;
    private LinearLayout sectionAlmostThere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textHeaderReady = findViewById(R.id.textHeaderReady);
        textNoMatches = findViewById(R.id.textNoMatches);
        textHeaderAlmost = findViewById(R.id.textHeaderAlmost);
        sectionAlmostThere = findViewById(R.id.sectionAlmostThere);

        RecyclerView recyclerSuggested = findViewById(R.id.recyclerSuggested);
        suggestedAdapter = new RecipeAdapter(this);
        recyclerSuggested.setAdapter(suggestedAdapter);

        RecyclerView recyclerAlmostThere = findViewById(R.id.recyclerAlmostThere);
        almostThereAdapter = new RecipeAdapter(this);
        recyclerAlmostThere.setAdapter(almostThereAdapter);
    }

    /** Re-run matching every time the screen is shown, so it reflects pantry changes. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Recipe> recipes = db.getAllRecipes();
        RecipeMatcher matcher = new RecipeMatcher(db.getAllPantryItems(),
                AppSettings.isExcludeExpired(this), ExpiryUtils.today());

        // 1) Strict suggestions: every ingredient present in enough quantity
        List<RecipeMatcher.MatchResult> suggestions = matcher.getSuggestions(recipes);
        suggestedAdapter.setResults(suggestions);
        textHeaderReady.setText(getString(R.string.header_ready_to_cook, suggestions.size()));
        textNoMatches.setVisibility(suggestions.isEmpty() ? View.VISIBLE : View.GONE);

        // 2) Optional separate list: missing exactly one ingredient
        List<RecipeMatcher.MatchResult> almostThere = matcher.getAlmostThere(recipes);
        boolean showAlmost = AppSettings.isShowAlmostThere(this) && !almostThere.isEmpty();
        almostThereAdapter.setResults(almostThere);
        textHeaderAlmost.setText(getString(R.string.header_almost_there, almostThere.size()));
        sectionAlmostThere.setVisibility(showAlmost ? View.VISIBLE : View.GONE);
    }

    /** Explicit Intent opens Recipe Detail, passing the recipe id as an extra. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
