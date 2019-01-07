package com.syv.takecare.takecare;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.syv.takecare.takecare.utilities.RequestedItemsInformation;

public class PendingRequestsFragment extends RequestedItemsBaseFragment {
    private static final String TAG = "PendingRequestsFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pending_requests, container, false);
        super.initializeFragment(view);
        return view;
    }

    @Override
    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        Query query = db.collection("users").document(user.getUid()).collection("requestedItems")
                .orderBy("requestStatus", Query.Direction.ASCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("requestStatus", 1);

        FirestoreRecyclerOptions<RequestedItemsInformation> response = new FirestoreRecyclerOptions.Builder<RequestedItemsInformation>()
                .setQuery(query, RequestedItemsInformation.class)
                .build();

        return new FirestoreRecyclerAdapter<RequestedItemsInformation, ItemsViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemsViewHolder holder, final int position, @NonNull final RequestedItemsInformation model) {
                Log.d(TAG, "model: " + model);
                holder.itemRoot.setVisibility(View.INVISIBLE);

                model.getItemRef()
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, "card in position " + position + " is REQUESTED");
                                holder.card.setCardBackgroundColor(getResources().getColor(R.color.colorAccentLite));
                                holder.itemTitle.setTextColor(getResources().getColor(R.color.colorAccent));
                                PendingRequestsFragment.super.setUpItemInfo(holder, documentSnapshot);
                                holder.itemRoot.setVisibility(View.VISIBLE);
                                holder.card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), ItemInfoActivity.class);
                                        intent.putExtra(Intent.EXTRA_UID, user.getUid());
                                        String path = model.getItemRef().getPath();
                                        intent.putExtra(EXTRA_ITEM_ID, path.replace("items/", ""));
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
                View view = null;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card_carousel, viewGroup, false);

                } else {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.taker_feed_card, viewGroup, false);
                }
                return new ItemsViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
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
