package com.smartpantry.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpantry.manager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for the Smart Pantry Manager.
 * Creates the database on first use and provides methods to save and load data.
 * The data is stored in a file on the device, so it survives closing the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // ---------- Pantry table ----------
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_EXPIRY + " TEXT)";

    // Single shared instance so the whole app uses one database connection
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Runs once, the first time the database is opened on a device. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
    }

    /** Runs when DATABASE_VERSION is increased. Rebuilds the tables. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Saves a new pantry item.
     * @return the new row id, or -1 if the insert failed
     */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName().trim());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.hasExpiryDate() ? item.getExpiryDate() : null);

        long newId = db.insert(TABLE_PANTRY, null, values);
        item.setId(newId);
        return newId;
    }

    // =========================================================
    // READ
    // =========================================================

    /** Returns every pantry item, sorted alphabetically by name. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                items.add(cursorToPantryItem(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    /** Returns one pantry item by id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToPantryItem(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /** Converts the current cursor row into a PantryItem object. */
    private PantryItem cursorToPantryItem(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY))
        );
    }
}
