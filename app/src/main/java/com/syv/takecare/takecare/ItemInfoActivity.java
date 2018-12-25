package com.syv.takecare.takecare;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemInfoActivity extends AppCompatActivity {

    private final static String TAG = "TakeCare";
    private static final String EXTRA_ITEM_ID = "Item Id";

    private final int imageY = 360; //Item ImageView height in dp

    private ImageView itemImageView;
    private TextView itemTitleView;
    private TextView itemDescriptionView;
    private ImageView uploaderProfilePictureView;
    private TextView uploaderNameView;
    private RatingBar uploaderRatingBar;
    private TextView itemPickupTimeView;
    private TextView itemLocationView;
    private TextView itemPhoneView;
    private Button messageButton;
    private Button reportButton;

    private CardView locationCard;
    private CardView phoneCard;
    private RelativeLayout request_button_layout;

    private FeedRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter = null;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.item_info_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        itemImageView = (ImageView) findViewById(R.id.item_image);
        itemTitleView = (TextView) findViewById(R.id.item_title);
        itemDescriptionView = (TextView) findViewById(R.id.item_description);
        uploaderProfilePictureView = (ImageView) findViewById(R.id.item_profile_pic);
        uploaderNameView = (TextView) findViewById(R.id.item_giver_name);
        uploaderRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        itemPickupTimeView = (TextView) findViewById(R.id.pickup_time_text);
        itemLocationView = (TextView) findViewById(R.id.location_text);
        itemPhoneView = (TextView) findViewById(R.id.phone_number);
        messageButton = (Button) findViewById(R.id.send_message_button);
        reportButton = (Button) findViewById(R.id.report_button);

        phoneCard = (CardView) findViewById(R.id.phone_card);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        final FirebaseUser currentUser = auth.getCurrentUser();
        final FirebaseUser publisher;
        Intent intent = getIntent();
        itemId = intent.getStringExtra(EXTRA_ITEM_ID);
        publisherID = intent.getStringExtra(Intent.EXTRA_UID);

        String uid = auth.getCurrentUser().getUid();
        isPublisher = publisherID.equals(uid);
        if (isPublisher) {
            setUpRecyclerView();
        }

        //phoneCard.setVisibility(View.GONE);     // FOR NOW

        if (currentUser != null) {
            DocumentReference docRef = db.collection("items").document(itemId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Log.d(TAG, "user fetch: onComplete started");
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            itemTitleView.setText(document.getString("title"));
                            if (document.getString("publisher") != null) {
                                Log.d(TAG, "Found publisher. Fetched id: "
                                        + document.getString("publisher"));
                                if (document.getString("publisher").equals(currentUser.getUid())) {
                                    request_button_layout = (RelativeLayout) findViewById(R.id.request_button_layout);
                                    request_button_layout.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    RelativeLayout requestButton = (RelativeLayout) findViewById(R.id.request_button_layout);
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
                                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                                Glide.with(getApplicationContext())
                                        .load(document.getString("photo"))
                                        .apply(requestOptions)
                                        .into(itemImageView);
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
        Log.d(TAG, "onCreate: finished");
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: started");
        recyclerView = (FeedRecyclerView) findViewById(R.id.requested_by_list);
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
                                .update("requestStatus", 1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "accepted request: committed changes");
                                        recyclerView.setVisibility(View.GONE);

                                        db.collection("items").document(itemId)
                                                .update("status", 2);
                                        db.collection("items").document(itemId)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        List<String> requests = (List<String>) documentSnapshot.get("requests");

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                        db.collection("items")
                                                .document(itemId)
                                                .collection("requestedBy")
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot entry : queryDocumentSnapshots) {

                                                        }
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
                            Glide.with(ItemInfoActivity.this)
                                    .load(document.getString("profilePicture"))
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(uploaderProfilePictureView);
                        } else {
                            Log.d(TAG, "Profile picture not found.");
                            Glide.with(ItemInfoActivity.this)
                                    .load(R.drawable.ic_user_vector)
                                    .into(uploaderProfilePictureView);
                        }
                        if (document.getLong("rating") != null
                                && document.getLong("ratingCount") != null) {
                            Log.d(TAG, "Found publisher rating.");
                            Long publisherRatingSum = document.getLong("rating");
                            Long publisherRatingCount = document.getLong("ratingCount");
                            float publisherRating;
                            if(publisherRatingSum == null || publisherRatingCount == null ||
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
                finish();
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
                                        //TODO: eliminate post from user feed, and finish the activity
                                        Toast.makeText(getApplicationContext(), "You've successfully requested the item!", Toast.LENGTH_SHORT).show();
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
                                        finish();
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
}
