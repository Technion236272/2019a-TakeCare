package com.syv.takecare.takecare.POJOs;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class RequestedByCardHolder extends RecyclerView.ViewHolder{
    public CardView card;
    public ImageView requesterProfilePicture;
    public TextView requesterName;
    public RatingBar requesterRating;
    public TextView requestDate;
    public TextView requestTime;
    public Button acceptButton;

    public RequestedByCardHolder(View v) {
        super(v);
        card = v.findViewById(R.id.request_card);
        requesterProfilePicture = v.findViewById(R.id.requester_profile_pic);
        requesterName = v.findViewById(R.id.requester_name);
        requesterRating = v.findViewById(R.id.requester_rating);
        requestDate = v.findViewById(R.id.request_date);
        requestTime = v.findViewById(R.id.request_time);
        acceptButton = v.findViewById(R.id.accept_request_button);
    }
}