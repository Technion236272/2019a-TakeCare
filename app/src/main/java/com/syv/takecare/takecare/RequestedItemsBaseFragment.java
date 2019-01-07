package com.syv.takecare.takecare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CenterScrollListener;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.syv.takecare.takecare.utilities.RequestedItemsInformation;

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
    private StorageReference storage;
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
        storage = FirebaseStorage.getInstance().getReference();

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
        } else{
            jumpButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getContext(), R.drawable.ic_arrow_up), null);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        recyclerView = (FeedRecyclerView) view.findViewById(R.id.requested_feed_list);
        if (recyclerView == null)
            Log.w(TAG, "recyclerView is null");
        View emptyFeedView = view.findViewById(R.id.requested_items_empty_feed_view);
        Log.d(TAG, "setUpRecyclerView: setting layout manager for the current orientation");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false));
            recyclerView.addOnScrollListener(new CenterScrollListener());
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setEmptyView(emptyFeedView);

        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        adapter = setFirestoreRecyclerAdapter();

        recyclerView.setEmptyView(emptyFeedView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePosition();
                } else {
                    jumpButton.setVisibility(View.GONE);
                }
                absolutePosition = (orientation == Configuration.ORIENTATION_LANDSCAPE) ?
                        ((CarouselLayoutManager) recyclerView.getLayoutManager()).getCenterItemPosition() :
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .findFirstVisibleItemPosition();
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
        Glide.with(holder.card)
                .load(documentSnapshot.getString("photo"))
                .apply(requestOptions)
                .into(holder.itemPhoto);

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

        holder.profilePhoto.setImageResource(R.drawable.ic_user_purple);
        db.collection("users").document(documentSnapshot.getString("publisher"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "Found the user: " + documentSnapshot);
                        holder.itemPublisher.setText(documentSnapshot.getString("name"));
                        if (documentSnapshot.getString("profilePicture") != null) {
                            Glide.with(holder.card)
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

    private void updatePosition() {
        assert recyclerView.getLayoutManager() != null;
        position = (orientation == Configuration.ORIENTATION_LANDSCAPE) ?
                ((CarouselLayoutManager) recyclerView.getLayoutManager()).getCenterItemPosition() :
                ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();

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
