package com.syv.takecare.takecare.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.syv.takecare.takecare.R;

import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForCategorySharesBadgeEligibility;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForLikesBadgeEligibility;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.checkForSharesBadgeEligibility;

public class UserProfileFragment extends DialogFragment {

    private static final String TAG = "TakeCare/ProfileFrag";

    private static final String UID_KEY = "PROFILE_USER";

    private String user;
    private View inflatedView;

    private View root;
    private ImageView pictureView;
    private TextView ratingView;
    private TextView nameView;
    private TextView descriptionView;
    private ProgressBar picturePB;

    public static UserProfileFragment newInstance(String uid) {
        UserProfileFragment f = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(UID_KEY, uid);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getString(UID_KEY) != null) {
            user = getArguments().getString(UID_KEY);
            Log.d(TAG, "Fetched user: " + user);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // We want the dialog to be dismissed when the user clicks outside of inflatedView
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initWidgets();
        descriptionView.setMovementMethod(new ScrollingMovementMethod());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {

                        String profilePicture = document.getString("profilePicture");
                        String name = document.getString("name");
                        String description = document.getString("description");
                        Long likes = document.getLong("likes");
                        Long totalGivenItems = document.getLong("totalGivenItems");
                        Long inPersonCount = document.getLong("inPersonCount");
                        Long giveawayCount = document.getLong("giveawayCount");
                        Long raceCount = document.getLong("raceCount");

                        if (profilePicture != null && getActivity() != null) {
                            Glide.with(getActivity().getApplicationContext())
                                    .load(profilePicture)
                                    .apply(RequestOptions.circleCropTransform())
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            if (getActivity() != null) {
                                                Toast.makeText(getActivity().getApplicationContext(),
                                                        "Error loading user\'s profile", Toast.LENGTH_LONG).show();
                                            }
                                            picturePB.setVisibility(View.GONE);
                                            dismiss();
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            root.setVisibility(View.VISIBLE);
                                            picturePB.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(pictureView);
                        }

                        if (name != null) {
                            nameView.setText(name);
                        }

                        if (description != null) {
                            descriptionView.setText(description);
                            descriptionView.setVisibility(View.VISIBLE);
                        }

                        if (likes != null) {
                            ratingView.setText(String.valueOf(likes));
                        } else {
                            ratingView.setText("0");
                        }

                        checkForLikesBadgeEligibility((ImageView)inflatedView.findViewById(R.id.likes_badge), likes);
                        checkForSharesBadgeEligibility((ImageView)inflatedView.findViewById(R.id.shares_badge), totalGivenItems);
                        checkForCategorySharesBadgeEligibility((ImageView)inflatedView.findViewById(R.id.in_person_badge), "In Person", inPersonCount);
                        checkForCategorySharesBadgeEligibility((ImageView)inflatedView.findViewById(R.id.giveaway_badge),"Giveaway", giveawayCount);
                        checkForCategorySharesBadgeEligibility((ImageView)inflatedView.findViewById(R.id.race_badge), "Race", raceCount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Error loading user's profile", Toast.LENGTH_LONG).show();
                        }
                        picturePB.setVisibility(View.GONE);
                        dismiss();
                    }
                });

        return inflatedView;
    }

    private void initWidgets() {
        root = inflatedView.findViewById(R.id.frag_user_profile_root);
        pictureView = inflatedView.findViewById(R.id.frag_profile_pic);
        ratingView = inflatedView.findViewById(R.id.frag_likes_counter);
        nameView = inflatedView.findViewById(R.id.frag_user_name);
        descriptionView = inflatedView.findViewById(R.id.frag_about);
        picturePB = inflatedView.findViewById(R.id.frag_profile_pic_progress_bar);
    }
}
