package com.appoapp.sukhmanisahib.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appoapp.sukhmanisahib.databinding.ItemNitenamBinding;
import com.appoapp.sukhmanisahib.model.NitnemModel;

import java.util.ArrayList;

public class NitnemAdapter
        extends RecyclerView.Adapter<NitnemAdapter.ViewHolder> {

    private ArrayList<NitnemModel> baniList;
    private OnItemClickListener listener;
    private String selectedLanguage;

    // 🔹 Click interface
    public interface OnItemClickListener {
        void onItemClick(NitnemModel model, int position);
    }

    // 🔹 Constructor
    public NitnemAdapter(ArrayList<NitnemModel> baniList,
                         OnItemClickListener listener, String selectedLanguage) {
        this.baniList = baniList;
        this.listener = listener;
        this.selectedLanguage = selectedLanguage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNitenamBinding binding = ItemNitenamBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NitnemModel model = baniList.get(position);

        holder.binding.titleTV.setText(model.getTitle(selectedLanguage));
        holder.binding.descriptionTV.setText(model.getDescription(selectedLanguage));

        // 🔥 Click handling
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return baniList != null ? baniList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNitenamBinding binding;

        ViewHolder(ItemNitenamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
