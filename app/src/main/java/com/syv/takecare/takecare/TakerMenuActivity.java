package com.syv.takecare.takecare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.internal.location.zzas;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static android.view.View.VISIBLE;

public class TakerMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "TakeCare";

    private static final String FILTER_CATEGORY_KEY = "CATEGORY FILTER";
    private static final String FILTER_PICKUP_KEY = "PICKUP FILTER";
    private static final String MAP_VIEW_ENABLED_KEY = "MAP VIEW ENABLED";
    private RelativeLayout rootLayout;
    private ImageView userProfilePicture;
    private TextView userName;
    private MenuItem currentDrawerChecked;
    private Toolbar toolbar;

    public int getAbsolutePosition() {
        return absolutePosition;
    }

    public void setAbsolutePosition(int absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    private int absolutePosition;

    private ConstraintLayout filterPopupMenu;
    private AppCompatImageButton chosenPickupMethod;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;
    private FirebaseUser user;


    private String queryCategoriesFilter = null;
    private String queryPickupMethodFilter = null;
    private boolean mapViewEnabled;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker_menu);
        //Set the toolbar as the AppBar for this activity
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up the onClick listener for the giver form button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakerMenuActivity.this, GiverFormActivity.class);
                startActivity(intent);
            }
        });

        rootLayout = findViewById(R.id.taker_root_layout);

        //Set up the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        currentDrawerChecked = (MenuItem) navigationView.getMenu().findItem(R.id.nav_show_all);
        currentDrawerChecked.setEnabled(true);
        currentDrawerChecked.setChecked(true);

        filterPopupMenu = (ConstraintLayout) findViewById(R.id.filter_menu_popup);
        chosenPickupMethod = (AppCompatImageButton) findViewById(R.id.pickup_any_button);
        View header = navigationView.getHeaderView(0);
        userProfilePicture = (ImageView) header.findViewById(R.id.nav_user_picture);
        userName = (TextView) header.findViewById(R.id.nav_user_name);

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        user = auth.getCurrentUser();
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        changeFragment();

    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            changeFragment();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setDrawerItem() {
        if (queryCategoriesFilter == null) {
            return;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        MenuItem item;
        switch (queryCategoriesFilter) {
            case "Food":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_food);
                break;
            case "Study Material":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_study_material);
                break;
            case "Households":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_furniture);
                break;
            case "Lost & Found":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_lost_and_found);
                break;
            case "Hitchhiking":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_hitchhike);
                break;
            case "Other":
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_other);
                break;
            default:
                item = (MenuItem) navigationView.getMenu().findItem(R.id.nav_show_all);
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

        View pickupButton = null;
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
                        if (document.exists()) {
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taker_menu, menu);
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
//            jumpButton.setVisibility(View.GONE);
            filterPopupMenu.setVisibility(VISIBLE);
//            if (orientation == Configuration.ORIENTATION_PORTRAIT && currentAdapter.getItemCount() == 0) {
//                (findViewById(R.id.empty_feed_arrow)).setVisibility(View.GONE);
//            }
        } else {
            filterPopupMenu.setVisibility(View.GONE);
//            tryToggleJumpButton();
//            if (orientation == Configuration.ORIENTATION_PORTRAIT && currentAdapter.getItemCount() == 0) {
//                (findViewById(R.id.empty_feed_arrow)).setVisibility(VISIBLE);
//            }
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
        //setUpAdapter();
        changeFragment();
        filterPopupMenu.setVisibility(View.GONE);
//        jumpButton.setVisibility(View.GONE);
//        if (orientation == Configuration.ORIENTATION_PORTRAIT && currentAdapter.getItemCount() == 0) {
//            ((findViewById(R.id.empty_feed_arrow))).setVisibility(VISIBLE);
//        }
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected: selected item id: " + id);
        Intent intent;
        if (id == R.id.nav_requested_items) {
            intent = new Intent(this, RequestedItemsActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
//            mapViewWrapper.setVisibility(View.GONE);
            return false;
        } else if (id == R.id.nav_my_items) {
            intent = new Intent(this, SharedItemsActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
//            mapViewWrapper.setVisibility(View.GONE);
            return false;
        } else if (id == R.id.nav_manage_favorites) {
            intent = new Intent(this, UserFavoritesActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            mapViewEnabled = false;
//            mapViewWrapper.setVisibility(View.GONE);
            return false;
        } else if (id == R.id.nav_chat) {
            //TODO: change this when chat is implemented
            makeHighlightedSnackbar("Chat will be added in the future");
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            makeHighlightedSnackbar("Filtering by favorites will be added in the future");
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
//            jumpButton.setVisibility(View.GONE);
            changeFragment();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onNavigationItemSelected: here");
        }

        return true;
    }


    private void makeHighlightedSnackbar(String str) {
        Snackbar snackbar = Snackbar
                .make(rootLayout, str, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void changeFragment(){

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
                    makeHighlightedSnackbar("Please allow location to use the map feature!");
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
}
