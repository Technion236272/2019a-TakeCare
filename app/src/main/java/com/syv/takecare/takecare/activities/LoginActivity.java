package com.syv.takecare.takecare.activities;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.syv.takecare.takecare.fragments.LoginScreenFragment;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.fragments.SignInFragment;
import com.syv.takecare.takecare.fragments.SignUpFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        changeFragment(null);

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
}
