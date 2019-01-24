package com.syv.takecare.takecare.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.ChatCardHolder;
import com.syv.takecare.takecare.POJOs.ChatCardInformation;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.ChatRoomActivity;

import java.text.SimpleDateFormat;

public class TakerMessagesFragment extends MessagingBaseFragment {

    private static final String TAG = "TakeCare/TakerMessages";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taker_messages, container, false);
        initializeFragment(view);
        restoreInstanceState(savedInstanceState);
        return view;
    }

    @Override
    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        Query query = db.collection("chats")
                .whereEqualTo("taker", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatCardInformation> response = new FirestoreRecyclerOptions.Builder<ChatCardInformation>()
                .setQuery(query, ChatCardInformation.class)
                .build();

        return new FirestoreRecyclerAdapter<ChatCardInformation, ChatCardHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatCardHolder holder, final int position, @NonNull final ChatCardInformation model) {
                Log.d(TAG, "onBindViewHolder: Starting.\nmodel: " + model);
                holder.itemRoot.setVisibility(View.VISIBLE);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                holder.title.setText(model.getTitle());
                holder.itemPhoto.setBackgroundResource(R.drawable.requested_round_feed_picture_frame);
                holder.userPhoto.setBackgroundResource(R.drawable.requested_round_picture_frame_no_border);
                holder.itemRoot.setBackgroundColor(getResources().getColor(R.color.colorAccentLite));

                if (model.getItemPhoto() == null && model.getCategory() != null) {
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
                } else if (getActivity() != null && model.getCategory() != null) {
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
