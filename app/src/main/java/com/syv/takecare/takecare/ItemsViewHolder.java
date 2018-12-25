package com.syv.takecare.takecare;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class ItemsViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "TakeCare";

//    private LinearLayout itemRoot;
//    private ViewGroup.LayoutParams params;

    CardView card;
    TextView itemTitle;
    ImageView itemPhoto;
    ImageView profilePhoto;
    TextView itemPublisher;
    ImageView itemCategory;
    ImageView itemPickupMethod;
    ImageView itemReport;
    View itemView;

    private LinearLayout scaleLayout;

    ItemsViewHolder(View itemView) {
        super(itemView);
//        itemRoot = itemView.findViewById(R.id.card_root);
//        params = itemRoot.getLayoutParams();

        card = (CardView) itemView.findViewById(R.id.taker_feed_card);
        itemTitle = (TextView) itemView.findViewById(R.id.item_title);
        itemPhoto = (ImageView) itemView.findViewById(R.id.item_photo);
        profilePhoto = (ImageView) itemView.findViewById(R.id.item_publisher_profile);
        itemPublisher = (TextView) itemView.findViewById(R.id.item_published_name);
        itemCategory = (ImageView) itemView.findViewById(R.id.item_category);
        itemPickupMethod = (ImageView) itemView.findViewById(R.id.item_pickup_method);
        itemReport = (ImageView) itemView.findViewById(R.id.item_report);
        this.itemView = itemView;

        scaleLayout = itemView.findViewById(R.id.card_scale);

        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    card.setCardElevation((float)8.0);
                    if(scaleLayout != null) {
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0,
                                1.0f
                        );
                        scaleLayout.setLayoutParams(param);
                    }
                } else {
                    card.setCardElevation((float)1.0);
                    if(scaleLayout != null) {
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

    /*
    void hideLayout() {
        params.height = 0;
        params.width = 0;
        //itemView.setLayoutParams(params); //This One.
        itemRoot.setLayoutParams(params);
        itemRoot.setVisibility(View.GONE);
    }*/
}
