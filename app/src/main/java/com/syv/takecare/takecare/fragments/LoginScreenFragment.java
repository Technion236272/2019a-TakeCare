package com.syv.takecare.takecare.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import com.facebook.login.LoginManager;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.syv.takecare.takecare.activities.LoginActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakerMenuActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        Button sign_in_button = view.findViewById(R.id.sign_in_button_login_screen);
        sign_in_button.setPaintFlags(sign_in_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Button signUpButton = view.findViewById(R.id.sign_up_with_email_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).changeFragment(v);
            }
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Note: this is the OAUth 2.0 client ID for our app:
        // 738513157372-ktkd6jopc3rqlsherd7c5759tv79eina.apps.googleusercontent.com
        // "1093192580078-lh7kjnjl4obk75nv49edosvfd16g5ffs.apps.googleusercontent.com" (Current)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        SignInButton googleSignIn = view.findViewById(R.id.google_login_button);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
            }
        });

//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();
//
//        if (isFacebookLoggedIn) {
//            TODO: handle when user is already signed in with facebook account
//
//        }

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = view.findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email")); //Add name
        loginButton.setFragment(this);
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
        String text = "TakeCare";
        AppCompatTextView title_view = view.findViewById(R.id.login_activity_title);
        SpannableString title = new SpannableString(text);
        ForegroundColorSpan fcsPurple = new ForegroundColorSpan(Color.parseColor("#4527a0"));
        ForegroundColorSpan fcsAmber = new ForegroundColorSpan(getResources().getColor(R.color.colorAccent));
        title.setSpan(fcsPurple,0,4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setSpan(fcsAmber, 4, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title_view.setText(title);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            Log.w(TAG, "Intent is null");
            return;
        }
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
            return;
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "handleGoogleAccount:" + account.getId());
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading data...");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
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
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            dialog.dismiss();
                            performSignIn(user);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(getActivity(), "Credentials has been malformed or expired",
                                        Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(getActivity(), "User with same credentials already exists",
                                        Toast.LENGTH_SHORT).show();
                                String email = credential.getSignInMethod();
                                String provider = ""; List<String> providers = null;
                                try {
                                    Task<SignInMethodQueryResult> resultTask = auth.fetchSignInMethodsForEmail(email);
                                    if (!task.isSuccessful())
                                        Log.w(TAG, "Task is not successful");
                                    else {
                                        providers = resultTask.getResult().getSignInMethods();
                                    }
                                } catch (NullPointerException nullptrex) {
                                    Log.w(TAG, "NullPointerException from getSignInMethods");
                                }
                                if (providers == null || providers.isEmpty())
                                    Log.w(TAG, "No existing sign in providers");
                                else
                                    provider = providers.get(0);
                                String alertDialogMessage = "Please sign in with your ";
                                if (provider.isEmpty())
                                    alertDialogMessage += "Other account";
                                else
                                    alertDialogMessage += provider + " account";
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("Please sign in with your " + provider + " account")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        }).show();
                                auth.signOut();
                                LoginManager.getInstance().logOut();
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void performSignIn(FirebaseUser user) {
        assert user != null;
        final String uid = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();
        String profilePicture;
        if (user.getPhotoUrl() == null) {
            profilePicture = null;
        } else {
            profilePicture = user.getPhotoUrl().toString();
        }
        final Map<String, Object> userInfo = new HashMap<>();
        if (name != null)
            userInfo.put("name", name);
        else
            userInfo.put("name", "user");
        if (email != null)
            userInfo.put("email", email);
        if (profilePicture != null) {
            userInfo.put("profilePicture", fixImageQuality(profilePicture, user.getProviderId()));
        }
        userInfo.put("likes", 0);
        userInfo.put("uid", uid);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "Existing user document fetched!" + documentSnapshot);
                        if (documentSnapshot.exists()) {
                            dialog.dismiss();
                            Intent intent = new Intent(getActivity(), TakerMenuActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            intent.putExtra(Intent.EXTRA_TEXT, true);
                            startActivity(intent);
                        } else {
                            db.collection("users").document(uid)
                                    .set(userInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "New user document successfully written!");
                                            dialog.dismiss();
                                            Intent intent = new Intent(getActivity(), TakerMenuActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.putExtra(Intent.EXTRA_TEXT, true);
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "error loading user document");
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

    public void onSignInClick(View view) {
    }
}