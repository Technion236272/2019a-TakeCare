package com.syv.takecare.takecare;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

/**
 * Base class that represents an activity of the app.
 * This activity is abstract - it does not contain a .xml layout and it's registered in the manifest
 */
@SuppressLint("Registered")
public class TakeCareActivity extends AppCompatActivity {

    // This field indicates whether the activity is running or not
    protected static boolean isActivityRunning = false;

    @Override
    protected void onResume() {
        super.onResume();
        isActivityRunning = true;
        setVisible(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityRunning = false;
        setVisible(false);
    }

    public static boolean isAppRunningInForeground() {
        return isActivityRunning;
    }
}
