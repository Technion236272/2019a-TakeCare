package com.example.yuval.takecare;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        final FirebaseUser currentUser = auth.getCurrentUser();
        final FirebaseUser publisher;
        final String itemID = "ITEM ID"; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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
                            if (document.getString("photo") != null) {
                                Log.d(TAG, "Found item photo. Fetched picture url: "
                                        + Uri.parse(document.getString("photo")));
                                Glide.with(ItemInfoActivity.this)
                                        .load(document.getString("photo"))
                                        .into(itemImageView);
                            }
                            if (document.getString("description") != null) {
                                Log.d(TAG, "Found description. Writing: ");
                                itemDescriptionView.setText(document.getString("description"));
                            }
                            if (document.getString("publisher") != null) {
                                Log.d(TAG, "Found publisher. Fetched id: "
                                        + document.getString("publisher"));
                                fillPublisherInfo(document.getString("publisher"),
                                        uploaderNameView, uploaderProfilePictureView,
                                        uploaderRatingBar);
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
                        }
                        if (document.getString("rating") != null) {
                            Log.d(TAG, "Found publisher rating.");
                            long publisherRatingSum = document.getLong("rating");
                            long publisherRatingCount = document.getLong("ratingCount");
                            float publisherRating = publisherRatingSum/publisherRatingCount;
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
