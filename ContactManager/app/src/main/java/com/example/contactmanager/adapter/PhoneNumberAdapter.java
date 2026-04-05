package com.example.contactmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactmanager.R;
import com.example.contactmanager.model.PhoneNumber;

import java.util.List;

public class PhoneNumberAdapter extends RecyclerView.Adapter<PhoneNumberAdapter.ViewHolder> {

    private final List<PhoneNumber> phoneNumbers;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onCallClick(String number);
        void onSmsClick(String number);
    }

    public PhoneNumberAdapter(List<PhoneNumber> phoneNumbers, OnActionListener listener) {
        this.phoneNumbers = phoneNumbers;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_phone_number, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhoneNumber phone = phoneNumbers.get(position);
        holder.tvNumber.setText(phone.getNumber());
        // Capitalize the type label
        String type = phone.getType();
        holder.tvType.setText(type != null && !type.isEmpty()
            ? type.substring(0, 1).toUpperCase() + type.substring(1)
            : "Mobile");
        holder.btnCall.setOnClickListener(v -> listener.onCallClick(phone.getNumber()));
        holder.btnSms.setOnClickListener(v  -> listener.onSmsClick(phone.getNumber()));
    }

    @Override
    public int getItemCount() { return phoneNumbers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvNumber, tvType;
        ImageButton btnCall, btnSms;

        ViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvType   = itemView.findViewById(R.id.tvPhoneType);
            btnCall  = itemView.findViewById(R.id.btnCall);
            btnSms   = itemView.findViewById(R.id.btnSms);
        }
    }
}
