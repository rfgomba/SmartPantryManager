package com.smartpantry.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that turns a list of PantryItem objects
 * into rows on screen, using the item_pantry.xml layout for each row.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets the screen that owns the list react to taps on a row. */
    public interface OnPantryItemListener {
        void onItemClick(PantryItem item);   // tap the row (edit)
        void onDeleteClick(PantryItem item); // tap the delete icon
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemListener listener;

    public PantryAdapter(OnPantryItemListener listener) {
        this.listener = listener;
    }

    /** Replaces the data shown in the list and redraws it. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** Called when the RecyclerView needs a new row view (inflates the XML once). */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /** Called to fill a row with the data at a given position (rows are recycled). */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(item.getDisplayQuantity());

        if (item.hasExpiryDate()) {
            holder.textExpiry.setVisibility(View.VISIBLE);
            holder.textExpiry.setText("Expires: " + item.getExpiryDate());
        } else {
            holder.textExpiry.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds references to the views in one row, so findViewById runs only once per row. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textQuantity;
        final TextView textExpiry;
        final ImageButton buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteItem);
        }
    }
}
