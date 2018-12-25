package com.syv.takecare.takecare;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.syv.takecare.takecare.utilities.RotateBitmap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class GiverFormActivity extends AppCompatActivity {

    private final static String TAG = "TakeCare";

    private String category;
    private String[] spinnerNames;
    private int[] spinnerIcons;

    private EditText title;
    private EditText description;
    private EditText pickupDescription;
    private EditText pickupLocation;
    private Spinner pickup;
    private ProgressDialog dialog;
    private TextView airTimeText;
    private TextView airTimeToggler;
    private SeekBar airTimePicker;

    private ImageView itemImageView;
    final static private int REQUEST_CAMERA = 1;
    final static private int SELECT_IMAGE = 2;
    private File selectedImageFile;
    private Uri selectedImage;
    private byte[] uploadBytes;
    private ProgressBar picturePB;
    private Button formBtn;

    private AppCompatImageButton chosenCategory = null;
    private int airTime = 0;
    private static final String changeText = "Change";
    private static final String hideText = "Hide";


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;

    enum formResult {
        ERROR_UNKNOWN,
        ERROR_TITLE,
        ERROR_PICTURE_NOT_INCLUDED,
        ERROR_NO_CATEGORY,
        PICTURE_UPLOADED,
        PICTURE_MISSING
    }

    int APP_PERMISSION_REQUEST_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.giver_form_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Pickup method spinner initialization
        spinnerNames = new String[]{"In Person", "Giveaway", "Race"};
        spinnerIcons = new int[]{R.drawable.ic_in_person, R.drawable.ic_giveaway, R.drawable.ic_race};
        IconTextAdapter ita = new IconTextAdapter(this, spinnerNames, spinnerIcons);
        pickup = (Spinner) findViewById(R.id.pickup_method_spinner);
        pickup.setAdapter(ita);

        title = (EditText) findViewById(R.id.title_input);
        description = (EditText) findViewById(R.id.item_description);
        pickupDescription = (EditText) findViewById(R.id.item_time);
        pickupLocation = (EditText) findViewById(R.id.item_location);
        itemImageView = (ImageView) findViewById(R.id.item_picture);
        picturePB = (ProgressBar) findViewById(R.id.picture_pb);
        airTimeText = (TextView) findViewById(R.id.air_time_text);
        airTimeToggler = (TextView) findViewById(R.id.air_time_change);
        airTimePicker = (SeekBar) findViewById(R.id.air_time_seek_bar);
        airTimePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        airTime = 1;
                        break;
                    case 1:
                        airTime = 3;
                        break;
                    case 2:
                        airTime = 6;
                        break;
                    case 3:
                        airTime = 12;
                        break;
                    case 4:
                        airTime = 24;
                        break;
                    case 5:
                        airTime = 24 * 3;
                        break;
                    case 6:
                        airTime = 24 * 5;
                        break;
                    case 7:
                        airTime = 24 * 7;
                        break;
                    case 8:
                        airTime = 24 * 10;
                        break;
                    case 9:
                        airTime = 24 * 14;
                        break;
                    case 10:
                        airTime = 24 * 21;
                        break;
                    case 11:
                        airTime = 24 * 30;
                        break;
                }
                setPostAirTimeText();
            }
        });
        formBtn = (Button) findViewById(R.id.send_form_button);

        selectedImage = null;
        selectedImageFile = null;
        uploadBytes = null;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
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
                return true;
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
                showAlertMessage("Please include a picture of the item");
