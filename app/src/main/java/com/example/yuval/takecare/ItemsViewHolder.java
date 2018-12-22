package com.example.yuval.takecare;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemsViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ViewHolder";

    CardView card;
    TextView itemTitle;
    ImageView itemPhoto;
    ImageView profilePhoto;
    TextView itemPublisher;
    ImageView itemCategory;
    ImageView itemPickupMethod;

    private LinearLayout scaleLayout;

    ItemsViewHolder(View itemView) {
        super(itemView);
        card = (CardView) itemView.findViewById(R.id.taker_feed_card);
        itemTitle = (TextView) itemView.findViewById(R.id.item_title);
        itemPhoto = (ImageView) itemView.findViewById(R.id.item_photo);
        profilePhoto = (ImageView) itemView.findViewById(R.id.item_publisher_profile);
        itemPublisher = (TextView) itemView.findViewById(R.id.item_published_name);
        itemCategory = (ImageView) itemView.findViewById(R.id.item_category);
        itemPickupMethod = (ImageView) itemView.findViewById(R.id.item_pickup_method);

        scaleLayout = itemView.findViewById(R.id.card_scale);
        Log.d(TAG, "ItemsViewHolder: scaleLayout: " + scaleLayout);

        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(TAG, "onFocusChange: got focus");
                    card.setCardElevation((float)8.0);
                    if(scaleLayout != null) {
                        Log.d(TAG, "onFocusChange: scaling bigger");
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0,
                                1.0f
                        );
                        scaleLayout.setLayoutParams(param);
                    }
                } else {
                    Log.d(TAG, "onFocusChange: lost focus");
                    card.setCardElevation((float)1.0);
                    if(scaleLayout != null) {
                        Log.d(TAG, "onFocusChange: scaling smaller");
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0,
                                0.8f
                        );
                        scaleLayout.setLayoutParams(param);
                    }
                }
            }
        });
    }
}
