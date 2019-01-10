package com.syv.takecare.takecare;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class ItemsViewHolder extends RecyclerView.ViewHolder {

    LinearLayout itemRoot;

    CardView card;
    TextView itemTitle;
    ImageView itemPhoto;
    ImageView profilePhoto;
    TextView itemPublisher;
    ImageView itemCategory;
    ImageView itemPickupMethod;
    ImageView itemReport;
    ImageView itemFavorite;
    View itemView;

    ItemsViewHolder(View itemView) {
        super(itemView);
        itemRoot = itemView.findViewById(R.id.card_root);
//        params = itemRoot.getLayoutParams();
        card = (CardView) itemView.findViewById(R.id.taker_feed_card);
        itemTitle = (TextView) itemView.findViewById(R.id.item_title);
        itemPhoto = (ImageView) itemView.findViewById(R.id.item_photo);
        profilePhoto = (ImageView) itemView.findViewById(R.id.item_publisher_profile);
        itemPublisher = (TextView) itemView.findViewById(R.id.item_published_name);
        itemCategory = (ImageView) itemView.findViewById(R.id.item_category);
        itemPickupMethod = (ImageView) itemView.findViewById(R.id.item_pickup_method);
        itemReport = (ImageView) itemView.findViewById(R.id.item_report);
        itemFavorite = (ImageView) itemView.findViewById(R.id.item_favorite);
        this.itemView = itemView;
    }
}
