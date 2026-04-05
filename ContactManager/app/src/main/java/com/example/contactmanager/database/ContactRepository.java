package com.example.contactmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.contactmanager.model.Contact;
import com.example.contactmanager.model.Group;
import com.example.contactmanager.model.PhoneNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactRepository: Single point of access for all database operations.
 * Acts as the Model layer in MVC — Activities (Controllers) call this,
 * never the DatabaseHelper directly.
 *
 * Handles:
 *  - Contact CRUD
 *  - Phone number CRUD
 *  - Group CRUD
 *  - Blacklist management
 *  - Search and sort queries
 */
public class ContactRepository {

    private final DatabaseHelper dbHelper;
    private static ContactRepository instance;

    public static synchronized ContactRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ContactRepository(context);
        }
        return instance;
    }

    private ContactRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // =========================================================================
    //  CONTACT CRUD
    // =========================================================================

    /** Inserts a new contact and its phone numbers. Returns the new row ID. */
    public long addContact(Contact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = contactToValues(contact);
        long id = db.insert(DatabaseHelper.TABLE_CONTACTS, null, values);
        contact.setId(id);
        for (PhoneNumber phone : contact.getPhoneNumbers()) {
            insertPhoneNumber(db, phone, id);
        }
        return id;
    }

    /** Retrieves a contact by ID with all its phone numbers. */
    public Contact getContact(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_CONTACTS, null,
            DatabaseHelper.COL_CONTACT_ID + "=?",
            new String[]{String.valueOf(id)},
            null, null, null
        );
        Contact contact = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) contact = cursorToContact(cursor);
            cursor.close();
        }
        if (contact != null) {
            contact.setPhoneNumbers(getPhoneNumbersForContact(id));
        }
        return contact;
    }

    /**
     * Returns all contacts sorted alphabetically by name (A–Z).
     * Each contact includes its phone numbers.
     */
    public List<Contact> getAllContacts() {
        return queryContacts(null, null,
            DatabaseHelper.COL_CONTACT_NAME + " COLLATE NOCASE ASC");
    }

    /**
     * Returns contacts sorted by group name first, then contact name.
     * Contacts with no group appear last under "No Group".
     */
    public List<Contact> getContactsSortedByGroup() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT c.* FROM " + DatabaseHelper.TABLE_CONTACTS + " c " +
            "LEFT JOIN " + DatabaseHelper.TABLE_GROUPS + " g " +
            "  ON c." + DatabaseHelper.COL_CONTACT_GROUP_ID +
            "   = g." + DatabaseHelper.COL_GROUP_ID +
            " ORDER BY " +
            "  CASE WHEN g." + DatabaseHelper.COL_GROUP_NAME + " IS NULL THEN 1 ELSE 0 END, " +
            "  g."  + DatabaseHelper.COL_GROUP_NAME + " COLLATE NOCASE ASC, " +
            "  c."  + DatabaseHelper.COL_CONTACT_NAME + " COLLATE NOCASE ASC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Contact c = cursorToContact(cursor);
                c.setPhoneNumbers(getPhoneNumbersForContact(c.getId()));
                contacts.add(c);
            }
            cursor.close();
        }
        return contacts;
    }

    /** Returns contacts whose name contains the query string (case-insensitive). */
    public List<Contact> searchContacts(String query) {
        return queryContacts(
            DatabaseHelper.COL_CONTACT_NAME + " LIKE ?",
            new String[]{"%" + query + "%"},
            DatabaseHelper.COL_CONTACT_NAME + " COLLATE NOCASE ASC"
        );
    }

    /** Updates an existing contact's fields and replaces all phone numbers. */
    public int updateContact(Contact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = contactToValues(contact);
        int rows = db.update(
            DatabaseHelper.TABLE_CONTACTS, values,
            DatabaseHelper.COL_CONTACT_ID + "=?",
            new String[]{String.valueOf(contact.getId())}
        );
        // Replace phone numbers
        db.delete(DatabaseHelper.TABLE_PHONE_NUMBERS,
            DatabaseHelper.COL_PHONE_CONTACT_ID + "=?",
            new String[]{String.valueOf(contact.getId())});
        for (PhoneNumber phone : contact.getPhoneNumbers()) {
            insertPhoneNumber(db, phone, contact.getId());
        }
        return rows;
    }

    /** Deletes a contact and cascades to its phone numbers (via FK). */
    public int deleteContact(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
            DatabaseHelper.TABLE_CONTACTS,
            DatabaseHelper.COL_CONTACT_ID + "=?",
            new String[]{String.valueOf(id)}
        );
    }

    // =========================================================================
    //  PHONE NUMBER OPERATIONS
    // =========================================================================

    public List<PhoneNumber> getPhoneNumbersForContact(long contactId) {
        List<PhoneNumber> phones = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_PHONE_NUMBERS, null,
            DatabaseHelper.COL_PHONE_CONTACT_ID + "=?",
            new String[]{String.valueOf(contactId)},
            null, null, null
        );
        if (cursor != null) {
            while (cursor.moveToNext()) phones.add(cursorToPhoneNumber(cursor));
            cursor.close();
        }
        return phones;
    }

    private void insertPhoneNumber(SQLiteDatabase db, PhoneNumber phone, long contactId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PHONE_CONTACT_ID, contactId);
        values.put(DatabaseHelper.COL_PHONE_NUMBER, phone.getNumber());
        values.put(DatabaseHelper.COL_PHONE_TYPE, phone.getType());
        db.insert(DatabaseHelper.TABLE_PHONE_NUMBERS, null, values);
    }

    // =========================================================================
    //  GROUP CRUD
    // =========================================================================

    /** Creates a new group. Returns -1 if the name already exists. */
    public long addGroup(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_GROUP_NAME, group.getName());
        return db.insert(DatabaseHelper.TABLE_GROUPS, null, values);
    }

    public Group getGroup(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GROUPS, null,
            DatabaseHelper.COL_GROUP_ID + "=?",
            new String[]{String.valueOf(id)},
            null, null, null
        );
        Group group = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) group = cursorToGroup(cursor);
            cursor.close();
        }
        return group;
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GROUPS, null,
            null, null, null, null,
            DatabaseHelper.COL_GROUP_NAME + " COLLATE NOCASE ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Group g = cursorToGroup(cursor);
                g.setContactCount(getContactCountForGroup(g.getId()));
                groups.add(g);
            }
            cursor.close();
        }
        return groups;
    }

    public int updateGroup(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_GROUP_NAME, group.getName());
        return db.update(
            DatabaseHelper.TABLE_GROUPS, values,
            DatabaseHelper.COL_GROUP_ID + "=?",
            new String[]{String.valueOf(group.getId())}
        );
    }

    /** Deletes a group and unassigns all its contacts (contacts are NOT deleted). */
    public int deleteGroup(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Unassign contacts from this group
        ContentValues unassign = new ContentValues();
        unassign.put(DatabaseHelper.COL_CONTACT_GROUP_ID, -1);
        db.update(DatabaseHelper.TABLE_CONTACTS, unassign,
            DatabaseHelper.COL_CONTACT_GROUP_ID + "=?",
            new String[]{String.valueOf(id)});
        // Delete the group
        return db.delete(
            DatabaseHelper.TABLE_GROUPS,
            DatabaseHelper.COL_GROUP_ID + "=?",
            new String[]{String.valueOf(id)}
        );
    }

    /** Returns all contacts assigned to a specific group, sorted by name. */
    public List<Contact> getContactsInGroup(long groupId) {
        return queryContacts(
            DatabaseHelper.COL_CONTACT_GROUP_ID + "=?",
            new String[]{String.valueOf(groupId)},
            DatabaseHelper.COL_CONTACT_NAME + " COLLATE NOCASE ASC"
        );
    }

    private int getContactCountForGroup(long groupId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACTS +
            " WHERE " + DatabaseHelper.COL_CONTACT_GROUP_ID + "=?",
            new String[]{String.valueOf(groupId)}
        );
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // =========================================================================
    //  BLACKLIST
    // =========================================================================

    /** Returns all contacts marked as blacklisted. */
    public List<Contact> getBlacklistedContacts() {
        return queryContacts(
            DatabaseHelper.COL_CONTACT_BLACKLISTED + "=1", null,
            DatabaseHelper.COL_CONTACT_NAME + " COLLATE NOCASE ASC"
        );
    }

    /** Sets or clears the blacklist flag for a contact. */
    public void setBlacklisted(long contactId, boolean blacklisted) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CONTACT_BLACKLISTED, blacklisted ? 1 : 0);
        db.update(DatabaseHelper.TABLE_CONTACTS, values,
            DatabaseHelper.COL_CONTACT_ID + "=?",
            new String[]{String.valueOf(contactId)});
    }

    /**
     * Returns true if the given phone number belongs to any blacklisted contact.
     * Used by the SmsReceiver to decide whether to block an incoming message.
     */
    public boolean isNumberBlacklisted(String number) {
        if (number == null || number.isEmpty()) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACTS + " c " +
            "INNER JOIN " + DatabaseHelper.TABLE_PHONE_NUMBERS + " p " +
            "  ON c." + DatabaseHelper.COL_CONTACT_ID +
            "   = p." + DatabaseHelper.COL_PHONE_CONTACT_ID +
            " WHERE c." + DatabaseHelper.COL_CONTACT_BLACKLISTED + " = 1" +
            "   AND p." + DatabaseHelper.COL_PHONE_NUMBER + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{number});
        boolean result = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) result = cursor.getInt(0) > 0;
            cursor.close();
        }
        return result;
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private List<Contact> queryContacts(String selection, String[] selectionArgs, String orderBy) {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_CONTACTS, null,
            selection, selectionArgs,
            null, null, orderBy
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Contact c = cursorToContact(cursor);
                c.setPhoneNumbers(getPhoneNumbersForContact(c.getId()));
                contacts.add(c);
            }
            cursor.close();
        }
        return contacts;
    }

    private Contact cursorToContact(Cursor cursor) {
        Contact c = new Contact();
        c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_ID)));
        c.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_NAME)));
        c.setPhotoUri(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_PHOTO_URI)));
        c.setBlacklisted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_BLACKLISTED)) == 1);
        c.setGroupId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_GROUP_ID)));
        return c;
    }

    private PhoneNumber cursorToPhoneNumber(Cursor cursor) {
        PhoneNumber p = new PhoneNumber();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE_ID)));
        p.setContactId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE_CONTACT_ID)));
        p.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE_NUMBER)));
        p.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE_TYPE)));
        return p;
    }

    private Group cursorToGroup(Cursor cursor) {
        Group g = new Group();
        g.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GROUP_ID)));
        g.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GROUP_NAME)));
        return g;
    }

    private ContentValues contactToValues(Contact contact) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_CONTACT_NAME,        contact.getName());
        v.put(DatabaseHelper.COL_CONTACT_PHOTO_URI,   contact.getPhotoUri());
        v.put(DatabaseHelper.COL_CONTACT_BLACKLISTED, contact.isBlacklisted() ? 1 : 0);
        v.put(DatabaseHelper.COL_CONTACT_GROUP_ID,    contact.getGroupId());
        return v;
    }
}
