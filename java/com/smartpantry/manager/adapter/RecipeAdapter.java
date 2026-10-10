package com.smartpantry.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.R;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.utils.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for recipe lists. Each row shows one MatchResult:
 * the recipe, plus what is missing (only shown if something is missing).
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** Lets the owning screen react when a recipe is tapped. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<RecipeMatcher.MatchResult> results = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    /** Replaces the data shown in the list and redraws it. */
    public void setResults(List<RecipeMatcher.MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeMatcher.MatchResult result = results.get(position);
        Recipe recipe = result.getRecipe();

        holder.textName.setText(recipe.getName());
        holder.textDescription.setText(recipe.getDescription());
        holder.textMeta.setText(holder.itemView.getContext().getString(R.string.recipe_meta,
                recipe.getPrepMinutes(), recipe.getIngredients().size()));

        if (result.isFullMatch()) {
            holder.textMissing.setVisibility(View.GONE);
        } else {
            StringBuilder missing = new StringBuilder();
            for (RecipeIngredient ingredient : result.getMissing()) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(ingredient.getDisplayText());
            }
            holder.textMissing.setText(holder.itemView.getContext()
                    .getString(R.string.recipe_missing, missing.toString()));
            holder.textMissing.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    /** Holds the views in one recipe row. */
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textDescription;
        final TextView textMeta;
        final TextView textMissing;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textDescription = itemView.findViewById(R.id.textRecipeDescription);
            textMeta = itemView.findViewById(R.id.textRecipeMeta);
            textMissing = itemView.findViewById(R.id.textRecipeMissing);
        }
    }
}
