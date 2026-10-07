package com.smartpantry.manager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.PantryItem;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Add button: opens Add Ingredient screen (built in Phase 6)
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v -> showComingSoon("Add Ingredient"));

        // TEMP TEST (Phase 4): check the database saves and reads. Removed in Phase 5.
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (db.getAllPantryItems().isEmpty()) {
            db.addPantryItem(new PantryItem("Tomato", 4, "pcs", null));
        }
        for (PantryItem p : db.getAllPantryItems()) {
            Log.d("DB_TEST", p.getId() + ": " + p.getName() + " - " + p.getDisplayQuantity());
        }
    }


    private void showComingSoon(String screenName) {
        Toast.makeText(this, screenName + " coming soon", Toast.LENGTH_SHORT).show();
    }
}