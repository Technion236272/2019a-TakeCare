package com.syv.takecare.takecare.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.ortiz.touchview.TouchImageView;
import com.syv.takecare.takecare.POJOs.ChatMessageInformation;
import com.syv.takecare.takecare.POJOs.MessagesHolder;
import com.syv.takecare.takecare.R;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ChatRoomActivity extends TakeCareActivity {
    private static final String TAG = "TakeCare/ChatRoom";
    // This is the minimum time that has to pass between new messages sounds
    private static final int MESSAGE_SOUND_INTERVAL = 2000;

    private RelativeLayout root;
    private ConstraintLayout chatLayout;
    private FloatingActionButton sendButton;
    private Toolbar toolbar;
    private TextView toolbarName;
    private AppCompatImageView toolbarImage;
    private Toolbar enlargedPhotoToolbar;
    private TextView enlargedPhotoToolbarTitle;
    private EditText userInput;
    private RecyclerView recyclerView;
//    private Button acceptButton;
    private FirestoreRecyclerAdapter<ChatMessageInformation, MessagesHolder> adapter;

    private String chatMode;
    private String itemId;
    private String chatId;
    private String otherId;
    private boolean redirectedFromItemInfo;
    private int initialMessagesAmount = Integer.MAX_VALUE;

    private final ReentrantLock lock = new ReentrantLock();

    private String itemPhotoURL = null;
    private String itemTitle = null;
    private RelativeLayout fullscreenImageContainer;
    private ImageView fullscreenImage;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private boolean isImageFullscreen;
    private View.OnClickListener minimizer = null;

    private Handler soundHandler = new Handler();

    // This task clears the sound block from the handler, allowing more new messages to play a sound
    private final Runnable soundTask = new Runnable() {
        @Override
        public void run() {
            soundHandler.removeMessages(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        root = findViewById(R.id.chat_root);
        chatLayout = findViewById(R.id.chat_layout);
        toolbar = findViewById(R.id.chat_toolbar);
        toolbarName = findViewById(R.id.toolbar_user_name);
        toolbarImage = findViewById(R.id.toolbar_item_picture);

        sendButton = findViewById(R.id.send_button);
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            sendButton.setRotation(180);
        }
        userInput = findViewById(R.id.user_input_text);
        recyclerView = findViewById(R.id.chat_recycler_view);
        //TODO: bring this back later
//        acceptButton = findViewById(R.id.accept_request_button_chat);

        enlargedPhotoToolbar = findViewById(R.id.enlarged_item_pic_toolbar);
        enlargedPhotoToolbarTitle = findViewById(R.id.enlarged_item_pic_toolbar_title);
        fullscreenImageContainer = findViewById(R.id.fullscreen_image_container);
        fullscreenImage = findViewById(R.id.item_image_fullscreen);
        fullscreenImageContainer.bringChildToFront(fullscreenImage);

        Intent creationIntent = getIntent();
        chatMode = creationIntent.getStringExtra("CHAT_MODE");
        chatId = creationIntent.getStringExtra("CHAT_ID");
        otherId = creationIntent.getStringExtra("OTHER_ID");
        itemId = creationIntent.getStringExtra("ITEM_ID");
        redirectedFromItemInfo = creationIntent.hasExtra("IS_NOT_REFERENCED_FROM_LOBBY");

        loadToolbarElements();

        getMessagesAmount();

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
            protected void onBindViewHolder(@NonNull MessagesHolder holder, int position, @NonNull final ChatMessageInformation model) {
                Log.d(TAG, "onBindViewHolder: started");
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Timestamp time = model.getTimestamp();
                if (time == null) {
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

                if (!soundHandler.hasMessages(0)) {
                    soundHandler.removeCallbacksAndMessages(null);
                    soundHandler.postDelayed(soundTask, MESSAGE_SOUND_INTERVAL);
                    tryToPlaySound(model.getSender().equals(user.getUid()), getItemCount());
                }
            }
        };
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "chat: " + chatId);
                Log.d(TAG, "other's ID " + otherId);
                Log.d(TAG, "onClick: mode " + chatMode);
                String messageToSend = userInput.getText().toString();
                if (messageToSend.replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", "")
                        .isEmpty()) {
                    Log.d(TAG, "empty message - do not send it");
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



    private boolean tryToPlaySound(boolean isSender, int count) {
        if (count > initialMessagesAmount) {
            Log.d(TAG, "new message detected");
            // Update the amount of already-acknowledged messages
            initialMessagesAmount = count;
            // Let the handler know about the successful sound invocation, so it can lock it for a set time
            soundHandler.sendEmptyMessage(0);

            if (isSender) {
                Log.d(TAG, "new message sent. playing sound");
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.message_sent_audio);
                mp.start();
                return true;
            } else {
                Log.d(TAG, "new message received. playing sound");
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.message_received_audio);
                mp.start();
                return true;
            }
        }
        return false;
    }

    private void getMessagesAmount() {
        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "fetching messages amount");
                        Long count = documentSnapshot.getLong("messagesCount");
                        if (count != null) {
                            initialMessagesAmount = count.intValue();
                            Log.d(TAG, "there are " + initialMessagesAmount + " messages in the chat room");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "failed to fetch message: " + e.getMessage());
                    }
                });
    }

    private void loadToolbarElements() {
        toolbar.setTitle("");
        setToolbar(toolbar);
//        db.collection("users").document(otherId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Log.d(TAG, "loading app bar's name");
//                        String name = documentSnapshot.getString("name");
//                        if (name != null) {
//                            toolbarName.setText(name);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // We don't want the app bar to stay empty: exit the activity
//                        Log.d(TAG, "error: couldn\'t load app bar elements");
//                        Toast.makeText(ChatRoomActivity.this, R.string.chat_load_fail,
//                                Toast.LENGTH_LONG).show();
//                        onBackPressed();
//                    }
//                });
        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "loading app bar's name");
                        String name;
                        if ("taker".equals(chatMode)) {
                            name = documentSnapshot.getString("giverName");
                        } else { // giver
                            name = documentSnapshot.getString("takerName");
                        }
                        if (name != null) {
                            toolbarName.setText(name);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // We don't want the app bar to stay empty: exit the activity
                        Log.d(TAG, "error: couldn't load app bar elements");
                        Toast.makeText(ChatRoomActivity.this, R.string.chat_load_fail,
                                Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
              });

