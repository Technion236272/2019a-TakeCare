package com.example.yuval.takecare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

public class TakerMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "TAKER";
    private static final int LIST_JUMP_THRESHOLD = 4;

    private FeedRecyclerView recyclerView;
    private ImageView userProfilePicture;
    private MenuItem currentDrawerChecked;

    private ConstraintLayout filterPopupMenu;
    private AppCompatImageButton chosenPickupMethod;
    private AppCompatButton jumpButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;
    FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder> adapter;
    private int position = 0;

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
                Intent intent = new Intent(TakerMenuActivity.this, GiverMenuActivity.class);
                startActivity(intent);
            }
        });

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
        jumpButton = (AppCompatButton) findViewById(R.id.jump_button);
        View header = navigationView.getHeaderView(0);
        userProfilePicture = (ImageView) header.findViewById(R.id.nav_user_picture);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

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
                                Glide.with(TakerMenuActivity.this)
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
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


        Query query = db.collection("items").orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<FeedCardInformation> response = new FirestoreRecyclerOptions.Builder<FeedCardInformation>()
                .setQuery(query, FeedCardInformation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, int position, @NonNull FeedCardInformation model) {
                Log.d(TAG, "model " + model.getPhoto());
                holder.itemTitle.setText(model.getTitle());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                Glide.with(holder.card)
                        .load(model.getPhoto())
                        .apply(requestOptions)
                        .into(holder.itemPhoto);

                // category selection
                int categoryId;
                if (model.getCategory().equals("Food")) {
                    categoryId = R.drawable.ic_pizza_slice_purple;
                } else if (model.getCategory().equals("Study Material")) {
                    categoryId = R.drawable.ic_book_purple;
                } else if (model.getCategory().equals("Households")) {
                    categoryId = R.drawable.ic_lamp_purple;
                } else if (model.getCategory().equals("Lost & Found")) {
                    categoryId = R.drawable.ic_lost_and_found_purple;
                } else if (model.getCategory().equals("Hitchhikes")) {
                    categoryId = R.drawable.ic_car_purple;
                } else {
                    categoryId = R.drawable.ic_treasure_96_purple_purple;
                }

                int pickupMethodId;
                if (model.getPickupMethod().equals("In Person")) {
                    pickupMethodId = R.drawable.ic_in_person_purple;
                } else if (model.getPickupMethod().equals("Giveaway")) {
                    pickupMethodId = R.drawable.ic_giveaway_purple;
                } else {
                    pickupMethodId = R.drawable.ic_race_purple;
                }

                holder.itemPublisher.setText(R.string.user_name);
                holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
                db.collection("users").document(model.getPublisher())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, "Found the user: " + documentSnapshot);
                                String publisherName, publisherPhoto;
                                publisherName = documentSnapshot.getString("name");
                                holder.itemPublisher.setText(publisherName);
                                if (documentSnapshot.getString("profilePicture") != null) {
                                    Glide.with(holder.card)
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
                //Glide.with(holder.card).load(model.getUserPictureURL()).apply(RequestOptions.circleCropTransform()).into(holder.profilePhoto);
                holder.itemPublisher.setText(model.getPublisher());
                holder.itemCategory.setImageResource(categoryId);
                holder.itemPickupMethod.setImageResource(pickupMethodId);
                holder.itemCategory.setTag(categoryId);
                holder.itemPickupMethod.setTag(pickupMethodId);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card, viewGroup, false);
                return new ItemsViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (adapter.getItemCount() == 0)
                    filterPopupMenu.setVisibility(View.GONE);
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(emptyFeedView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePosition();
                }
            }
        });
    }

    private void updatePosition() {
        position = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        Log.d(TAG, "onScrollStateChanged: POSITION IS: " + position);
        tryToggleJumpButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        recyclerView.toggleVisibility();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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
        if (adapter.getItemCount() == 0) {
            Toast.makeText(getApplicationContext(), "Filter menu is not available when the feed is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (filterPopupMenu.getVisibility() == View.GONE) {
            jumpButton.setVisibility(View.GONE);
            filterPopupMenu.setVisibility(View.VISIBLE);
        } else {
            filterPopupMenu.setVisibility(View.GONE);
            tryToggleJumpButton();
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

        //TODO: handle
        switch (view.getId()) {
            case R.id.pickup_any_button:
                break;
            case R.id.pickup_in_person_button:
                break;
            case R.id.pickup_giveaway_button:
                break;
            case R.id.pickup_race_button:
                break;
        }
        filterPopupMenu.setVisibility(View.GONE);
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
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        assert layoutManager != null;
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
            return false;
        } else if (id == R.id.nav_user_settings) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            item.setChecked(false);
            currentDrawerChecked.setChecked(true);
            return false;
        } else {
            currentDrawerChecked.setChecked(false);
            currentDrawerChecked = item;
            currentDrawerChecked.setChecked(true);
            //TODO: implement handler for each filter option
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    public void onItemCategoryPress(View view) {
        String str = "";
        if (view.getId() == R.id.item_category) {
            switch ((int) view.getTag()) {
                case R.drawable.ic_pizza_slice_purple:
                    str = "This item's category is food";
                    break;
                case R.drawable.ic_book_purple:
                    str = "This item's category is study material";
                    break;
                case R.drawable.ic_lamp_purple:
                    str = "This item's category is household objects";
                    break;
                case R.drawable.ic_lost_and_found_purple:
                    str = "This item's category is lost&founds";
                    break;
                case R.drawable.ic_car_purple:
                    str = "This item's category is hitchhiking";
                    break;
                default:
                    str = "This item is in a category of its own";
                    break;
            }
        } else {
            switch ((int) view.getTag()) {
                case R.drawable.ic_in_person_purple:
                    str = "This item is available for pick-up in person";
                    break;
                case R.drawable.ic_giveaway_purple:
                    str = "This item is available in a public giveaway";
                    break;
                case R.drawable.ic_race_purple:
                    str = "Race to get this item before anyone else!";
                    break;
                default:
                    break;
            }
        }
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    public void onReportPress(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.report_menu, menu.getMenu());
        menu.show();
    }

    public void onTakerCardSelected(View view) {
        //TODO: add extra information to intent
        Intent intent = new Intent(this, ItemInfoActivity.class);
        startActivity(intent);
    }

    private void makeHighlightedSnackbar(String str) {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.taker_root_layout), str, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
