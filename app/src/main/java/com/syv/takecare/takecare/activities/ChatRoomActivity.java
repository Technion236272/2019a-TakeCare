package com.syv.takecare.takecare.activities;

import android.content.Intent;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.Map;

public class ChatRoomActivity extends TakeCareActivity {
    private FloatingActionButton sendButton;
    private EditText userInput;
    private FirestoreRecyclerAdapter<ChatMessageInformation, MessagesHolder> adapter;
    private String chatMode;
    private String chatId;
    private String otherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        sendButton = findViewById(R.id.send_button);
        userInput = findViewById(R.id.user_input_text);

        Intent creationIntent = getIntent();
        chatMode = creationIntent.getStringExtra("CHAT_MODE");
        chatId = creationIntent.getStringExtra("CHAT_ID");
        otherId = creationIntent.getStringExtra("OTHER_ID");


        Query query = db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<ChatMessageInformation> response = new FirestoreRecyclerOptions.Builder<ChatMessageInformation>()
                .setQuery(query, ChatMessageInformation.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ChatMessageInformation, MessagesHolder>(response){

            @NonNull
            @Override
            public MessagesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_holder, viewGroup, false);
                return new MessagesHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessagesHolder holder, int position, @NonNull ChatMessageInformation model) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Timestamp time = model.getTimestamp();

                if(model.getSender().equals(user.getUid())){
                    holder.userText.setText(model.getMessage());
                    holder.userText.setVisibility(View.VISIBLE);
                    holder.userTime.setText(sdf.format(time.toDate()));
                    holder.userTime.setVisibility(View.VISIBLE);
                } else {
                    holder.otherText.setText(model.getMessage());
                    holder.otherText.setVisibility(View.VISIBLE);
                    holder.userTime.setText(sdf.format(time.toDate()));
                    holder.userTime.setVisibility(View.VISIBLE);
                }
            }

        };

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference documentRef = db.collection("chats").document(chatId).collection("messages").document();
                Map<String, Object> chatInfo = new HashMap<String, Object>();
                chatInfo.put("sender",user.getUid());
                chatInfo.put("receiver", otherId);
                String message = userInput.getText().toString();
                chatInfo.put("message", message);
                FieldValue timestamp = FieldValue.serverTimestamp();
                chatInfo.put("timestamp", timestamp);
                documentRef.set(chatInfo);
            }
        });

    }
}
