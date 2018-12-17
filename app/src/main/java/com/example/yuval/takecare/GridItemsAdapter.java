package com.example.yuval.takecare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class GridItemsAdapter extends RecyclerView.Adapter<GridItemsAdapter.ItemViewHolder> {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView nameView;
        public ImageView iconView;
        public AppCompatImageButton imageButton;
        public ItemViewHolder(View c) {
            super(c);
            nameView = c.findViewById(R.id.category_name);
            iconView = c.findViewById(R.id.category_icon);
            imageButton = c.findViewById(R.id.category_button);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public GridItemsAdapter() {
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GridItemsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.giver_menu_grid_item, parent, false);
        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        switch(position) {
            case 0:
                holder.iconView.setImageResource(R.drawable.ic_pizza_96_big_purple);
                holder.nameView.setText("Food");
                break;
            case 1:
                holder.iconView.setImageResource(R.drawable.ic_book_purple);
                holder.nameView.setText("Study Material");
                break;
            case 2:
                holder.iconView.setImageResource(R.drawable.ic_lamp_purple);
                holder.nameView.setText("Households");
                break;
            case 3:
                holder.iconView.setImageResource(R.drawable.ic_lost_and_found_purple);
                holder.nameView.setText("Lost & Found");
                break;
            case 4:
                holder.iconView.setImageResource(R.drawable.ic_car_purple);
                holder.nameView.setText("Hitchhike");
                break;
            case 5:
                holder.iconView.setImageResource(R.drawable.ic_treasure_96_purple_purple);
                holder.nameView.setText("Other");
                break;
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return 6;
    }
}