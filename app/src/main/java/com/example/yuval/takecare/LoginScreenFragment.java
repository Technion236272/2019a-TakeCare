package com.example.yuval.takecare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class LoginScreenFragment extends Fragment {
    private static final int REQ_GOOGLE_SIGN_IN = 1;
    private final static String TAG = "LoginScreenFragment";

    ProgressDialog dialog;
    private GoogleSignInClient googleSignInClient;

    FirebaseAuth auth;
    FirebaseFirestore db;
    private CallbackManager callbackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.fragment_login_screen, container, false);
        Button sign_in_button = (Button) view.findViewById(R.id.sign_in_button_login_screen);
        sign_in_button.setPaintFlags(sign_in_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Note: this is the OAUth 2.0 client ID for our app:
        // 738513157372-ktkd6jopc3rqlsherd7c5759tv79eina.apps.googleusercontent.com
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1093192580078-b5dgbk1rhnn5sb395se8s3b8dv8o28g8.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        SignInButton googleSignIn = view.findViewById(R.id.google_login_button);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                getActivity().startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isFacebookLoggedIn) {
            //TODO: handle when user is already signed in with facebook account

        }

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.facebook_login_button);
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

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            Log.w(TAG, "Intent is null");
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (!task.isSuccessful()) {
                Log.w(TAG, "Failed to get account from intent");
            }
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                if (idToken == null)
                    Log.w(TAG, "ID Token is null");
                // Successful google sign in
                handleGoogleAccount(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            } catch (Exception ex) {
                Log.w(TAG, "Some other exception caught: " + ex.toString());
            }
        }
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "handleGoogleAccount:" + account.getId());
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading data...");
        dialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading data...");
        dialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(getActivity(), "Authentication failed.",
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
                        Toast.makeText(getActivity(), "Authentication success!",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), GatewayActivity.class);
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
        String originalUrlSeg = "s96-c/photo.jpg";
        String newUrlSeg = "s400-c/photo.jpg";
        imagePath = imagePath.replace(originalUrlSeg, newUrlSeg);
        return imagePath.concat("?type=large");
    }

    public void onSignUpClick(View view) {
    }

    public void onSignInClick(View view) {
    }

}