//        db.collection("items").document(itemId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Log.d(TAG, "onSuccess: ");
//
//                        itemPhotoURL = documentSnapshot.getString("photo");
//                        itemTitle = documentSnapshot.getString("title");
//                        enlargedPhotoToolbarTitle.setText(itemTitle);

        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "onSuccess: ");
                        itemPhotoURL = documentSnapshot.getString("itemPhoto");
                        itemTitle = documentSnapshot.getString("title");
                        enlargedPhotoToolbarTitle.setText(itemTitle);

                        /*
                        String pickupMethod = documentSnapshot.getString("pickupMethod");
                        if (pickupMethod != null && pickupMethod.equals("In Person")) {
                            if (chatMode.equals("giver"))
                                acceptButton.setVisibility(View.VISIBLE);
//                             // TODO:
//                                add to the chat document a field of "requested" to distinguish between
//                                chats that are open for requested items, and chats that are open without the
//                                taker requesting them
//
                            else {
                                acceptButton.setText(R.string.request_item);
                                acceptButton.setVisibility(View.VISIBLE);
                            }
                        }
                        */

                        if (itemPhotoURL != null) {
                            Log.d(TAG, "Found item photo");
                            loadToolbarImage(itemPhotoURL);
                        } else {
                            Log.d(TAG, "Didn't find item photo");
                            db.collection("items").document(itemId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Bitmap bitmap;
                                            try {
                                                switch (documentSnapshot.getString("category")) {
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
                                                loadToolbarImage(bitmap);
                                            } catch (Exception e) {
                                                Log.d(TAG, "Couldn't find item's category");
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "error: couldn't load app bar's photo");
                        Toast.makeText(ChatRoomActivity.this, R.string.chat_load_fail,
                                Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                });
    }

    private void loadToolbarImage(String uri) {
        loadToolbarImage(Glide.with(getApplicationContext()).load(uri));
    }
    private void loadToolbarImage(Bitmap bitmap) {
        loadToolbarImage(Glide.with(getApplicationContext()).load(bitmap));
    }

    private void loadToolbarImage(RequestBuilder<Drawable> builder) {
        builder.apply(RequestOptions.circleCropTransform())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d(TAG, "error: couldn't load app bar's name");
                        Toast.makeText(ChatRoomActivity.this, R.string.chat_load_fail,
                                Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        toolbarImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                zoomImageFromThumb(toolbarImage);
                            }
                        });
                        return false;
                    }
                })
                .into(toolbarImage);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        chatPartnerId = otherId;
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        chatPartnerId = null;
    }

    @Override
    public void onBackPressed() {
        if (isImageFullscreen) {
            Log.d(TAG, "onBackPressed: closing fullscreen image");
            if (!minimizeFullscreenImage()) {
                redirectToPreviousActivity();
            }
        } else {
            Log.d(TAG, "onBackPressed: finishing activity");
            redirectToPreviousActivity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isImageFullscreen) {
                    Log.d(TAG, "onOptionsItemSelected: fake toolbar clicked");
                    if (!minimizeFullscreenImage()) {
                        redirectToPreviousActivity();
                    }
                } else {
                    Log.d(TAG, "onOptionsItemSelected: real toolbar clicked");
                    redirectToPreviousActivity();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void redirectToPreviousActivity() {
        if (redirectedFromItemInfo) {
            Intent intent = new Intent(this, ChatLobbyActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void zoomImageFromThumb(final View thumbView) {
        Log.d(TAG, "zoomImageFromThumb: Starting");

        if (itemPhotoURL == null || itemTitle == null) {
            return;
        }

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        fullscreenImage.setVisibility(View.VISIBLE);
        fullscreenImageContainer.setVisibility(View.VISIBLE);
        root.setBackgroundColor(getResources().getColor(android.R.color.black));
        chatLayout.setVisibility(View.GONE);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(itemPhotoURL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        fullscreenImage.setImageBitmap(resource);
                    }
                });

        isImageFullscreen = true;
        toggleToolbars();

        Log.d(TAG, "zoomImageFromThumb: Inflated fullscreen image");

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.fullscreen_image_container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        fullscreenImage.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        fullscreenImage.setPivotX(0f);
        fullscreenImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(fullscreenImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(fullscreenImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(fullscreenImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(fullscreenImage,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        minimizer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(fullscreenImage, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(fullscreenImage,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        chatLayout.setVisibility(View.VISIBLE);
                        root.setBackgroundColor(getResources().getColor(android.R.color.white));
                        super.onAnimationStart(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        fullscreenImage.setVisibility(View.GONE);
                        fullscreenImageContainer.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        fullscreenImage.setVisibility(View.GONE);
                        fullscreenImageContainer.setVisibility(View.GONE);
                        chatLayout.setVisibility(View.VISIBLE);
                        root.setBackgroundColor(getResources().getColor(android.R.color.white));
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
                isImageFullscreen = false;
                toggleToolbars();
            }
        };
    }

    private boolean minimizeFullscreenImage() {
        if (minimizer == null) {
            return false;
        }
        ((TouchImageView) fullscreenImage).resetZoom();
        minimizer.onClick(fullscreenImage);
        return true;
    }

    private void toggleToolbars() {
        if (!isImageFullscreen) {
            Log.d(TAG, "toggleToolbars: setting the real toolbar");
            enlargedPhotoToolbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
            setToolbar(toolbar);
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            Log.d(TAG, "toggleToolbars: setting the fake toolbar");
            toolbar.setVisibility(View.GONE);
            enlargedPhotoToolbar.setVisibility(View.VISIBLE);
            setToolbar(enlargedPhotoToolbar);
            changeStatusBarColor(getResources().getColor(android.R.color.black));
        }
    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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
}
