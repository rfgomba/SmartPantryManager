package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.smartpantry.manager.adapter.PantryAdapter;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.PantryItem;

import java.util.List;

/**
 * Pantry List screen (home). Shows all pantry items from the SQLite database
 * in a RecyclerView, plus the bottom navigation bar and the + button.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemListener {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerPantry;
    private TextView textEmptyPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(this);

        // List setup: the adapter turns PantryItem objects into rows
        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        pantryAdapter = new PantryAdapter(this);
        recyclerPantry.setAdapter(pantryAdapter);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_pantry);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_pantry) {
                    // Current home view (Pantry List)
                    return true;
                } else if (itemId == R.id.nav_suggestions) {
                    // open SuggestedRecipesActivity via Intent
                    showComingSoon("Suggested Recipes");
                    return false;
                } else if (itemId == R.id.nav_settings) {
                    // open SettingsActivity via Intent
                    showComingSoon("Settings");
                    return false;
                }
                return false;
            }
        });

        // Add button: explicit Intent opens the Add Ingredient screen
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));
    }

    /**
     * Runs every time this screen comes back into view (lifecycle),
     * so the list always reflects the latest database contents.
     */
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_pantry);
        loadPantryItems();
    }

    /** Reads all items from SQLite and shows either the list or the empty message. */
    private void loadPantryItems() {
        List<PantryItem> items = db.getAllPantryItems();
        pantryAdapter.setItems(items);

        boolean isEmpty = items.isEmpty();
        textEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerPantry.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ---------- Row taps from the adapter ----------

    @Override
    public void onItemClick(PantryItem item) {
        
        showComingSoon("Edit " + item.getName());
    }

    @Override
    public void onDeleteClick(PantryItem item) {
        
        showComingSoon("Delete " + item.getName());
    }

    private void showComingSoon(String screenName) {
        Toast.makeText(this, screenName + " coming soon", Toast.LENGTH_SHORT).show();
    }
}
