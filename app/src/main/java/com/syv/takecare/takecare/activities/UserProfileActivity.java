package com.syv.takecare.takecare.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.fragments.FeedMapFragment;
import com.syv.takecare.takecare.utilities.RotateBitmap;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends TakeCareActivity {

    private final static String TAG = "TakeCare";

    private ImageView profilePictureView;
    private ProgressBar picturePB;
    private EditText userNameView;
    private Drawable originalEditTextDrawable;
    private KeyListener originalKeyListener;
    private EditText userDescriptionView;
    private ImageButton editNameBtn;
    private ImageButton acceptNameBtn;
    private ImageButton declineNameBtn;
    private ImageButton acceptDescriptionBtn;
    private ImageButton declineDescriptionBtn;
    private Switch itemNotificationsSwitch;


    private String currentName = "User";
    private String currentDescription = "";

    private File selectedImageFile;
    private Uri selectedImage;

    private static int APP_PERMISSION_REQUEST_CAMERA;
    private final static int REQUEST_CAMERA = 1;
    private final static int SELECT_IMAGE = 2;

    private AppCompatButton changePasswordButton;
    private AppCompatButton myFavoritesButton;
    private AppCompatButton myItemsButton;
    private AppCompatButton pendingRequestsButton;
    private AppCompatButton logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        profilePictureView = findViewById(R.id.profile_pic);
        picturePB = findViewById(R.id.profile_pic_progress_bar);
        userNameView = findViewById(R.id.user_name);
        userDescriptionView = findViewById(R.id.about);
        editNameBtn = findViewById(R.id.edit_name_button);

        acceptNameBtn = findViewById(R.id.accept_name_btn);
        declineNameBtn = findViewById(R.id.decline_name_btn);
        acceptDescriptionBtn = findViewById(R.id.accept_description_btn);
        declineDescriptionBtn = findViewById(R.id.decline_description_btn);
        itemNotificationsSwitch = findViewById(R.id.item_notifications_switch);

        originalEditTextDrawable = userNameView.getBackground();
        originalKeyListener = userNameView.getKeyListener();
        userNameView.setBackground(null);
        userNameView.setKeyListener(null);

        userNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: name");
                if (hasFocus) {
                    declineNameBtn.setVisibility(View.VISIBLE);
                    acceptNameBtn.setVisibility(View.VISIBLE);
                    editNameBtn.setVisibility(View.GONE);
                } else {
                    String newName = ((EditText) v).getText().toString();
                    if (currentName.equals(newName)) {
                        disableNameText();
                        declineNameBtn.setVisibility(View.GONE);
                        acceptNameBtn.setVisibility(View.GONE);
                        editNameBtn.setVisibility(View.VISIBLE);
                    } else {
                        alertTextChanges(v, currentName,
                                "Are you sure you want to discard the changes to your name?",
                                acceptNameBtn, declineNameBtn, true);
                    }
                }
            }
        });

        userDescriptionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: description");
                if (hasFocus) {
                    declineDescriptionBtn.setVisibility(View.VISIBLE);
                    acceptDescriptionBtn.setVisibility(View.VISIBLE);
                } else {
                    String newDescription = ((EditText) v).getText().toString();
                    if (currentDescription.equals(newDescription)) {
                        declineDescriptionBtn.setVisibility(View.GONE);
                        acceptDescriptionBtn.setVisibility(View.GONE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
                    } else {
                        alertTextChanges(v, currentDescription,
                                "Are you sure you want to discard the changes to your profile?",
                                acceptDescriptionBtn, declineDescriptionBtn, false);
                    }
                }
            }
        });

        selectedImageFile = null;
        selectedImage = null;

        final TextView usernameViewRef = userNameView;
        final TextView userDescriptionRef = userDescriptionView;
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            currentName = document.getString("name");
                            usernameViewRef.setText(currentName);
                            if (document.getString("profilePicture") != null) {
                                Log.d(TAG, "Found profile pic. Fetched picture url: " + Uri.parse(document.getString("profilePicture")));
                                Glide.with(getApplicationContext())
                                        .load(document.getString("profilePicture"))
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profilePictureView);
                            }
                            if (document.getString("description") != null) {
                                Log.d(TAG, "Found description. Writing: ");
                                currentDescription = document.getString("description");
                                userDescriptionRef.setText(currentDescription);
                            }

                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }

        itemNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                }
            }
        });

        changePasswordButton = findViewById(R.id.change_password);
        myFavoritesButton = findViewById(R.id.my_favorites);
        myItemsButton = findViewById(R.id.my_items);
        pendingRequestsButton = findViewById(R.id.pending_requests);
        logOutButton = findViewById(R.id.logout);

        changePasswordButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_forward_black_24dp), null);
        myFavoritesButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_forward_black_24dp), null);
        myItemsButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_forward_black_24dp), null);
        pendingRequestsButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_forward_black_24dp), null);
        logOutButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_logout), null);

        myItemsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onMyItemsPressed(v);
            }
        });
        pendingRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPendingRequestsPressed(v);
            }
        });
        logOutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onLogOutClick(v);
            }
        });
    }

    @Override
    public void onBackPressed() {
        View focusView = getCurrentFocus();
        if (focusView == null) {
            super.onBackPressed();
        } else if (focusView.equals(userDescriptionView) &&
                !userDescriptionView.getText().toString().equals(currentDescription)) {
            userDescriptionView.clearFocus(); // Invokes the focus change listener
        } else if (focusView.equals(userNameView) &&
                !userNameView.getText().toString().equals(currentName)) {
            userNameView.clearFocus(); // Invokes the focus change listener
        } else {
            super.onBackPressed();
        }
    }

    private void alertTextChanges(final View v, final String backup, final String msg,
                                  final ImageButton acceptBtn, final ImageButton declineBtn,
                                  final boolean isName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Discard changes")
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((EditText) v).setText(backup);
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                        hideKeyboard(UserProfileActivity.this);
                        acceptBtn.setVisibility(View.GONE);
                        declineBtn.setVisibility(View.GONE);
                        if (isName)
                            editNameBtn.setVisibility(View.VISIBLE);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        v.setFocusableInTouchMode(true);
                        v.requestFocus();
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onChangeNameClick(View view) {
        userNameView.setKeyListener(originalKeyListener);
        userNameView.setBackground(originalEditTextDrawable);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(userNameView, InputMethodManager.SHOW_IMPLICIT);
        userNameView.setFocusableInTouchMode(true);
        userNameView.requestFocus();
    }

    private void disableNameText() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
        userNameView.setFocusableInTouchMode(false);
        userNameView.setKeyListener(null);
        userNameView.setBackground(null);
    }

    private void setUserName(final String newName, final String restore, final boolean undoable) {
        if (user == null) {
            return;
        }
        db.collection("users").document(user.getUid())
                .update("name", newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user name updated!");
                        if (undoable) {
                            Snackbar override = Snackbar
                                    .make(findViewById(R.id.user_profile_root), "Name updated!", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            setUserName(restore, null, false);
                                        }
                                    });
                            override.show();
                        } else {
                            Snackbar undo = Snackbar.make(findViewById(R.id.user_profile_root), "Previous name restored", Snackbar.LENGTH_SHORT);
                            undo.show();
                        }
                        userNameView.setText(newName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error updating name");
                    }
                });
    }

    private void setUserDescription(final String newText, final String restore, final boolean undoable) {
        if (user == null) {
            return;
        }
        db.collection("users").document(user.getUid())
                .update("description", newText)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user description updated!");
                        if (undoable) {
                            Snackbar override = Snackbar
                                    .make(findViewById(R.id.user_profile_root), "Profile updated!", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            setUserDescription(restore, null, false);
                                        }
                                    });
                            override.show();
                        } else {
                            Snackbar undo = Snackbar.make(findViewById(R.id.user_profile_root), "Previous profile restored", Snackbar.LENGTH_SHORT);
                            undo.show();
                        }
                        userDescriptionView.setText(newText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error updating description");
                    }
                });
    }


    public void onLogOutClick(View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Close login session of the user: delete token
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "getInstanceId failed", task.getException());
                                            return;
                                        }
                                        String token = task.getResult().getToken();
                                        Log.d(TAG, "Token is: " + token);
                                        db.collection("users").document(user.getUid())
                                                .update("tokens", FieldValue.arrayRemove(token));

                                        // Perform log out

                                        FirebaseAuth.getInstance().signOut();
                                        LoginManager.getInstance().logOut();
                                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing: dismiss alert dialog
                    }
                })
                .show();
    }

    private void setUserProfilePic(byte[] uploadBytes) {
        assert user != null;
        final StorageReference storageRef = storage.child("userProfilePictures/" + user.getUid());
        UploadTask uploadTask = storageRef.putBytes(uploadBytes);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String, Object> profilePicRef = new HashMap<>();
                                        profilePicRef.put("profilePicture", uri.toString());
                                        db.collection("users").document(user.getUid())
                                                .set(profilePicRef, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "All data added successfully!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void onChangeProfilePic(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.photo_upload_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.upload_camera:
                        if (ContextCompat.checkSelfPermission(UserProfileActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(UserProfileActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    APP_PERMISSION_REQUEST_CAMERA);
                        } else {
                            startCameraActivity();
                        }
                        break;
                    case R.id.upload_gallery:
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, SELECT_IMAGE);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        menu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == APP_PERMISSION_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            }
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        selectedImageFile = new File(getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        selectedImage = FileProvider.getUriForFile(UserProfileActivity.this, getPackageName() + ".provider", selectedImageFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
        Log.d(TAG, "Activating camera");
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (selectedImage != null) {
                        uploadPhoto(selectedImage);
                    }
                    break;
                case SELECT_IMAGE:
                    selectedImage = data.getData();
                    uploadPhoto(selectedImage);
                    break;
            }
        }
    }

    private void uploadPhoto(Uri imagePath) {
        ImageCompressTask resize = new ImageCompressTask();
        resize.execute(imagePath);
    }

    public void onAcceptDescription(View view) {
        String newDescription = userDescriptionView.getText().toString();
        if (!currentDescription.equals(newDescription)) {
            String previousDescription = currentDescription;
            currentDescription = newDescription;
            setUserDescription(currentDescription, previousDescription, true);
        }
        userDescriptionView.clearFocus();
    }

    public void onDeclineDescription(View view) {
        userDescriptionView.clearFocus();
    }

    public void onAcceptName(View view) {
        String newName = userNameView.getText().toString();
        if (!currentName.equals(newName)) {
            String previousName = currentName;
            currentName = newName;
            setUserName(currentName, previousName, true);
        }
        userNameView.clearFocus();
    }

    public void onDeclineName(View view) {
        userNameView.clearFocus();
    }

    private class ImageCompressTask extends AsyncTask<Uri, Integer, byte[]> {

        @Override
        protected void onPreExecute() {
            picturePB.setVisibility(View.VISIBLE);
            profilePictureView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            try {
                RotateBitmap rotateBitmap = new RotateBitmap();
                Bitmap bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(UserProfileActivity.this, uris[0]);
                Log.d(TAG, "doInBackground: MBs before compression: " + (double) bitmap.getByteCount() / 1e6);
                byte[] bytes = getBytesFromBitmap(bitmap, 80);
                Log.d(TAG, "doInBackground: MBs after compression: " + (double) bytes.length / 1e6);
                return bytes;
            } catch (IOException e) {
                Log.d(TAG, "doInBackground: exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(bytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePictureView);
            picturePB.setVisibility(View.GONE);
            profilePictureView.setVisibility(View.VISIBLE);
            setUserProfilePic(bytes);
        }

        private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            return stream.toByteArray();
        }
    }

    public void onMyItemsPressed(View view) {
        Intent intent = new Intent(this, SharedItemsActivity.class);
        startActivity(intent);
    }

    public void onPendingRequestsPressed(View view) {
        Intent intent = new Intent(this, FeedMapFragment.RequestedItemsActivity.class);
        startActivity(intent);
    }
}
