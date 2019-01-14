package com.syv.takecare.takecare.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.*;
import com.syv.takecare.takecare.activities.ItemInfoActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakerMenuActivity;

import java.util.HashSet;
import java.util.List;
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
    private RecyclerView recyclerView;
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
        if (currentAdapter != null)
            currentAdapter.stopListening();

        // Default: no filters
        Query query = db.collection("items")
                .whereEqualTo("displayStatus", true)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        if (queryCategoriesFilter != null && queryPickupMethodFilter != null) {
            // Filter by categories and pickup method
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter + " pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("displayStatus", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (queryCategoriesFilter != null) {
            // Filter by categories
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("displayStatus", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (queryPickupMethodFilter != null) {
            // Filter by pickup method
            Log.d(TAG, "setUpAdapter: query has: pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("displayStatus", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }


        FirestoreRecyclerOptions<FeedCardInformation> response = new FirestoreRecyclerOptions.Builder<FeedCardInformation>()
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

            @SuppressLint("ClickableViewAccessibility")
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, final int position, @NonNull final FeedCardInformation model) {
                // Attempt to remove item from feed if reported by the user
                final String itemId = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                holder.itemTitle.setText(model.getTitle());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                if (model.getPhoto() == null) {
                    holder.itemPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    switch (model.getCategory()) {
                        case "Food":
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_pizza_slice_purple));
                            break;
                        case "Study Material":
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_book_purple));
                            break;
                        case "Households":
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_lamp_purple));
                            break;
                        case "Lost & Found":
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_lost_and_found_purple));
                            break;
                        case "Hitchhikes":
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_car_purple));
                            break;
                        default:
                            holder.itemPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_treasure_purple));
                            break;
                    }
                    holder.itemPhoto.setScaleType(ImageView.ScaleType.CENTER);
                } else {
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

                holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
                db.collection("users").document(model.getPublisher())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                holder.itemPublisher.setText(documentSnapshot.getString("name"));
                                if (documentSnapshot.getString("profilePicture") != null) {
                                    if (getActivity() == null) {
                                        return;
                                    }
                                    Glide.with(getActivity().getApplicationContext())
                                            .load(documentSnapshot.getString("profilePicture"))
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(holder.profilePhoto);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                holder.itemCategory.setImageResource(categoryId);
                holder.itemPickupMethod.setImageResource(pickupMethodId);

                activateViewHolderIcons(holder, model);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card_carousel, viewGroup, false);

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
                        str = "This item is available for pick-up in person";
                        break;
                    case "Giveaway":
                        str = "This item is available in a public giveaway";
                        break;
                    default:
                        str = "Race to get this item before anyone else!";
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
                } catch (NullPointerException e ) {
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
            emptyFeedView.setVisibility(
                    (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) ?
                            VISIBLE : View.GONE);
            //The list itself is set to be invisible if there are no items, in order to display emptyFeedView in its stead
            recyclerView.setVisibility(
                    (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) ?
                            View.GONE : View.VISIBLE);
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
