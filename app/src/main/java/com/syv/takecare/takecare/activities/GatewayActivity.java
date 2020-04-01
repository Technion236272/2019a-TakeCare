package com.syv.takecare.takecare.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.syv.takecare.takecare.R;

import androidx.annotation.NonNull;

public class GatewayActivity extends TakeCareActivity {
    private final static String TAG = "TakeCare/Gateway";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gateway);

        // The extra boolean put in the intent denotes whether the user is new or already existing,
        // and shows the appropriate toast
        Intent intent = getIntent();
        boolean welcomeToast = intent.getBooleanExtra(Intent.EXTRA_TEXT, false);
        makeWelcomeToast(welcomeToast);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Token is: "+ token);
                        db.collection("users").document(user.getUid())
                                .update("tokens", FieldValue.arrayUnion(token));
                    }
                });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.takecare_notification_channel_name);
            String description = getString(R.string.takecare_notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void makeWelcomeToast(boolean fromLoginScreen) {
        if (!fromLoginScreen) {
            return;
        }
        final String toastText = "Welcome, ";
        final FirebaseUser user = auth.getCurrentUser();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Found user");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Toast.makeText(getApplicationContext(), toastText
                                + document.getString("name") + "!", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    public void openTakerMenu(View view) {
        //Create intent to navigate to the taker menu
        Intent intent = new Intent(this, TakerMenuActivity.class);
        startActivity(intent);
    }

    public void openGiverForm(View view) {
        //Create intent to navigate to the giver menu
        Intent intent = new Intent(this, GiverFormActivity.class);
        startActivity(intent);
    }
}
