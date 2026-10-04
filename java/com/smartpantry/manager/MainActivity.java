package com.smartpantry.manager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


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
    }


    private void showComingSoon(String screenName) {
        Toast.makeText(this, screenName + " coming soon", Toast.LENGTH_SHORT).show();
    }
}
