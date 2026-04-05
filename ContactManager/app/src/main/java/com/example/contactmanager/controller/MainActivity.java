package com.example.contactmanager.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactmanager.R;
import com.example.contactmanager.adapter.ContactAdapter;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;
import com.example.contactmanager.util.ContactImporterExporter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ContactAdapter.OnContactClickListener {

    private static final int SORT_BY_NAME  = 0;
    private static final int SORT_BY_GROUP = 1;

    private ContactAdapter    contactAdapter;
    private ContactRepository repository;
    private TextView          tvEmptyState;

    private int    currentSortOrder  = SORT_BY_NAME;
    private String currentSearchQuery = "";

    // ---- Activity Result Launchers ----

    private final ActivityResultLauncher<Intent> addContactLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                refreshContacts();
                Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
            }
        });

    private final ActivityResultLauncher<Intent> importFileLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    int count = ContactImporterExporter.importContacts(this, uri, repository);
                    Toast.makeText(this,
                        "Imported " + count + " contact" + (count != 1 ? "s" : ""),
                        Toast.LENGTH_SHORT).show();
                    refreshContacts();
                }
            }
        });

    // ---- Lifecycle ----

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        repository = ContactRepository.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
            new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        contactAdapter = new ContactAdapter(this);
        recyclerView.setAdapter(contactAdapter);

        tvEmptyState = findViewById(R.id.tvEmptyState);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddContact);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            addContactLauncher.launch(intent);
        });

        refreshContacts();
        requestRuntimePermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContacts();
    }

    // ---- Options Menu ----

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search contacts…");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim();
                refreshContacts();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_name) {
            currentSortOrder = SORT_BY_NAME;
            refreshContacts();
            Toast.makeText(this, "Sorted by name", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sort_group) {
            currentSortOrder = SORT_BY_GROUP;
            refreshContacts();
            Toast.makeText(this, "Sorted by group", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_groups) {
            startActivity(new Intent(this, GroupsActivity.class));
            return true;
        } else if (id == R.id.action_blacklist) {
            startActivity(new Intent(this, BlacklistActivity.class));
            return true;
        } else if (id == R.id.action_export) {
            exportContacts();
            return true;
        } else if (id == R.id.action_import) {
            importContacts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---- Data ----

    private void refreshContacts() {
        List<Contact> contacts;
        boolean groupedView = false;

        if (!currentSearchQuery.isEmpty()) {
            contacts = repository.searchContacts(currentSearchQuery);
        } else if (currentSortOrder == SORT_BY_GROUP) {
            contacts = repository.getContactsSortedByGroup();
            groupedView = true;
        } else {
            contacts = repository.getAllContacts();
        }

        contactAdapter.updateContacts(contacts, groupedView, repository);

        // Toggle empty-state message
        tvEmptyState.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---- Export / Import ----

    private void exportContacts() {
        List<Contact> contacts = repository.getAllContacts();
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts to export", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri fileUri = ContactImporterExporter.exportContacts(this, contacts);
        if (fileUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Export contacts via…"));
        } else {
            Toast.makeText(this, "Export failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void importContacts() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        importFileLauncher.launch(Intent.createChooser(intent, "Select CSV contacts file"));
    }

    // ---- Contact click callbacks ----

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.getId());
        startActivity(intent);
    }

    @Override
    public void onContactLongClick(Contact contact) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Delete \"" + contact.getName() + "\"? This cannot be undone.")
            .setPositiveButton("Delete", (d, w) -> {
                repository.deleteContact(contact.getId());
                refreshContacts();
                Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ---- Runtime permissions ----

    private void requestRuntimePermissions() {
        List<String> needed = new ArrayList<>();
        String[] permissions = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_MEDIA_IMAGES
            };
        }
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needed.add(perm);
            }
        }
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 100);
        }
    }
}
