package com.syv.takecare.takecare.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.lang.String;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.syv.takecare.takecare.POJOs.ChatCardHolder;
import com.syv.takecare.takecare.POJOs.ChatCardInformation;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.ChatRoomActivity;
import com.syv.takecare.takecare.customViews.FeedRecyclerView;

import java.text.SimpleDateFormat;
import java.util.Map;

public class MessagingBaseFragment extends Fragment {

    private static final String TAG = "TakeCare/MessagingFrag";
    protected static final String RECYCLER_STATE_KEY = "KEY_RECYCLER_STATE";
    public static final String CHAT_ID = "CHAT_ID";
    public static final String CHAT_MODE = "CHAT_MODE";
    public static final String ITEM_ID = "ITEM_ID";
    public static final String OTHER_ID = "OTHER_ID";

    protected FeedRecyclerView recyclerView;
    protected FirestoreRecyclerAdapter adapter;
    protected int orientation;
    protected Parcelable listState;
    protected View emptyChatView;
    protected FirebaseFirestore db;
    protected FirebaseAuth auth;
    protected FirebaseUser user;
    private View view;
    protected int position = 0;

    protected void initializeFragment(View view) {
        this.view = view;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        emptyChatView = view.findViewById(R.id.chat_lobby_empty_feed_view);
        orientation = getResources().getConfiguration().orientation;
        setUpRecyclerView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putParcelable(RECYCLER_STATE_KEY, listState);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void restoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(RECYCLER_STATE_KEY);
            if (listState != null) {
                Log.d(TAG, "onCreate: restoring list state");
                try {
                    recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                } catch (NullPointerException e) {
                    Log.d(TAG, "Layout manager wasn't set on recycler view");
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: setting layout manager for the current orientation");
        recyclerView = view.findViewById(R.id.chat_lobby_recycler_view);
        if (recyclerView == null)
            Log.w(TAG, "recyclerView is null");
        recyclerView.setEmptyView(emptyChatView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = setFirestoreRecyclerAdapter();

        adapter.notifyDataSetChanged();
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        // Overridden function in the derived fragment class is called.
        // This is a dummy for implementation.
        return null;
    }

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter(FirestoreRecyclerOptions response) {
        return new FirestoreRecyclerAdapter<ChatCardInformation, ChatCardHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatCardHolder holder, final int position, @NonNull final ChatCardInformation model) {
                Log.d(TAG, "onBindViewHolder: Starting.\nmodel: " + model);
                holder.itemRoot.setVisibility(View.VISIBLE);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                holder.title.setText(model.getTitle());

                if (model.getItemPhoto() == null) {
                    Bitmap bitmap;
                    switch (model.getCategory()) {
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
                        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, 128, 128, true);
                        holder.itemPhoto.setImageBitmap(bitmapScaled);
                        holder.itemPhoto.setScaleType(ImageView.ScaleType.CENTER);
                    }
                } else if (getActivity() != null) {
                    Glide.with(getActivity().getApplicationContext())
                            .load(model.getItemPhoto())
                            .apply(requestOptions)
                            .into(holder.itemPhoto);
                }

                final boolean isCurrentUserGiver = model.getGiver().equals(user.getUid());

                String otherUserName;
                String otherUserPhoto;
                if (isCurrentUserGiver) {
                    otherUserName = model.getTakerName();
                    otherUserPhoto = model.getTakerPhoto();
                } else {
                    otherUserName = model.getGiverName();
                    otherUserPhoto = model.getGiverPhoto();
                }
                holder.user.setText(otherUserName);
                try {
                    Glide.with(getActivity().getApplicationContext())
                            .load(otherUserPhoto)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.userPhoto);
                } catch (NullPointerException e) {
                    Log.d(TAG, "Failed to load user photo");
                }
                Timestamp timestamp = model.getTimestamp();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                holder.timestamp.setText(sdf.format(timestamp.toDate()));

                holder.itemRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String otherID = isCurrentUserGiver ? model.getTaker() : model.getGiver();
                        String chatMode = isCurrentUserGiver ? "giver" : "taker";

                        Intent intent = new Intent(getContext(), ChatRoomActivity.class);
                        intent.putExtra(CHAT_ID, model.getChat());
                        intent.putExtra(CHAT_MODE, chatMode);
                        intent.putExtra(OTHER_ID, otherID);
                        intent.putExtra(ITEM_ID, model.getItem());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public ChatCardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_card_holder, viewGroup, false);
                return new ChatCardHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (position == 0 && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.scrollToPosition(0);
                }
            }
        };
    }

}
