package com.example.yuval.takecare;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TakerRVAdapter extends FeedRecyclerView.Adapter<TakerRVAdapter.ItemsViewHolder> {

    List<FeedCardInformation> cards;

    TakerRVAdapter(List<FeedCardInformation> cards){
        this.cards = cards;
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
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

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card, viewGroup, false);
        ItemsViewHolder holder = new ItemsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder itemsViewHolder, int i) {
        FeedCardInformation currentCard = cards.get(i);
        itemsViewHolder.itemTitle.setText(currentCard.title);
        Glide.with(itemsViewHolder.card).load(currentCard.photoURL).into(itemsViewHolder.itemPhoto);
        itemsViewHolder.profilePhoto.setImageResource(currentCard.userProfileId);
        itemsViewHolder.itemPublisher.setText(currentCard.publisher);
        itemsViewHolder.itemCategory.setImageResource(cards.get(i).itemCategoryId);
        itemsViewHolder.itemPickupMethod.setImageResource(currentCard.itemPickupMethodId);
        //Tag the category & pickup views
        itemsViewHolder.itemCategory.setTag(currentCard.itemCategoryId);
        itemsViewHolder.itemPickupMethod.setTag(currentCard.itemPickupMethodId);
//        itemsViewHolder.itemCategory.setTag(currentCard.itemCategoryId, itemsViewHolder.itemCategory.getTag());
//        itemsViewHolder.itemPickupMethod.setTag(currentCard.itemPickupMethodId, itemsViewHolder.itemPickupMethod.getTag());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }
}