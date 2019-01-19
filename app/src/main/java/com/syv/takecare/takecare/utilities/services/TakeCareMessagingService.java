package com.syv.takecare.takecare.utilities.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syv.takecare.takecare.POJOs.ActivityCode;
import com.syv.takecare.takecare.activities.LoginActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakeCareActivity;
import com.syv.takecare.takecare.activities.TakerMenuActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.squareup.okhttp.internal.http.HttpDate.parse;


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

    private void buildAdminBroadcastNotification(Map<String, String> messageData) {
        String notificationType = messageData.get(getString(R.string.payload_data_notification_type));
        String title = messageData.get(getString(R.string.payload_data_message_title));
        String message = messageData.get(getString(R.string.payload_data_message_body));
        String itemId = messageData.get(getString(R.string.payload_data_item_id));
        if (notificationType == null) {
            Log.d(TAG, "buildAdminBroadcastNotification: notification has no type");
            return;
        }
        switch (notificationType) {
            case "CHAT":
                String senderId = messageData.get(getString(R.string.payload_data_sender_id));
                String chatId = messageData.get(getString(R.string.payload_data_chat_id));
                String senderPhotoURL = messageData.get(getString(R.string.payload_data_sender_photo));
                sendChatNotification(title, message, senderId, senderPhotoURL, chatId, itemId);
                break;
            case "ACCEPTED_ITEM":
                sendAcceptedItemNotification(title, message, itemId);
                break;
        }
    }

    private void sendChatNotification(String title, String message, String senderId,
                                      String senderPhotoURL, String chatId, String itemId) {
        Log.d(TAG, "sendChatNotification: checking if user is currently in an active chat with the sender");
        if (TakerMenuActivity.chatPartnerId != null && TakerMenuActivity.chatPartnerId.equals(senderId)) {
            Log.d(TAG, "sendChatNotification: user is in chat with the sender - do not fire notification");
            return;
        }

        Log.d(TAG, "sendChatNotification: user is not in an active chat with the sender. Building the notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                getString(R.string.takecare_notification_channel_name));

        Intent notificationIntent = new Intent(getApplicationContext(), TakerMenuActivity.class);
        notificationIntent.putExtra("CHAT_ID", chatId);
        notificationIntent.putExtra("OTHER_ID", senderId);
        notificationIntent.putExtra("ITEM_ID", itemId);
        notificationIntent.putExtra("IS_NOT_REFERENCED_FROM_LOBBY", true);
        notificationIntent.putExtra(TakeCareActivity.EXTRA_CHANGE_ACTIVITY,
                ActivityCode.ActivityChatRoom);

        // This intent will start when the user clicks the notification.
        // A pending intent is required because it's "lazy" - a regular intent is instantaneous
        // and requires a context. We wrap it in a pending intent
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (senderPhotoURL != null) {
            FutureTarget<Bitmap> futureTarget = Glide.with(this)
                    .asBitmap()
                    .load(senderPhotoURL)
                    .submit();

            try {
                Bitmap bitmap = futureTarget.get();
                builder.setLargeIcon(bitmap);
            } catch (InterruptedException e) {
                // do nothing
            } catch (ExecutionException e) {
                // do nothing
            }

            Glide.with(this).clear(futureTarget);
        }

//        long notificationMilliseconds = parse(time).getTime();

        Resources resources = getResources(),
                systemResources = Resources.getSystem();

        // Add the notification's properties
        builder.setSmallIcon(R.drawable.ic_app_notifications)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(message)
//                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(notificationPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{500, 500})
                .setLights(ContextCompat.getColor(this, systemResources
                                .getIdentifier("config_defaultNotificationColor", "color", "android")),
                        resources.getInteger(systemResources
                                .getIdentifier("config_defaultNotificationLedOn", "integer", "android")),
                        resources.getInteger(systemResources
                                .getIdentifier("config_defaultNotificationLedOff", "integer", "android")))
//                .setLights(getResources().getColor(R.color.colorPrimary), 3000, 3000)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Log.d(TAG, "sendChatNotification: firing notification");

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
        // It's impossible to create a unique integer key from a string value.
        // I used the hash code value to generate a decent key in terms of uniqueness
        if (notificationManager != null) {
            notificationManager.notify(senderId.hashCode(), builder.build());
        }
    }


    private void sendAcceptedItemNotification(String title, String message, String itemId) {
        Log.d(TAG, "sendAcceptedItemNotification: building an admin broadcast notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                getString(R.string.takecare_notification_channel_name));

        Intent notificationIntent = new Intent(getApplicationContext(), TakerMenuActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra(TakeCareActivity.EXTRA_CHANGE_ACTIVITY,
                ActivityCode.ActivityRequestedItems);

        // This intent will start when the user clicks the notification.
        // A pending intent is required because it's "lazy" - a regular intent is instantaneous
        // and requires a context. We wrap it in a pending intent
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (itemId != null && !itemId.equals("NA")) {
            FutureTarget<Bitmap> futureTarget = Glide.with(this)
                    .asBitmap()
                    .load(itemId)
                    .submit();

            try {
                Bitmap bitmap = futureTarget.get();
                builder.setLargeIcon(bitmap);
            } catch (InterruptedException e) {
                // do nothing
            } catch (ExecutionException e) {
                // do nothing
            }

            Glide.with(this).clear(futureTarget);
        }

        // Add the notification's properties
        builder.setSmallIcon(R.drawable.ic_app_notifications)
                .setColor(getResources().getColor(R.color.colorPrimary))
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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(VISIBILITY_PUBLIC)
                .setGroup(GROUP_KEY_ITEMS)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Log.d(TAG, "sendAcceptedItemNotification: firing notification");

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

        if (notificationManager != null) {
            // We want unique notifications for these events
            int time = (int) System.currentTimeMillis();
            notificationManager.notify(time, builder.build());
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