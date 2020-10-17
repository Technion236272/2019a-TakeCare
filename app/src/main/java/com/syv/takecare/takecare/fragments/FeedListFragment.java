package com.syv.takecare.takecare.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.*;
import com.syv.takecare.takecare.activities.ItemInfoActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakerMenuActivity;
import com.syv.takecare.takecare.activities.UserProfileActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static android.view.View.VISIBLE;


public class FeedListFragment extends Fragment {
    private final static String TAG = "FeedListFragment";
    private static final int LIST_JUMP_THRESHOLD = 4;
    private static final int ICON_FILL_ITERATIONS = 12;
    private static final int ICON_FILL_DURATION = 200;
    private static final int ICON_ACTIVATED_DURATION = 400;
    private static final String RECYCLER_STATE_POSITION_KEY = "RECYCLER POSITION";
    private static final String RECYCLER_STATE_KEY = "KEY_RECYCLER_STATE";
    private static final int LOAD_FAVORITES_INTERVAL_WAIT_TIME = 200;
    private static final int TOAST_MSG_TAGS_MAX_LENGTH = 48;
    private static final String EXTRA_ITEM_ID = "Item Id";

    private ReentrantLock iconLock = new ReentrantLock();
    public RecyclerView recyclerView;
    private View emptyFeedView;

    private AppCompatButton jumpButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private int position = 0;

    private FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder> currentAdapter = null;

    private int orientation;
    private int absolutePosition;

    private HashSet<String> userKeywords = new HashSet<>();
    private boolean keywordsLoaded = false;
    private Parcelable listState;

