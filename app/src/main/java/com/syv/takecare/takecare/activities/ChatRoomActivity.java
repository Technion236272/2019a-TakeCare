package com.syv.takecare.takecare.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.syv.takecare.takecare.POJOs.ChatMessageInformation;
import com.syv.takecare.takecare.POJOs.MessagesHolder;
import com.syv.takecare.takecare.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatRoomActivity extends TakeCareActivity {
    private static final String TAG = "TakeCare/ChatRoom";

    private ConstraintLayout root;
    private FloatingActionButton sendButton;
    private Toolbar toolbar;
    private EditText userInput;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<ChatMessageInformation, MessagesHolder> adapter;
    private String chatMode;
    private String chatId;
    private String otherId;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean redirectedFromItemInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        root = findViewById(R.id.chat_root);
        sendButton = findViewById(R.id.send_button);
        toolbar = findViewById(R.id.chat_toolbar);
        setToolbar(toolbar);
        userInput = findViewById(R.id.user_input_text);
        recyclerView = findViewById(R.id.chat_recycler_view);
        Intent creationIntent = getIntent();
        chatMode = creationIntent.getStringExtra("CHAT_MODE");
        chatId = creationIntent.getStringExtra("CHAT_ID");
        otherId = creationIntent.getStringExtra("OTHER_ID");
        redirectedFromItemInfo = creationIntent.hasExtra("IS_REFERENCED_FROM_ITEM_INFO");

        Log.d(TAG, "chat activity referenced from ItemInfoActivity: " + redirectedFromItemInfo);

        Query query = db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageInformation> response = new FirestoreRecyclerOptions.Builder<ChatMessageInformation>()
                .setQuery(query, ChatMessageInformation.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ChatMessageInformation, MessagesHolder>(response) {

            @NonNull
            @Override
            public MessagesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_holder, viewGroup, false);
                return new MessagesHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessagesHolder holder, int position, @NonNull ChatMessageInformation model) {
                Log.d("YUVAL", "onBindViewHolder: YUVAL");
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Timestamp time = model.getTimestamp();
                if(time == null){
                    return;
                }
                if (model.getSender().equals(user.getUid())) {
                    holder.userTime.setText(sdf.format(time.toDate()));
                    holder.userText.setText(model.getMessage());
                    holder.userText.setVisibility(View.VISIBLE);
                    holder.userTime.setVisibility(View.VISIBLE);
                } else {
                    holder.otherTime.setText(sdf.format(time.toDate()));
                    holder.otherText.setText(model.getMessage());
                    holder.otherText.setVisibility(View.VISIBLE);
                    holder.otherTime.setVisibility(View.VISIBLE);
                }
            }
        };
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("YUVAL", "YUVAL chat: " + chatId);
                Log.d("YUVAL", "YUVAL other ID " + otherId);
                Log.d("YUVAL", "onClick: mode " + chatMode);
                String messageToSend = userInput.getText().toString();
                if(messageToSend.isEmpty()){
                    return;
                }
                new UploadMessage().execute(messageToSend);
                userInput.setText("");
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        recyclerView.setItemViewCacheSize(40);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.scrollToPosition(0);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        if (redirectedFromItemInfo) {
            Intent intent = new Intent(this, ChatLobbyActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UploadMessage extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            lock.lock();
            String toUpload = strings[0];
            final DocumentReference documentRef = db.collection("chats").document(chatId).collection("messages").document();
            Map<String, Object> chatInfo = new HashMap<String, Object>();
            chatInfo.put("sender", user.getUid());
            chatInfo.put("receiver", otherId);
            chatInfo.put("message", toUpload);
            FieldValue timestamp = FieldValue.serverTimestamp();
            chatInfo.put("timestamp", timestamp);
            documentRef.set(chatInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "message sent successfully!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "error uploading message: " + e.getMessage());
                        }
                    });
            lock.unlock();
            return null;
        }

    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}
