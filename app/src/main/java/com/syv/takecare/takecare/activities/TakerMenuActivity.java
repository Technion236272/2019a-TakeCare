package com.syv.takecare.takecare.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cleveroad.blur_tutorial.BlurTutorial;
import com.cleveroad.blur_tutorial.TutorialBuilder;
import com.cleveroad.blur_tutorial.listener.SimpleTutorialListener;
import com.cleveroad.blur_tutorial.state.tutorial.MenuState;
import com.cleveroad.blur_tutorial.state.tutorial.PathState;
import com.cleveroad.blur_tutorial.state.tutorial.TutorialState;
import com.cleveroad.blur_tutorial.state.tutorial.ViewState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.syv.takecare.takecare.fragments.FeedListFragment;
import com.syv.takecare.takecare.fragments.FeedMapFragment;
import com.syv.takecare.takecare.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.view.View.VISIBLE;
import static com.syv.takecare.takecare.adapters.AchievementsAdapter.TOTAL_LIKES_BADGES;
import static com.syv.takecare.takecare.adapters.AchievementsAdapter.TOTAL_PICKUP_METHOD_BADGES;
import static com.syv.takecare.takecare.adapters.AchievementsAdapter.TOTAL_SHARING_BADGES;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.ALTRUIST_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.AUDIENCE_FAVORITE_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_BRONZE_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_GOLD_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_SILVER_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.COMMUNITY_HERO_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.GOOD_NEIGHBOUR_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.LEGENDARY_SHARER;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.LOCAL_CELEBRITY_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.PHILANTHROPIST_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForCategoryBadgeEligibility;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForLikesBadgeEligibility;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForSharesBadgeEligibility;

