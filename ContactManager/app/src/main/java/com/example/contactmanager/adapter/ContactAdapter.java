package com.example.contactmanager.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.contactmanager.R;
import com.example.contactmanager.database.ContactRepository;
import com.example.contactmanager.model.Contact;
import com.example.contactmanager.model.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactAdapter: Displays a list of contacts in a RecyclerView.
 *
 * Supports two display modes:
 *   1. Plain list  – contacts sorted alphabetically.
 *   2. Grouped     – contacts under section headers for each group.
 *
 * Each list item can be either a Contact or a String (section header).
 */
public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER  = 0;
    private static final int TYPE_CONTACT = 1;

    private final List<Object> items = new ArrayList<>();

    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
        void onContactLongClick(Contact contact);
    }

    public ContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void updateContacts(List<Contact> contacts, boolean groupedView,
                               ContactRepository repository) {
        items.clear();
        if (groupedView && repository != null) {
            long currentGroupId = Long.MIN_VALUE;
            for (Contact contact : contacts) {
                if (contact.getGroupId() != currentGroupId) {
                    currentGroupId = contact.getGroupId();
                    String header;
                    if (currentGroupId == -1) {
                        header = "No Group";
                    } else {
                        Group group = repository.getGroup(currentGroupId);
                        header = (group != null) ? group.getName() : "Unknown Group";
                    }
                    items.add(header);
                }
                items.add(contact);
            }
        } else {
            items.addAll(contacts);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_CONTACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else {
            ((ContactViewHolder) holder).bind((Contact) items.get(position));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    // -------------------------------------------------------------------------
    //  ViewHolders
    // -------------------------------------------------------------------------

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView   tvName, tvPhone, tvBlacklisted, tvInitial;
        ImageView  ivPhoto;

        ContactViewHolder(View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvContactName);
            tvPhone       = itemView.findViewById(R.id.tvContactPhone);
            tvBlacklisted = itemView.findViewById(R.id.tvBlacklisted);
            tvInitial     = itemView.findViewById(R.id.tvInitial);
            ivPhoto       = itemView.findViewById(R.id.ivContactPhoto);
        }

        void bind(final Contact contact) {
            tvName.setText(contact.getName());
            String primary = contact.getPrimaryPhoneNumber();
            tvPhone.setText(primary != null ? primary : "No number");
            tvBlacklisted.setVisibility(contact.isBlacklisted() ? View.VISIBLE : View.GONE);

            // Show photo or initial avatar
            if (contact.getPhotoUri() != null && !contact.getPhotoUri().isEmpty()) {
                tvInitial.setVisibility(View.GONE);
                ivPhoto.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                    .load(Uri.parse(contact.getPhotoUri()))
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar)
                    .into(ivPhoto);
            } else {
                ivPhoto.setVisibility(View.GONE);
                tvInitial.setVisibility(View.VISIBLE);
                tvInitial.setText(contact.getInitial());
            }

            itemView.setOnClickListener(v -> listener.onContactClick(contact));
            itemView.setOnLongClickListener(v -> {
                listener.onContactLongClick(contact);
                return true;
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }

        void bind(String header) {
            tvHeader.setText(header);
        }
    }
}
