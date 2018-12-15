package com.example.yuval.takecare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.Explode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private static final String TAG = "LOGIN ACTIVITY:";

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.faded_image).setVisibility(View.GONE);
        Log.d(TAG, "onCreate: loaded UI");

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null) {
            Intent intent = new Intent(this, GatewayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        if(AccessToken.getCurrentAccessToken() != null) {
            //TODO: handle when user is already signed in with facebook account

        }

        Log.d(TAG, "onCreate: reached callbackManager");

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email")); //Add name
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                dialog = new ProgressDialog(LoginActivity.this);
                dialog.setMessage("Fetching data...");
                dialog.show();

                String accessToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        dialog.dismiss();
                        getFacebookData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        Log.d(TAG, "onCreate: end");
    }

    private void getFacebookData(JSONObject object) {
        try {
            URL profile_picture = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");
            Log.d(TAG, "getFacebookData: Found profile picture: "+profile_picture);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void transition(View view) {
        ImageView faded_background_image = findViewById(R.id.faded_image);
        final ImageView background_image = findViewById(R.id.imageView3);
        faded_background_image.setAlpha(0f);
        faded_background_image.setVisibility(View.VISIBLE);
        faded_background_image.animate().alpha(0.666f).setDuration(100).setListener(null);
        background_image.animate().alpha(0f).setDuration(100).setListener(null);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_login2);
        TransitionManager.beginDelayedTransition((ConstraintLayout) findViewById(R.id.login_screen_layout));
        constraintSet.applyTo((ConstraintLayout) findViewById(R.id.login_screen_layout));
    }

    public void onSignUpClick(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void onSignInClick(View view) {
    }
}
