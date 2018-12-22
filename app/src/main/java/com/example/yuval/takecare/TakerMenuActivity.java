package com.example.yuval.takecare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.concurrent.locks.ReentrantLock;

public class TakerMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "TakeCare";
    private static final int LIST_JUMP_THRESHOLD = 4;
    private static final int ICON_FILL_ITERATIONS = 12;
    private static final int ICON_FILL_DURATION = 200;
    private static final int ICON_ACTIVATED_DURATION = 400;
    ReentrantLock iconLock = new ReentrantLock();

    private RelativeLayout rootLayout;
    private FeedRecyclerView recyclerView;
    private ImageView userProfilePicture;
    private MenuItem currentDrawerChecked;

    private ConstraintLayout filterPopupMenu;
    private AppCompatImageButton chosenPickupMethod;
    private Button jumpButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;
    private int position = 0;

    private FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder> currentAdapter = null;

    private String queryCategoriesFilter = null;
    private String queryPickupMethodFilter = null;

    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker_menu);
        //Set the toolbar as the AppBar for this activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        jumpButton = (Button) findViewById(R.id.jump_button);
        View header = navigationView.getHeaderView(0);
        userProfilePicture = (ImageView) header.findViewById(R.id.nav_user_picture);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.d(TAG, "onCreate: getting screen orientation");
        orientation = getResources().getConfiguration().orientation;
        setUpRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
    }

    private void setUpRecyclerView() {
        recyclerView = (FeedRecyclerView) findViewById(R.id.taker_feed_list);
        View emptyFeedView = findViewById(R.id.empty_feed_view);
        Log.d(TAG, "setUpRecyclerView: setting layout manager for the current orientation");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false));
            recyclerView.addOnScrollListener(new CenterScrollListener());

        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        }
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        setUpAdapter();

        recyclerView.setEmptyView(emptyFeedView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePosition();
                } else {
                    jumpButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setUpAdapter() {
        Log.d(TAG, "setUpAdapter: setting up adapter");
        if (currentAdapter != null)
            currentAdapter.stopListening();

        // Default: no filters
        Query query = db.collection("items")
                .whereEqualTo("status", 1)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        if (queryCategoriesFilter != null && queryPickupMethodFilter != null) {
            // Filter by categories and pickup method
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter + " pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("status", 1)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (queryCategoriesFilter != null) {
            // Filter by categories
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("status", 1)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (queryPickupMethodFilter != null) {
            // Filter by pickup method
            Log.d(TAG, "setUpAdapter: query has: pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("status", 1)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }


        FirestoreRecyclerOptions<FeedCardInformation> response = new FirestoreRecyclerOptions.Builder<FeedCardInformation>()
                .setQuery(query, FeedCardInformation.class)
                .build();

        currentAdapter = new FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder>(response) {

            private int focusedItem = 0;

            @Override
            public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
                super.onAttachedToRecyclerView(recyclerView);

                // Handle key up and key down and attempt to move selection
                recyclerView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                        // Return false if scrolled to the bounds and allow focus to move off the list
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                return tryMoveSelection(lm, 1);
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                                return tryMoveSelection(lm, -1);
                            }
                        }

                        return false;
                    }
                });
            }

            private boolean tryMoveSelection(RecyclerView.LayoutManager layoutManager, int direction) {
                int tryFocusItem = focusedItem + direction;

                // If still within valid bounds, move the selection, notify to redraw, and scroll
                if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
                    notifyItemChanged(focusedItem);
                    focusedItem = tryFocusItem;
                    notifyItemChanged(focusedItem);
                    layoutManager.scrollToPosition(focusedItem);
                    return true;
                }
                return false;
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, int position, @NonNull final FeedCardInformation model) {
                Log.d(TAG, "model " + model.getPhoto());
                holder.itemTitle.setText(model.getTitle());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                Glide.with(getApplicationContext())
                        .load(model.getPhoto())
                        .apply(requestOptions)
                        .into(holder.itemPhoto);

                // Category selection
                int categoryId;
                switch (model.getCategory()) {
                    case "Food":
                        categoryId = R.drawable.ic_pizza_slice_purple;
                        break;
                    case "Study Material":
                        categoryId = R.drawable.ic_book_purple;
                        break;
                    case "Households":
                        categoryId = R.drawable.ic_lamp_purple;
                        break;
                    case "Lost & Found":
                        categoryId = R.drawable.ic_lost_and_found_purple;
                        break;
                    case "Hitchhikes":
                        categoryId = R.drawable.ic_car_purple;
                        break;
                    default:
                        categoryId = R.drawable.ic_treasure_purple;
                        break;
                }

                int pickupMethodId;
                switch (model.getPickupMethod()) {
                    case "In Person":
                        pickupMethodId = R.drawable.ic_in_person_purple;
                        break;
                    case "Giveaway":
                        pickupMethodId = R.drawable.ic_giveaway_purple;
                        break;
                    default:
                        pickupMethodId = R.drawable.ic_race_purple;
                        break;
                }

                holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
                db.collection("users").document(model.getPublisher())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, "Found the user: " + documentSnapshot);
                                holder.itemPublisher.setText(documentSnapshot.getString("name"));
                                if (documentSnapshot.getString("profilePicture") != null) {
                                    Glide.with(getApplicationContext())
                                            .load(documentSnapshot.getString("profilePicture"))
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(holder.profilePhoto);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                switch (model.getStatus()) {
                    case 0:
                        Log.d(TAG, "card in position " + position + " is REQUESTED");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                        break;
                    case 1:
                        Log.d(TAG, "card in position " + position + " is AVAILABLE");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorCardDefault));
                        break;
                }

                holder.itemCategory.setImageResource(categoryId);
                holder.itemPickupMethod.setImageResource(pickupMethodId);

                activateViewHolderIcons(holder, model);
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ItemInfoActivity.class);
                        intent.putExtra(Intent.EXTRA_UID, getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());
                        startActivity(intent);
                    }
                });

                holder.itemView.setSelected(focusedItem == position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = null;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card_carousel, viewGroup, false);

                } else {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card, viewGroup, false);
                }
                return new ItemsViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (position == 0 && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.scrollToPosition(0);
                }
                if (getItemCount() == 0)
                    filterPopupMenu.setVisibility(View.GONE);
            }

            class AdapterViewHolder extends ItemsViewHolder {
                public AdapterViewHolder(View itemView) {
                    super(itemView);

                    // Handle item click and set the selection
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Redraw the old selection and the new
                            notifyItemChanged(focusedItem);
                            focusedItem = getLayoutPosition();
                            notifyItemChanged(focusedItem);
                        }
                    });
                }
            }
        };

        Log.d(TAG, "setUpAdapter: created adapter");
        currentAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(currentAdapter);
        currentAdapter.startListening();
        Log.d(TAG, "setUpAdapter: done");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activateViewHolderIcons(final ItemsViewHolder holder, final FeedCardInformation model) {


        holder.itemCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (iconLock.isLocked()) {
                    return;
                }

                new Thread(new Runnable() {
                    public void run() {
                        iconLock.lock();
                        float alpha = (float) 0.6;
                        for (int i = 0; i < ICON_FILL_ITERATIONS; i++) {
                            v.setAlpha(alpha);
                            try {
                                Thread.sleep(ICON_FILL_DURATION / ICON_FILL_ITERATIONS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alpha += (float) (1 - 0.6) / ICON_FILL_ITERATIONS;
                        }

                        try {
                            Thread.sleep(ICON_ACTIVATED_DURATION);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < ICON_FILL_ITERATIONS; i++) {
                            v.setAlpha(alpha);
                            try {
                                Thread.sleep(ICON_FILL_DURATION / ICON_FILL_ITERATIONS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alpha -= (float) (1 - 0.6) / ICON_FILL_ITERATIONS;
                        }
                        iconLock.unlock();
                    }
                }).start();

                String str;
                switch (model.getCategory()) {
                    case "Food":
                        str = "This item's category is food";
                        break;
                    case "Study Material":
                        str = "This item's category is study material";
                        break;
                    case "Households":
                        str = "This item's category is household objects";
                        break;
                    case "Lost & Found":
                        str = "This item's category is lost&founds";
                        break;
                    case "Hitchhikes":
                        str = "This item's category is hitchhiking";
                        break;
                    default:
                        str = "This item is in a category of its own";
                        break;
                }

                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemPickupMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (iconLock.isLocked()) {
                    return;
                }

                new Thread(new Runnable() {
                    public void run() {
                        iconLock.lock();
                        float alpha = (float) 0.6;
                        for (int i = 0; i < ICON_FILL_ITERATIONS; i++) {
                            v.setAlpha(alpha);
                            try {
                                Thread.sleep(ICON_FILL_DURATION / ICON_FILL_ITERATIONS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alpha += (float) (0.9 - 0.6) / ICON_FILL_ITERATIONS;
                        }

                        try {
                            Thread.sleep(ICON_ACTIVATED_DURATION);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < ICON_FILL_ITERATIONS; i++) {
                            v.setAlpha(alpha);
                            try {
                                Thread.sleep(ICON_FILL_DURATION / ICON_FILL_ITERATIONS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alpha -= (float) (0.9 - 0.6) / ICON_FILL_ITERATIONS;
                        }
                        iconLock.unlock();
                    }
                }).start();

                String str;
                switch (model.getPickupMethod()) {
                    case "In Person":
                        str = "This item is available for pick-up in person";
                        break;
                    case "Giveaway":
                        str = "This item is available in a public giveaway";
                        break;
                    default:
                        str = "Race to get this item before anyone else!";
                        break;
                }

                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePosition() {
        position = (orientation == Configuration.ORIENTATION_LANDSCAPE) ?
                ((CarouselLayoutManager) recyclerView.getLayoutManager()).getCenterItemPosition() :
                ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();

        Log.d(TAG, "onScrollStateChanged: POSITION IS: " + position);
        tryToggleJumpButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        currentAdapter.startListening();
        recyclerView.toggleVisibility();

        // Update user name and picture if necessary (changed via user profile)
        final FirebaseUser user = auth.getCurrentUser();
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
    public void onStop() {
        super.onStop();
        currentAdapter.stopListening();
        recyclerView.toggleVisibility();
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
        switch (item.getItemId()) {
            case R.id.action_filter:
                toggleFilterMenu();
                break;
            case R.id.action_change_display:
                makeHighlightedSnackbar("Map display will be added in the future");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFilterMenu() {
        if (filterPopupMenu.getVisibility() == View.GONE) {
            jumpButton.setVisibility(View.GONE);
            filterPopupMenu.setVisibility(View.VISIBLE);
            if (currentAdapter.getItemCount() == 0) {
                (findViewById(R.id.empty_feed_arrow)).setVisibility(View.GONE);
            }
        } else {
            filterPopupMenu.setVisibility(View.GONE);
            tryToggleJumpButton();
            if (currentAdapter.getItemCount() == 0) {
                (findViewById(R.id.empty_feed_arrow)).setVisibility(View.VISIBLE);
            }
        }
    }

    public void openUserSettings(View view) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    public void onChoosePickupMethod(View view) {
        if (chosenPickupMethod.equals(view)) {
            return;
        }
        ViewCompat.setBackgroundTintList(chosenPickupMethod, getResources().getColorStateList(R.color.divider));
        ImageViewCompat.setImageTintList(chosenPickupMethod, getResources().getColorStateList(R.color.secondary_text));
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
        setUpAdapter();
        filterPopupMenu.setVisibility(View.GONE);
        if (currentAdapter.getItemCount() == 0) {
            ((findViewById(R.id.empty_feed_arrow))).setVisibility(View.VISIBLE);
        }
    }

    private void tryToggleJumpButton() {
        if (position >= LIST_JUMP_THRESHOLD && filterPopupMenu.getVisibility() == View.GONE) {
            jumpButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "tryToggleJumpButton: jump button is visible");
        } else {
            jumpButton.setVisibility(View.GONE);
        }
    }


    public void onJumpClick(View view) {
        recyclerView.smoothScrollToPosition(0);
        jumpButton.setVisibility(View.GONE);
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
            return false;
        } else if (id == R.id.nav_my_items) {
            intent = new Intent(this, SharedItemsActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else if (id == R.id.nav_manage_favorites) {
            intent = new Intent(this, UserFavoritesActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
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
                    queryCategoriesFilter = "Hitchhike";
                    break;
                case R.id.nav_other:
                    queryCategoriesFilter = "Other";
                    break;
                //TODO: add favorites filter in the future. For now we ignore this
            }
            setUpAdapter();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    public void onReportPress(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.report_menu, menu.getMenu());
        menu.show();
    }

    private void makeHighlightedSnackbar(String str) {
        Snackbar snackbar = Snackbar
                .make(rootLayout, str, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