public class TakerMenuActivity extends TakeCareActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "TakeCare/TakerMenu";

    private static final String FILTER_CATEGORY_KEY = "CATEGORY FILTER";
    private static final String FILTER_PICKUP_KEY = "PICKUP FILTER";
    private static final String MAP_VIEW_ENABLED_KEY = "MAP VIEW ENABLED";
    public static final String EXTRA_ITEM = "EXTRA_ITEM";
    private RelativeLayout rootLayout;
    private ImageView userProfilePicture;
    private TextView userName;
    private MenuItem currentDrawerChecked;
    private Toolbar toolbar;
    private AutoCompleteTextView searchBar;
    private ImageButton searchButton;

    private ConstraintLayout filterPopupMenu;
    private AppCompatImageButton chosenPickupMethod;


    private String queryCategoriesFilter = null;
    private String queryPickupMethodFilter = null;
    private String queryKeywordFilter = null;
    private boolean mapViewEnabled;
    private boolean mLocationPermissionGranted;

    // Used in case the activity was started in order to display a location of a given item only
    private double Lat, Lng;
    public boolean launchInMapMode = false;

    final Runnable suggestionsTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "setting up auto-complete adapter for search view");
            setAutoCompleteAdapter();
        }
    };

    private FloatingActionButton fab;
    private NavigationView navigationView;
    private DrawerLayout drawer;

    public boolean isTutorialOn = false;
    private String currentLanguage;
    private BlurTutorial tutorial = null;
    private SimpleTutorialListener tutorialListener;
    private int originalOrientation;
    SharedPreferences prefs;
    private final String FIRST_LAUNCH = "first_launch";
    private final int TUT_STARTUP_DELAY = 1000;
    private final int TUT_RESTART_DELAY = 500;
    private final int TUT_INTRO = 0;
    private final int TUT_FEED = 1;
    private final int TUT_FAB = 2;
    private final int TUT_TOOLBAR_MAP = 3;
    private final int TUT_TOOLBAR_FILTER = 4;
    private final int TUT_FILTER_KEYWORDS = 5;
    private final int TUT_FILTER_METHOD = 6;
    private final int TUT_FILTER_IN_PERSON = 7;
    private final int TUT_FILTER_GIVEAWAY = 8;
    private final int TUT_FILTER_RACE = 9;
    private final int TUT_NAV_DRAWER_TOGGLE = 10;
    private final int TUT_NAV_DRAWER_HEADER = 11;
    private final int TUT_NAV_DRAWER_CONTENT = 12;
    private final int TUT_NAV_DRAWER_CONTENT_2 = 13;
    private final int TUT_NAV_DRAWER_CONTENT_3 = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker_menu);
        //Set the toolbar as the AppBar for this activity
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Update user's tokens
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful() || task.getResult() == null) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Token is: "+ token);
                        db.collection("users").document(user.getUid())
                                .update("tokens", FieldValue.arrayUnion(token));
                    }
                });

        //Set up the onClick listener for the giver form button
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTutorialOn) return;
                Intent intent = new Intent(TakerMenuActivity.this, GiverFormActivity.class);
                startActivity(intent);
            }
        });

        rootLayout = findViewById(R.id.taker_root_layout);

        //Set up the navigation drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        currentDrawerChecked = navigationView.getMenu().findItem(R.id.nav_show_all);
        currentDrawerChecked.setEnabled(true);
        currentDrawerChecked.setChecked(true);

        filterPopupMenu = findViewById(R.id.filter_menu_popup);
        chosenPickupMethod = findViewById(R.id.pickup_any_button);
        View header = navigationView.getHeaderView(0);
        userProfilePicture = header.findViewById(R.id.nav_user_picture);
        userName = header.findViewById(R.id.nav_user_name);

        // Adding pickup method filtering buttons' onClick listener

        List<View> pickupMethodFilterOptions = new ArrayList<>();
        pickupMethodFilterOptions.add(findViewById(R.id.pickup_any_button));
        pickupMethodFilterOptions.add(findViewById(R.id.pickup_in_person_button));
        pickupMethodFilterOptions.add(findViewById(R.id.pickup_giveaway_button));
        pickupMethodFilterOptions.add(findViewById(R.id.pickup_race_button));

        final View.OnClickListener pickupMethodfilterListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChoosePickupMethod(view);
            }
        };

        for (View view : pickupMethodFilterOptions) {
            view.setOnClickListener(pickupMethodfilterListener);
        }

        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: saveInstanceState is null");
            Intent intent = getIntent();
            if (intent != null) {
                launchInMapMode = intent.getBooleanExtra("LaunchInMapMode", false);
                mapViewEnabled = launchInMapMode;
            }
            changeFragment();
        }

