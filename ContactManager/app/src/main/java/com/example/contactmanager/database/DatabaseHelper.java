package com.example.contactmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper: Manages the SQLite database schema.
 * Tables:
 *   - contacts:      id, name, photo_uri, blacklisted, group_id
 *   - phone_numbers: id, contact_id, number, type
 *   - groups:        id, name
 *
 * Uses a singleton pattern so only one instance exists per process.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    // ---- Table names ----
    public static final String TABLE_CONTACTS     = "contacts";
    public static final String TABLE_PHONE_NUMBERS = "phone_numbers";
    public static final String TABLE_GROUPS       = "groups";

    // ---- Contacts columns ----
    public static final String COL_CONTACT_ID         = "id";
    public static final String COL_CONTACT_NAME       = "name";
    public static final String COL_CONTACT_PHOTO_URI  = "photo_uri";
    public static final String COL_CONTACT_BLACKLISTED = "blacklisted";
    public static final String COL_CONTACT_GROUP_ID   = "group_id";

    // ---- Phone numbers columns ----
    public static final String COL_PHONE_ID         = "id";
    public static final String COL_PHONE_CONTACT_ID = "contact_id";
    public static final String COL_PHONE_NUMBER     = "number";
    public static final String COL_PHONE_TYPE       = "type";

    // ---- Groups columns ----
    public static final String COL_GROUP_ID   = "id";
    public static final String COL_GROUP_NAME = "name";

    // ---- CREATE statements ----
    private static final String CREATE_TABLE_GROUPS =
        "CREATE TABLE " + TABLE_GROUPS + " (" +
        COL_GROUP_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_GROUP_NAME + " TEXT NOT NULL UNIQUE" +
        ")";

    private static final String CREATE_TABLE_CONTACTS =
        "CREATE TABLE " + TABLE_CONTACTS + " (" +
        COL_CONTACT_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_CONTACT_NAME        + " TEXT NOT NULL, " +
        COL_CONTACT_PHOTO_URI   + " TEXT, " +
        COL_CONTACT_BLACKLISTED + " INTEGER DEFAULT 0, " +
        COL_CONTACT_GROUP_ID    + " INTEGER DEFAULT -1" +
        ")";

    private static final String CREATE_TABLE_PHONE_NUMBERS =
        "CREATE TABLE " + TABLE_PHONE_NUMBERS + " (" +
        COL_PHONE_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_PHONE_CONTACT_ID + " INTEGER NOT NULL, " +
        COL_PHONE_NUMBER     + " TEXT NOT NULL, " +
        COL_PHONE_TYPE       + " TEXT DEFAULT 'mobile', " +
        "FOREIGN KEY(" + COL_PHONE_CONTACT_ID + ") REFERENCES " +
            TABLE_CONTACTS + "(" + COL_CONTACT_ID + ") ON DELETE CASCADE" +
        ")";

    private static DatabaseHelper instance;

    /** Returns the singleton DatabaseHelper instance. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");
        db.execSQL(CREATE_TABLE_GROUPS);
        db.execSQL(CREATE_TABLE_CONTACTS);
        db.execSQL(CREATE_TABLE_PHONE_NUMBERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop and recreate on schema change
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONE_NUMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }
}
