package com.syv.takecare.takecare.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.hootsuite.nachos.NachoTextView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ortiz.touchview.TouchImageView;
import com.syv.takecare.takecare.customViews.CustomInfoWindow;
import com.syv.takecare.takecare.fragments.FeedMapFragment;
import com.syv.takecare.takecare.fragments.UserProfileFragment;
import com.syv.takecare.takecare.customViews.FeedRecyclerView;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.POJOs.RequestedByCardHolder;
import com.syv.takecare.takecare.POJOs.RequesterCardInformation;
import com.syv.takecare.takecare.fragments.WorkaroundMapFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class ItemInfoActivity extends TakeCareActivity implements OnMapReadyCallback {

    private final static String TAG = "TakeCare/ItemInfo";
    private static final String EXTRA_ITEM_ID = "Item Id";
    private static final int USER_LIKES_MAX_DISPLAY = 999;
    private static final int TAGS_SEPARATING_LENGTH = 4;

    private RelativeLayout root;
    private Toolbar toolbar;
    private Toolbar enlargedPhotoToolbar;
    private TextView enlargedPhotoToolbarTitle;
    private ImageView itemImageView;
    private TextView itemTitleView;
    private TextView itemDescriptionView;
    private ImageView uploaderProfilePictureView;
    private TextView uploaderNameView;
    private TextView itemPickupTimeView;
    private TextView itemLocationView;
    private AppCompatButton messageButton;
    private AppCompatButton requestButton;
    private ImageButton deleteItem;
    private View imageSpaceView;
    private ScrollView itemInfoScrollView;
    private ImageView expandedImageView;
    private LikeButton likeButton;
    private TextView likesCounterView;
    private ImageView categoryIcon;
    private TextView category;
    private ImageView pickupMethodIcon;
    private TextView pickupMethod;
    private Button showOnMap;
    private NachoTextView tagsBox;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private boolean isImageFullscreen;

    private RelativeLayout request_button_layout;

    private FeedRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter = null;

    private String itemId;
    private String publisherID;
    private boolean isPublisher = false;

    private View.OnClickListener minimizer = null;
    private ProgressBar uploaderProgress;
    private TextView recyclerViewText;
    private TextView publishTimeTextView;
    private TextView timeLeftTextView;

    private boolean mLocationPermissionGranted;
    private FrameLayout minimapWrapper;
    private GoogleMap mMap;
    private Marker marker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(32.777751, 35.021508);
    private double latitude = 32.777751;
    private double longitude = 35.021508;
    private GeoPoint location;

    private String itemCategory;

    @Override
    protected void onStart() {
        super.onStart();
        if (isPublisher && recyclerView.getVisibility() == View.VISIBLE) {
            adapter.startListening();
            recyclerView.toggleVisibility();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPublisher) {
            adapter.stopListening();
            recyclerView.toggleVisibility();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);
        supportPostponeEnterTransition();
        toolbar = findViewById(R.id.item_info_toolbar);
        enlargedPhotoToolbar = findViewById(R.id.enlarged_item_info_toolbar);
        enlargedPhotoToolbarTitle = findViewById(R.id.enlarged_item_info_toolbar_title);
        setToolbar(toolbar);

        itemImageView = findViewById(R.id.item_image);
        itemTitleView = findViewById(R.id.item_title);
        itemDescriptionView = findViewById(R.id.item_description);
        uploaderProfilePictureView = findViewById(R.id.publisher_profile_pic);
        uploaderProgress = findViewById(R.id.item_load_bar);
        uploaderNameView = findViewById(R.id.item_giver_name);
        itemPickupTimeView = findViewById(R.id.pickup_time_text);
        itemLocationView = findViewById(R.id.location_text);
        messageButton = findViewById(R.id.send_message_button);
        deleteItem = findViewById(R.id.delete_post_button);
        requestButton = findViewById(R.id.request_button);
        imageSpaceView = findViewById(R.id.image_space);
        itemInfoScrollView = findViewById(R.id.item_info_scroll_view);
        expandedImageView = findViewById(R.id.item_image_fullscreen);
        likeButton = findViewById(R.id.like_button);
        likesCounterView = findViewById(R.id.likes_counter);
        root = findViewById(R.id.item_root);
        categoryIcon = findViewById(R.id.item_category_icon);
        category = findViewById(R.id.item_category);
        pickupMethodIcon = findViewById(R.id.item_pickup_method_icon);
        pickupMethod = findViewById(R.id.item_pickup_method);
        showOnMap = findViewById(R.id.item_map_button);
        tagsBox = findViewById(R.id.item_tags_box);
        publishTimeTextView = findViewById(R.id.item_publish_time);
        timeLeftTextView = findViewById(R.id.item_time_left);
        minimapWrapper = findViewById(R.id.minimap_wrapper);

        // Enable transition animations only for supported systems (API 21 and above).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle extras = getIntent().getExtras();
            String transitionName = extras.getParcelable(TakerMenuActivity.EXTRA_ITEM);
            itemImageView.setTransitionName(transitionName);
        }

        Intent intent = getIntent();
        itemId = intent.getStringExtra(EXTRA_ITEM_ID);
        publisherID = intent.getStringExtra(Intent.EXTRA_UID);

        final String uid = user.getUid();
        isPublisher = publisherID.equals(uid);
        if (isPublisher) {
            setUpRecyclerView();
            request_button_layout = findViewById(R.id.request_button_layout);
            request_button_layout.setVisibility(View.GONE);
            deleteItem.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                recyclerViewText.setVisibility(View.VISIBLE);
            }
        } else {
            messageButton.setVisibility(View.VISIBLE);
        }

        fillPublisherInfo(publisherID,
                uploaderNameView, uploaderProfilePictureView);

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                doLike(true);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                doLike(false);
            }
        });

        final DocumentReference docRef = db.collection("items").document(itemId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "user fetch: onComplete started");
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String title = document.getString("title");
                        itemTitleView.setText(title);
                        enlargedPhotoToolbarTitle.setText(title);

                        if (document.getString("photo") != null) {
                            Log.d(TAG, "Found item photo. Fetched picture url: "
                                    + Uri.parse(document.getString("photo")));

                            Glide.with(getApplicationContext())
                                    .load(document.getString("photo"))
                                    .into(itemImageView);

                            supportStartPostponedEnterTransition();

                            imageSpaceView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "Image zoom onClick: Initiated");
                                    zoomImageFromThumb(itemImageView, document);
                                }
                            });
                            // Retrieve and cache the system's default "short" animation time.
                            mShortAnimationDuration = getResources().getInteger(
                                    android.R.integer.config_shortAnimTime);
                        } else if (document.getString("category") != null){
                            itemCategory = document.getString("category");
                            Bitmap bitmap;
                            switch (itemCategory) {
                                case "Food":
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pizza_black_big);
                                    break;
                                case "Study Material":
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_book_black_big);
                                    break;
                                case "Households":
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lamp_black_big);
                                    break;
                                case "Lost & Found":
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lost_and_found_black_big);
                                    break;
                                case "Hitchhikes":
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car_black_big);
                                    break;
                                default:
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_treasure_black_big);
                                    break;
                            }
                            if (bitmap != null) {
                                Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                                itemImageView.setImageBitmap(bitmapScaled);
                                itemImageView.setScaleType(ImageView.ScaleType.CENTER);
                            }
                        }

                        Date timestamp = document.getDate("timestamp");
                        if (timestamp != null) {
                            Log.d(TAG, "setting publish timestamp date");
                            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm");
                            String dateText = "Published at: " + formatter.format(timestamp);
                            publishTimeTextView.setText(dateText);
                            Long airTime = document.getLong("airTime");
                            if (airTime != null) {
                                Log.d(TAG, "setting airtime");
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(timestamp);
                                calendar.add(Calendar.HOUR_OF_DAY, (int) ((long) airTime));
                                Date targetTime = calendar.getTime();
                                Date currentTime = Calendar.getInstance().getTime();
                                Log.d(TAG, "current time is: " + currentTime + "\ntarget time is: " + targetTime);
                                if (currentTime.before(targetTime)) {
                                    String timeLeftText = "Expires at: " + formatter.format(targetTime);
                                    timeLeftTextView.setText(timeLeftText);
                                }

                            }
                        }
                        String description = document.getString("description");
                        if (description != null) {
                            Log.d(TAG, "Found description. Writing: ");
                            itemDescriptionView.setText(description);
                        }
                        String pickupInformation = document.getString("pickupInformation");
                        if (pickupInformation != null) {
                            Log.d(TAG, "Found pickup time.");
                            itemPickupTimeView.setText(pickupInformation);
                        } else {
                            Log.d(TAG, "No Pickup time found.");
                            itemPickupTimeView.setText(R.string.flexible);
                        }
                        String pickupLocation = document.getString("pickupLocation");
                        if (pickupLocation != null) {
                            Log.d(TAG, "Found pickup location.");
                            itemLocationView.setText(pickupLocation);
                        }
                        location = document.getGeoPoint("location");
                        if (location != null) {
                            if (pickupLocation == null)
                                //itemLocationView.setVisibility(View.GONE);

                            if (!mLocationPermissionGranted) getLocationPermission();

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            minimapWrapper.setVisibility(View.VISIBLE);
                            try {
                                WorkaroundMapFragment mapFragment =
                                        (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.minimap);
                                mapFragment.getMapAsync(ItemInfoActivity.this);
                                ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.minimap)).setListener(new WorkaroundMapFragment.OnTouchListener() {
                                    @Override
                                    public void onTouch() {
                                        itemInfoScrollView.requestDisallowInterceptTouchEvent(true);
                                    }
                                });
                            } catch (NullPointerException e) {
                                Log.d(TAG, "Activity destroyed");
                            }
                            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                            showOnMap.setVisibility(View.VISIBLE);
