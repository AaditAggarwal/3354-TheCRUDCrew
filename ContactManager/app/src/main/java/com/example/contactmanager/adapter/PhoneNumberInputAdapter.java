package com.example.contactmanager.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactmanager.R;
import com.example.contactmanager.model.PhoneNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * PhoneNumberInputAdapter: Editable phone number rows used in AddEditContactActivity.
 * Supports adding and removing rows, selecting phone type, and live editing the number.
 *
 * Satisfies FR 1: A contact may have one or more phone numbers.
 */
public class PhoneNumberInputAdapter
        extends RecyclerView.Adapter<PhoneNumberInputAdapter.ViewHolder> {

    public static final String[] PHONE_TYPES = {"mobile", "home", "work", "other"};

    private List<PhoneNumber> phoneNumbers;

    public PhoneNumberInputAdapter(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = new ArrayList<>(phoneNumbers);
    }

    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = new ArrayList<>(phoneNumbers);
        notifyDataSetChanged();
    }

    /** Appends a blank mobile-type phone number row. */
    public void addPhoneNumber() {
        phoneNumbers.add(new PhoneNumber(0, 0, "", "mobile"));
        notifyItemInserted(phoneNumbers.size() - 1);
        // Refresh all items so remove-button visibility updates correctly
        notifyDataSetChanged();
    }

    /** Returns the current list (may contain empty strings – caller should filter). */
    public List<PhoneNumber> getPhoneNumbers() {
        return new ArrayList<>(phoneNumbers);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_phone_number_input, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhoneNumber phone = phoneNumbers.get(position);

        // Detach old watcher before setting text to avoid spurious callbacks
        if (holder.textWatcher != null) {
            holder.etNumber.removeTextChangedListener(holder.textWatcher);
        }
        holder.etNumber.setText(phone.getNumber());

        // Set the type spinner
        for (int i = 0; i < PHONE_TYPES.length; i++) {
            if (PHONE_TYPES[i].equals(phone.getType())) {
                holder.spinnerType.setSelection(i);
                break;
            }
        }

        // Attach a fresh TextWatcher
        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && pos < phoneNumbers.size()) {
                    phoneNumbers.get(pos).setNumber(s.toString());
                }
            }
        };
        holder.etNumber.addTextChangedListener(holder.textWatcher);

        holder.spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos2, long id) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_ID && adapterPos < phoneNumbers.size()) {
                    phoneNumbers.get(adapterPos).setType(PHONE_TYPES[pos2]);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Only show remove button when there is more than one row
        holder.btnRemove.setVisibility(phoneNumbers.size() > 1 ? View.VISIBLE : View.GONE);
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && phoneNumbers.size() > 1) {
                phoneNumbers.remove(pos);
                notifyItemRemoved(pos);
                notifyDataSetChanged(); // Refresh remove-button visibility
            }
        });
    }

    @Override
    public int getItemCount() { return phoneNumbers.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        EditText    etNumber;
        Spinner     spinnerType;
        ImageButton btnRemove;
        TextWatcher textWatcher;

        ViewHolder(View itemView) {
            super(itemView);
            etNumber    = itemView.findViewById(R.id.etPhoneNumber);
            spinnerType = itemView.findViewById(R.id.spinnerPhoneType);
            btnRemove   = itemView.findViewById(R.id.btnRemovePhone);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                itemView.getContext(),
                android.R.layout.simple_spinner_item,
                PHONE_TYPES
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerType.setAdapter(adapter);
        }
    }
}
