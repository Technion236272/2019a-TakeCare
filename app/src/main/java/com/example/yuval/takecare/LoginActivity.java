package com.example.yuval.takecare;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "TakeCare";
    private static final int REQ_GOOGLE_SIGN_IN = 1;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CallbackManager callbackManager;

    ProgressDialog dialog;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        findViewById(R.id.faded_image).setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, GatewayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isFacebookLoggedIn) {
            //TODO: handle when user is already signed in with facebook account

        }

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("public_profile", "email"); //Add name
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d(TAG, "facebook:onError", e);
            }
        });

        // Note: this is the OAUth 2.0 client ID for our app:
        // 738513157372-ktkd6jopc3rqlsherd7c5759tv79eina.apps.googleusercontent.com
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton googleSignIn = findViewById(R.id.google_login_button);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Loading data...");
        dialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            performSignIn(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Successful google sign in
                handleGoogleAccount(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "handleGoogleAccount:" + account.getId());
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Loading data...");
        dialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithCredential: success!");
                            FirebaseUser user = auth.getCurrentUser();
                            performSignIn(user);
                        } else {
                            // Sign in failed
                            Log.w(TAG, "signInWithCredential: failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void performSignIn(FirebaseUser user) {
        assert user != null;
        String uid = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();
        String profilePicture;
        if (user.getPhotoUrl() == null) {
            profilePicture = null;
        } else {
            profilePicture = user.getPhotoUrl().toString();
        }
        Map<String, Object> userInfo = new HashMap<>();
        if (name != null)
            userInfo.put("name", name);
        else
            userInfo.put("name", "user");
        if (email != null)
            userInfo.put("email", email);
        if (profilePicture != null) {
            userInfo.put("profilePicture", fixImageQuality(profilePicture, user.getProviderId()));
        }
        userInfo.put("rating", 0);
        userInfo.put("ratingCount", 0);

        db.collection("users").document(uid)
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Authentication success!",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, GatewayActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document " + e);
                    }
                });
    }

    private String fixImageQuality(String imagePath, String providerId) {
        Log.d(TAG, "fixImageQuality: provider id is: " + providerId);
        if (providerId.contains("google")) {
            // Google account
            String originalUrlSeg = "s96-c/photo.jpg";
            String newUrlSeg = "s400-c/photo.jpg";
            return imagePath.replace(originalUrlSeg, newUrlSeg);
        }
        // Facebook account
        return imagePath.concat("?type=large");
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
}