//        Intent intent = getIntent();
//        if (intent != null) {
//            boolean welcomeToast = intent.getBooleanExtra(Intent.EXTRA_TEXT, false);
//            makeWelcomeToast(welcomeToast);
//        }

        // Set the search bar and search button in the filter menu
        searchBar = findViewById(R.id.search_bar);
        addAutoCompleteOptions();
        searchButton = findViewById(R.id.search_button);
        try {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryKeywordFilter = searchBar.getText().toString();
                    if (!queryKeywordFilter.isEmpty()) {
                        searchBar.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                        filterPopupMenu.setVisibility(View.GONE);
                    }
                    changeFragment();
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "Search button is null");
        }

        try {
            searchBar.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER)
                        searchButton.callOnClick();
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                        TakerMenuActivity.this.onBackPressed();
                    return false;
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "Search bar is null");
        }

        prefs = getSharedPreferences(FIRST_LAUNCH, MODE_PRIVATE);
        if (prefs.getBoolean(FIRST_LAUNCH, true)) {
            currentLanguage = getLocaleCode();
            originalOrientation = getRequestedOrientation();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            new Handler().postDelayed(new Runnable() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void run() {
                    isTutorialOn = true;
                    showTutorial();
                }
            }, TUT_STARTUP_DELAY);
        }
        db.collection("users").document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable final DocumentSnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                        if (value == null || error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }
                        checkAchievementsUpdates(value, TakerMenuActivity.this);
                    }
                });
    }

    private void showTutorial() {
        configureTutorialListener();
        tutorial = new TutorialBuilder()
                .withParent(rootLayout)
                .withPopupLayout(R.layout.popup_window)
                .withPopupCornerRadius(30)
                .withBlurRadius(10)
                .withOverlayColor(R.color.colorAccentLite)
                .withListener(tutorialListener)
                .build();

        configureStatesPart1();
    }

    private void configureStatesPart1() {
        Path feedHighlight = new Path();
        RelativeLayout feedContainer = findViewById(R.id.fragment_container);
        feedHighlight.addRoundRect(
                0,
                0,
                feedContainer.getWidth(),
                feedContainer.getHeight() * 2/3,
                10,
                10,
                Path.Direction.CW);

        Path emptyPath = new Path();
        emptyPath.addRect(0, 0, rootLayout.getWidth(), 0, Path.Direction.CW);

        // Intro with fade animation
        tutorial.addState(new PathState(TUT_INTRO, emptyPath, null, rootLayout));
        tutorial.configure()
                .withPopupAppearAnimation(R.anim.tutorial_fade_in)
                .withPopupDisappearAnimation(R.anim.tutorial_fade_out);
        tutorial.start();

        List<TutorialState> states = new ArrayList<>();
        states.add(new PathState(TUT_FEED, feedHighlight, null, rootLayout));
        states.add(new ViewState(TUT_FAB, fab, null));
        states.add(new MenuState(TUT_TOOLBAR_MAP, toolbar, R.id.action_change_display, null));
        states.add(new MenuState(TUT_TOOLBAR_FILTER, toolbar, R.id.action_filter, null));

        tutorial.addAllStates(states);
    }

    private void configureStatesPart2() {
        Path searchKeywordHighlight = new Path();
        ConstraintLayout searchKeywordLayout = findViewById(R.id.searchKeywordLayout);
        searchKeywordHighlight.addRect(
                0,
                0,
                searchKeywordLayout.getWidth(),
                searchKeywordLayout.getHeight(),
                Path.Direction.CW
        );

        Path filterMethodHighlight = new Path();
        ConstraintLayout pickupMethodLayout = findViewById(R.id.pickupMethodLayout);
        filterMethodHighlight.addRect(
                0,
                searchKeywordLayout.getHeight(),
                pickupMethodLayout.getWidth(),
                searchKeywordLayout.getHeight() + pickupMethodLayout.getHeight(),
                Path.Direction.CW
        );

        List<TutorialState> states = new ArrayList<>();
        states.add(new PathState(TUT_FILTER_KEYWORDS, searchKeywordHighlight, null, rootLayout));
        states.add(new PathState(TUT_FILTER_METHOD, filterMethodHighlight, null, rootLayout));
        states.add(new ViewState(TUT_FILTER_IN_PERSON, findViewById(R.id.pickup_in_person_button), null));
        states.add(new ViewState(TUT_FILTER_GIVEAWAY, findViewById(R.id.pickup_giveaway_button), null));
        states.add(new ViewState(TUT_FILTER_RACE, findViewById(R.id.pickup_race_button), null));

        tutorial.clearStates();
        tutorial.addAllStates(states);
    }

    private void configureStatesPart3() {
        Path drawerToggleHighlight = new Path();
        float highlightOffset = 75f;
        if (currentLanguage.equals("iw")) {
            highlightOffset = toolbar.getWidth() - 75f;
        }
        drawerToggleHighlight.addCircle(
                highlightOffset,
                toolbar.getHeight()/2,
                toolbar.getHeight()/2,
                Path.Direction.CW
        );

        tutorial.addState(new PathState(TUT_NAV_DRAWER_TOGGLE, drawerToggleHighlight, null, toolbar));
    }

    private void configureStatesPart4() {
        View navHeader = navigationView.getHeaderView(0);
        Path drawerContentHighlight = new Path();
        float drawerLeftBorder = 0, drawerRightBorder = navigationView.getWidth();
        if (currentLanguage.equals("iw")) {
            drawerLeftBorder = rootLayout.getWidth() - navigationView.getWidth();
            drawerRightBorder = rootLayout.getWidth();
        }
        drawerContentHighlight.addRect(
                drawerLeftBorder,
                navHeader.getHeight(),
                drawerRightBorder,
                navigationView.getHeight(),
                Path.Direction.CW
        );

        List<TutorialState> states = new ArrayList<>();
        states.add(new ViewState(TUT_NAV_DRAWER_HEADER, navigationView.getHeaderView(0), null));
        states.add(new PathState(TUT_NAV_DRAWER_CONTENT, drawerContentHighlight, null, drawer));
        states.add(new PathState(TUT_NAV_DRAWER_CONTENT_2, drawerContentHighlight, null, drawer));
        states.add(new PathState(TUT_NAV_DRAWER_CONTENT_3, drawerContentHighlight, null, drawer));
        tutorial.addAllStates(states);
    }

    private void configureTutorialListener() {
        tutorialListener = new SimpleTutorialListener() {
            @Override
            public void onPopupViewInflated(@NotNull final TutorialState state, @NotNull final View popupView) {
                final Button nextButton = popupView.findViewById(R.id.tutorialNextButton);
                setTutorialText(state.getId(), popupView);

                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextButton.setClickable(false); // To prevent multiple clicks before the popup is closed
                        updateTutorialState(state.getId());
                        tutorial.next();
                    }
                });
            }
        };
    }

    private void setTutorialText(int stateId, View popupView) {
        final TextView description = popupView.findViewById(R.id.tutorialDescription);
        final Button nextButton = popupView.findViewById(R.id.tutorialNextButton);

        switch (stateId) {
            case TUT_INTRO:
                description.setText(R.string.tut_intro);
                RelativeLayout feedContainer = findViewById(R.id.fragment_container);
                popupView.setLayoutParams(new ViewGroup.LayoutParams(feedContainer.getWidth(), feedContainer.getHeight()/3));
                TextView title = popupView.findViewById(R.id.tutorialTitle);
                title.setVisibility(VISIBLE);
                title.setText(R.string.tut_intro_title);
                description.setTextSize(20);
                break;
            case TUT_FEED:
                description.setText(R.string.tut_feed);
                break;
            case TUT_FAB:
                description.setText(R.string.tut_fab);
                break;
            case TUT_TOOLBAR_MAP:
                description.setText(R.string.tut_map);
                break;
            case TUT_TOOLBAR_FILTER:
                description.setText(R.string.tut_filter);
                break;
            case TUT_FILTER_KEYWORDS:
                description.setText(R.string.tut_keywords);
                break;
            case TUT_FILTER_METHOD:
                description.setText(R.string.tut_pickup_methods);
                break;
            case TUT_FILTER_IN_PERSON:
                description.setText(R.string.tut_filter_in_person);
                break;
            case TUT_FILTER_GIVEAWAY:
                description.setText(R.string.tut_filter_giveaway);
                break;
            case TUT_FILTER_RACE:
                description.setText(R.string.tut_filter_race);
                break;
            case TUT_NAV_DRAWER_TOGGLE:
                description.setText(R.string.tut_drawer_toggle);
                break;
            case TUT_NAV_DRAWER_HEADER:
                description.setText(R.string.tut_drawer_header);
                break;
            case TUT_NAV_DRAWER_CONTENT:
                description.setText(R.string.tut_drawer_content);
                break;
            case TUT_NAV_DRAWER_CONTENT_2:
                description.setText(R.string.tut_drawer_content_2);
                break;
            case TUT_NAV_DRAWER_CONTENT_3:
                description.setText(R.string.tut_drawer_content_3);
                nextButton.setText(R.string.tut_button_done);
                break;
            default:
                break;
        }
    }

    private void updateTutorialState(int stateId) {
        switch (stateId) {
            case TUT_FEED:
                tutorial.configure()
                        .withPopupAppearAnimation(R.anim.slide_in_left)
                        .withPopupDisappearAnimation(R.anim.slide_out_left);
                break;

            case TUT_FAB:
                tutorial.configure()
                        .withPopupDisappearAnimation(R.anim.slide_out_right);
                break;

            case TUT_TOOLBAR_FILTER:
                tutorial.clearStates();
                tutorial.configure()
                        .withPopupAppearAnimation(R.anim.tutorial_fade_in);
                filterPopupMenu.setVisibility(VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        configureStatesPart2();
                        tutorial.start();
                    }
                }, TUT_RESTART_DELAY);
                break;

            case TUT_FILTER_KEYWORDS:
                tutorial.configure()
                        .withPopupDisappearAnimation(R.anim.tutorial_fade_out);
                break;

            case TUT_FILTER_METHOD:
                tutorial.configure()
                        .withPopupAppearAnimation(R.anim.slide_in_left)
                        .withPopupDisappearAnimation(R.anim.slide_out_right);
                break;

            case TUT_FILTER_RACE:
                filterPopupMenu.setVisibility(View.GONE);
                tutorial.clearStates();
                tutorial.configure()
                        .withPopupAppearAnimation(R.anim.slide_in_right)
                        .withPopupDisappearAnimation(R.anim.slide_out_right);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        configureStatesPart3();
                        tutorial.start();
                    }
                }, TUT_RESTART_DELAY);
                break;

            case TUT_NAV_DRAWER_TOGGLE:
                drawer.openDrawer(GravityCompat.START, true);
                tutorial.clearStates();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        configureStatesPart4();
                        tutorial.start();
                    }
                }, TUT_RESTART_DELAY);
                break;

            case TUT_NAV_DRAWER_HEADER:
                tutorial.configure()
                        .withPopupAppearAnimation(R.anim.tutorial_fade_in)
                        .withPopupDisappearAnimation(R.anim.tutorial_fade_out);
                break;

            case TUT_NAV_DRAWER_CONTENT_3:
                drawer.closeDrawer(GravityCompat.START, true);
                tutorial.clearStates();
                prefs.edit().putBoolean(FIRST_LAUNCH, false).apply();
                setRequestedOrientation(originalOrientation);
                isTutorialOn = false;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(FILTER_CATEGORY_KEY, queryCategoriesFilter);
        savedInstanceState.putString(FILTER_PICKUP_KEY, queryPickupMethodFilter);
        savedInstanceState.putBoolean(MAP_VIEW_ENABLED_KEY, mapViewEnabled);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: started");
        if (savedInstanceState.containsKey(FILTER_CATEGORY_KEY)) {
            queryCategoriesFilter = savedInstanceState.getString(FILTER_CATEGORY_KEY);
            queryPickupMethodFilter = savedInstanceState.getString(FILTER_PICKUP_KEY);
            mapViewEnabled = savedInstanceState.getBoolean(MAP_VIEW_ENABLED_KEY);
            setDrawerItem();
            setPickupItem();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setDrawerItem() {
        if (queryCategoriesFilter == null) {
            return;
        }

        navigationView = findViewById(R.id.nav_view);
        MenuItem item;
        switch (queryCategoriesFilter) {
            case "Food":
                item = navigationView.getMenu().findItem(R.id.nav_food);
                break;
            case "Study Material":
                item = navigationView.getMenu().findItem(R.id.nav_study_material);
                break;
            case "Households":
                item = navigationView.getMenu().findItem(R.id.nav_furniture);
                break;
            case "Lost & Found":
                item = navigationView.getMenu().findItem(R.id.nav_lost_and_found);
                break;
            case "Hitchhiking":
                item = navigationView.getMenu().findItem(R.id.nav_hitchhike);
                break;
            case "Other":
                item = navigationView.getMenu().findItem(R.id.nav_other);
                break;
            default:
                item = navigationView.getMenu().findItem(R.id.nav_show_all);
                break;
        }

        onNavigationItemSelected(item);
    }

    private void setPickupItem() {
        if (queryPickupMethodFilter == null) {
            return;
        }

        // Invalidate currently chosen pickup method
        ViewCompat.setBackgroundTintList(chosenPickupMethod, getResources().getColorStateList(R.color.divider));
        ImageViewCompat.setImageTintList(chosenPickupMethod, getResources().getColorStateList(R.color.secondary_text));
        chosenPickupMethod = null;

        View pickupButton;
        switch (queryPickupMethodFilter) {
            case "In Person":
                pickupButton = findViewById(R.id.pickup_in_person_button);
                break;
            case "Giveaway":
                pickupButton = findViewById(R.id.pickup_giveaway_button);
                break;
            case "Race":
                pickupButton = findViewById(R.id.pickup_race_button);
                break;
            default:
                pickupButton = findViewById(R.id.pickup_any_button);
                break;
        }
        onChoosePickupMethod(pickupButton);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Update user name and picture if necessary (changed via user profile)
        Log.d("TAG", "Checking for user");
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            Log.d("TAG", "User is logged in");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "Found user");
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            userName.setText(document.getString("name"));
                            if (document.getString("profilePicture") != null) {
                                Glide.with(getApplicationContext())
                                        .load(document.getString("profilePicture"))
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(userProfilePicture);
                            }
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START) && !isTutorialOn) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (filterPopupMenu.getVisibility() == VISIBLE && !isTutorialOn) {
            Log.d(TAG, "onBackPressed: Search bar is focused");
            filterPopupMenu.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taker_menu, menu);
        if (mapViewEnabled) {
            toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_item_appbar);
            toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_list);
        } else {
            toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_map_display);
            toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_map);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isTutorialOn) return true;
        switch (item.getItemId()) {
            case R.id.action_filter:
                toggleFilterMenu();
                break;
            case R.id.action_change_display:
                if (!mapViewEnabled) {
                    if(mLocationPermissionGranted) {
                        toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_item_appbar);
                        toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_list);
                    }
                    mapViewEnabled = true;
                } else {

                    toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_map_display);
                    toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_map);
                    mapViewEnabled = false;
                }
                changeFragment();
                //tryToggleJumpButton();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFilterMenu() {
        if (filterPopupMenu.getVisibility() == View.GONE) {
            filterPopupMenu.setVisibility(VISIBLE);
        } else {
            // Close the keyboard if it is open
            hideKeyboard(this);
            // Flush the search bar if the user has written to it
            searchBar.setText("");
            filterPopupMenu.setVisibility(View.GONE);
        }
    }

    public void openUserSettings(View view) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    public void onChoosePickupMethod(View view) {
        if (chosenPickupMethod != null && chosenPickupMethod.equals(view)) {
            return;
        }
        if (chosenPickupMethod != null) {
            ViewCompat.setBackgroundTintList(chosenPickupMethod, getResources().getColorStateList(R.color.divider));
            ImageViewCompat.setImageTintList(chosenPickupMethod, getResources().getColorStateList(R.color.secondary_text));
        }
        ViewCompat.setBackgroundTintList(view, getResources().getColorStateList(R.color.colorPrimary));
        ImageViewCompat.setImageTintList((ImageView) view, getResources().getColorStateList(R.color.icons));
        chosenPickupMethod = (AppCompatImageButton) view;
        switch (view.getId()) {
            case R.id.pickup_any_button:
                queryPickupMethodFilter = null;
                break;
            case R.id.pickup_in_person_button:
                queryPickupMethodFilter = "In Person";
                break;
            case R.id.pickup_giveaway_button:
                queryPickupMethodFilter = "Giveaway";
                break;
            case R.id.pickup_race_button:
                queryPickupMethodFilter = "Race";
                break;
        }
        changeFragment();
        if (!searchBar.isFocused())
            filterPopupMenu.setVisibility(View.GONE);
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (isTutorialOn) return false;
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected: selected item id: " + id);
        Intent intent;
        if (id == R.id.nav_requested_items) {
            intent = new Intent(this, RequestedItemsActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
            return false;
        } else if (id == R.id.nav_my_items) {
            intent = new Intent(this, SharedItemsActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
            return false;
        } else if (id == R.id.nav_manage_favorites) {
            intent = new Intent(this, UserFavoritesActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
            return false;
        } else if (id == R.id.nav_chat) {
            intent = new Intent(this, ChatLobbyActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else if (id == R.id.nav_user_settings) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else if (id == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else {
            // Category filtering
            currentDrawerChecked.setChecked(false);
            currentDrawerChecked = item;
            currentDrawerChecked.setChecked(true);
            switch (id) {
                case R.id.nav_show_all:
                    queryCategoriesFilter = null;
                    break;
                case R.id.nav_food:
                    queryCategoriesFilter = "Food";
                    break;
                case R.id.nav_study_material:
                    queryCategoriesFilter = "Study Material";
                    break;
                case R.id.nav_furniture:
                    queryCategoriesFilter = "Households";
                    break;
                case R.id.nav_lost_and_found:
                    queryCategoriesFilter = "Lost & Found";
                    break;
                case R.id.nav_hitchhike:
                    queryCategoriesFilter = "Hitchhikes";
                    break;
                case R.id.nav_other:
                    queryCategoriesFilter = "Other";
                    break;
                //TODO: add favorites filter in the future. For now we ignore this
            }
            changeFragment();
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onNavigationItemSelected: here");
        }

        return true;
    }

    private void changeFragment() {
        Log.d(TAG, "changeFragment: Starting");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mapViewEnabled) {
            if (mLocationPermissionGranted) {
                Intent intent = getIntent();
                if (intent != null) {
                    if (launchInMapMode) {
                        Bundle geoPoint = intent.getBundleExtra("GeoPointToShow");
                        try {
                            Lat = Double.parseDouble(geoPoint.getString("Lat"));
                            Lng = Double.parseDouble(geoPoint.getString("Lng"));
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Unable to fetch coordinates from bundle");
                        }
                    }
                }
                ft.replace(R.id.fragment_container, new FeedMapFragment());
            } else {
                getLocationPermission();
                return;
            }
        } else {
            ft.replace(R.id.fragment_container, new FeedListFragment());
        }
        ft.disallowAddToBackStack().commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_item_appbar);
                    toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_list);
                    changeFragment();
                } else{
                    makeHighlightedSnackbar(rootLayout, "Please allow sharing your location to use the map");
                    toolbar.getMenu().findItem(R.id.action_change_display).setIcon(R.drawable.ic_map_display);
                    toolbar.getMenu().findItem(R.id.action_change_display).setTitle(R.string.action_switch_to_map);
                    mapViewEnabled = false;
                }
            }
        }
    }
    public String getQueryPickupMethodFilter(){
        return queryPickupMethodFilter;
    }

    public String getQueryCategoriesFilter() {
        return queryCategoriesFilter;
    }

    public String getQueryKeywordsFilter() {
        return queryKeywordFilter;
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void makeWelcomeToast(boolean fromLoginScreen) {
        if (!fromLoginScreen) {
            return;
        }
        final String toastText = "Welcome, ";
        final FirebaseUser user = auth.getCurrentUser();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Found user");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Toast.makeText(getApplicationContext(), toastText
                                + document.getString("name") + "!", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    private List<String> allExistingTags = new ArrayList<>();

    private void addAutoCompleteOptions() {
        final Handler suggestionsHandler = new Handler();

        Query query = db.collection("tags")
                .orderBy("tag");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Listen failed with: " + e);
                    return;
                }

                if (queryDocumentSnapshots == null) {
                    Log.d(TAG, "Did not find any tags in database");
                    return;
                }

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("tag") != null) {
                        allExistingTags.add(doc.getString("tag"));
                    }
                }
                suggestionsHandler.post(suggestionsTask);
            }
        });
    }

    private void setAutoCompleteAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (getApplicationContext(), R.layout.auto_complete_dropdown_item, allExistingTags);
        searchBar.setAdapter(adapter);
        Log.d(TAG, "set the auto-complete adapter");
    }

    public double getLat() {
        return Lat;
    }

    public double getLng() {
        return Lng;
    }
}
