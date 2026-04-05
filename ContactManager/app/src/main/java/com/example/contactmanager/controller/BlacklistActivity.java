package com.example.contactmanager.controller;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactmanager.R;
import com.example.contactmanager.adapter.ContactAdapter;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;

import java.util.List;

/**
 * BlacklistActivity (Controller): Displays all blacklisted contacts.
 *
 * Long-pressing a contact offers to remove it from the blacklist.
 * Tapping a contact navigates to its detail view.
 *
 * Satisfies FR 2: Blacklist management (block SMS and phone calls).
 */
public class BlacklistActivity extends AppCompatActivity
        implements ContactAdapter.OnContactClickListener {

    private ContactAdapter    adapter;
    private ContactRepository repository;
    private TextView          tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Blacklist");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ContactRepository.getInstance(this);
        tvEmpty    = findViewById(R.id.tvBlacklistEmpty);

        RecyclerView rv = findViewById(R.id.rvBlacklist);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this);
        rv.setAdapter(adapter);

        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<Contact> blacklisted = repository.getBlacklistedContacts();
        adapter.updateContacts(blacklisted, false, null);
        tvEmpty.setVisibility(blacklisted.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onContactClick(Contact contact) {
        android.content.Intent intent =
            new android.content.Intent(this, ContactDetailActivity.class);
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.getId());
        startActivity(intent);
    }

    @Override
    public void onContactLongClick(Contact contact) {
        new AlertDialog.Builder(this)
            .setTitle("Remove from Blacklist")
            .setMessage("Allow calls and SMS from " + contact.getName() + " again?")
            .setPositiveButton("Remove", (d, w) -> {
                repository.setBlacklisted(contact.getId(), false);
                refreshList();
                Toast.makeText(this, contact.getName() + " removed from blacklist",
                    Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