    private int invisibleItemsCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starting");
        orientation = getResources().getConfiguration().orientation;
        View view = inflater.inflate(R.layout.fragment_feed_list, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = view.findViewById(R.id.taker_feed_list);
        emptyFeedView = view.findViewById(R.id.empty_feed_view);
        emptyFeedView.setVisibility(View.GONE);
        initUserKeywords();
        setUpRecyclerView();
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable("KEY_RECYCLER_STATE");
            if (listState != null) {
                Log.d(TAG, "onCreateView: restoring list state");
                recyclerView.getLayoutManager().onRestoreInstanceState(listState);
            }
            if (savedInstanceState.containsKey(RECYCLER_STATE_POSITION_KEY)) {
                this.absolutePosition = savedInstanceState.getInt(RECYCLER_STATE_POSITION_KEY);
                Log.d(TAG, "onCreateView: absolute position is: " + this.absolutePosition);
            }
        }
        jumpButton = view.findViewById(R.id.jump_button);
        jumpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
                jumpButton.setVisibility(View.GONE);
            }
        });

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            jumpButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getContext(), R.drawable.ic_arrow_back), null, null, null);
            jumpButton.setText(R.string.jump_to_top_button_landscape);
        } else {
            jumpButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getContext(), R.drawable.ic_arrow_up), null);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) jumpButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: writing position " + absolutePosition);
        savedInstanceState.putInt(RECYCLER_STATE_POSITION_KEY, absolutePosition);
        //savedInstanceState.putParcelable("ADAPTER", (Parcelable)currentAdapter);
        savedInstanceState.putParcelable(RECYCLER_STATE_KEY, listState);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: setting layout manager for the current orientation");
        try {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "Activity is null: " + e.getMessage());
            return;
        }
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        setUpAdapter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePosition();
                } else {
                    jumpButton.setVisibility(View.GONE);
                }
                try {
                    absolutePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == -1 ?
                            absolutePosition : ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Failed to find first visible item: activity is destroyed" + e.getMessage());
                }
            }
        });
    }

    private void setUpAdapter() {
        Log.d(TAG, "setUpAdapter: setting up adapter");

        if (getActivity() == null) {
            Log.d(TAG, "Activity is null");
            return;
        }

        String queryCategoriesFilter = ((TakerMenuActivity) getActivity()).getQueryCategoriesFilter();
        String queryPickupMethodFilter = ((TakerMenuActivity) getActivity()).getQueryPickupMethodFilter();
        String queryKeywordsFilter = ((TakerMenuActivity) getActivity()).getQueryKeywordsFilter();
        if (currentAdapter != null)
            currentAdapter.stopListening();

        Query query = db.collection("items")
                .whereEqualTo("displayStatus", true);

        if (queryCategoriesFilter != null) {
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter);
            query = query.whereEqualTo("category", queryCategoriesFilter);
        }
        if (queryPickupMethodFilter != null) {
            Log.d(TAG, "setUpAdapter: query has: pickup: " + queryPickupMethodFilter);
            query = query.whereEqualTo("pickupMethod", queryPickupMethodFilter);
        }
        if (queryKeywordsFilter != null && !queryKeywordsFilter.isEmpty()) {
            query = query.whereArrayContains("tags", queryKeywordsFilter);
        }
        query = query.whereEqualTo("displayStatus", true)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        final FirestoreRecyclerOptions<FeedCardInformation> response = new FirestoreRecyclerOptions.Builder<FeedCardInformation>()
                .setQuery(query, FeedCardInformation.class)
                .build();

        currentAdapter = new FirestoreRecyclerAdapter<FeedCardInformation, ItemsViewHolder>(response) {

            private int focusedItem = 0;

            @Override
            public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
                super.onAttachedToRecyclerView(recyclerView);

                // Handle key up and key down and attempt to move selection
                recyclerView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                        // Return false if scrolled to the bounds and allow focus to move off the list
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                return tryMoveSelection(lm, 1);
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                                return tryMoveSelection(lm, -1);
                            }
                        }

                        return false;
                    }
                });
            }

            private boolean tryMoveSelection(RecyclerView.LayoutManager layoutManager, int direction) {
                int tryFocusItem = focusedItem + direction;

                // If still within valid bounds, move the selection, notify to redraw, and scroll
                if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
                    notifyItemChanged(focusedItem);
                    focusedItem = tryFocusItem;
                    notifyItemChanged(focusedItem);
                    layoutManager.scrollToPosition(focusedItem);
                    return true;
                }
                return false;
            }

            private boolean insideSearchRadius(final GeoPoint itemGeoPoint, int position) {
                if (itemGeoPoint == null) {
                    Log.d(TAG, "GeoPoint is null at position " + position);
                    return !(((TakerMenuActivity) Objects.requireNonNull(getActivity())).hideNoLocationPostsEnabled());
                }
                if (!(((TakerMenuActivity) Objects.requireNonNull(getActivity())).isDistanceFilterEnabled())) return true;

                final float distanceRadius = ((TakerMenuActivity)getActivity()).getDistanceRadius();
                Log.d(TAG, "distanceRadius: " + distanceRadius);
                Location currentLocation = ((TakerMenuActivity)getActivity()).getCurrentLocation();
                if (currentLocation == null) {
                    Log.d(TAG, "Cannot fetch last known location");
                    return true;
                }
                Location itemLocation = new Location("itemLocation");
                itemLocation.setLatitude(itemGeoPoint.getLatitude());
                itemLocation.setLongitude(itemGeoPoint.getLongitude());
                return (currentLocation.distanceTo(itemLocation) / 1000) <= distanceRadius;
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, final int position, @NonNull final FeedCardInformation model) {
                if (!insideSearchRadius(model.getLocation(), position)) {
                    invisibleItemsCount++;
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    toggleVisibility();
                    return;
                }

                holder.setIsRecyclable(false);
                final String itemId = model.getItemId();
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

                activateViewHolderIcons(holder, model);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() != null)
                            if (((TakerMenuActivity)(getActivity())).isTutorialOn) return;
                        Intent intent = new Intent(getActivity().getApplicationContext(), ItemInfoActivity.class);
                        intent.putExtra(EXTRA_ITEM_ID, itemId);
                        intent.putExtra(Intent.EXTRA_UID, model.getPublisher());
                        startActivity(intent);
                    }
                });


                holder.itemView.setSelected(focusedItem == position);

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            int attempts = 20;
                            for (int i = 0; i < attempts; i++) {
                                if (keywordsLoaded) {
                                    // User keywords finished loading: safe to check favorites
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            checkFavoriteKeywords(holder, model);
                                        }
                                    });
                                    break;
                                }
                                try {
                                    // Still loading user keywords: wait and try again
                                    Thread.sleep(LOAD_FAVORITES_INTERVAL_WAIT_TIME);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }).start();

                if (!user.getUid().equals(model.getPublisher())) {
                    // Publisher is not the user, so user might want to see information about the publisher
                    holder.profilePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager fm = getFragmentManager();
                            UserProfileFragment dialogFragment =
                                    UserProfileFragment.newInstance(model.getPublisher());
                            dialogFragment.show(fm, null);
                        }
                    });
                } else {
                    // Publisher is the user: clicking on profile picture will open the profile page
                    holder.profilePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getActivity() != null) {
                                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
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
                toggleVisibility();
                if (position == 0 && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.scrollToPosition(0);
                }
            }
        };

        Log.d(TAG, "setUpAdapter: created adapter");
        currentAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(currentAdapter);
