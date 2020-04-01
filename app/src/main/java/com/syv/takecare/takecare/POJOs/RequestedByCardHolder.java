package com.syv.takecare.takecare.POJOs;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class RequestedByCardHolder extends RecyclerView.ViewHolder{
    public CardView card;
    public ImageView requesterProfilePicture;
    public TextView requesterName;
    public TextView requesterLikesCounter;
    public TextView requestDate;
    public TextView requestTime;
    public Button acceptButton;

    public RequestedByCardHolder(View v) {
        super(v);
        card = v.findViewById(R.id.request_card);
        requesterProfilePicture = v.findViewById(R.id.requester_profile_pic);
        requesterName = v.findViewById(R.id.requester_name);
        requesterLikesCounter = v.findViewById(R.id.likes_counter);
        requestDate = v.findViewById(R.id.request_date);
        requestTime = v.findViewById(R.id.request_time);
        acceptButton = v.findViewById(R.id.accept_request_button);
    }
}