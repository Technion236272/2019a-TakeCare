package com.example.yuval.takecare;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.yuval.takecare.utilities.RotateBitmap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.text.InputType.TYPE_CLASS_TEXT;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profilePictureView;
    private ProgressBar picturePB;
    private EditText userNameView;
    private Drawable originalEditTextDrawable;
    private KeyListener originalKeyListener;
    private EditText userDescriptionView;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;

    private File selectedImageFile;
    private Uri selectedImage;
    private static int APP_PERMISSION_REQUEST_CAMERA;
    private final static int REQUEST_CAMERA = 1;
    private final static int SELECT_IMAGE = 2;
    private final static String TAG = "MyProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        profilePictureView = (ImageView) findViewById(R.id.profile_pic);
        picturePB = (ProgressBar) findViewById(R.id.profile_pic_progress_bar);
        userNameView = (EditText) findViewById(R.id.user_name);
        userDescriptionView = (EditText) findViewById(R.id.about);
        originalEditTextDrawable = userNameView.getBackground();
        originalKeyListener = userNameView.getKeyListener();
        userNameView.setBackground(null);
        userNameView.setKeyListener(null);

        userNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
                    userNameView.setFocusableInTouchMode(false);
                    userNameView.setKeyListener(null);
                    userNameView.setBackground(null);
                    setUserName();
                }
            }
        });

        userDescriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setUserDescription();
            }
        });

        selectedImageFile = null;
        selectedImage = null;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        final TextView usernameViewRef = userNameView;
        final TextView userDescriptionRef = userDescriptionView;
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            usernameViewRef.setText(document.getString("name"));
                            if (document.getString("profilePicture") != null) {
                                Log.d(TAG, "Found profile pic. Fetched picture url: " + Uri.parse(document.getString("profilePicture")));
                                Glide.with(UserProfileActivity.this)
                                        .load(document.getString("profilePicture"))
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profilePictureView);
                            }
                            if (document.getString("description") != null) {
                                Log.d(TAG, "Found description. Writing: ");
                                userDescriptionRef.setText(document.getString("description"));
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.taker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, TakerMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.action_user_settings:
                break;
            case R.id.action_my_items:
                intent = new Intent(this, SharedItemsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.action_requested_items:
                intent = new Intent(this, RequestedItemsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.action_favorites:
                intent = new Intent(this, UserFavoritesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
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

    private void setUserName() {
        final FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }
        db.collection("users").document(user.getUid())
                .update("name", userNameView.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user name updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void setUserDescription() {
        final FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }
        db.collection("users").document(user.getUid())
                .update("description", userDescriptionView.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user name updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
                        //close login session of the user
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
        final FirebaseUser user = auth.getCurrentUser();
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
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            selectedImageFile = new File(getExternalCacheDir(),
                                    String.valueOf(System.currentTimeMillis()) + ".jpg");
                            selectedImage = FileProvider.getUriForFile(UserProfileActivity.this, getPackageName() + ".provider", selectedImageFile);
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                            startActivityForResult(intent, REQUEST_CAMERA);
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

    public class ImageCompressTask extends AsyncTask<Uri, Integer, byte[]> {

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
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Glide.with(UserProfileActivity.this)
                    .asBitmap()
                    .load(bytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePictureView);
//            profilePictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, profilePictureView.getWidth(), profilePictureView.getHeight(), false));
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
}
