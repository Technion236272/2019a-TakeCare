package com.syv.takecare.takecare.POJOs;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class ItemsViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout itemRoot;

    public CardView card;
    public TextView itemTitle;
    public ImageView itemPhoto;
    public ImageView profilePhoto;
    public TextView itemPublisher;
    public ImageView itemCategory;
    public ImageView itemPickupMethod;
    public ImageView itemFavorite;
    public View itemView;

    public ItemsViewHolder(View itemView) {
        super(itemView);
        itemRoot = itemView.findViewById(R.id.card_root);
        card = itemView.findViewById(R.id.taker_feed_card);
        itemTitle = itemView.findViewById(R.id.item_title);
        itemPhoto = itemView.findViewById(R.id.item_photo);
        profilePhoto = itemView.findViewById(R.id.item_publisher_profile);
        itemPublisher = itemView.findViewById(R.id.item_published_name);
        itemCategory = itemView.findViewById(R.id.item_category);
        itemPickupMethod = itemView.findViewById(R.id.item_pickup_method);
        itemFavorite = itemView.findViewById(R.id.item_favorite);
        this.itemView = itemView;
    }
}
