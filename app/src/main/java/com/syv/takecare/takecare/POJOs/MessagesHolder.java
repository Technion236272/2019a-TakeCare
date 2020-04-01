package com.syv.takecare.takecare.POJOs;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.syv.takecare.takecare.R;

public class MessagesHolder extends RecyclerView.ViewHolder {
    public AppCompatTextView userText;
    public AppCompatTextView otherText;
    public AppCompatTextView userTime;
    public AppCompatTextView otherTime;
    public LinearLayout layout;
    public View itemView;
    public MessagesHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.message_holder_layout);
        userText = itemView.findViewById(R.id.message_holder_user);
        userTime = itemView.findViewById(R.id.time_user);
        otherText = itemView.findViewById(R.id.message_holder_other);
        otherTime = itemView.findViewById(R.id.time_other);
        this.itemView = itemView;
    }
}
