package com.syv.takecare.takecare.activities;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
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

        // We create our notification channel here to allow notifications in the future -
        // if the user has entered the app in the past, the notification channel won't be overridden
        createNotificationChannel();

    FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(this, TakerMenuActivity.class);
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.takecare_notification_channel_name);
            String description = getString(R.string.takecare_notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
