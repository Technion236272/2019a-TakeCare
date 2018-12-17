package com.example.yuval.takecare;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.yuval.takecare.utilities.RotateBitmap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class GiverFormActivity extends AppCompatActivity {

    private final static String TAG = "GiverTag";

    String category;
    String[] spinnerNames;
    int[] spinnerIcons;
    Calendar calander;

    EditText title;
    EditText description;
    Spinner pickup;
    ProgressDialog dialog;

    private ImageView itemImageView;
    final static int REQUEST_CAMERA = 1;
    final static int SELECT_IMAGE = 2;
    private File selectedImageFile;
    private Uri selectedImage;
    private byte[] uploadBytes;
    private ProgressBar picturePB;
    private Button formBtn;

    private AppCompatImageButton chosenCategory = null;
    private int airTime = 0;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;

    enum formResult {
        ERROR_UNKNOWN,
        ERROR_TITLE,
        ERROR_PICTURE_NOT_INCLUDED,
        ERROR_OTHER_CATEGORY_NOT_SPECIFIED,
        PICTURE_UPLOADED,
        PICTURE_MISSING
    }

    int APP_PERMISSION_REQUEST_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver_form);

        try {
            category = getIntent().getExtras().getString("CATEGORY");
        } catch (Throwable e) {
            category = "ERROR";
        }

        // Pickup method spinner initialization
        spinnerNames = new String[]{"In Person", "Giveaway", "Race"};
        spinnerIcons = new int[]{R.drawable.ic_in_person, R.drawable.ic_giveaway, R.drawable.ic_race};
        IconTextAdapter ita = new IconTextAdapter(this, spinnerNames, spinnerIcons);
        pickup = (Spinner) findViewById(R.id.pickup_method_spinner);
        pickup.setAdapter(ita);
        title = (EditText) findViewById(R.id.title_input);
        description = (EditText) findViewById(R.id.item_description);

        calander = Calendar.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.giver_form_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        itemImageView = (ImageView) findViewById(R.id.item_picture);
        selectedImage = null;
        selectedImageFile = null;
        //formPB = (ProgressBar) findViewById(R.id.form_pb);
        picturePB = (ProgressBar) findViewById(R.id.picture_pb);
        formBtn = (Button) findViewById(R.id.send_form_button);
        uploadBytes = null;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
    }

    private void addDeviceFilesPath() {
        String path = System.getenv("EXTERNAL_STORAGE");

    }


    public void pickDate(View view) {

    }

    public void pickTime(View view) {
        Calendar calander = Calendar.getInstance();
        int cHour = calander.get(Calendar.HOUR);
        int cMinute = calander.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            }
        }, cHour, cMinute, true);
        tpd.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Form")
                .setMessage("Are you sure you want to discard the form?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //discard the giver form
                        GiverFormActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing: dismiss alert dialog
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

    public void onSendForm(View view) {
        Log.d(TAG, "entered send form method");
        dialog = new ProgressDialog(this);
        dialog.setMessage("Publishing item...");
        dialog.show();
        Map<String, Object> itemInfo = new HashMap<>();
        final FirebaseUser user = auth.getCurrentUser();
        FieldValue timestamp = serverTimestamp();
        switch (formStatus(itemInfo, user, timestamp)) {
            case ERROR_UNKNOWN:
                dialog.dismiss();
                showAlertMessage("An unknown error has occurred. Please try again later");
                break;
            case ERROR_TITLE:
                dialog.dismiss();
                showAlertMessage("Please include a title to describe your item");
                break;
            case ERROR_PICTURE_NOT_INCLUDED:
                dialog.dismiss();
                showAlertMessage("Please include a picture of the item when posting for pick-up in person");
                break;
            case PICTURE_MISSING:
                assert user != null;
                uploadItemDataNoPicture(itemInfo, user, timestamp);
                break;
            case PICTURE_UPLOADED:
                assert user != null;
                uploadItemDataWithPicture(itemInfo, user, timestamp);
                break;
        }
    }

    private void uploadItemDataNoPicture(Map<String, Object> itemInfo, final FirebaseUser user, final FieldValue timestamp) {
        db.collection("items").document(timestamp.toString())
                .set(itemInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Item added successfully");
                        Map<String, Object> itemRef = new HashMap<>();
                        final DocumentReference ref = db.collection("items").document(timestamp.toString());
                        itemRef.put("ref", ref);
                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                .set(itemRef)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Item reference added successfully");
                                        Toast.makeText(GiverFormActivity.this, "Item uploaded successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(GiverFormActivity.this, GatewayActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Error adding item reference");
                                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                        ref.delete();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error adding document");
                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadItemDataWithPicture(final Map<String, Object> itemInfo, final FirebaseUser user, final FieldValue timestamp) {
        Log.d(TAG, "uploadItemAndPictureData: starting data upload ");
        final StorageReference storageRef = storage.child("itemPictures/userUploads/" + user.getUid() + "/" + timestamp.toString());
        UploadTask uploadTask = storageRef.putBytes(uploadBytes);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "uploadItemAndPictureData: image uploaded successfully ");
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "uploadItemAndPictureData: storage uri for image fetched successfully ");
                                        itemInfo.put("photo", uri.toString());
                                        final DocumentReference documentRef = db.collection("items").document();
                                        documentRef
                                                .set(itemInfo)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "uploadItemAndPictureData: item added successfully ");
                                                        Map<String, Object> itemRef = new HashMap<>();
                                                        itemRef.put("item", documentRef);
                                                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                                                .set(itemRef)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "uploadItemAndPictureData: user's published item reference added successfully ");
                                                                        Toast.makeText(GiverFormActivity.this, "Item uploaded successfully!",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(GiverFormActivity.this, GatewayActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private formResult formStatus(final Map<String, Object> itemInfo, FirebaseUser user, FieldValue timestamp) {
        String uid;
        //Stop if the user is not logged in
        if (user == null) {
            return formResult.ERROR_UNKNOWN;
        }
        uid = user.getUid();
        itemInfo.put("publisher", uid);
        Log.d(TAG, "filled publisher");
        itemInfo.put("timestamp", timestamp);
        Log.d(TAG, "filled timestamp");
        //Air Time is in hours
        itemInfo.put("airTime", 72); //TODO: change this when the layout file changes
        Log.d(TAG, "filled airtime");
        itemInfo.put("category", category); //TODO: change this when support for "other" category is enabled in the form
        Log.d(TAG, "filled category");
        String pickupMethod;
        switch (pickup.getSelectedItemPosition()) {
            case 2:
                pickupMethod = "Giveaway";
                break;
            case 3:
                pickupMethod = "Race";
                break;
            default:
                pickupMethod = "In Person";
        }
        itemInfo.put("pickupMethod", pickupMethod);
        Log.d(TAG, "filled pickup method");
        if (selectedImage == null && pickupMethod.equals("In Person")) {
            return formResult.ERROR_PICTURE_NOT_INCLUDED;
        }
        if (title.getText().toString().isEmpty()) {
            return formResult.ERROR_TITLE;
        }
        itemInfo.put("title", title.getText().toString());
        Log.d(TAG, "filled title");
        if (!description.getText().toString().isEmpty()) {
            itemInfo.put("description", description.getText().toString());
            Log.d(TAG, "filled description");
        }
        Log.d(TAG, "filled description");
        if (selectedImage == null)
            return formResult.PICTURE_MISSING;
        return formResult.PICTURE_UPLOADED;
    }

    public void onUploadPicture(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.photo_upload_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.upload_camera:
                        Log.d(TAG, "Starting upload from camera");
                        if (ContextCompat.checkSelfPermission(GiverFormActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(GiverFormActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    APP_PERMISSION_REQUEST_CAMERA);
                        } else {
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            selectedImageFile = new File(getExternalCacheDir(),
                                    String.valueOf(System.currentTimeMillis()) + ".jpg");
                            selectedImage = FileProvider.getUriForFile(GiverFormActivity.this, getPackageName() + ".provider", selectedImageFile);
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                            Log.d(TAG, "Activating camera");
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }
                        break;
                    case R.id.upload_gallery:
                        Log.d(TAG, "Starting upload from gallery");
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
        Log.d(TAG, "Media activity finished!");
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (selectedImage != null) {
                        Log.d(TAG, "About to set image");
                        itemImageView.setVisibility(View.INVISIBLE);
                        uploadPhoto(selectedImage);
                    }
                    break;
                case SELECT_IMAGE:
                    Log.d(TAG, "Fetching gallery's image");
                    selectedImage = data.getData();
                    //TODO: add Glide
                    Log.d(TAG, "About to set image");
                    uploadPhoto(selectedImage);
                    break;
                default:
                    Log.d(TAG, "Error: unknown request code");
            }
        }
    }

    private void showAlertMessage(final String msg) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Information")
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                })
                .show();
    }

    private void uploadPhoto(Uri imagePath) {
        Log.d(TAG, "uploadPhoto: started");
        ImageCompressTask resize = new ImageCompressTask();
        Log.d(TAG, "uploadPhoto: starting execute");
        resize.execute(imagePath);
    }

    public void onCategorySelect(View view) {
        if (chosenCategory != null && chosenCategory.equals(view)) {
            return;
        } else if (chosenCategory == null) {
            ViewCompat.setBackgroundTintList(view, getResources().getColorStateList(R.color.colorAccent));
            ImageViewCompat.setImageTintList((ImageView) view, getResources().getColorStateList(R.color.icons));
            chosenCategory = (AppCompatImageButton) view;
        } else {
            ViewCompat.setBackgroundTintList(chosenCategory, getResources().getColorStateList(R.color.colorCards));
            ImageViewCompat.setImageTintList(chosenCategory, getResources().getColorStateList(R.color.secondary_text));

            ViewCompat.setBackgroundTintList(view, getResources().getColorStateList(R.color.colorAccent));
            ImageViewCompat.setImageTintList((ImageView) view, getResources().getColorStateList(R.color.icons));
            chosenCategory = (AppCompatImageButton) view;
        }
        //TODO: change form dynamically according to the chosenCategory
        switch(view.getId()) {
            case R.id.category_food_btn:
                airTime = 24 * 3;
                break;
            case R.id.category_study_material_btn:
                airTime = 24 * 14;
                break;
            case R.id.category_households_btn:
                airTime = 24 * 14;
                break;
            case R.id.category_lost_and_found_btn:
                airTime = 24 * 5;
                break;
            case R.id.category_hitchhikes_btn:
                airTime = 12;
                break;
            case R.id.category_other_btn:
                airTime = 24 * 7;
                break;
        }
        setDefaultAirTime();
    }

    private void setDefaultAirTime() {
        TextView airTimeText = (TextView) findViewById(R.id.air_time_text);
        String str = "Listed for ";
        if(airTime == 1) {
            str += airTime;
            str += " hour";
        } else if (airTime%24 != 0) {
            str += airTime;
            str+= " hours";
        } else if (airTime == 24) {
            str += airTime/24;
            str+= " day";
        } else if (airTime%24 == 0) {
            str += airTime/24;
            str+= " days";
        } else if (airTime == 24*7) {
            str += airTime/(24*7);
            str+= " week";
        } else if (airTime%24*7 == 0) {
            str += airTime/(24*7);
            str+= " weeks";
        } else if (airTime == 24*30) {
            str+= "1 month";
        } else {
            str+= airTime;
            str+= " hours";
        }
        airTimeText.setText(str);
        airTimeText.setVisibility(View.VISIBLE);
        (findViewById(R.id.air_time_change)).setVisibility(View.VISIBLE);
    }

    public class ImageCompressTask extends AsyncTask<Uri, Integer, byte[]> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: started");
            picturePB.setVisibility(View.VISIBLE);
            itemImageView.setVisibility(View.INVISIBLE);
            formBtn.setClickable(false);
            formBtn.setAlpha((float) 0.6);
            Log.d(TAG, "onPreExecute: finished");
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d(TAG, "doInBackground: compressing");
            try {
                RotateBitmap rotateBitmap = new RotateBitmap();
                Log.d(TAG, "doInBackground: test: " + uris[0].toString());
                Bitmap bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(GiverFormActivity.this, uris[0]);
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
            Log.d(TAG, "onPostExecute: starting");
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            Glide.with(GiverFormActivity.this)
                    .asBitmap()
                    .load(bytes)
                    .into(itemImageView);
            picturePB.setVisibility(View.GONE);
            Log.d(TAG, "onPostExecute: showing image");
            itemImageView.setVisibility(View.VISIBLE);
            formBtn.setAlpha((float) 1.0);
            formBtn.setClickable(true);
            Log.d(TAG, "onPostExecute: done");
        }

        private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
            Log.d(TAG, "getBytesFromBitmap: compress started");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            Log.d(TAG, "getBytesFromBitmap: compress finished");
            return stream.toByteArray();
        }
    }
}