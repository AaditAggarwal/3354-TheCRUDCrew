package com.example.contactmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactmanager.R;
import com.example.contactmanager.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups = new ArrayList<>();
    private final OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
        void onGroupLongClick(Group group);
    }

    public GroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void updateGroups(List<Group> groups) {
        this.groups = new ArrayList<>(groups);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() { return groups.size(); }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvContactCount;

        GroupViewHolder(View itemView) {
            super(itemView);
            tvGroupName    = itemView.findViewById(R.id.tvGroupName);
            tvContactCount = itemView.findViewById(R.id.tvContactCount);
        }

        void bind(final Group group) {
            tvGroupName.setText(group.getName());
            int n = group.getContactCount();
            tvContactCount.setText(n + " contact" + (n != 1 ? "s" : ""));

            itemView.setOnClickListener(v -> listener.onGroupClick(group));
            itemView.setOnLongClickListener(v -> {
                listener.onGroupLongClick(group);
                return true;
            });
        }
    }
}