//                            showOnMap.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Log.d(TAG, "Clicked on Show on map");
//                                    boolean mLocationPermissionGranted = ContextCompat.checkSelfPermission(
//                                            ItemInfoActivity.this,
//                                            android.Manifest.permission.ACCESS_FINE_LOCATION)
//                                            == PackageManager.PERMISSION_GRANTED;
//                                    if(mLocationPermissionGranted) {
//                                        Intent intent = new Intent(ItemInfoActivity.this, TakerMenuActivity.class);
//                                        intent.putExtra("LaunchInMapMode", true);
//                                        Bundle geoPoint = new Bundle();
//                                        geoPoint.putString("Lat", "32.77656875467437");
//                                        geoPoint.putString("Lng", "35.023163482546806");
//                                        intent.putExtra("GeoPointToShow", geoPoint);
//                                        startActivity(intent);
//                                    } else{
//                                        ActivityCompat.requestPermissions(ItemInfoActivity.this,
//                                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                                                1);
//                                    }
//                                }
//                            });
                        }
                        String category = document.getString("category");
                        if (category != null) {
                            Log.d(TAG, "Found category: " + category);
                            setCategory(category);
                        }
                        String pickupMethod = document.getString("pickupMethod");
                        if (pickupMethod != null) {
                            Log.d(TAG, "Found pickup method.");
                            ItemInfoActivity.this.pickupMethod.setText(document.getString("pickupMethod"));
                            switch (pickupMethod) {
                                case "In Person":
                                    pickupMethodIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_in_person_purple));
                                    break;
                                case "Giveaway":
                                    pickupMethodIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_giveaway_purple));
                                    break;
                                case "Race":
                                    pickupMethodIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_race_purple));
                            }
                            if (pickupMethod.equals("In Person") && !isPublisher) {
                                tryShowRequestButton();
                            }
                        }
                        List<String> tags = (List<String>) document.get("tags");
                        if (tags == null || tags.isEmpty()) {
                            findViewById(R.id.item_tags_layout).setVisibility(View.GONE);
                        } else {
                            initChips(tags);
                        }

                        Log.d(TAG, "user fetch: onComplete finished ");


                        Query query = db.collection("chats")
                                .whereEqualTo("giver", publisherID)
                                .whereEqualTo("taker", user.getUid())
                                .whereEqualTo("item", itemId);
                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                                    Log.d(TAG, "did not find existing chat document for this session");
                                    setNewChatSessionListener(document);
                                } else {
                                    Log.d(TAG, "found existing chat document for this session");
                                    setExistingChatSessionListener(queryDocumentSnapshots.getDocuments().get(0));
                                }
                            }
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("likedUsers") != null) {
                            Log.d(TAG, "Searching user's liked persons");
                            List<String> likedUsers = (List<String>) documentSnapshot.get("likedUsers");
                            for (String likedUser : likedUsers) {
                                if (publisherID.equals(likedUser)) {
                                    Log.d(TAG, "User has already liked this person");
                                    likeButton.setLiked(true);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error loading user's document");
                    }
                });

        messageButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.message_text_outline), null, null, null);
        requestButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_heart_outline), null, null, null);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestItem(v);
            }
        });

        if (!isPublisher) {
            Log.d(TAG, "setting on-click listener for profile picture");
            uploaderProfilePictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getSupportFragmentManager();
                    UserProfileFragment dialogFragment =
                            UserProfileFragment.newInstance(publisherID);
                    dialogFragment.show(fm, null);
                }
            });
        }

        Log.d(TAG, "onCreate: finished");
    }

    private void tryShowRequestButton() {
        db.collection("users")
                .document(user.getUid())
                .collection("requestedItems")
                .document(itemId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot == null || !documentSnapshot.exists()) {
                            Log.d(TAG, "showing request button");
                            RelativeLayout requestButton = findViewById(R.id.request_button_layout);
                            requestButton.setVisibility(View.VISIBLE);
                        } else {
                            Log.d(TAG, "user already requested this item");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "could not show request button");
                        makeHighlightedSnackbar(root, "Error fully loading this page. Please check your internet connection");
                    }
                });
    }

    private void setExistingChatSessionListener(final DocumentSnapshot chatDocument) {
        assert chatDocument != null;

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                Log.d(TAG, "redirecting to existing chat session");
                Intent intent = new Intent(ItemInfoActivity.this, ChatRoomActivity.class);

                intent.putExtra("CHAT_MODE", "taker");
                intent.putExtra("CHAT_ID", chatDocument.getId());
                intent.putExtra("OTHER_ID", publisherID);
                intent.putExtra("ITEM_ID", itemId);
                intent.putExtra("IS_NOT_REFERENCED_FROM_LOBBY", true);

                startActivity(intent);
                finish();
            }
        });
    }

    private void setNewChatSessionListener(final DocumentSnapshot giverDocument) {
        assert giverDocument != null;

        db.collection("users").document(publisherID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot userDocument) {
                        db.collection("users").document(user.getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(final DocumentSnapshot selfDocument) {
                                        requestButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                requestItem(v);
                                                createNewChatSession(v, userDocument, giverDocument, selfDocument, false);
                                            }
                                        });
                                        messageButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View v) {
                                                createNewChatSession(v, userDocument, giverDocument, selfDocument, true);
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

    private void doLike(final boolean isLiked) {
        Log.d(TAG, "onLikeClicked: started");
        likeButton.setEnabled(false);
        final DocumentReference publisherRef = db.collection("users").document(publisherID);
        final DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(new Transaction.Function<Long>() {
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(publisherRef);
                long newLikes = snapshot.getLong("likes") != null ?
                        snapshot.getLong("likes") : 0;
                if (isLiked) {
                    newLikes += 1;
                    userRef.update("likedUsers", FieldValue.arrayUnion(publisherID));
                } else {
                    newLikes -= 1;
                    userRef.update("likedUsers", FieldValue.arrayRemove(publisherID));
                }
                if (newLikes < 0) {
                    throw new FirebaseFirestoreException("Negative likes",
                            FirebaseFirestoreException.Code.ABORTED);
                }
                transaction.update(publisherRef, "likes", newLikes);
                return newLikes;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Long>() {
                    @Override
                    public void onSuccess(Long likes) {
                        Log.d(TAG, "onSuccess: likes transaction success");
                        if (likes > USER_LIKES_MAX_DISPLAY) {
                            String likesText = String.valueOf(USER_LIKES_MAX_DISPLAY).concat("+");
                            likesCounterView.setText(likesText);
                        } else {
                            String likesText = String.valueOf(likes);
                            likesCounterView.setText(likesText);
                        }
                        likeButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: likes transaction failed with: " + e.getMessage());
                        makeHighlightedSnackbar(root, "An error has occurred. Please check your internet connection");
                    }
                });
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: started");
        recyclerViewText = findViewById(R.id.requested_by_list_text);
        recyclerView = findViewById(R.id.requested_by_list);
        recyclerView.setEmptyView(findViewById(R.id.empty_feed_view));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        Query query = db.collection("items").document(itemId).collection("requestedBy")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<RequesterCardInformation> response = new FirestoreRecyclerOptions.Builder<RequesterCardInformation>()
                .setQuery(query, RequesterCardInformation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<RequesterCardInformation, RequestedByCardHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestedByCardHolder holder, int position, @NonNull final RequesterCardInformation model) {
                Log.d(TAG, "onBindViewHolder: started");

                final String uid = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();

                model.getUserRef()
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                setUserData(holder, model, documentSnapshot, uid);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onBindViewHolder: error loading user");
                            }
                        });
            }

            @NonNull
            @Override
            public RequestedByCardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item_request, viewGroup, false);
                return new RequestedByCardHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (getItemCount() > 0 && recyclerViewText.getVisibility() == View.GONE) {
                    recyclerViewText.setVisibility(View.VISIBLE);
                } else if (getItemCount() == 0 && recyclerViewText.getVisibility() == View.VISIBLE) {
                    recyclerViewText.setVisibility(View.GONE);
                }
                super.onDataChanged();
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        Log.d(TAG, "setUpRecyclerView: done");
    }

    private void setUserData(RequestedByCardHolder holder, @NonNull RequesterCardInformation model,
                             final DocumentSnapshot documentSnapshot, final String uid) {
        Log.d(TAG, "setUserData: started");
        holder.requesterProfilePicture.setImageResource(R.drawable.ic_user_purple);
        holder.requesterName.setText(documentSnapshot.getString("name"));
        String photo = documentSnapshot.getString("profilePicture");
        if (photo != null) {
            Glide.with(getApplicationContext())
                    .load(photo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.requesterProfilePicture);
        }

        Long likes = documentSnapshot.getLong("likes");
        if (likes != null) {
            holder.requesterLikesCounter.setText(String.valueOf(likes));
        } else {
            holder.requesterLikesCounter.setText("0");
        }

        Log.d(TAG, "setting on-click listener for requesters' profiles");
        holder.requesterProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                UserProfileFragment dialogFragment =
                        UserProfileFragment.newInstance(uid);
                dialogFragment.show(fm, null);
            }
        });

        Date timeOfRequest = model.getTimestamp();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        holder.requestDate.setText(dateFormat.format(timeOfRequest));
        DateFormat hourFormat = new SimpleDateFormat("hh:mm");
        holder.requestTime.setText(hourFormat.format(timeOfRequest));

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertAcceptRequest(documentSnapshot, uid);
            }
        });
        Log.d(TAG, "setUserData: finished");
    }

    private void alertAcceptRequest(DocumentSnapshot documentSnapshot, final String uid) {
        String strPre = "Are you sure you want to accept ";
        String strName = "<b><small>" + documentSnapshot.getString("name") + "</small></b>";
        String strSuff = "\'s request?";
        Spanned alertMsg = Html.fromHtml(strPre + strName + strSuff);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Accept Request")
                .setMessage(alertMsg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "accepted request: starting");
                        db.collection("users")
                                .document(uid)
                                .collection("requestedItems")
                                .document(itemId)
                                .update("requestStatus", 0)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "accepted request: committing changes");
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("status", 2);
                                        updates.put("displayStatus", false);
                                        updates.put("takenTimestamp", serverTimestamp());
                                        db.collection("items").document(itemId)
                                                .update(updates);
                                        recyclerView.setVisibility(View.GONE);
                                        recyclerViewText.setVisibility(View.GONE);
                                        (findViewById(R.id.item_info_root))
                                                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryLite));
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        Log.d(TAG, "rejected request");
                    }
                })
                .show();
    }

    void fillPublisherInfo(String publisher, final TextView uploaderNameView, final ImageView uploaderProfilePictureView) {
        Log.d(TAG, "fillPublisherInfo: started");
        final DocumentReference docRef = db.collection("users").document(publisher);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "publisherInfo onComplete: started");
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Publisher Document Snapshot Data: " + document.getData());
                        uploaderNameView.setText(document.getString("name"));

                        if (document.get("likes") != null) {
                            Log.d(TAG, "Found likes counter");
                            String likesText = String.valueOf(document.getLong("likes"));
                            likesCounterView.setText(likesText);
                        } else {
                            likesCounterView.setText("0");
                        }

                        if (document.getString("profilePicture") != null) {
                            Log.d(TAG, "Found profile pic. Fetched picture url: "
                                    + Uri.parse(document.getString("profilePicture")));
                            Glide.with(getApplicationContext())
                                    .load(document.getString("profilePicture"))
                                    .apply(RequestOptions.circleCropTransform())
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            uploaderProgress.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            uploaderProgress.setVisibility(View.GONE);
                                            likeButton.setVisibility(View.VISIBLE);
                                            if (isPublisher) {
                                                likeButton.setLiked(true);
                                                likeButton.setEnabled(false);
                                            }
                                            likesCounterView.setVisibility(View.VISIBLE);
                                            return false;
                                        }
                                    })
                                    .into(uploaderProfilePictureView);
                        } else {
                            Log.d(TAG, "Profile picture not found.");
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.ic_user_vector)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            uploaderProgress.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            Log.d(TAG, "finished loading user profile");
                                            uploaderProgress.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(uploaderProfilePictureView);
                        }
                        Log.d(TAG, "publisherInfo onComplete: done");
                    } else {
                        Log.d(TAG, "No such document - item publisher");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isImageFullscreen) {
            Log.d(TAG, "onOptionsItemSelected: fake toolbar clicked");
            if (!minimizeFullscreenImage()) {
                super.onBackPressed();
            }
        } else {
            Log.d(TAG, "onOptionsItemSelected: real toolbar clicked");
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (isImageFullscreen) {
            Log.d(TAG, "onBackPressed: closing fullscreen image");
            if (!minimizeFullscreenImage()) {
                super.onBackPressed();
            }
        } else {
            Log.d(TAG, "onBackPressed: finishing activity");
            super.onBackPressed();
        }
    }

    public void requestItem(View view) {
        final String userId = user.getUid();
        final DocumentReference userRef = db.collection("users").document(userId);
        final DocumentReference itemRef = db.collection("items").document(itemId);
        Map<String, Object> doc = new HashMap<>();
        doc.put("timestamp", FieldValue.serverTimestamp());
        doc.put("userRef", userRef);

        startLoading("Making a request...", null);
        db.collection("items").document(itemId).collection("requestedBy").document(userId)
                .set(doc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "requestedItem: item successfully requested");
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("itemRef", itemRef);
                        doc.put("timestamp", FieldValue.serverTimestamp());
                        doc.put("requestStatus", 1);

                        db.collection("users")
                                .document(userId)
                                .collection("requestedItems")
                                .document(itemId)
                                .set(doc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("items").document(itemId)
                                                .update("status", 0)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "item requested successfully!");
                                                        //TODO: eliminate post from user feed, and finish the activity
                                                        Toast.makeText(getApplicationContext(), "You've successfully requested the item!", Toast.LENGTH_SHORT).show();
                                                        RelativeLayout requestButton = findViewById(R.id.request_button_layout);
                                                        requestButton.setVisibility(View.GONE);
                                                        stopLoading();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "an error occurred with the request");
                                                        db.collection("users")
                                                                .document(userId)
                                                                .collection("requestedItems")
                                                                .document(itemId)
                                                                .delete();
                                                        stopLoading();
                                                    }

                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        stopLoading();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "requestedItem: failed to request item");
                        stopLoading();
                    }
                });
    }

    public void onDeletePost(View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("items").document(itemId).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Item successfully deleted");
                                        Toast.makeText(getApplicationContext(), "Item successfully deleted!",
                                                Toast.LENGTH_SHORT).show();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            finishAfterTransition();
                                        }
                                        supportFinishAfterTransition();
                                        //finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Failed to delete post");
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void zoomImageFromThumb(final View thumbView, final DocumentSnapshot document) {
        Log.d(TAG, "zoomImageFromThumb: Starting");
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        expandedImageView.setVisibility(View.VISIBLE);
        itemInfoScrollView.setVisibility(View.GONE);
        requestButton.setVisibility(View.GONE);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(document.getString("photo"))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        expandedImageView.setImageBitmap(resource);
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
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
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
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        itemInfoScrollView.setVisibility(View.VISIBLE);
                        requestButton.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        itemInfoScrollView.setVisibility(View.VISIBLE);
                        requestButton.setVisibility(View.VISIBLE);
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

        ((TouchImageView) expandedImageView).resetZoom();
        minimizer.onClick(expandedImageView);
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

    private void setCategory(String category) {
        switch (category) {
            case "Food":
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_pizza_96_big_purple));
                this.category.setText(R.string.nav_food_title);
                break;
            case "Study Material":
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_book_purple));
                this.category.setText(R.string.nav_study_material_title);
                break;
            case "Households":
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_lamp_purple));
                this.category.setText(R.string.nav_furniture_title);
                break;
            case "Lost & Found":
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_lost_and_found_purple));
                this.category.setText(R.string.nav_lost_and_found_title);
                break;
            case "Hitchhikes":
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_car_purple));
                this.category.setText(R.string.nav_hitchhike_title);
                break;
            default:
                categoryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_treasure_purple));
                this.category.setText(R.string.nav_other);
                break;
        }
    }

    private void initChips(Collection<String> tags) {
        if (tags == null || tags.isEmpty()) {
            tagsBox.setText("");
            return;
        }
        StringBuilder tagsTextBuilder = new StringBuilder();
        for (String tag : tags) {
            tagsTextBuilder.append(tag);
        }

        tagsBox.setText(tagsTextBuilder.toString());
        int index = 0;
        for (String tag : tags) {
            tagsBox.chipify(index, index + tag.length());
            index += tag.length() + TAGS_SEPARATING_LENGTH;
        }
    }

    private void createNewChatSession(final View v, DocumentSnapshot userDocument, DocumentSnapshot giverDocument,
                                      DocumentSnapshot selfDocument, boolean fromMessageButton) {
        Log.d(TAG, "creating new chat session document");

        v.setClickable(false);

        final Map<String, Object> newChat = new HashMap<>();

        try {
            newChat.put("giver", publisherID);
            newChat.put("giverName", userDocument.getString("name"));
            newChat.put("giverPhoto", userDocument.getString("profilePicture"));
            newChat.put("item", itemId);
            newChat.put("itemPhoto", giverDocument.getString("photo"));
            newChat.put("title", giverDocument.getString("title"));
            newChat.put("taker", user.getUid());
            newChat.put("takerName", selfDocument.getString("name"));
            newChat.put("takerPhoto", selfDocument.getString("profilePicture"));
            newChat.put("timestamp", FieldValue.serverTimestamp());
            newChat.put("category", giverDocument.getString("category"));
            newChat.put("messagesCount", 0);

            final DocumentReference chatRef = db.collection("chats").document();
            newChat.put("chat", chatRef.getId());
            Task<Void> task = chatRef.set(newChat);
            if (fromMessageButton) {
                startLoading("Opening new chat session...", null);
                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "chat document created. starting chat activity");
                        Intent intent = new Intent(ItemInfoActivity.this, ChatRoomActivity.class);
                        intent.putExtra("CHAT_MODE", "taker");
                        intent.putExtra("CHAT_ID", chatRef.getId());
                        intent.putExtra("OTHER_ID", publisherID);
                        intent.putExtra("ITEM_ID", itemId);
                        intent.putExtra("IS_NOT_REFERENCED_FROM_LOBBY", true);

                        startActivity(intent);
                        stopLoading();
                        Log.d(TAG, "closing ItemInfoActivity");
                        finish();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                stopLoading();
                                v.setClickable(true);
                                makeHighlightedSnackbar(root, "Error opening chat. Please check your internet connection");
                            }
                        });
            }

        } catch (NullPointerException e) {
            Log.d(TAG, "error setting message button: one of the fields is missing. " + e.getMessage());
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            return;
        }
        updateLocationUI();
        getLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker != null) {
                    marker.remove();
                }
                MarkerOptions selection = new MarkerOptions();
                selection.position(latLng).draggable(true);
                marker = mMap.addMarker(selection);
            }
        });

        if (location != null) {
            MarkerOptions testMarker = new MarkerOptions();
            String category = itemCategory;
            int iconId;
            switch (category) {
                case "Food":
                    iconId = R.drawable.pizza_markernobg;
                    break;
                case "Study Material":
                    iconId = R.drawable.book_marker;
                    break;
                case "Households":
                    iconId = R.drawable.households_marker;
                    break;
                case "Lost & Found":
                    iconId = R.drawable.lost_and_found_marker;
                    break;
                case "Hitchhikes":
                    iconId = R.drawable.car_marker;
                    break;
                default:
                    iconId = R.drawable.treasure_marker;
            }
            try {
                BitmapDescriptor resultImage = BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconId, 170, 170));
                testMarker.position(new LatLng(latitude, longitude))
                        .icon(resultImage);
            } catch (Exception exc) {
                return;
            }
            mMap.addMarker(testMarker);
        }
    }

    public Bitmap resizeMapIcons(int markerIcon, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), markerIcon);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocation() {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation == null) {
                            return;
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(latitude, longitude), 15));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, 15));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}