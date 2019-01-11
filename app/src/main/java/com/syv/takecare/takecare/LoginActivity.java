package com.syv.takecare.takecare;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare";
    private static final int REQ_GOOGLE_SIGN_IN = 1;
    private CallbackManager callbackManager;

    ProgressDialog dialog;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        changeFragment(null);
        //findViewById(R.id.faded_image).setVisibility(View.GONE);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, GatewayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }

    public void changeFragment(View view) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (view == null) {
            ft.replace(R.id.login_screen_fragment, new LoginScreenFragment());
        }
        else {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);  //The last two arguments apply to the 'back' button
            if (view == findViewById(R.id.sign_in_button_login_screen)) {
                ft.replace(R.id.login_screen_fragment, new SignInFragment());
            }
            else if (view == findViewById(R.id.sign_up_with_email_button)) {
                ft.replace(R.id.login_screen_fragment, new SignUpFragment());
            }
        }
        ft.addToBackStack(null).commit();
    }

    public void onSignInClick(View view) {
        changeFragment(view);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    /*
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
    */
}
