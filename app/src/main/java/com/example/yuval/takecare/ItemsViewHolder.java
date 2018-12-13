package com.example.yuval.takecare;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ItemsViewHolder extends RecyclerView.ViewHolder {
    CardView card;
    TextView itemTitle;
    ImageView itemPhoto;
    ImageView profilePhoto;
    TextView itemPublisher;
    ImageView itemCategory;
    ImageView itemPickupMethod;

    ItemsViewHolder(View itemView) {
        super(itemView);
        card = (CardView) itemView.findViewById(R.id.taker_feed_card);
        itemTitle = (TextView) itemView.findViewById(R.id.item_title);
        itemPhoto = (ImageView) itemView.findViewById(R.id.item_photo);
        profilePhoto = (ImageView) itemView.findViewById(R.id.item_publisher_profile);
        itemPublisher = (TextView) itemView.findViewById(R.id.item_published_name);
        itemCategory = (ImageView) itemView.findViewById(R.id.item_category);
        itemPickupMethod = (ImageView) itemView.findViewById(R.id.item_pickup_method);

    }

}
