package com.syv.takecare.takecare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.syv.takecare.takecare.utilities.TokenServiceFCM;

public class GatewayActivity extends AppCompatActivity {
    private final static String TAG = "TakeCare";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gateway);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // The extra boolean put in the intent denotes whether the user is new or already existing,
        // and shows the appropriate toast
        Intent intent = getIntent();
        boolean welcomeToast = intent.getBooleanExtra(Intent.EXTRA_TEXT, false);
        makeWelcomeToast(welcomeToast);
//        String token = TokenServiceFCM.getToken(getApplicationContext());
//        Log.d(TAG, "onCreate: token is " + token);
//        Toast.makeText(getApplicationContext(), token, Toast.LENGTH_LONG).show();
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
