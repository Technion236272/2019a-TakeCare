package com.syv.takecare.takecare.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.customViews.FeedRecyclerView;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.POJOs.RequestedByCardHolder;
import com.syv.takecare.takecare.POJOs.RequesterCardInformation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class ItemInfoActivity extends TakeCareActivity {

    private final static String TAG = "ItemInfoActivity";
    private static final String EXTRA_ITEM_ID = "Item Id";

    private ImageView itemImageView;
    private TextView itemTitleView;
    private TextView itemDescriptionView;
    private ImageView uploaderProfilePictureView;
    private TextView uploaderNameView;
    private RatingBar uploaderRatingBar;
    private TextView itemPickupTimeView;
    private TextView itemLocationView;
    private TextView itemPhoneView;
    private AppCompatButton messageButton;
    private AppCompatButton requestButton;
    private Button reportButton;
    private ImageButton deleteItem;
    private View imageSpaceView;
    private ScrollView itemInfoScrollView;
    private ImageView expandedImageView;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private boolean isImageFullscreen;

    private CardView locationCard;
    private CardView phoneCard;
    private RelativeLayout request_button_layout;

    private FeedRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter = null;

    private String itemId;
    private String publisherID;
    private boolean isPublisher = false;

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
        Toolbar toolbar = findViewById(R.id.item_info_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        itemImageView = findViewById(R.id.item_image);
        itemTitleView = findViewById(R.id.item_title);
        itemDescriptionView = findViewById(R.id.item_description);
        uploaderProfilePictureView = findViewById(R.id.item_profile_pic);
        uploaderNameView = findViewById(R.id.item_giver_name);
        uploaderRatingBar = findViewById(R.id.ratingBar);
        itemPickupTimeView = findViewById(R.id.pickup_time_text);
        itemLocationView = findViewById(R.id.location_text);
        itemPhoneView = findViewById(R.id.phone_number);
        messageButton = findViewById(R.id.send_message_button);
        reportButton = findViewById(R.id.report_button);
        deleteItem = findViewById(R.id.delete_post_button);
        requestButton = findViewById(R.id.request_button);
        phoneCard = findViewById(R.id.phone_card);
        imageSpaceView = findViewById(R.id.image_space);
        itemInfoScrollView = findViewById(R.id.item_info_scroll_view);
        expandedImageView = findViewById(R.id.item_image_fullscreen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle extras = getIntent().getExtras();
            String transitionName = extras.getParcelable(TakerMenuActivity.EXTRA_ITEM);
            itemImageView.setTransitionName(transitionName);
        }

        final FirebaseUser currentUser = auth.getCurrentUser();
        Intent intent = getIntent();
        itemId = intent.getStringExtra(EXTRA_ITEM_ID);
        publisherID = intent.getStringExtra(Intent.EXTRA_UID);

        String uid = auth.getCurrentUser().getUid();
        isPublisher = publisherID.equals(uid);
        if (isPublisher) {
            setUpRecyclerView();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageFullscreen) {
                    expandedImageView.callOnClick();
                } else {
                    ItemInfoActivity.super.onBackPressed();
                }
            }
        });


        if (currentUser != null) {
            DocumentReference docRef = db.collection("items").document(itemId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Log.d(TAG, "user fetch: onComplete started");
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            itemTitleView.setText(document.getString("title"));
                            if (document.getString("publisher") != null) {
                                Log.d(TAG, "Found publisher. Fetched id: "
                                        + document.getString("publisher"));
                                if (document.getString("publisher").equals(currentUser.getUid())) {
                                    request_button_layout = findViewById(R.id.request_button_layout);
                                    request_button_layout.setVisibility(View.GONE);
                                    deleteItem.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    RelativeLayout requestButton = findViewById(R.id.request_button_layout);
                                    requestButton.setVisibility(View.VISIBLE);
                                    messageButton.setVisibility(View.VISIBLE);
                                    reportButton.setVisibility(View.VISIBLE);
                                }
                                fillPublisherInfo(document.getString("publisher"),
                                        uploaderNameView, uploaderProfilePictureView,
                                        uploaderRatingBar);
                            }
                            if (document.getString("photo") != null) {
                                Log.d(TAG, "Found item photo. Fetched picture url: "
                                        + Uri.parse(document.getString("photo")));

                                RequestOptions requestOptions = new RequestOptions();
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

                            } else {
                                switch (document.getString("category")) {
                                    case "Food":
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pizza_96_big_purple));
                                        break;
                                    case "Study Material":
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_book_purple));
                                        break;
                                    case "Households":
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lamp_purple));
                                        break;
                                    case "Lost & Found":
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lost_and_found_purple));
                                        break;
                                    case "Hitchhikes":
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_car_purple));
                                        break;
                                    default:
                                        itemImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_treasure_purple));
                                        break;
                                }
                            }
                            if (document.getString("description") != null) {
                                Log.d(TAG, "Found description. Writing: ");
                                itemDescriptionView.setText(document.getString("description"));
                            }
                            if (document.getString("pickupInformation") != null) {  // Change key to "pickupTime"
                                Log.d(TAG, "Found pickup time.");
                                itemPickupTimeView.setText(document.getString("pickupInformation"));
                            } else {
                                Log.d(TAG, "No Pickup time found.");
                                itemPickupTimeView.setText(R.string.flexible);
                            }
                            if (document.getString("pickupLocation") != null) {
                                Log.d(TAG, "Found pickup location.");
                                itemLocationView.setText(document.getString("pickupLocation"));
                            } else {
                                locationCard = (CardView) findViewById(R.id.location_card);
                                //locationCard.setVisibility(View.GONE);
                            }
                            if (document.getString("phoneNumber") != null) {
                                Log.d(TAG, "Found phone number.");
                                itemPhoneView.setText(document.getString("phoneNumber"));
                            }
                            Log.d(TAG, "user fetch: onComplete finished ");
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        messageButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.message_text_outline), null, null, null);
        requestButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_heart_outline), null, null, null);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestItem(v);
            }
        });
        reportButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.alert_circle), null, null, null);
        Log.d(TAG, "onCreate: finished");
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: started");
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

        Date timeOfRequest = model.getTimestamp();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        holder.requestDate.setText(dateFormat.format(timeOfRequest));
        DateFormat hourFormat = new SimpleDateFormat("hh:mm");
        holder.requestTime.setText(hourFormat.format(timeOfRequest));

        Long ratingTotal = documentSnapshot.getLong("rating");
        Long ratingCount = documentSnapshot.getLong("ratingCount");
        if (ratingTotal != null && ratingCount != null && ratingCount > 0) {
            holder.requesterRating.setRating((float) ratingTotal / ratingCount);
        } else {
            holder.requesterRating.setRating(0);
        }
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
                        //TODO: add this document when requesting an item!
                        db.collection("users")
                                .document(uid)
                                .collection("requestedItems")
                                .document(itemId)
                                .update("requestStatus", 0)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "accepted request: committed changes");
                                        db.collection("items").document(itemId)
                                                .update("status", 2,
                                                        "takenTimestamp", serverTimestamp());
                                        recyclerView.setVisibility(View.GONE);
                                        (findViewById(R.id.item_info_root))
                                                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryLite));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

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

    void fillPublisherInfo(String publisher, final TextView uploaderNameView, final ImageView uploaderProfilePictureView,
                           final RatingBar uploaderRatingBar) {
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
                        if (document.getString("profilePicture") != null) {
                            Log.d(TAG, "Found profile pic. Fetched picture url: "
                                    + Uri.parse(document.getString("profilePicture")));
                            Glide.with(getApplicationContext())
                                    .load(document.getString("profilePicture"))
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(uploaderProfilePictureView);
                        } else {
                            Log.d(TAG, "Profile picture not found.");
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.ic_user_vector)
                                    .into(uploaderProfilePictureView);
                        }
                        if (document.getLong("rating") != null
                                && document.getLong("ratingCount") != null) {
                            Log.d(TAG, "Found publisher rating.");
                            Long publisherRatingSum = document.getLong("rating");
                            Long publisherRatingCount = document.getLong("ratingCount");
                            float publisherRating;
                            if (publisherRatingSum == null || publisherRatingCount == null ||
                                    publisherRatingCount == 0) {
                                publisherRating = 0;
                            } else {
                                publisherRating = publisherRatingSum / publisherRatingCount;
                            }
                            uploaderRatingBar.setRating(publisherRating);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                }
                supportFinishAfterTransition();
                //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestItem(View view) {
        final String userId = auth.getCurrentUser().getUid();
        final DocumentReference userRef = db.collection("users").document(userId);
        final DocumentReference itemRef = db.collection("items").document(itemId);
        Map<String, Object> doc = new HashMap<>();
        doc.put("timestamp", FieldValue.serverTimestamp());
        doc.put("userRef", userRef);
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
                        Log.d(TAG, "requestedItem: failed to request item");
                    }
                });

        /*itemRef.update("hideFrom", FieldValue.arrayUnion(userId)); // upload to hideFrom
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("Assert")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = "user";
                    String userPicture = "";
                    long rating = 0;
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getString("name") != null) {
                            userName = document.getString("name");
                        }
                        if (document.getString("profilePicture") != null) {
                            userPicture = document.getString("profilePicture");
                        }
                        try {
                            rating = document.getLong("rating");
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Error: rating doesn't exist");
                        }
                        Map<String, Object> userToAdd = new HashMap<>();
                        userToAdd.put("name", userName);
                        userToAdd.put("picture", userPicture);
                        userToAdd.put("timestamp", FieldValue.serverTimestamp());
                        userToAdd.put("rating", rating);
                        itemRef.collection("requestedBy").document(userId).set(userToAdd);
                        //userRef.collection("requestedItems").add(itemRef);
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });*/
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
                .load(document.getString("photo"))
                .into(expandedImageView);
        isImageFullscreen = true;

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
        expandedImageView.setOnClickListener(new View.OnClickListener() {
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
            }
        });
    }
}