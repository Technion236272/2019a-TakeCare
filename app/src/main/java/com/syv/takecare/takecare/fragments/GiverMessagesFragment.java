package com.syv.takecare.takecare.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.ChatCardInformation;
import com.syv.takecare.takecare.R;

public class GiverMessagesFragment extends MessagingBaseFragment {

    private static final String TAG = "TakeCare/GiverMessages";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_giver_messages, container, false);
        initializeFragment(view);
        restoreInstanceState(savedInstanceState);
        return view;
    }

    @Override
    protected FirestoreRecyclerAdapter setFirestoreRecyclerAdapter() {
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        Log.d(TAG, "my ID: " + user.getUid());
        Query query = db.collection("chats")
                .whereEqualTo("giver", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatCardInformation> response = new FirestoreRecyclerOptions.Builder<ChatCardInformation>()
                .setQuery(query, ChatCardInformation.class)
                .build();

        return super.setFirestoreRecyclerAdapter(response);
    }
}