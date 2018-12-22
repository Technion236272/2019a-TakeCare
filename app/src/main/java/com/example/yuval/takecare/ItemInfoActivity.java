package com.example.yuval.takecare;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ItemInfoActivity extends AppCompatActivity {

    private final static String TAG = "ItemInfoActivity";

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

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.item_info_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
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
        final String itemID = intent.getStringExtra(Intent.EXTRA_UID);


        phoneCard.setVisibility(View.GONE);     // FOR NOW

        if (currentUser != null) {
            DocumentReference docRef = db.collection("items").document(itemID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                                GlideApp.with(ItemInfoActivity.this)
                                        .load(document.getString("photo"))
                                        .apply(new RequestOptions())
                                        .centerCrop()
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
                                locationCard.setVisibility(View.GONE);
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

    void fillPublisherInfo(String publisher, final TextView uploaderNameView, final ImageView uploaderProfilePictureView,
                           final RatingBar uploaderRatingBar) {
        final DocumentReference docRef = db.collection("users").document(publisher);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                            long publisherRatingSum = document.getLong("rating");
                            long publisherRatingCount = document.getLong("ratingCount");
                            float publisherRating = (publisherRatingCount == 0) ? 0 :
                                    publisherRatingSum/publisherRatingCount;
                            uploaderRatingBar.setRating(publisherRating);
                        }
                    } else {
                        Log.d(TAG, "No such document - item publisher");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
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
}
