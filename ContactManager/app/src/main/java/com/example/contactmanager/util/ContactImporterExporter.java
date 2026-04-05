package com.example.contactmanager.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;
import com.example.contactmanager.model.PhoneNumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContactImporterExporter {

    private static final String TAG = "ContactExporter";
    private static final String EXPORT_FILENAME = "contacts_export.csv";

    public static Uri exportContacts(Context context, List<Contact> contacts) {
        try {
            File exportDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                Log.e(TAG, "Could not create export directory");
                return null;
            }
            File file = new File(exportDir, EXPORT_FILENAME);
            FileWriter writer = new FileWriter(file);

            // Header row
            writer.write("Name,PhoneNumber,PhoneType,PhotoUri,Blacklisted,GroupId\n");

            for (Contact contact : contacts) {
                String name        = escapeCsv(contact.getName());
                String photoUri    = escapeCsv(contact.getPhotoUri() != null ? contact.getPhotoUri() : "");
                String blacklisted = contact.isBlacklisted() ? "1" : "0";
                String groupId     = String.valueOf(contact.getGroupId());

                if (contact.getPhoneNumbers().isEmpty()) {
                    // Emit one row with empty phone fields
                    writer.write(name + ",,mobile," + photoUri + "," + blacklisted + "," + groupId + "\n");
                } else {
                    for (PhoneNumber phone : contact.getPhoneNumbers()) {
                        writer.write(
                            name + "," +
                            escapeCsv(phone.getNumber()) + "," +
                            escapeCsv(phone.getType()) + "," +
                            photoUri + "," +
                            blacklisted + "," +
                            groupId + "\n"
                        );
                    }
                }
            }
            writer.close();

            return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
        } catch (Exception e) {
            Log.e(TAG, "Export failed", e);
            return null;
        }
    }

    public static int importContacts(Context context, Uri uri, ContactRepository repository) {
        int count = 0;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return 0;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;

            // Accumulate multi-phone contacts
            String   lastContactName = null;
            Contact  pendingContact  = null;

            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip CSV header
                if (line.trim().isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length < 6) continue;

                String  nameRaw    = parts[0].trim();
                String  number     = parts[1].trim();
                String  type       = parts[2].trim().isEmpty() ? "mobile" : parts[2].trim();
                String  photoUri   = parts[3].trim().isEmpty() ? null : parts[3].trim();
                boolean blacklisted = "1".equals(parts[4].trim());
                long    groupId;
                try { groupId = Long.parseLong(parts[5].trim()); }
                catch (NumberFormatException e) { groupId = -1; }

                if (!nameRaw.equals(lastContactName)) {
                    // Save previous pending contact
                    if (pendingContact != null) {
                        repository.addContact(pendingContact);
                        count++;
                    }
                    // Start new contact
                    pendingContact = new Contact();
                    pendingContact.setName(nameRaw);
                    pendingContact.setPhotoUri(photoUri);
                    pendingContact.setBlacklisted(blacklisted);
                    pendingContact.setGroupId(groupId);
                    lastContactName = nameRaw;
                }

                // Add phone number if non-empty
                if (!number.isEmpty() && pendingContact != null) {
                    PhoneNumber phone = new PhoneNumber();
                    phone.setNumber(number);
                    phone.setType(type);
                    pendingContact.addPhoneNumber(phone);
                }
            }

            // Save last pending contact
            if (pendingContact != null) {
                repository.addContact(pendingContact);
                count++;
            }

            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Import failed", e);
        }
        return count;
    }

    // ---- CSV helpers ----

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String[] parseCsvLine(String line) {
        List<String> result  = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
