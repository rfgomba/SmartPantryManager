package com.smartpantry.manager.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.utils.ExpiryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that turns a list of PantryItem objects
 * into rows on screen, using the item_pantry.xml layout for each row.
 * Expired items are shown in red; items expiring soon in orange (if enabled).
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private static final int COLOR_EXPIRED = Color.parseColor("#C62828");  // red
    private static final int COLOR_SOON = Color.parseColor("#E65100");     // orange

    /** Lets the screen that owns the list react to taps on a row. */
    public interface OnPantryItemListener {
        void onItemClick(PantryItem item);   // tap the row (edit)
        void onDeleteClick(PantryItem item); // tap the delete icon
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemListener listener;
    private boolean highlightExpiring = true;
    private int warnDays = 3;

    public PantryAdapter(OnPantryItemListener listener) {
        this.listener = listener;
    }

    /** Replaces the data shown in the list and redraws it. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** Applies the expiring-soon setting from the Settings screen. */
    public void setExpiryHighlight(boolean enabled, int days) {
        highlightExpiring = enabled;
        warnDays = days;
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
        Context context = holder.itemView.getContext();

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(item.getDisplayQuantity());
        bindExpiry(holder, item, context);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    /** Shows the expiry line with a colour that reflects how close the date is. */
    private void bindExpiry(PantryViewHolder holder, PantryItem item, Context context) {
        if (!item.hasExpiryDate()) {
            holder.textExpiry.setVisibility(View.GONE);
            return;
        }
        holder.textExpiry.setVisibility(View.VISIBLE);
        String date = item.getExpiryDate();
        Long days = ExpiryUtils.daysUntil(date);

        if (days != null && days < 0) {
            holder.textExpiry.setText(context.getString(R.string.expired_on, date));
            holder.textExpiry.setTextColor(COLOR_EXPIRED);
        } else if (highlightExpiring && days != null && days <= warnDays) {
            holder.textExpiry.setText(context.getString(
                    days == 0 ? R.string.expires_today : R.string.expires_soon, date));
            holder.textExpiry.setTextColor(COLOR_SOON);
        } else {
            holder.textExpiry.setText(context.getString(R.string.expires_on, date));
            // Recycled rows may still be coloured, so reset to the original colour
            holder.textExpiry.setTextColor(holder.defaultExpiryColors);
        }
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
        final ColorStateList defaultExpiryColors;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteItem);
            defaultExpiryColors = textExpiry.getTextColors();
        }
    }
}
