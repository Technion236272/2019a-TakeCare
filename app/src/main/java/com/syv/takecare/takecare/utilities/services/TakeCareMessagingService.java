package com.syv.takecare.takecare.utilities.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syv.takecare.takecare.activities.LoginActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakeCareActivity;

import java.util.Map;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;


public class TakeCareMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TakeCare/Messaging";
    private static final String EXTRA_ITEM_ID = "Item Id";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    private static final String GROUP_KEY_ITEMS = "TakeCare/Items";
    private static final String GROUP_KEY_CHAT = "TakeCare/Chat";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: started");

        if (remoteMessage.getData() == null) {
            Log.d(TAG, "onMessageReceived: notification data is null");
            return;
        }

        Log.d(TAG, "onMessageReceived: message is: " + remoteMessage.getData());

        String payloadDisplayStatus = remoteMessage.getData().get(getString(R.string.payload_display_status));
        if (payloadDisplayStatus == null) {
            Log.d(TAG, "onMessageReceived: did not find a display status field");
            // Do something about it
            return;
        }

        if (payloadDisplayStatus.equals(getString(R.string.payload_admin_broadcast))) {
            Log.d(TAG, "onMessageReceived: notification is an admin notification");
            buildAdminBroadcastNotification(remoteMessage.getData());
        }
    }

    private void buildAdminBroadcastNotification(Map<String,String> messageData) {
            String title = messageData.get(getString(R.string.payload_data_message_title));
            String message = messageData.get(getString(R.string.payload_data_message_body));
            sendBroadcastNotification(title, message);
    }


    private void sendBroadcastNotification(String title, String message) {
        Log.d(TAG, "sendBroadcastNotification: building an admin broadcast notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                getString(R.string.takecare_notification_channel_name));
        //TODO: change the intent
        Intent notificationIntent = new Intent(getApplicationContext(), LoginActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // This intent will start when the user clicks the notification.
        // A pending intent is required because it's "lazy" - a regular intent is instantaneous
        // and requires a context. We wrap it in a pending intent
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Add the notification's properties
        builder.setSmallIcon(R.drawable.ic_heart_muffin)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(notificationPendingIntent)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setVibrate(new long[]{500, 500})
                .setLights(getResources().getColor(R.color.colorPrimary), 3000, 3000)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(VISIBILITY_PUBLIC)
                .setGroup(GROUP_KEY_ITEMS)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Log.d(TAG, "sendBroadcastNotification: firing notification");

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // On newer APIs - need to set the channel ID for our notification
//            builder.setChannelId(getString(R.string.takecare_notification_channel_name));

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.takecare_notification_channel_name),
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH);


            // Configure the notification channel.
            notificationChannel.setDescription(message);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(sound, attributes);


            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }

        // Notification ID prevents spamming the device's notification tray -
        // notifications of the same ID will override one another
        if (notificationManager != null) {
            notificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());
        }
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