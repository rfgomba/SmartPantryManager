package com.smartpantry.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.PantryItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add Ingredient screen. Validates the form, then saves a new PantryItem
 * to SQLite and returns to the Pantry List (which reloads in onResume).
 */
public class AddEditItemActivity extends AppCompatActivity {

    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 40;
    private static final double QUANTITY_MAX = 100000;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private Spinner spinnerUnit;
    private TextView textExpiry;

    private String selectedExpiryDate = null; // yyyy-MM-dd, or null if none
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = DatabaseHelper.getInstance(this);

        // Toolbar back arrow closes this screen
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        textExpiry = findViewById(R.id.textExpiry);

        // Unit dropdown filled from the string-array in strings.xml
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);
        Button buttonSave = findViewById(R.id.buttonSave);

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> setExpiryDate(null));
        buttonSave.setOnClickListener(v -> saveItem());
    }

    /** Opens a calendar dialog; the chosen date is stored as yyyy-MM-dd. */
    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    // month is 0-based in DatePicker, so add 1
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                    setExpiryDate(date);
                },
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setExpiryDate(String date) {
        selectedExpiryDate = date;
        textExpiry.setText(date == null ? getString(R.string.no_expiry) : date);
    }

    /** Validates the form; saves and closes only if every field is valid. */
    private void saveItem() {
        String name = validateName();
        Double quantity = validateQuantity();
        if (name == null || quantity == null) {
            return; // errors are already shown under the fields
        }
        String unit = spinnerUnit.getSelectedItem().toString();

        PantryItem item = new PantryItem(name, quantity, unit, selectedExpiryDate);
        long newId = db.addPantryItem(item);

        if (newId == -1) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.item_added, name), Toast.LENGTH_SHORT).show();
        finish(); // back to Pantry List, which reloads in onResume()
    }

    /** @return the cleaned-up name, or null if invalid (error shown on the field). */
    private String validateName() {
        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        layoutName.setError(null);

        if (TextUtils.isEmpty(name)) {
            layoutName.setError(getString(R.string.error_name_required));
            return null;
        }
        if (name.length() < NAME_MIN || name.length() > NAME_MAX) {
            layoutName.setError(getString(R.string.error_name_length));
            return null;
        }
        // Letters (any language), spaces, hyphens and apostrophes only
        if (!name.matches("[\\p{L} '\\-]+")) {
            layoutName.setError(getString(R.string.error_name_chars));
            return null;
        }
        // Collapse double spaces, e.g. "green   pepper" -> "green pepper"
        return name.replaceAll("\\s+", " ");
    }

    /** @return the quantity, or null if invalid (error shown on the field). */
    private Double validateQuantity() {
        String text = editQuantity.getText() == null ? "" : editQuantity.getText().toString().trim();
        layoutQuantity.setError(null);

        if (TextUtils.isEmpty(text)) {
            layoutQuantity.setError(getString(R.string.error_quantity_required));
            return null;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return null;
        }
        if (quantity <= 0) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return null;
        }
        if (quantity > QUANTITY_MAX) {
            layoutQuantity.setError(getString(R.string.error_quantity_too_large));
            return null;
        }
        return quantity;
    }
}