//                showAlertMessage("Please include a picture of the item when posting for pick-up in person");
                break;
            case ERROR_NO_CATEGORY:
                dialog.dismiss();
                showAlertMessage("Please select the item's category");
                break;
            case PICTURE_MISSING:
                assert user != null;
                uploadItemDataNoPicture(itemInfo, user, timestamp);
                break;
            case PICTURE_UPLOADED:
                assert user != null;
                uploadItemDataWithPicture(itemInfo, user);
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
                                        dialog.dismiss();
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
                                        dialog.dismiss();
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
                        dialog.dismiss();
                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadItemDataWithPicture(final Map<String, Object> itemInfo, final FirebaseUser user) {
        Log.d(TAG, "uploadItemAndPictureData: starting data upload ");
        final String uniqueID = UUID.randomUUID().toString();
        final StorageReference storageRef = storage.child("itemPictures/userUploads/" + user.getUid() + "/" + uniqueID);
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
                                                        dialog.dismiss();
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
                                                        dialog.dismiss();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });
    }

    private formResult formStatus(final Map<String, Object> itemInfo, FirebaseUser user, FieldValue timestamp) {
        String uid;
        //Stop if the user is not logged in
        if (user == null) {
            return formResult.ERROR_UNKNOWN;
        }
        if (category == null) {
            return formResult.ERROR_NO_CATEGORY;
        }
        uid = user.getUid();
        itemInfo.put("publisher", uid);
        Log.d(TAG, "filled publisher");

        itemInfo.put("timestamp", timestamp);
        Log.d(TAG, "filled timestamp");

        //Air Time is in hours
        itemInfo.put("airTime", airTime);
        Log.d(TAG, "filled airtime");

        itemInfo.put("category", category);
        Log.d(TAG, "filled category");

        String pickupMethod;
        Log.d(TAG, "pickup method poition: " + pickup.getSelectedItemPosition());
        switch (pickup.getSelectedItemPosition()) {
            case 0:
                pickupMethod = "In Person";
                break;
            case 1:
                pickupMethod = "Giveaway";
                break;
            case 2:
                pickupMethod = "Race";
                break;
            default:
                Log.d(TAG, "formStatus: Error in spinner position: " + pickup.getSelectedItemPosition());
                pickupMethod = "ERROR";
                break;
        }
        itemInfo.put("pickupMethod", pickupMethod);
        Log.d(TAG, "filled pickup method");

        //TODO: allow users to not upload a picture under some circumstances
        if (selectedImage == null) {
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

        if (!pickupDescription.getText().toString().isEmpty()) {
            itemInfo.put("pickupInformation", pickupDescription.getText().toString());
            Log.d(TAG, "filled pickup description");
        }

        if (!pickupLocation.getText().toString().isEmpty()) {
            itemInfo.put("pickupLocation", pickupLocation.getText().toString());
            Log.d(TAG, "filled pickup location");
        }

        // Status 1 = available
        itemInfo.put("status", 1);
        Log.d(TAG, "filled item's status");

        // We don't get here for now
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
                            startCameraActivity();
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

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        selectedImageFile = new File(getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        selectedImage = FileProvider.getUriForFile(GiverFormActivity.this, getPackageName() + ".provider", selectedImageFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
        Log.d(TAG, "Activating camera");
        startActivityForResult(intent, REQUEST_CAMERA);
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
        switch (view.getId()) {
            case R.id.category_food_btn:
                airTime = 24 * 3;
                airTimePicker.setProgress(5);
                category = "Food";
                break;
            case R.id.category_study_material_btn:
                airTime = 24 * 14;
                airTimePicker.setProgress(9);
                category = "Study Material";
                break;
            case R.id.category_households_btn:
                airTime = 24 * 14;
                airTimePicker.setProgress(9);
                category = "Households";
                break;
            case R.id.category_lost_and_found_btn:
                airTime = 24 * 5;
                airTimePicker.setProgress(6);
                category = "Lost & Found";
                break;
            case R.id.category_hitchhikes_btn:
                airTime = 12;
                airTimePicker.setProgress(2);
                category = "Hitchhikes";
                break;
            case R.id.category_other_btn:
                airTime = 24 * 7;
                airTimePicker.setProgress(7);
                category = "Other";
                break;
        }
        setDefaultAirTime();
    }

    private void setDefaultAirTime() {
        setPostAirTimeText();
        airTimeText.setVisibility(View.VISIBLE);
        airTimeToggler.setVisibility(View.VISIBLE);
        airTimeToggler.setText(changeText);
        airTimePicker.setVisibility(View.GONE);
    }

    private void setPostAirTimeText() {
        String str = "Listed for ";
        if (airTime == 1) {
            str += airTime;
            str += " hour";
        } else if (airTime % 24 != 0) {
            str += airTime;
            str += " hours";
        } else if (airTime == 24) {
            str += airTime / 24;
            str += " day";
        } else if (airTime < 24 * 7 || (airTime % (24 * 7) != 0 && airTime % (24 * 30) != 0)) {
            str += airTime / 24;
            str += " days";
        } else if (airTime == 24 * 7) {
            str += airTime / (24 * 7);
            str += " week";
        } else if (airTime < 24 * 30 || airTime % (24 * 30) != 0) {
            str += airTime / (24 * 7);
            str += " weeks";
        } else if (airTime == 24 * 30) {
            str += "1 month";
        } else {
            str += airTime;
            str += " hours";
        }
        airTimeText.setText(str);
    }

    public void onChangeAirTimeClick(View view) {
        String currentState = ((TextView) view).getText().toString();
        if (currentState.equals(changeText)) {
            airTimePicker.setVisibility(View.VISIBLE);
            airTimeToggler.setText(hideText);
        } else if (currentState.equals(hideText)) {
            airTimePicker.setVisibility(View.GONE);
            airTimeToggler.setText(changeText);
        }
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
            Glide.with(getApplicationContext())
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