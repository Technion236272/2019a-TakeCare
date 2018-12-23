package com.example.yuval.takecare;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class RequestedByCardHolder extends RecyclerView.ViewHolder{
    CardView card;
    ImageView requesterProfilePicture;
    TextView requesterName;
    RatingBar requeterRating;
    TextView requestTimestampTitle;
    TextView requestDate;
    TextView requestTime;
    ImageButton acceptButton;
    ImageButton declineButton;
    RequestedByCardHolder(View v) {
        super(v);
        card = v.findViewById(R.id.request_card);
        requesterProfilePicture = v.findViewById(R.id.requester_profile_pic);
        requesterName = v.findViewById(R.id.requester_name);
        requeterRating = v.findViewById(R.id.requester_rating);
        requestTimestampTitle = v.findViewById(R.id.request_timestamp_title);
        requestDate = v.findViewById(R.id.request_date);
        requestTime = v.findViewById(R.id.request_time);
        acceptButton = v.findViewById(R.id.accept_request_button);
        declineButton = v.findViewById(R.id.decline_request_button);

    }
}
