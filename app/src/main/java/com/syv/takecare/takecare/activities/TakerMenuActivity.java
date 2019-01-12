package com.syv.takecare.takecare.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.syv.takecare.takecare.fragments.FeedListFragment;
import com.syv.takecare.takecare.fragments.FeedMapFragment;
import com.syv.takecare.takecare.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;

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

    private ConstraintLayout filterPopupMenu;
    private AppCompatImageButton chosenPickupMethod;


    private String queryCategoriesFilter = null;
    private String queryPickupMethodFilter = null;
    private boolean mapViewEnabled;
    private boolean mLocationPermissionGranted;

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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakerMenuActivity.this, GiverFormActivity.class);
                startActivity(intent);
            }
        });

        rootLayout = findViewById(R.id.taker_root_layout);

        //Set up the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
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

        View.OnClickListener pickupMethodfilterListener = new View.OnClickListener() {
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
            changeFragment();
        }

        Intent intent = getIntent();
        if (intent != null) {
            boolean welcomeToast = intent.getBooleanExtra(Intent.EXTRA_TEXT, false);
            makeWelcomeToast(welcomeToast);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        NavigationView navigationView = findViewById(R.id.nav_view);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        //TODO: fragment changes
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
        filterPopupMenu.setVisibility(View.GONE);
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected: selected item id: " + id);
        Intent intent;
        if (id == R.id.nav_requested_items) {
            intent = new Intent(this, FeedMapFragment.RequestedItemsActivity.class);
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
            //TODO: change this when chat is implemented
            makeHighlightedSnackbar(rootLayout, "Chat will be added in the future");
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } else if (id == R.id.nav_user_settings) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else if (id == R.id.nav_favorites) {
            //TODO: remove this when favorites is implemented
            makeHighlightedSnackbar(rootLayout, "Filtering by favorites will be added in the future");
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            mapViewEnabled = false;
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
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onNavigationItemSelected: here");
        }

        return true;
    }

    private void changeFragment(){
        Log.d(TAG, "changeFragment: Starting");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(mapViewEnabled) {
            if(mLocationPermissionGranted) {
                ft.replace(R.id.fragment_container, new FeedMapFragment());
            } else{
                getLocationPermission();
                return;
            }
        }  else {
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
}
