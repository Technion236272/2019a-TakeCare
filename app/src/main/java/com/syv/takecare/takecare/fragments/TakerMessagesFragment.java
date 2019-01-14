package com.syv.takecare.takecare.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.ChatCardInformation;
import com.syv.takecare.takecare.R;

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

        return super.setFirestoreRecyclerAdapter(response);
    }
}
