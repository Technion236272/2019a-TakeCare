package com.syv.takecare.takecare;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.utilities.RequestedItemsInformation;

public class AcceptedRequestsFragment extends RequestedItemsBaseFragment {
    private static final String TAG = "AcceptedRequestsFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accepted_items, container, false);
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
                .whereEqualTo("requestStatus", 0)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<RequestedItemsInformation> response = new FirestoreRecyclerOptions.Builder<RequestedItemsInformation>()
                .setQuery(query, RequestedItemsInformation.class)
                .build();

        return super.setFirestoreRecyclerAdapter(response, 0);
    }
}
