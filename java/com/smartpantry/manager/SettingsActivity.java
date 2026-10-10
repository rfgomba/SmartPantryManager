package com.smartpantry.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.utils.AppSettings;

/**
 * Settings screen. Each change is saved straight away with SharedPreferences,
 * so it is remembered after the app is closed.
 */
public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView textWarnDays;
    private TextView textDataInfo;
    private Slider sliderWarnDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialSwitch switchExcludeExpired = findViewById(R.id.switchExcludeExpired);
        MaterialSwitch switchShowAlmostThere = findViewById(R.id.switchShowAlmostThere);
        MaterialSwitch switchHighlightExpiring = findViewById(R.id.switchHighlightExpiring);
        sliderWarnDays = findViewById(R.id.sliderWarnDays);
        textWarnDays = findViewById(R.id.textWarnDays);
        textDataInfo = findViewById(R.id.textDataInfo);
        Button buttonClearPantry = findViewById(R.id.buttonClearPantry);

        // Show the saved values
        switchExcludeExpired.setChecked(AppSettings.isExcludeExpired(this));
        switchShowAlmostThere.setChecked(AppSettings.isShowAlmostThere(this));
        switchHighlightExpiring.setChecked(AppSettings.isHighlightExpiring(this));
        int warnDays = AppSettings.getWarnDays(this);
        sliderWarnDays.setValue(warnDays);
        updateWarnDaysLabel(warnDays);
        sliderWarnDays.setEnabled(switchHighlightExpiring.isChecked());

        // Save each change immediately
        switchExcludeExpired.setOnCheckedChangeListener((button, checked) ->
                AppSettings.setExcludeExpired(this, checked));

        switchShowAlmostThere.setOnCheckedChangeListener((button, checked) ->
                AppSettings.setShowAlmostThere(this, checked));

        switchHighlightExpiring.setOnCheckedChangeListener((button, checked) -> {
            AppSettings.setHighlightExpiring(this, checked);
            sliderWarnDays.setEnabled(checked);
        });

        sliderWarnDays.addOnChangeListener((slider, value, fromUser) -> {
            int days = (int) value;
            AppSettings.setWarnDays(this, days);
            updateWarnDaysLabel(days);
        });

        buttonClearPantry.setOnClickListener(v -> confirmClearPantry());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDataInfo();
    }

    private void updateWarnDaysLabel(int days) {
        textWarnDays.setText(getString(R.string.setting_warn_days, days));
    }

    private void updateDataInfo() {
        textDataInfo.setText(getString(R.string.data_info,
                db.getRecipeCount(), db.getAllPantryItems().size()));
    }

    /** Asks before deleting every pantry item (recipes are kept). */
    private void confirmClearPantry() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_pantry_title)
                .setMessage(R.string.clear_pantry_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteAllPantryItems();
                    updateDataInfo();
                    Toast.makeText(this, R.string.clear_pantry_done, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
