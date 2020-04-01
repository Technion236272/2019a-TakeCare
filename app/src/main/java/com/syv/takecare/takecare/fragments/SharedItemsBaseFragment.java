package com.syv.takecare.takecare.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.syv.takecare.takecare.POJOs.*;
import com.syv.takecare.takecare.activities.ItemInfoActivity;
import com.syv.takecare.takecare.customViews.FeedRecyclerView;
import com.syv.takecare.takecare.R;

import java.util.concurrent.locks.ReentrantLock;

public class SharedItemsBaseFragment extends Fragment {
    private static final String TAG = "SharedItemsBaseFragment";
    private static final int LIST_JUMP_THRESHOLD = 4;
    private static final int ICON_FILL_ITERATIONS = 12;
    private static final int ICON_FILL_DURATION = 200;
    private static final int ICON_ACTIVATED_DURATION = 400;
    private static final String RECYCLER_STATE_POSITION_KEY = "RECYCLER POSITION";
    protected static final String EXTRA_ITEM_ID = "Item Id";

    ReentrantLock iconLock = new ReentrantLock();
    protected FeedRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    protected int position = 0;
    private AppCompatButton jumpButton;
    protected int orientation;
    private int absolutePosition;
    private View view;


    public void initializeFragment(View view) {
        this.view = view;
        jumpButton = view.findViewById(R.id.shared_items_jump_button);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        orientation = getResources().getConfiguration().orientation;

        setUpRecyclerView();

        jumpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onJumpClick(v);
            }
        });
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            jumpButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getContext(), R.drawable.ic_arrow_back), null, null, null);
            jumpButton.setText(R.string.jump_to_top_button_landscape);
        } else{
            jumpButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getContext(), R.drawable.ic_arrow_up), null);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) jumpButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: writing position " + absolutePosition);
        savedInstanceState.putInt(RECYCLER_STATE_POSITION_KEY, absolutePosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: started");
        if (savedInstanceState != null && savedInstanceState.containsKey(RECYCLER_STATE_POSITION_KEY)) {
            absolutePosition = savedInstanceState.getInt(RECYCLER_STATE_POSITION_KEY);
            Log.d(TAG, "onRestoreInstanceState: fetched position: " + absolutePosition);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "thread run: moving to " + absolutePosition);
                    recyclerView.scrollToPosition(absolutePosition);
                }
            }, 300);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity() != null)
                    getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        recyclerView = view.findViewById(R.id.shared_feed_list);
        if (recyclerView == null)
            Log.w(TAG, "recyclerView is null");
        View emptyFeedView = view.findViewById(R.id.shared_items_empty_feed_view);
        Log.d(TAG, "setUpRecyclerView: setting layout manager for the current orientation");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        updatePosition();
                    } else {
                        jumpButton.setVisibility(View.GONE);
                    }
                    absolutePosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition();
                }
            });
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setEmptyView(emptyFeedView);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        adapter = setFirestoreRecyclerAdapter();

        adapter.notifyDataSetChanged();
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        Log.d(TAG, "setFirestoreRecyclerAdapter: Dummy is initiated");
        // Overridden function in the derived fragment class is called.
        // This is a dummy for implementation.
        return null;
    }

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter(FirestoreRecyclerOptions response) {
        Log.d(TAG, "setFirestoreRecyclerAdapter: Starting.");
        // Returns the appropriate Recycler Adapter using the passed query wrapper.
        // Called from the child class (with its appropriate query)
        return new FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, int position, @NonNull FeedCardInformation model) {
                Log.d(TAG, "model " + model.getPhoto());
                final String itemId = model.getItemId();

                switch (model.getStatus()) {
                    case 0:
                        Log.d(TAG, "card in position " + position + " is REQUESTED");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorAccentLite));
//                        holder.itemTitle.setTextColor(getResources().getColor(R.color.colorAccent));
                        holder.itemPhoto.setBackgroundResource(R.drawable.requested_round_feed_picture_frame);
                        break;
                    case 2:
                        Log.d(TAG, "card in position " + position + " is TAKEN");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryLite));
                        holder.itemTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        holder.itemPhoto.setBackgroundResource(R.drawable.accepted_round_feed_picture_frame);
                        break;
                    case 3:
                        Log.d(TAG, "card in position " + position + " is TIMED OUT");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorRedLite));
                        holder.itemTitle.setTextColor(Color.RED);
                        holder.itemPhoto.setBackgroundResource(R.drawable.expired_round_feed_picture_frame);
                        ViewCompat.setBackgroundTintList(holder.itemCategory, getResources().getColorStateList(R.color.secondary_text));
                        ViewCompat.setBackgroundTintList(holder.itemPickupMethod, getResources().getColorStateList(R.color.secondary_text));
                        break;
                    default:
                        // status == 1
                        Log.d(TAG, "card in position " + position + " is AVAILABLE");
                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorCardDefault));
                }

                holder.itemTitle.setText(model.getTitle());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                if (model.getPhoto() == null) {
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
                        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                        holder.itemPhoto.setImageBitmap(bitmapScaled);
                        holder.itemPhoto.setScaleType(ImageView.ScaleType.CENTER);
                    }
                } else if (getActivity() != null) {
                    Glide.with(getActivity().getApplicationContext())
                            .load(model.getPhoto())
                            .apply(requestOptions)
                            .into(holder.itemPhoto);
                }

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

                holder.itemPublisher.setText(model.getUserName());
                if (model.getUserProfilePicture() != null) {
                    if (getActivity() == null) {
                        return;
                    }
                    Glide.with(getActivity().getApplicationContext())
                            .load(model.getUserProfilePicture())
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.profilePhoto);
                } else {
                    holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
                }

                holder.itemCategory.setImageResource(categoryId);
                holder.itemPickupMethod.setImageResource(pickupMethodId);

                // Remove option to report own items

                activateViewHolderIcons(holder, model);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ItemInfoActivity.class);
                        intent.putExtra(EXTRA_ITEM_ID, itemId);
                        intent.putExtra(Intent.EXTRA_UID, user.getUid());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card_horizontal, viewGroup, false);

                } else {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card, viewGroup, false);
                }
                return new ItemsViewHolder(view);
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

    @SuppressLint("ClickableViewAccessibility")
    protected void activateViewHolderIcons(final ItemsViewHolder holder, final FeedCardInformation model) {


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

                Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
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

                Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePosition() {
        assert recyclerView.getLayoutManager() != null;
        position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

        Log.d(TAG, "onScrollStateChanged: POSITION IS: " + position);
        tryToggleJumpButton();
    }

    private void tryToggleJumpButton() {
        if (position >= LIST_JUMP_THRESHOLD) {
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
}
