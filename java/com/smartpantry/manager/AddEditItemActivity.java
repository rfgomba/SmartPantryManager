package com.smartpantry.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.model.PantryItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Ingredient screen.
 * - Opened with no extra: Add mode (creates a new item).
 * - Opened with EXTRA_ITEM_ID: Edit mode (loads that item and updates it).
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** Intent extra key used by other screens to pass the item id to edit. */
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 40;
    private static final double QUANTITY_MAX = 100000;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private Spinner spinnerUnit;
    private TextView textExpiry;
    private ArrayAdapter<CharSequence> unitAdapter;

    private String selectedExpiryDate = null; // yyyy-MM-dd, or null if none
    private PantryItem editingItem = null;    // null = Add mode
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
        unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Edit mode: read the id passed in the Intent and load that item
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId);
            if (editingItem == null) {
                Toast.makeText(this, R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(R.string.title_edit_ingredient);
            // Only pre-fill on first open; after rotation Android restores typed text itself
            if (savedInstanceState == null) {
                fillForm(editingItem);
            }
        }

        // Restore the chosen date after rotation (it is not stored in a view)
        if (savedInstanceState != null) {
            setExpiryDate(savedInstanceState.getString(STATE_EXPIRY));
        }

        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);
        Button buttonSave = findViewById(R.id.buttonSave);

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> setExpiryDate(null));
        buttonSave.setOnClickListener(v -> saveItem());
    }

    /** Lifecycle: save values that would otherwise be lost on rotation. */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, selectedExpiryDate);
    }

    /** Puts an existing item's values into the form (Edit mode). */
    private void fillForm(PantryItem item) {
        editName.setText(item.getName());
        // Show 4 instead of 4.0
        double q = item.getQuantity();
        editQuantity.setText(q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q));
        int unitPosition = unitAdapter.getPosition(item.getUnit());
        if (unitPosition >= 0) {
            spinnerUnit.setSelection(unitPosition);
        }
        setExpiryDate(item.getExpiryDate());
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

    /** Validates the form, then inserts (Add mode) or updates (Edit mode). */
    private void saveItem() {
        String name = validateName();
        Double quantity = validateQuantity();
        if (name == null || quantity == null) {
            return; // errors are already shown under the fields
        }
        String unit = spinnerUnit.getSelectedItem().toString();

        if (editingItem == null) {
            // CREATE
            PantryItem item = new PantryItem(name, quantity, unit, selectedExpiryDate);
            if (db.addPantryItem(item) == -1) {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, getString(R.string.item_added, name), Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE: keep the same id, change the other fields
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(selectedExpiryDate);
            if (db.updatePantryItem(editingItem) == 0) {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, getString(R.string.item_updated, name), Toast.LENGTH_SHORT).show();
        }
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
