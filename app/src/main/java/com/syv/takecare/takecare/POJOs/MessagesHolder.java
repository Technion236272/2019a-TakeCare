package com.syv.takecare.takecare.POJOs;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.icu.util.ULocale;
import android.os.Build;
import android.util.Log;
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public MessagesHolder(@NonNull View itemView) {
        super(itemView);
        boolean isRTL = itemView.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        layout = itemView.findViewById(R.id.message_holder_layout);
        userText = itemView.findViewById(R.id.message_holder_user);
        userTime = itemView.findViewById(R.id.time_user);
        otherText = itemView.findViewById(R.id.message_holder_other);
        otherTime = itemView.findViewById(R.id.time_other);
        if (isRTL) {
            userText.setBackgroundDrawable(itemView.getResources().getDrawable(R.drawable.user_chat_bubble_rtl));
            otherText.setBackgroundDrawable(itemView.getResources().getDrawable(R.drawable.other_chat_bubble_rtl));
        }
        this.itemView = itemView;
    }
}
