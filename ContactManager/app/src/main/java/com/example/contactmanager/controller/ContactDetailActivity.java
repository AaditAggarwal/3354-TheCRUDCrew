package com.example.contactmanager.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contactmanager.R;
import com.example.contactmanager.adapter.PhoneNumberAdapter;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;
import com.example.contactmanager.model.Group;
import com.google.android.material.chip.Chip;

/**
 * ContactDetailActivity (Controller): Displays all information about one contact.
 *
 * Features:
 *  - Show photo (or initial avatar), name, group, blacklist status
 *  - List all phone numbers with Call and SMS action buttons (FR 3)
 *  - Toggle blacklist status (FR 2)
 *  - Navigate to edit or delete the contact
 */
public class ContactDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_ID = "contact_id";

    private Contact           contact;
    private ContactRepository repository;
    private long              contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ContactRepository.getInstance(this);
        contactId  = getIntent().getLongExtra(EXTRA_CONTACT_ID, -1);
        if (contactId == -1) {
            finish();
            return;
        }
        loadAndDisplayContact();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayContact();
    }

    private void loadAndDisplayContact() {
        contact = repository.getContact(contactId);
        if (contact == null) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(contact.getName());
        }

        // ---- Views ----
        TextView  tvName       = findViewById(R.id.tvContactName);
        ImageView ivPhoto      = findViewById(R.id.ivContactPhoto);
        TextView  tvInitial    = findViewById(R.id.tvInitialDetail);
        TextView  tvGroup      = findViewById(R.id.tvGroupLabel);
        Chip      chipBlacklist = findViewById(R.id.chipBlacklist);
        RecyclerView rvPhones  = findViewById(R.id.rvPhoneNumbers);

        // Name
        tvName.setText(contact.getName());

        // Photo or initial
        if (contact.getPhotoUri() != null && !contact.getPhotoUri().isEmpty()) {
            ivPhoto.setVisibility(View.VISIBLE);
            tvInitial.setVisibility(View.GONE);
            Glide.with(this)
                .load(Uri.parse(contact.getPhotoUri()))
                .circleCrop()
                .placeholder(R.drawable.bg_avatar)
                .into(ivPhoto);
        } else {
            ivPhoto.setVisibility(View.GONE);
            tvInitial.setVisibility(View.VISIBLE);
            tvInitial.setText(contact.getInitial());
        }

        // Group
        if (contact.getGroupId() != -1) {
            Group group = repository.getGroup(contact.getGroupId());
            tvGroup.setText(group != null ? group.getName() : "");
            tvGroup.setVisibility(View.VISIBLE);
        } else {
            tvGroup.setVisibility(View.GONE);
        }

        // Blacklist chip
        chipBlacklist.setText(contact.isBlacklisted() ? "Blacklisted" : "Add to Blacklist");
        chipBlacklist.setChecked(contact.isBlacklisted());
        chipBlacklist.setOnClickListener(v -> promptToggleBlacklist());

        // Phone numbers list
        rvPhones.setLayoutManager(new LinearLayoutManager(this));
        PhoneNumberAdapter adapter = new PhoneNumberAdapter(
            contact.getPhoneNumbers(),
            new PhoneNumberAdapter.OnActionListener() {
                @Override public void onCallClick(String number) { initiateCall(number); }
                @Override public void onSmsClick(String number)  { openSms(number);  }
            }
        );
        rvPhones.setAdapter(adapter);
    }

    // ---- Blacklist ----

    private void promptToggleBlacklist() {
        boolean willBlacklist = !contact.isBlacklisted();
        String title   = willBlacklist ? "Blacklist Contact"        : "Remove from Blacklist";
        String message = willBlacklist
            ? "Incoming calls and SMS from " + contact.getName() + " will be blocked."
            : contact.getName() + " will be removed from the blacklist.";

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", (d, w) -> {
                repository.setBlacklisted(contactId, willBlacklist);
                loadAndDisplayContact();
                Toast.makeText(this,
                    willBlacklist ? "Contact blacklisted" : "Removed from blacklist",
                    Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", (d, w) -> loadAndDisplayContact()) // Revert chip state
            .show();
    }

    // ---- Call / SMS (FR 3) ----

    private void initiateCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Phone call permission is required", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to place call", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSms(String number) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        startActivity(intent);
    }

    // ---- Options menu ----

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra(EXTRA_CONTACT_ID, contactId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Delete \"" + contact.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    repository.deleteContact(contactId);
                    Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
