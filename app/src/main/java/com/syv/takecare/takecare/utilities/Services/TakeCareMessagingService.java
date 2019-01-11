package com.syv.takecare.takecare.utilities.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syv.takecare.takecare.LoginActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.TakeCareActivity;


public class TakeCareMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TakeCare";
    private static final int BROADCAST_NOTIFICATION_ID = 0;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /*String notificationData = "",
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
        Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);*/

        String payloadDisplayStatus = remoteMessage.getData().get(getString(R.string.payload_display_status));

        if (payloadDisplayStatus != null) {
            // Notification has a display status
            if (isApplicationInForeground()) {
                // Application is in the foreground: some activity is currently running on user's device
                if (payloadDisplayStatus.equals(getString(R.string.payload_admin_broadcast))) {
                    buildAdminBroadcastNotification(remoteMessage);
                }
            } else {
                // Application is either in the background or it is closed
                if (payloadDisplayStatus.equals(getString(R.string.payload_admin_broadcast))) {
                    buildAdminBroadcastNotification(remoteMessage);
                } else if (payloadDisplayStatus.equals(getString(R.string.payload_item_request_message))) {
                    // Build chat message notification
                }
            }
        } else {
            // Notification does not have a display status
            // Do nothing.
        }

    }

    private void buildAdminBroadcastNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get(getString(R.string.payload_data_title));
        String message = remoteMessage.getData().get(getString(R.string.payload_data_message));
        sendBroadcastNotification(title, message);
    }

    private void sendBroadcastNotification(String title, String message) {
        Log.d(TAG, "sendBroadcastNotification: building an admin broadcast notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.common_google_play_services_notification_channel_name));
        //TODO: change the intent
        Intent notificationIntent = new Intent(this, LoginActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // This intent will start when the user clicks the notification
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Add notification display properties
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(message)
                .setColor(getResources().getColor(R.color.colorPrimaryLite))
                .setAutoCancel(true);

        builder.setContentIntent(notificationPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification ID prevents spamming the device's notification tray -
        // notifications of the same ID override one another
        notificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed user token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("tokens", FieldValue.arrayUnion(token));

        }
    }

    private boolean isApplicationInForeground() {
        return TakeCareActivity.isAppRunningInForeground();
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}