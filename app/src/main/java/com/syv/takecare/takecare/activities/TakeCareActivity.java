package com.syv.takecare.takecare.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.syv.takecare.takecare.R;

/**
 * Base class that represents an activity of the app.
 * This activity is abstract - it does not contain an xml layout.
 * The activity is, however, registered in the manifest (to gain access to the alert dialog style)
 */
@SuppressLint("Registered")
public class TakeCareActivity extends AppCompatActivity {

    private static final String TAKECARE_TAG = "TakeCare";

    private static final int LOAD_TIMEOUT = 5000;

    // This field indicates whether the activity is running or not
    protected static boolean isActivityRunning = false;

    // IMPORTANT - for safety reasons, these variables must never be public
    protected FirebaseFirestore db;
    protected StorageReference storage;
    protected FirebaseAuth auth;
    protected FirebaseUser user;

    private ProgressDialog progress;
    private Handler progressHandler;

    // A task that's executed when a time-out occurs with a loading process
    final Runnable loadTimeoutTask  = new Runnable() {
        @Override
        public void run() {
            Log.d(TAKECARE_TAG, "Loading timed out!");
            if (progress != null) {
                progress.dismiss();
            }

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(TakeCareActivity.this);
            builder.setTitle("An error has occurred")
                    .setIcon(R.drawable.ic_alert_warning)
                    .setMessage("Loading process takes too long!\nCheck your internet connection and try again.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the alert - do nothing
                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();

            // Display "OK" button in the middle
            final Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
            buttonLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            neutralButton.setLayoutParams(buttonLayoutParams);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        if (user == null) {
            // User should not be here: re-direct them to the login screen
            Log.d(TAKECARE_TAG, "TakeCare onCreate: user is null. Redirecting to login activity");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user == null) {
            // User should not be here: re-direct them to the login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        isActivityRunning = true;
        setVisible(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityRunning = false;
        setVisible(false);
    }

    /**
     * Invoked to initialize application-wide variables (such as backend services' instances),
     * which are stored in every activity
     */
    private void initialize() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    /**
     * A method for checking whether the app is running or not
     * @return true iff some TakeCare activity instance is running
     */
    public static boolean isAppRunningInForeground() {
        return isActivityRunning;
    }

    /**
     * Shows a highlighted text (colored yellow) in a snackbar.
     * Used mainly to notify the user about problems
     *
     * @param root: the context's root element (activity's root layout)
     * @param msg:  the message displayed in the snackbar
     */
    protected void makeHighlightedSnackbar(@NonNull final View root, final String msg) {
        Snackbar snackbar = Snackbar
                .make(root, msg, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    /**
     * Blocks user interaction and displays a progress dialog.
     * After a timeout, progress dialog is dismissed and an alert dialog pops up to inform the user about an error.
     * This method should be invoked when some asynchronous task is running in the background
     * @param msg: message to display to the user while loading
     * @param timeout: amount of time to pass (in milliseconds) before the progress dialog is dismissed
     */
    protected void startLoading(final String msg, @Nullable Integer timeout) {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setMessage(msg);
        progress.show();
        if (progressHandler == null) {
            progressHandler = new Handler();
        }

        progressHandler.removeCallbacks(loadTimeoutTask);
        int loadTime = timeout != null ? timeout : LOAD_TIMEOUT;
        progressHandler.postDelayed(loadTimeoutTask, loadTime);
    }

    /**
     * Dismisses a loading progress dialog. Should be called after {@link #startLoading}
     */
    protected void stopLoading() {
        if (progress == null || !progress.isShowing())
            return;
        progress.dismiss();
        if (progressHandler != null) {
            progressHandler.removeCallbacks(loadTimeoutTask);
        }
    }

    /**
     * Changes the status bar's color
     * @param color: the selected color for the status bar
     */
    protected void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
