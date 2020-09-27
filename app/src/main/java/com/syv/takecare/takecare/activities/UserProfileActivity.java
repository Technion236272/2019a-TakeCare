package com.syv.takecare.takecare.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.text.method.KeyListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ortiz.touchview.TouchImageView;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.fragments.AchievementsFragment;
import com.syv.takecare.takecare.utilities.RotateBitmap;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.syv.takecare.takecare.utilities.AchievementsFunctions.addCategoryBadge;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.addLikesBadge;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.addSharesBadge;

public class UserProfileActivity extends TakeCareActivity {

    private final static String TAG = "TakeCare/UserProfile";
    private static final String LANGUAGE_CHANGED = "LANGUAGE_CHANGED";

    private String profileOwner = null;

    private RelativeLayout root;
    private Toolbar toolbar;
    private Toolbar enlargedPhotoToolbar;
    private ImageView profilePictureView;
    private ProgressBar picturePB;
    private EditText userNameView;
    private Drawable originalEditTextDrawable;
    private KeyListener originalKeyListener;
    private EditText userDescriptionView;
    private TextView userLikesView;
    private ImageButton editNameBtn;
    private ImageButton acceptNameBtn;
    private ImageButton declineNameBtn;
    private ImageButton acceptDescriptionBtn;
    private ImageButton declineDescriptionBtn;
    private FloatingActionButton changePictureBtn;


    private String currentName = "User";
    private String currentDescription = "";

    private File selectedImageFile;
    private Uri selectedImage;

    private static int APP_PERMISSION_REQUEST_CAMERA = 100;
    private final static int REQUEST_CAMERA = 1;
    private final static int SELECT_IMAGE = 2;

    private AppCompatButton myFavoritesButton;
    private AppCompatButton myItemsButton;
    private AppCompatButton pendingRequestsButton;
    private AppCompatButton logOutButton;

    private boolean isSafeFocus = true;

    private String userPhotoURL = null;
    private ScrollView scrollView;
    private RelativeLayout fullscreenImageContainer;
    private ImageView fullscreenImage;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private boolean isImageFullscreen;

    private Bundle achievementsStatsBundle;
    private boolean isLanguageChanged;

    private View.OnClickListener minimizer = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // This page has a maximum of 8.5 seconds dedicated to loading
        startLoading(getString(R.string.loading_profile), 8500);

        initWidgets();
        setToolbar(toolbar);
        userDescriptionView.setMovementMethod(new ScrollingMovementMethod());

        changePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeProfilePic(v);
            }
        });

        originalEditTextDrawable = userNameView.getBackground();
        originalKeyListener = userNameView.getKeyListener();
        userNameView.setBackground(null);
        userNameView.setKeyListener(null);

        userNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: name");
                if (hasFocus) {
                    isSafeFocus = false;
                    declineNameBtn.setVisibility(View.VISIBLE);
                    acceptNameBtn.setVisibility(View.VISIBLE);
                    editNameBtn.setVisibility(View.GONE);
                } else {
                    isSafeFocus = true;
                    String newName = ((EditText) v).getText().toString();
                    if (currentName.equals(newName)) {
                        disableNameText();
                        declineNameBtn.setVisibility(View.GONE);
                        acceptNameBtn.setVisibility(View.GONE);
                        editNameBtn.setVisibility(View.VISIBLE);
                    } else {
                        alertTextChanges(v, currentName,
                                getString(R.string.discard_changes_name_body),
                                acceptNameBtn, declineNameBtn, true);
                    }
                }
            }
        });

        userDescriptionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: description");
                if (hasFocus) {
                    isSafeFocus = false;
                    declineDescriptionBtn.setVisibility(View.VISIBLE);
                    acceptDescriptionBtn.setVisibility(View.VISIBLE);
                } else {
                    isSafeFocus = true;
                    String newDescription = ((EditText) v).getText().toString();
                    if (currentDescription.equals(newDescription)) {
                        declineDescriptionBtn.setVisibility(View.GONE);
                        acceptDescriptionBtn.setVisibility(View.GONE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
                    } else {
                        alertTextChanges(v, currentDescription,
                                getString(R.string.discard_changes_profile_body),
                                acceptDescriptionBtn, declineDescriptionBtn, false);
                    }
                }
            }
        });

        selectedImageFile = null;
        selectedImage = null;

        final TextView usernameViewRef = userNameView;
        final TextView userDescriptionRef = userDescriptionView;
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            currentName = document.getString("name");
                            enlargedPhotoToolbar.setTitle(currentName);
                            usernameViewRef.setText(currentName);
                            if (document.getString("profilePicture") != null) {
                                Log.d(TAG, "Found profile pic. Fetched picture url: " + Uri.parse(document.getString("profilePicture")));
                                userPhotoURL = document.getString("profilePicture");
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(userPhotoURL)
                                        .apply(RequestOptions.circleCropTransform())
                                        .listener(new RequestListener<Bitmap>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                profilePictureView.setImageResource(R.drawable.ic_user_vector);
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                return false;
                                            }
                                        })
                                        .into(profilePictureView);
                            } else {
                                profilePictureView.setImageResource(R.drawable.ic_user_vector);
                            }
                            if (document.getString("description") != null) {
                                Log.d(TAG, "Found user's description");
                                currentDescription = document.getString("description");
                                userDescriptionRef.setText(currentDescription);
                            }
                            Long likes = document.getLong("likes");
                            if (likes != null) {
                                Log.d(TAG, "Found user's likes");
                                userLikesView.setText(String.valueOf(likes));
                            }

                            Long totalGivenItems = document.getLong("totalGivenItems");
                            Long inPersonCount = document.getLong("inPersonCount");
                            Long giveawayCount = document.getLong("giveawayCount");
                            Long raceCount = document.getLong("raceCount");

                            int likesBadge = addLikesBadge((ImageView)findViewById(R.id.likes_badge), likes);
                            int sharesBadge = addSharesBadge((ImageView)findViewById(R.id.shares_badge), totalGivenItems);
                            int inPersonBadge = addCategoryBadge((ImageView)findViewById(R.id.in_person_badge), "In Person", inPersonCount);
                            int giveawayBadge = addCategoryBadge((ImageView)findViewById(R.id.giveaway_badge),"Giveaway", giveawayCount);
                            int raceBadge = addCategoryBadge((ImageView)findViewById(R.id.race_badge), "Race", raceCount);

                            achievementsStatsBundle = new Bundle();
                            achievementsStatsBundle.putInt("LIKES_BADGE", likesBadge);
                            achievementsStatsBundle.putInt("SHARES_BADGE", sharesBadge);
                            achievementsStatsBundle.putInt("IN_PERSON_BADGE", inPersonBadge);
                            achievementsStatsBundle.putInt("GIVEAWAY_BADGE", giveawayBadge);
                            achievementsStatsBundle.putInt("RACE_BADGE", raceBadge);

                            getPreferences(Context.MODE_PRIVATE).edit().putInt("LIKES_BADGE", likesBadge).apply();
                            getPreferences(Context.MODE_PRIVATE).edit().putInt("SHARES_BADGE", sharesBadge).apply();
                            getPreferences(Context.MODE_PRIVATE).edit().putInt("IN_PERSON_BADGE", inPersonBadge).apply();
                            getPreferences(Context.MODE_PRIVATE).edit().putInt("GIVEAWAY_BADGE", giveawayBadge).apply();
                            getPreferences(Context.MODE_PRIVATE).edit().putInt("RACE_BADGE", raceBadge).apply();

                            profilePictureView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    zoomImageFromThumb(profilePictureView);
                                }
                            });
                            // Retrieve and cache the system's default "short" animation time.
                            mShortAnimationDuration = getResources().getInteger(
                                    android.R.integer.config_shortAnimTime);

                            stopLoading();

                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }

        myFavoritesButton = findViewById(R.id.my_favorites);
        myItemsButton = findViewById(R.id.my_items);
        pendingRequestsButton = findViewById(R.id.pending_requests);
        logOutButton = findViewById(R.id.logout);

        Drawable arrow = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_forward_black_24dp);
        Drawable arrowRTL = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_black);
        Drawable logoutIcon = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_logout);
        if (getResources().getConfiguration().locale.getLanguage().equals("iw")) {
            myFavoritesButton.setCompoundDrawablesWithIntrinsicBounds(arrowRTL, null, null, null);
            myItemsButton.setCompoundDrawablesWithIntrinsicBounds(arrowRTL, null, null, null);
            pendingRequestsButton.setCompoundDrawablesWithIntrinsicBounds(arrowRTL, null, null, null);
            logOutButton.setCompoundDrawablesWithIntrinsicBounds(logoutIcon, null, null, null);
        } else {
            myFavoritesButton.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
            myItemsButton.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
            pendingRequestsButton.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
            logOutButton.setCompoundDrawablesWithIntrinsicBounds(null, null, logoutIcon, null);
        }

        myItemsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onMyItemsPressed(v);
            }
        });
        pendingRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPendingRequestsPressed(v);
            }
        });
        logOutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onLogOutClick(v);
            }
        });

        Spinner langSpinner = findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(langAdapter);

        final String currentLanguage = getLocaleCode();
        if (currentLanguage.equals("iw")) {
            langSpinner.setSelection(2);
        } else if (currentLanguage.equals("en")) {
            langSpinner.setSelection(1);
        }
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 && !currentLanguage.equals("system")) {
                    setDefaultDeviceLocale();
                    Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(LANGUAGE_CHANGED, true);
                    startActivity(intent);
                } else if (position == 1 && !currentLanguage.equals("en")) {
                    setLocale("en");
                    Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(LANGUAGE_CHANGED, true);
                    startActivity(intent);
                } else if (position == 2 && !currentLanguage.equals("iw")) {
                    setLocale("iw");
                    Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(LANGUAGE_CHANGED, true);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        setTitle(R.string.user_profile_menu_title);

        final ConstraintLayout badgesLayout = findViewById(R.id.badges_layout);
        final float originalElevation = badgesLayout.getElevation();
        badgesLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) badgesLayout.setElevation(0);
                else {
                    badgesLayout.setElevation(originalElevation);
                    FragmentManager fm = getSupportFragmentManager();
                    AchievementsFragment dialogFragment = AchievementsFragment.newInstance(achievementsStatsBundle);
                    dialogFragment.show(fm, null);
                }
                return true;
            }
        });
        isLanguageChanged = getIntent().getBooleanExtra(LANGUAGE_CHANGED, false);
    }

    private void initWidgets() {
        root = findViewById(R.id.user_profile_root);
        toolbar = findViewById(R.id.user_profile_toolbar);
        enlargedPhotoToolbar = findViewById(R.id.enlarged_user_pic_toolbar);

        scrollView = findViewById(R.id.scroll_view);
        fullscreenImageContainer = findViewById(R.id.fullscreen_image_container);
        fullscreenImageContainer.bringChildToFront(fullscreenImage);
        fullscreenImage = findViewById(R.id.item_image_fullscreen);

        profilePictureView = findViewById(R.id.profile_pic);
        picturePB = findViewById(R.id.profile_pic_progress_bar);
        userNameView = findViewById(R.id.user_name);
        userDescriptionView = findViewById(R.id.about);
        userLikesView = findViewById(R.id.likes_counter);
        editNameBtn = findViewById(R.id.edit_name_button);

        acceptNameBtn = findViewById(R.id.accept_name_btn);
        declineNameBtn = findViewById(R.id.decline_name_btn);
        acceptDescriptionBtn = findViewById(R.id.accept_description_btn);
        declineDescriptionBtn = findViewById(R.id.decline_description_btn);
        changePictureBtn = findViewById(R.id.camera_fab);
    }

    private void alertTextChanges(final View v, final String backup, final String msg,
                                  final ImageButton acceptBtn, final ImageButton declineBtn,
                                  final boolean isName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle(R.string.discard_changes_title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((EditText) v).setText(backup);
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        acceptBtn.setVisibility(View.GONE);
                        declineBtn.setVisibility(View.GONE);
                        if (isName)
                            editNameBtn.setVisibility(View.VISIBLE);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        v.setFocusableInTouchMode(true);
                        v.requestFocus();
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isImageFullscreen) {
            Log.d(TAG, "onOptionsItemSelected: fake toolbar clicked");
            if (!minimizeFullscreenImage()) {
                if (isLanguageChanged) {
                    Intent intent = new Intent(this, TakerMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            Log.d(TAG, "onOptionsItemSelected: real toolbar clicked");
            if (isLanguageChanged) {
                Intent intent = new Intent(this, TakerMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        View focusView = getCurrentFocus();
        if (focusView == null) {
            if (isLanguageChanged) {
                Intent intent = new Intent(this, TakerMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        } else if (focusView.equals(userDescriptionView) &&
                !userDescriptionView.getText().toString().equals(currentDescription)) {
            userDescriptionView.clearFocus(); // Invokes the focus change listener
        } else if (focusView.equals(userNameView) &&
                !userNameView.getText().toString().equals(currentName)) {
            userNameView.clearFocus(); // Invokes the focus change listener
        } else {
            if (isImageFullscreen) {
                Log.d(TAG, "onBackPressed: closing fullscreen image");
                if (!minimizeFullscreenImage()) {
                    if (isLanguageChanged) {
                        Intent intent = new Intent(this, TakerMenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        super.onBackPressed();
                    }
                }
            } else {
                Log.d(TAG, "onBackPressed: finishing activity");
                if (isLanguageChanged) {
                    Intent intent = new Intent(this, TakerMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    public void onChangeNameClick(View view) {
        userNameView.setKeyListener(originalKeyListener);
        userNameView.setBackground(originalEditTextDrawable);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(userNameView, InputMethodManager.SHOW_IMPLICIT);
        userNameView.setFocusableInTouchMode(true);
        userNameView.requestFocus();
    }

    private void disableNameText() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
        userNameView.setFocusableInTouchMode(false);
        userNameView.setKeyListener(null);
        userNameView.setBackground(null);
    }

    private void setUserName(final String newName, final String restore, final boolean undoable) {
        if (user == null) {
            return;
        }
        if (!isNameValid(newName)) {
            userNameView.setText(restore);
            return;
        }
        startLoading(getString(R.string.updating_profile_name), null);
        db.collection("users").document(user.getUid())
                .update("name", newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user name updated!");
                        if (undoable) {
                            Snackbar override = Snackbar
                                    .make(root, getString(R.string.profile_name_updated), Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.undo_snackbar), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            setUserName(restore, restore, false);
                                        }
                                    });
                            override.show();
                        } else {
                            Snackbar undo = Snackbar.make(root, getString(R.string.profile_name_restored), Snackbar.LENGTH_SHORT);
                            undo.show();
                        }
                        userNameView.setText(newName);
                        stopLoading();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error updating name");
                        makeHighlightedSnackbar(root, getString(R.string.profile_name_error));
                        currentName = restore;
                        userNameView.setText(restore);
                        stopLoading();
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(TAG, "onCanceled: error updating name (no internet connection?)");
                        makeHighlightedSnackbar(root, getString(R.string.profile_name_error));
                        currentName = restore;
                        userNameView.setText(restore);
                        stopLoading();
                    }
                });
    }

    private boolean isNameValid(String newName) {
        if (newName == null || newName.replace(" ", "").isEmpty()) {
            Toast.makeText(this, getString(R.string.profile_name_empty_warning), Toast.LENGTH_SHORT).show();
            return false;
        }

        //TODO: explore the option of adding stricter validation logic here

        return true;
    }

    private void setUserDescription(final String newText, final String restore, final boolean undoable) {
        if (user == null) {
            return;
        }
        startLoading(getString(R.string.profile_page_updating), null);
        db.collection("users").document(user.getUid())
                .update("description", newText)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user description updated!");
                        if (undoable) {
                            Snackbar override = Snackbar
                                    .make(root, getString(R.string.profile_page_updated), Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.undo_snackbar), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            setUserDescription(restore, null, false);
                                        }
                                    });
                            override.show();
                        } else {
                            Snackbar undo = Snackbar.make(root, getString(R.string.profile_restored), Snackbar.LENGTH_SHORT);
                            undo.show();
                        }
                        userDescriptionView.setText(newText);
                        stopLoading();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error updating description");
                        makeHighlightedSnackbar(root, getString(R.string.profile_page_error));
                        currentDescription = restore;
                        userDescriptionView.setText(restore);
                        stopLoading();
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(TAG, "onCanceled: error updating description (no internet connection?)");
                        makeHighlightedSnackbar(root, getString(R.string.profile_page_error));
                        currentDescription = restore;
                        userDescriptionView.setText(restore);
                        stopLoading();
                    }
                });
    }


    public void onLogOutClick(View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.profile_logout_title)
                .setMessage(R.string.profile_logout_body)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Close login session of the user: delete token
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "getInstanceId failed", task.getException());
                                            return;
                                        }
                                        String token = task.getResult().getToken();
                                        Log.d(TAG, "Token is: " + token);
                                        db.collection("users").document(user.getUid())
                                                .update("tokens", FieldValue.arrayRemove(token));

                                        // Perform log out

                                        FirebaseAuth.getInstance().signOut();
                                        LoginManager.getInstance().logOut();
                                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing: dismiss alert dialog
                    }
                })
                .show();
    }

    private void setUserProfilePic(byte[] uploadBytes) {
        assert user != null;
        final StorageReference storageRef = storage.child("userProfilePictures/" + user.getUid());
        UploadTask uploadTask = storageRef.putBytes(uploadBytes);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String, Object> profilePicRef = new HashMap<>();
                                        userPhotoURL = uri.toString();
                                        isSafeFocus = true;
                                        profilePicRef.put("profilePicture", userPhotoURL);
                                        db.collection("users").document(user.getUid())
                                                .set(profilePicRef, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "All data added successfully!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void onChangeProfilePic(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.photo_upload_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.upload_camera:
                        if (ContextCompat.checkSelfPermission(UserProfileActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(UserProfileActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    APP_PERMISSION_REQUEST_CAMERA);
                        } else {
                            startCameraActivity();
                        }
                        break;
                    case R.id.upload_gallery:
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, SELECT_IMAGE);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        menu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == APP_PERMISSION_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            }
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        selectedImageFile = new File(getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        selectedImage = FileProvider.getUriForFile(UserProfileActivity.this, getPackageName() + ".provider", selectedImageFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
        Log.d(TAG, "Activating camera");
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (selectedImage != null) {
                        uploadPhoto(selectedImage);
                    }
                    break;
                case SELECT_IMAGE:
                    selectedImage = data.getData();
                    uploadPhoto(selectedImage);
                    break;
            }
        }
    }

    private void uploadPhoto(Uri imagePath) {
        isSafeFocus = false;
        ImageCompressTask resize = new ImageCompressTask();
        resize.execute(imagePath);
    }

    public void onAcceptDescription(View view) {
        String newDescription = userDescriptionView.getText().toString();
        if (!currentDescription.equals(newDescription)) {
            String previousDescription = currentDescription;
            currentDescription = newDescription;
            setUserDescription(currentDescription, previousDescription, true);
        }
        userDescriptionView.clearFocus();
    }

    public void onDeclineDescription(View view) {
        userDescriptionView.clearFocus();
    }

    public void onAcceptName(View view) {
        String newName = userNameView.getText().toString();
        if (!currentName.equals(newName)) {
            String previousName = currentName;
            currentName = newName;
            setUserName(currentName, previousName, true);
            enlargedPhotoToolbar.setTitle(currentName);
        }
        userNameView.clearFocus();
    }

    public void onDeclineName(View view) {
        userNameView.clearFocus();
    }

    private class ImageCompressTask extends AsyncTask<Uri, Integer, byte[]> {

        @Override
        protected void onPreExecute() {
            picturePB.setVisibility(View.VISIBLE);
            profilePictureView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            try {
                RotateBitmap rotateBitmap = new RotateBitmap();
                Bitmap bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(UserProfileActivity.this, uris[0]);
                Log.d(TAG, "doInBackground: MBs before compression: " + (double) bitmap.getByteCount() / 1e6);
                byte[] bytes = getBytesFromBitmap(bitmap, 80);
                Log.d(TAG, "doInBackground: MBs after compression: " + (double) bytes.length / 1e6);
                return bytes;
            } catch (IOException e) {
                Log.d(TAG, "doInBackground: exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(bytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePictureView);
            picturePB.setVisibility(View.GONE);
            profilePictureView.setVisibility(View.VISIBLE);
            setUserProfilePic(bytes);
        }

        private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            return stream.toByteArray();
        }
    }

    public void onMyItemsPressed(View view) {
        Intent intent = new Intent(this, SharedItemsActivity.class);
        startActivity(intent);
    }

    public void onPendingRequestsPressed(View view) {
        Intent intent = new Intent(this, RequestedItemsActivity.class);
        startActivity(intent);
    }

    private void zoomImageFromThumb(final View thumbView) {
        Log.d(TAG, "zoomImageFromThumb: Starting");

        if (userPhotoURL == null || !isSafeFocus) {
            return;
        }

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        fullscreenImage.setVisibility(View.VISIBLE);
        fullscreenImageContainer.setVisibility(View.VISIBLE);
        root.setBackgroundColor(getResources().getColor(android.R.color.black));
        scrollView.setVisibility(View.GONE);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(userPhotoURL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        fullscreenImage.setImageBitmap(resource);
                    }
                });

        isImageFullscreen = true;
        toggleToolbars();

        Log.d(TAG, "zoomImageFromThumb: Inflated fullscreen image");

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.fullscreen_image_container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        fullscreenImage.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        fullscreenImage.setPivotX(0f);
        fullscreenImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(fullscreenImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(fullscreenImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(fullscreenImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(fullscreenImage,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        minimizer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(fullscreenImage, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        fullscreenImage.setVisibility(View.GONE);
                        fullscreenImageContainer.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        root.setBackgroundColor(getResources().getColor(android.R.color.white));
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        fullscreenImage.setVisibility(View.GONE);
                        fullscreenImageContainer.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        root.setBackgroundColor(getResources().getColor(android.R.color.white));
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
                isImageFullscreen = false;
                toggleToolbars();
            }
        };
    }

    private boolean minimizeFullscreenImage() {
        if (minimizer == null) {
            return false;
        }
        ((TouchImageView) fullscreenImage).resetZoom();
        minimizer.onClick(fullscreenImage);
        return true;
    }

    private void toggleToolbars() {
        if (!isImageFullscreen) {
            Log.d(TAG, "toggleToolbars: setting the real toolbar");
            enlargedPhotoToolbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
            setToolbar(toolbar);
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            Log.d(TAG, "toggleToolbars: setting the fake toolbar");
            toolbar.setVisibility(View.GONE);
            enlargedPhotoToolbar.setVisibility(View.VISIBLE);
            setToolbar(enlargedPhotoToolbar);
            changeStatusBarColor(getResources().getColor(android.R.color.black));
        }
    }


    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}
