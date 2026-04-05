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
import com.example.contactmanager.model.Group;

import java.util.List;

/**
 * GroupDetailActivity (Controller): Shows contacts belonging to a specific group.
 *
 * Allows:
 *  - Viewing members of the group
 *  - Long-pressing a contact to remove it from the group
 *  - Adding contacts to the group via a multi-select dialog
 *
 * Satisfies FR 4: Manage contact groups (add/remove contacts).
 */
public class GroupDetailActivity extends AppCompatActivity
        implements ContactAdapter.OnContactClickListener {

    public static final String EXTRA_GROUP_ID = "group_id";

    private ContactRepository repository;
    private Group             group;
    private ContactAdapter    memberAdapter;
    private TextView          tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ContactRepository.getInstance(this);
        long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        if (groupId == -1) { finish(); return; }

        group = repository.getGroup(groupId);
        if (group == null) { finish(); return; }
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(group.getName());

        tvEmpty = findViewById(R.id.tvGroupDetailEmpty);

        RecyclerView rvMembers = findViewById(R.id.rvGroupMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new ContactAdapter(this);
        rvMembers.setAdapter(memberAdapter);

        findViewById(R.id.btnAddToGroup).setOnClickListener(v -> showAddContactsDialog());

        refreshMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMembers();
    }

    private void refreshMembers() {
        List<Contact> members = repository.getContactsInGroup(group.getId());
        memberAdapter.updateContacts(members, false, null);
        tvEmpty.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Shows a multi-select dialog of all contacts NOT in this group. */
    private void showAddContactsDialog() {
        List<Contact> allContacts  = repository.getAllContacts();
        List<Contact> members      = repository.getContactsInGroup(group.getId());

        // Filter to only non-member contacts
        allContacts.removeIf(c -> {
            for (Contact m : members) if (m.getId() == c.getId()) return true;
            return false;
        });

        if (allContacts.isEmpty()) {
            Toast.makeText(this, "All contacts are already in this group", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] names = new CharSequence[allContacts.size()];
        boolean[]      checked = new boolean[allContacts.size()];
        for (int i = 0; i < allContacts.size(); i++) names[i] = allContacts.get(i).getName();

        new AlertDialog.Builder(this)
            .setTitle("Add Contacts to " + group.getName())
            .setMultiChoiceItems(names, checked, (d, which, isChecked) -> checked[which] = isChecked)
            .setPositiveButton("Add", (d, w) -> {
                int added = 0;
                for (int i = 0; i < allContacts.size(); i++) {
                    if (checked[i]) {
                        Contact c = allContacts.get(i);
                        c.setGroupId(group.getId());
                        repository.updateContact(c);
                        added++;
                    }
                }
                if (added > 0) {
                    refreshMembers();
                    Toast.makeText(this,
                        "Added " + added + " contact" + (added != 1 ? "s" : ""),
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ---- ContactAdapter callbacks ----

    @Override
    public void onContactClick(Contact contact) {
        // Navigate to detail view
        android.content.Intent intent =
            new android.content.Intent(this, ContactDetailActivity.class);
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.getId());
        startActivity(intent);
    }

    @Override
    public void onContactLongClick(Contact contact) {
        new AlertDialog.Builder(this)
            .setTitle("Remove from Group")
            .setMessage("Remove \"" + contact.getName() + "\" from " + group.getName() + "?")
            .setPositiveButton("Remove", (d, w) -> {
                contact.setGroupId(-1);
                repository.updateContact(contact);
                refreshMembers();
                Toast.makeText(this, "Removed from group", Toast.LENGTH_SHORT).show();
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
