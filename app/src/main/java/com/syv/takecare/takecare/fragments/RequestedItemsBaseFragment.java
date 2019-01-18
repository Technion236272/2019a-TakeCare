package com.syv.takecare.takecare.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.syv.takecare.takecare.customViews.FeedRecyclerView;
import com.syv.takecare.takecare.activities.ItemInfoActivity;
import com.syv.takecare.takecare.POJOs.*;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.POJOs.RequestedItemsInformation;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RequestedItemsBaseFragment extends Fragment {
    private static final String TAG = "RequestedItemsBaseFrag";
    private static final int LIST_JUMP_THRESHOLD = 4;
    private static final int ICON_FILL_ITERATIONS = 12;
    private static final int ICON_FILL_DURATION = 200;
    private static final int ICON_ACTIVATED_DURATION = 400;
    private static final String RECYCLER_STATE_POSITION_KEY = "RECYCLER POSITION";
    protected static final String EXTRA_ITEM_ID = "Item Id";

    private ReentrantLock iconLock = new ReentrantLock();
    private AppCompatButton jumpButton;
    protected FeedRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    protected int position = 0;
    protected int orientation;
    private int absolutePosition;
    private View view;


    public void initializeFragment(View view) {
        this.view = view;
        jumpButton = view.findViewById(R.id.requested_items_jump_button);
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: writing position " + absolutePosition);
        savedInstanceState.putInt(RECYCLER_STATE_POSITION_KEY, absolutePosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {// Converted from onRestoreInstanceState
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: started");
        if (savedInstanceState != null && savedInstanceState.containsKey(RECYCLER_STATE_POSITION_KEY)) {
            absolutePosition = savedInstanceState.getInt(RECYCLER_STATE_POSITION_KEY);
            Log.d(TAG, "onActivityCreated: fetched position: " + absolutePosition);
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
        recyclerView = view.findViewById(R.id.requested_feed_list);
        if (recyclerView == null)
            Log.w(TAG, "recyclerView is null");
        View emptyFeedView = view.findViewById(R.id.requested_items_empty_feed_view);
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

        adapter = setFirestoreRecyclerAdapter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePosition();
                } else {
                    jumpButton.setVisibility(View.GONE);
                }
                absolutePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            }
        });

        adapter.notifyDataSetChanged();
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    protected void setUpItemInfo(final ItemsViewHolder holder, final DocumentSnapshot documentSnapshot) {
        holder.itemTitle.setText(documentSnapshot.getString("title"));
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
        try {
            Glide.with(getActivity().getApplicationContext())
                    .load(documentSnapshot.getString("photo"))
                    .apply(requestOptions)
                    .into(holder.itemPhoto);
        } catch (NullPointerException e) {
            Log.d(TAG, "could not load picture");
        }

        // category selection
        int categoryId;
        switch (documentSnapshot.getString("category")) {
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
        switch (documentSnapshot.getString("pickupMethod")) {
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

        holder.itemPublisher.setText(documentSnapshot.getString("userName"));
        if (documentSnapshot.getString("userProfilePicture") != null) {
            if (getActivity() == null) {
                return;
            }
            Glide.with(getActivity().getApplicationContext())
                    .load(documentSnapshot.getString("userProfilePicture"))
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.profilePhoto);
        } else {
            holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
        }

        holder.itemCategory.setImageResource(categoryId);
        holder.itemPickupMethod.setImageResource(pickupMethodId);
        activateViewHolderIcons(holder, categoryId, pickupMethodId);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activateViewHolderIcons(final ItemsViewHolder holder, final int categoryId, final int pickupMethodId) {

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
                switch (categoryId) {
                    case R.drawable.ic_pizza_slice_purple:
                        str = "This item's category is food";
                        break;
                    case R.drawable.ic_book_purple:
                        str = "This item's category is study material";
                        break;
                    case R.drawable.ic_lamp_purple:
                        str = "This item's category is household objects";
                        break;
                    case R.drawable.ic_lost_and_found_purple:
                        str = "This item's category is lost&founds";
                        break;
                    case R.drawable.ic_car_purple:
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
                switch (pickupMethodId) {
                    case R.drawable.ic_in_person_purple:
                        str = "This item is available for pick-up in person";
                        break;
                    case R.drawable.ic_giveaway_purple:
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

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        // Overridden function in the derived fragment class is called.
        // This is a dummy for implementation.
        return null;
    }

    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter(FirestoreRecyclerOptions response, final int status) {
        return new FirestoreRecyclerAdapter<RequestedItemsInformation, ItemsViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, final int position, @NonNull final RequestedItemsInformation model) {
                Log.d(TAG, "onBindViewHolder: Starting.\nmodel: " + model);
                holder.itemRoot.setVisibility(View.INVISIBLE);

                model.getItemRef()
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                if (getActivity() == null) {
                                    return;
                                }

                                Map<String, Object> map = documentSnapshot.getData();
                                if (map == null || map.size() == 0) {
                                    // Item has just been deleted by the user, and this request will be deleted soon as well
                                    return;
                                }

                                switch (status) {
                                    case 0:
                                        Log.d(TAG, "card in position " + position + " is ACCEPTED");
                                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryLite));
                                        holder.itemTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                        break;
                                    case 1:
                                        Log.d(TAG, "card in position " + position + " is REQUESTED");
                                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorAccentLite));
                                        holder.itemTitle.setTextColor(getResources().getColor(R.color.colorAccent));
                                        break;
                                    case 2:
                                        Log.d(TAG, "card in position " + position + " is REJECTED");
                                        holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorRedLite));
                                        holder.itemTitle.setTextColor(Color.RED);
                                        ViewCompat.setBackgroundTintList(holder.itemCategory, getResources().getColorStateList(R.color.secondary_text));
                                        ViewCompat.setBackgroundTintList(holder.itemPickupMethod, getResources().getColorStateList(R.color.secondary_text));
                                        break;
                                }
                                setUpItemInfo(holder, documentSnapshot);
                                holder.itemRoot.setVisibility(View.VISIBLE);
                                holder.card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), ItemInfoActivity.class);
                                        intent.putExtra(Intent.EXTRA_UID, user.getUid());
                                        intent.putExtra(EXTRA_ITEM_ID, documentSnapshot.getString("id"));
//                                        String path = model.getItemRef().getPath();
//                                        intent.putExtra(EXTRA_ITEM_ID, path.replace("items/", ""));
                                        startActivity(intent);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "ERROR LOADING ITEM!");
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