//        toggleVisibility();
        currentAdapter.startListening();
        Log.d(TAG, "setUpAdapter: done");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activateViewHolderIcons(final ItemsViewHolder holder, final FeedCardInformation model) {

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
                        str = getString(R.string.taker_card_hint_food);
                        break;
                    case "Study Material":
                        str = getString(R.string.taker_card_hint_study_material);
                        break;
                    case "Households":
                        str = getString(R.string.taker_card_hint_households);
                        break;
                    case "Lost & Found":
                        str = getString(R.string.taker_card_hint_lost_and_found);
                        break;
                    case "Hitchhikes":
                        str = getString(R.string.taker_card_hint_hitchhikes);
                        break;
                    default:
                        str = getString(R.string.taker_card_hint_other);
                        break;
                }

                try {
                    Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Activity is null: " + e.getMessage());
                }
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
                        str = getString(R.string.taker_card_hint_in_person);
                        break;
                    case "Giveaway":
                        str = getString(R.string.taker_card_hint_giveaway);
                        break;
                    default:
                        str = getString(R.string.taker_card_hint_race);
                        break;
                }

                try {
                    Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Activity is null: " + e.getMessage());
                }
            }
        });
    }

    private void updatePosition() {
        assert recyclerView.getLayoutManager() != null;
        position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Log.d(TAG, "onScrollStateChanged: POSITION IS: " + position);
        tryToggleJumpButton();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Starting.");
        currentAdapter.startListening();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "thread run: moving to " + absolutePosition);
                try {
                    recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                    position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Activity is null");
                }
            }
        }, 300);
        if (absolutePosition >= LIST_JUMP_THRESHOLD) {
            jumpButton.setVisibility(VISIBLE);
        } else {
            jumpButton.setVisibility(View.GONE);
        }
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        listState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onStop() {
        super.onStop();
        currentAdapter.stopListening();
    }

    public void toggleVisibility() {
        if (emptyFeedView != null) {
            //Make the emptyFeedView visible of the adapter has no items (feed is empty)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emptyFeedView.setVisibility(
                            (recyclerView.getAdapter() == null || (recyclerView.getAdapter().getItemCount() - invisibleItemsCount) == 0) ?
                                    VISIBLE : View.GONE);
                    //The list itself is set to be invisible if there are no items, in order to display emptyFeedView in its stead
                    recyclerView.setVisibility(
                            (recyclerView.getAdapter() == null || (recyclerView.getAdapter().getItemCount() - invisibleItemsCount) == 0) ?
                                    View.GONE : View.VISIBLE);
                }
            }, 300);
        }
    }

    private void tryToggleJumpButton() {
        if (position >= LIST_JUMP_THRESHOLD) {
            jumpButton.setVisibility(VISIBLE);
            Log.d(TAG, "tryToggleJumpButton: jump button is visible");
        } else {
            jumpButton.setVisibility(View.GONE);
        }
    }

    private void showBlockAlertMessage(final Spanned msg, final String itemId, final int cause) {
        try {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getActivity().getApplicationContext());
            builder.setTitle("Block Item")
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hideItem(itemId);
                            switch (cause) {
                                //TODO: add
                                case R.id.report_inappropriate:
                                    break;
                                case R.id.report_no_fit:
                                    break;
                                case R.id.report_spam:
                                    break;
                                default:
                                    // User hides item - do nothing
                                    break;
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .show();
        } catch (NullPointerException e) {
            Log.d(TAG, "Activity is null: " + e.getMessage());
        }
    }

    private void hideItem(final String itemId) {
        if (user == null) {
            return;
        }
        // Atomic operation - no need for transactions!
        Log.d(TAG, "hideItem: hiding item from user");
        db.collection("items").document(itemId)
                .update("hideFrom", FieldValue.arrayUnion(user.getUid()));
    }

    private void initUserKeywords() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "found user's favorite keywords");
                        if (documentSnapshot.get("tags") == null) {
                            keywordsLoaded = true;
                            userKeywords.clear();
                            return;
                        }
                        userKeywords = new HashSet<>((List<String>) documentSnapshot.get("tags"));
                        keywordsLoaded = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "error trying to find favorite keywords in card");
                    }
                });
    }

    private void checkFavoriteKeywords(final ItemsViewHolder holder, final FeedCardInformation model) {
        if (model.getTags() == null) {
            return;
        }

        Set<String> intersection = new HashSet<>();
        for (String keyword : model.getTags()) {
            if (userKeywords.contains(keyword)) {
                intersection.add(keyword);
            }
        }

        if (!intersection.isEmpty()) {
            // Item is wish-listed by the user!
            customizeFavoriteCard(holder, intersection);
        }
    }

    private void customizeFavoriteCard(final ItemsViewHolder holder, final Set<String> keywords) {
        holder.itemFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: favorite icon clicked");
                int keywordsAmount = keywords.size();
                String msg = "Associated tags: ";
                int tagsTextLength = 0;
                StringBuilder tagsBuilder = new StringBuilder();
                for (String keyword : keywords) {
                    if (tagsTextLength + keyword.length() > TOAST_MSG_TAGS_MAX_LENGTH) {
                        break;
                    }
                    tagsTextLength -= keyword.length() + 2;
                    keywordsAmount--;
                    tagsBuilder.append(keyword)
                            .append(", ");
                }

                if (tagsBuilder.length() > 0) {
                    tagsBuilder.deleteCharAt(tagsBuilder.length() - 2);
                }

                msg += (tagsBuilder.toString());
                if (keywordsAmount > 0) {
                    msg += "and " + keywordsAmount + " more";
                }

                try {
                    Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Activity is null: " + e.getMessage());
                }
            }
        });

        holder.itemFavorite.setVisibility(View.VISIBLE);
        holder.itemFavorite.animate().alpha(0.9f);
    }
}
