package com.example.contactmanager.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contactmanager.R;
import com.example.contactmanager.adapter.PhoneNumberInputAdapter;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;
import com.example.contactmanager.model.Group;
import com.example.contactmanager.model.PhoneNumber;
import com.example.contactmanager.util.InputValidator;

import java.util.ArrayList;
import java.util.List;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText               etName;
    private ImageView              ivPhoto;
    private Spinner                spinnerGroup;
    private PhoneNumberInputAdapter phoneAdapter;
    private ContactRepository      repository;
    private Contact                editingContact = null;
    private String                 selectedPhotoUri = null;
    private List<Group>            groups;

    // Launcher for the system photo picker
    private final ActivityResultLauncher<String> photoPickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // Persist read permission across reboots
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                selectedPhotoUri = uri.toString();
                Glide.with(this).load(uri).circleCrop().into(ivPhoto);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ContactRepository.getInstance(this);

        // Bind views
        etName           = findViewById(R.id.etContactName);
        ivPhoto          = findViewById(R.id.ivContactPhotoEdit);
        spinnerGroup     = findViewById(R.id.spinnerGroup);
        RecyclerView rvPhones = findViewById(R.id.rvPhoneNumbersEdit);
        Button btnSave   = findViewById(R.id.btnSave);
        Button btnAddPhone = findViewById(R.id.btnAddPhoneNumber);
        ImageButton btnSelectPhoto = findViewById(R.id.btnSelectPhoto);

        // Phone number list (starts with one blank row)
        List<PhoneNumber> initialPhones = new ArrayList<>();
        initialPhones.add(new PhoneNumber(0, 0, "", "mobile"));
        phoneAdapter = new PhoneNumberInputAdapter(initialPhones);
        rvPhones.setLayoutManager(new LinearLayoutManager(this));
        rvPhones.setNestedScrollingEnabled(false);
        rvPhones.setAdapter(phoneAdapter);

        // Group spinner
        loadGroupsSpinner();

        btnSelectPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnAddPhone.setOnClickListener(v -> phoneAdapter.addPhoneNumber());
        btnSave.setOnClickListener(v -> saveContact());

        // Determine if editing
        long contactId = getIntent().getLongExtra(ContactDetailActivity.EXTRA_CONTACT_ID, -1);
        if (contactId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Contact");
            editingContact = repository.getContact(contactId);
            if (editingContact != null) populateForm(editingContact);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("New Contact");
        }
    }

    // ---- Helpers ----

    private void loadGroupsSpinner() {
        groups = repository.getAllGroups();
        List<String> names = new ArrayList<>();
        names.add("No Group");
        for (Group g : groups) names.add(g.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(adapter);
    }

    private void populateForm(Contact contact) {
        etName.setText(contact.getName());

        // Photo
        selectedPhotoUri = contact.getPhotoUri();
        if (selectedPhotoUri != null && !selectedPhotoUri.isEmpty()) {
            Glide.with(this).load(Uri.parse(selectedPhotoUri)).circleCrop().into(ivPhoto);
        }

        // Phone numbers
        List<PhoneNumber> phones = contact.getPhoneNumbers();
        if (phones.isEmpty()) {
            phones = new ArrayList<>();
            phones.add(new PhoneNumber(0, 0, "", "mobile"));
        }
        phoneAdapter.setPhoneNumbers(phones);

        // Group spinner
        if (contact.getGroupId() != -1) {
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).getId() == contact.getGroupId()) {
                    spinnerGroup.setSelection(i + 1); // +1 for "No Group"
                    break;
                }
            }
        }
    }

    private void saveContact() {
        String name = etName.getText().toString().trim();
        if (!InputValidator.isValidName(name)) {
            etName.setError("Please enter a valid name (1–100 characters)");
            etName.requestFocus();
            return;
        }

        // Collect and validate phone numbers
        List<PhoneNumber> rawPhones = phoneAdapter.getPhoneNumbers();
        List<PhoneNumber> validPhones = new ArrayList<>();
        for (PhoneNumber phone : rawPhones) {
            String num = phone.getNumber().trim();
            if (num.isEmpty()) continue;
            if (!InputValidator.isValidPhoneNumber(num)) {
                Toast.makeText(this,
                    "\"" + num + "\" is not a valid phone number",
                    Toast.LENGTH_LONG).show();
                return;
            }
            phone.setNumber(num);
            validPhones.add(phone);
        }

        if (validPhones.isEmpty()) {
            Toast.makeText(this, "Please add at least one phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resolve group
        int spinnerPos = spinnerGroup.getSelectedItemPosition();
        long groupId = (spinnerPos == 0) ? -1 : groups.get(spinnerPos - 1).getId();

        if (editingContact != null) {
            // Update existing
            editingContact.setName(name);
            editingContact.setPhotoUri(selectedPhotoUri);
            editingContact.setGroupId(groupId);
            editingContact.setPhoneNumbers(validPhones);
            repository.updateContact(editingContact);
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
        } else {
            // Create new
            Contact newContact = new Contact();
            newContact.setName(name);
            newContact.setPhotoUri(selectedPhotoUri);
            newContact.setGroupId(groupId);
            newContact.setPhoneNumbers(validPhones);
            repository.addContact(newContact);
            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
