package com.syv.takecare.takecare.POJOs;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class ChatCardHolder extends RecyclerView.ViewHolder {

    public ConstraintLayout itemRoot;

    public TextView title;
    public ImageView itemPhoto;
    public TextView user;
    public ImageView userPhoto;
    public TextView timestamp;
    public View itemView;

    public ChatCardHolder(View itemView) {
        super(itemView);
        itemRoot = itemView.findViewById(R.id.chat_card_root);
        title = itemView.findViewById(R.id.chat_card_item_title);
        itemPhoto = itemView.findViewById(R.id.chat_card_item_photo);
        user = itemView.findViewById(R.id.chat_card_user_name);
        userPhoto = itemView.findViewById(R.id.chat_card_user_photo);
        timestamp = itemView.findViewById(R.id.chat_card_timestamp);
        this.itemView = itemView;
    }
}
