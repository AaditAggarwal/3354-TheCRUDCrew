package com.example.contactmanager.controller;

import android.content.Intent;
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
import com.example.contactmanager.adapter.GroupAdapter;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Group;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class GroupsActivity extends AppCompatActivity
        implements GroupAdapter.OnGroupClickListener {

    private GroupAdapter       groupAdapter;
    private ContactRepository  repository;
    private TextView           tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact Groups");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository   = ContactRepository.getInstance(this);
        tvEmpty      = findViewById(R.id.tvGroupsEmpty);
        RecyclerView rv = findViewById(R.id.rvGroups);
        rv.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter(this);
        rv.setAdapter(groupAdapter);

        FloatingActionButton fabAddGroup = findViewById(R.id.fabAddGroup);
        fabAddGroup.setOnClickListener(v -> showAddGroupDialog());

        refreshGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGroups();
    }

    private void refreshGroups() {
        List<Group> groups = repository.getAllGroups();
        groupAdapter.updateGroups(groups);
        tvEmpty.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputEditText etInput = dialogView.findViewById(R.id.etDialogInput);
        etInput.setHint("Group name");

        new AlertDialog.Builder(this)
            .setTitle("New Group")
            .setView(dialogView)
            .setPositiveButton("Create", (d, w) -> {
                String name = etInput.getText() != null
                    ? etInput.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Group group = new Group();
                group.setName(name);
                long id = repository.addGroup(group);
                if (id == -1) {
                    Toast.makeText(this, "A group with that name already exists",
                        Toast.LENGTH_SHORT).show();
                } else {
                    refreshGroups();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ---- GroupAdapter callbacks ----

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getId());
        startActivity(intent);
    }

    @Override
    public void onGroupLongClick(Group group) {
        new AlertDialog.Builder(this)
            .setTitle("Group Options")
            .setItems(new CharSequence[]{"Rename", "Delete"}, (d, which) -> {
                if (which == 0) showRenameGroupDialog(group);
                else            confirmDeleteGroup(group);
            })
            .show();
    }

    private void showRenameGroupDialog(Group group) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputEditText etInput = dialogView.findViewById(R.id.etDialogInput);
        etInput.setHint("New group name");
        etInput.setText(group.getName());

        new AlertDialog.Builder(this)
            .setTitle("Rename Group")
            .setView(dialogView)
            .setPositiveButton("Rename", (d, w) -> {
                String newName = etInput.getText() != null
                    ? etInput.getText().toString().trim() : "";
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                group.setName(newName);
                repository.updateGroup(group);
                refreshGroups();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void confirmDeleteGroup(Group group) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Delete \"" + group.getName()
                + "\"? Contacts in this group will not be deleted.")
            .setPositiveButton("Delete", (d, w) -> {
                repository.deleteGroup(group.getId());
                refreshGroups();
                Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
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
