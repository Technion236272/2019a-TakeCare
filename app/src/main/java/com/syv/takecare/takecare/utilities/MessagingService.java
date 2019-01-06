package com.syv.takecare.takecare.utilities;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "TakeCare";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String notificationData = "",
                notificationTitle = "",
                notificationBody = "";
        try {
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getData().toString();
            notificationBody = remoteMessage.getData().toString();
        } catch (NullPointerException e) {
            Log.d(TAG, "onMessageReceived: NullPointerException " + e.getMessage());
        }

        Log.d(TAG, "onMessageReceived: data: " + notificationData);
        Log.d(TAG, "onMessageReceived: notification body: " + notificationBody);
        Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
