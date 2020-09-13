package com.syv.takecare.takecare.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.syv.takecare.takecare.POJOs.ActivityCode;
import com.syv.takecare.takecare.R;

/**
 * Base class that represents an activity of the app.
 * This activity is abstract - it does not contain an xml layout.
 * The activity is, however, registered in the manifest (to gain access to the alert dialog style)
 */
@SuppressLint("Registered")
public class TakeCareActivity extends AppCompatActivity {

    private static final String TAKECARE_TAG = "TakeCare";

    public static final String EXTRA_CHANGE_ACTIVITY = "EXTRA_CHANGE_ACTIVITY";

    private static final int LOAD_TIMEOUT = 5000;

    // This field indicates whether the activity is running or not
    protected static boolean isActivityRunning = false;

    public static String chatPartnerId = null;

    // IMPORTANT - for safety reasons, these variables must never be public
    protected FirebaseFirestore db;
    protected StorageReference storage;
    protected FirebaseAuth auth;
    protected FirebaseUser user;

    private ProgressDialog progress;
    private Handler progressHandler;

    // A task that's executed when a time-out occurs with a loading process
    final Runnable loadTimeoutTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAKECARE_TAG, "Loading timed out!");
            if (progress != null) {
                progress.dismiss();
            }

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(TakeCareActivity.this);
            builder.setTitle(R.string.error_title)
                    .setIcon(R.drawable.ic_alert_warning)
                    .setMessage(R.string.error_body)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();

            // Display "OK" button in the middle
            final Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            LinearLayout.LayoutParams buttonLayoutParams =
                    (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
            buttonLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            neutralButton.setLayoutParams(buttonLayoutParams);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        tryRedirectToLogin();
        tryRedirectActivity(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryRedirectToLogin();
        tryRedirectActivity(getIntent());
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
     * Redirects a user back to LoginActivity so that they may sign in,
     * if the user's login session has run out
     */
    private void tryRedirectToLogin() {
        if (user == null) {
            Log.d(TAKECARE_TAG, "TakeCare: user is null. Redirecting to login activity");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * A method for checking whether the app is running or not
     *
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
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    /**
     * Blocks user interaction and displays a progress dialog.
     * After a timeout, progress dialog is dismissed and an alert dialog pops up to inform the user
     * about an error.
     * This method should be invoked when some asynchronous task is running in the background
     *
     * @param msg:     message to display to the user while loading
     * @param timeout: amount of time (in milliseconds) before the progress dialog is dismissed
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
     * Changes the status bar's color (only works on API 21+)
     *
     * @param color: the selected color for the status bar
     */
    protected void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    /**
     * Hides the virtual keyboard in the activity, if it is open
     *
     * @param activity: activity in which the keyboard should be hidden
     */
    protected static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            // There is no view to pass the focus to, so we create a new view
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Checks if the intent sent to the activity requires starting another activity.
     * If it does - opens the required activity with the intent's bundle intact
     *
     * @param intent: the intent passed on to this activity
     */
    protected void tryRedirectActivity(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_CHANGE_ACTIVITY)) {
            return;
        }
        ActivityCode code = (ActivityCode) intent.getSerializableExtra(EXTRA_CHANGE_ACTIVITY);
        intent.removeExtra(EXTRA_CHANGE_ACTIVITY);
        Bundle intentBundle = intent.getExtras();
        Class<? extends Activity> redirectedActivity = parseActivityCode(code);
        Intent newIntent = new Intent(this, redirectedActivity);
        if (intentBundle != null) {
            newIntent.putExtras(intentBundle);
        }
        startActivity(newIntent);
    }

    /**
     * Parses an activity code.
     * @param code: the activity code to be parsed.
     * @return the class of the activity referenced by the activity code
     */
    public static Class<? extends Activity> parseActivityCode(ActivityCode code) {
        switch (code) {
            case ActivityAbout:
                return AboutActivity.class;
            case ActivityChatLobby:
                return ChatLobbyActivity.class;
            case ActivityChatRoom:
                return ChatRoomActivity.class;
            case ActivityGiverForm:
                return GiverFormActivity.class;
            case ActivityItemInfo:
                return ItemInfoActivity.class;
            case ActivityLogin:
                return LoginActivity.class;
            case ActivityRequestedItems:
                return RequestedItemsActivity.class;
            case ActivitySharedItems:
                return SharedItemsActivity.class;
            case ActivityTakerMenu:
                return TakerMenuActivity.class;
            case ActivityUserFavorites:
                return UserFavoritesActivity.class;
            case ActivityUserProfile:
                return UserProfileActivity.class;
        }
        return null;
    }

    /**
     * Creates a serializable code in the form of ActivityCode to represent an activity
     * @param activityName: the name of the activity to be encoded
     * @return the applicable ActivityCode instance corresponding to the given activityName
     */
    public static ActivityCode encodeActivity(String activityName) {
        switch(activityName) {
            case "ActivityAbout":
                return ActivityCode.ActivityAbout;
            case "ActivityChatLobby":
                return ActivityCode.ActivityChatLobby;
            case "ActivityChatRoom":
                return ActivityCode.ActivityChatRoom;
            case "ActivityGiverForm":
                return ActivityCode.ActivityGiverForm;
            case "ActivityItemInfo":
                return ActivityCode.ActivityItemInfo;
            case "ActivityLogin":
                return ActivityCode.ActivityLogin;
            case "ActivityRequestedItems":
                return ActivityCode.ActivityRequestedItems;
            case "ActivitySharedItems":
                return ActivityCode.ActivitySharedItems;
            case "ActivityTakerMenu":
                return ActivityCode.ActivityTakerMenu;
            case "ActivityUserFavorites":
                return ActivityCode.ActivityUserFavorites;
            case "ActivityUserProfile":
                return ActivityCode.ActivityUserProfile;
        }
        return ActivityCode.NA;
    }
}
