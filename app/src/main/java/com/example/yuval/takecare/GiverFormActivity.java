package com.example.yuval.takecare;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    String category;
    String[] spinnerNames;
    int[] spinnerIcons;
    Calendar calander;

    EditText title;
    EditText description;
    Spinner pickup;
    EditText pickupDescription;

    private ImageView itemImageView;
    final static int REQUEST_CAMERA = 1;
    final static int SELECT_IMAGE = 2;
    private File selectedImageFile;
    private Uri selectedImage;
    private byte[] uploadBytes;
    private ProgressBar formPB;
    private ProgressBar picturePB;
    private Button formBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;
    StorageReference storage;
    private double progress = 0;
    private final int UPLOAD_PROGRESS_MIN_FACTOR = 20;

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
        pickupDescription = (EditText) findViewById(R.id.pickup_description);

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
        formPB = (ProgressBar) findViewById(R.id.form_pb);
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


    public void pickTimeSlot(View view) {

        final TextView time_slot_text = findViewById(R.id.time_slots_text);
        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH);
        int cYear = calander.get(Calendar.YEAR);
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d");
                time_slot_text.append(dateFormat.format(c.getTime()));
                int cHour = calander.get(Calendar.HOUR);
                int cMinute = calander.get(Calendar.MINUTE);
                TimePickerDialog startTime = new TimePickerDialog(GiverFormActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time_slot_text.append(" " + (hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute + " - ");
                        TimePickerDialog endTime = new TimePickerDialog(GiverFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                time_slot_text.append((hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute + "\n");
                            }
                        }, hourOfDay, minute, true);
                        endTime.show();
                    }
                }, cHour, cMinute, true);
                startTime.show();
            }
        }, cYear, cMonth, cDay);
        dpd.setTitle("Pick a Time");
        dpd.show();
    }

    public void deadlinePressed(View view) {
        EditText e = findViewById(R.id.pickup_description);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.VISIBLE);
        e.setVisibility(View.GONE);

    }

    public void flexiblePressed(View view) {
        EditText e = findViewById(R.id.pickup_description);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.GONE);
        e.setVisibility(View.VISIBLE);
    }

    public void nowPressed(View view) {
        EditText e = findViewById(R.id.pickup_description);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.GONE);
        e.setVisibility(View.GONE);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSendForm(View view) {
        Log.d("TAG", "entered send form method");
        formPB.setVisibility(View.VISIBLE);
        Map<String, Object> itemInfo = new HashMap<>();
        final FirebaseUser user = auth.getCurrentUser();
        FieldValue timestamp = serverTimestamp();
        switch (formStatus(itemInfo, user, timestamp)) {
            case ERROR_UNKNOWN:
                formPB.setVisibility(View.GONE);
                showAlertMessage("An unknown error has occurred. Please try again later");
                break;
            case ERROR_TITLE:
                formPB.setVisibility(View.GONE);
                showAlertMessage("Please include a title to describe your item");
                break;
            case ERROR_PICTURE_NOT_INCLUDED:
                formPB.setVisibility(View.GONE);
                showAlertMessage("Please include a picture of the item it's posted for pick-up in person");
                break;
            case PICTURE_MISSING:
                assert user != null;
                uploadItemData(itemInfo, user, timestamp);
                break;
            case PICTURE_UPLOADED:
                assert user != null;
                //TODO: fix this case
//                uploadPhoto(itemInfo, user, timestamp, selectedImage);
                break;
        }
    }

    private void uploadItemData(Map<String, Object> itemInfo, final FirebaseUser user, final FieldValue timestamp) {
        db.collection("items").document(timestamp.toString())
                .set(itemInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Item added successfully");
                        Map<String, Object> itemRef = new HashMap<>();
                        final DocumentReference ref = db.collection("items").document(timestamp.toString());
                        itemRef.put("ref", ref);
                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                .set(itemRef)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "Item reference added successfully");
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
                                        Log.d("TAG", "Error adding item reference");
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
                        Log.d("TAG", "Error adding document");
                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadItemAndPictureData(final Map<String, Object> itemInfo, final FirebaseUser user, final FieldValue timestamp) {
        Log.d("TAG", "uploadItemAndPictureData: starting data upload ");
        final StorageReference storageRef = storage.child("itemPictures/userUploads/" + user.getUid());
        UploadTask uploadTask = storageRef.putBytes(uploadBytes);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("TAG", "uploadItemAndPictureData: image uploaded successfully ");
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d("TAG", "uploadItemAndPictureData: storage uri for image fetched successfully ");
                                        itemInfo.put("photo", uri);
                                        Log.d("TAG", "uploadItemAndPictureData: step 2 TEMP " + itemInfo.toString());
                                        final DocumentReference publishedItemRef = db.collection("items").document(timestamp.toString());
                                        publishedItemRef
                                                .set(itemInfo)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "uploadItemAndPictureData: item added successfully ");
                                                        Map<String, Object> itemRef = new HashMap<>();
                                                        itemRef.put("item", publishedItemRef);
                                                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                                                .set(itemRef)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d("TAG", "uploadItemAndPictureData: user's published item reference added successfully ");
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
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double currentProgress = taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount();
                        if (currentProgress > progress + UPLOAD_PROGRESS_MIN_FACTOR) {
                            progress = currentProgress;
                            Toast.makeText(GiverFormActivity.this, progress + "%", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        /*
        db.collection("items").document(timestamp.toString())
                .set(itemInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Item added successfully");
                        Map<String, Object> itemRef = new HashMap<>();
                        final DocumentReference ref = db.collection("items").document(timestamp.toString());
                        itemRef.put("ref", ref);
                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                .set(itemRef)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "Item reference added successfully");
                                        StorageReference storageRef = storage.child("itemPictures/userUploads/" + user.getUid() + "/" + timestamp.toString());
                                        storageRef.putFile(selectedImage)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d("TAG", "uploaded image successfully");
                                                        Uri cloudUri = taskSnapshot.getUploadSessionUri();
                                                        Map<String, Object> itemPhoto = new HashMap<>();
                                                        assert cloudUri != null;
                                                        itemPhoto.put("photo", cloudUri);
                                                        db.collection("users").document(user.getUid()).collection("publishedItems").document(timestamp.toString())
                                                                .set(itemPhoto, SetOptions.merge())
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d("TAG", "added image to item document successfully");
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
                                                                        Log.d("TAG", "failed to add image to item document");
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("TAG", "failed to upload image");
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", "Error adding item reference");
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
                        Log.d("TAG", "Error adding document");
                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                }); */
    }

    private formResult formStatus(final Map<String, Object> itemInfo, FirebaseUser user, FieldValue timestamp) {
        String uid;
        //Stop if the user is not logged in
        if (user == null) {
            return formResult.ERROR_UNKNOWN;
        }
        uid = user.getUid();
        itemInfo.put("publisher", uid);
        Log.d("TAG", "filled publisher");
        itemInfo.put("timestamp", timestamp);
        Log.d("TAG", "filled timestamp");
        //Air Time is in hours
        itemInfo.put("airTime", 72); //TODO: change this when the layout file changes
        Log.d("TAG", "filled airtime");
        itemInfo.put("category", category); //TODO: change this when support for "other" category is enabled in the form
        Log.d("TAG", "filled category");
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
        Log.d("TAG", "filled pickup method");
        if (selectedImage == null && pickupMethod.equals("In Person")) {
            return formResult.ERROR_PICTURE_NOT_INCLUDED;
        }
        if (title.getText().toString().isEmpty()) {
            return formResult.ERROR_TITLE;
        }
        itemInfo.put("title", title.getText().toString());
        Log.d("TAG", "filled title");
        if (!description.getText().toString().isEmpty()) {
            itemInfo.put("description", description.getText().toString());
            Log.d("TAG", "filled description");
        }
        Log.d("TAG", "filled description");
        if (!pickupDescription.getText().toString().isEmpty()) {
            itemInfo.put("pickupDescription", pickupDescription.getText().toString());
            Log.d("TAG", "filled pickup description");
        }
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
                        Log.d("IMAGE", "Starting upload from camera");
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
                            Log.d("IMAGE", "Activating camera");
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }
                        break;
                    case R.id.upload_gallery:
                        Log.d("IMAGE", "Starting upload from gallery");
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
        Log.d("IMAGE", "Media activity finished!");
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (selectedImage != null) {
                        Log.d("IMAGE", "About to set image");
                        uploadPhoto(selectedImage);
                    }
                    break;
                case SELECT_IMAGE:
                    Log.d("IMAGE", "Fetching gallery's image");
                    selectedImage = data.getData();
                    //TODO: add Glide
                    Log.d("IMAGE", "About to set image");
                    uploadPhoto(selectedImage);
                    break;
                default:
                    Log.d("IMAGE", "Error: unknown request code");
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
        Log.d("TAG", "uploadPhoto: started");
        ImageCompressTask resize = new ImageCompressTask();
        Log.d("TAG", "uploadPhoto: starting execute");
        resize.execute(imagePath);
    }

    public class ImageCompressTask extends AsyncTask<Uri, Integer, byte[]> {

        @Override
        protected void onPreExecute() {
            picturePB.setVisibility(View.VISIBLE);
            itemImageView.setVisibility(View.INVISIBLE);
            formBtn.setClickable(false);
            formBtn.setAlpha((float) 0.6);
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d("TAG", "doInBackground: compressing");
            try {
                RotateBitmap rotateBitmap = new RotateBitmap();
                Log.d("TAG", "doInBackground: test: "+uris[0].toString());
                Bitmap bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(GiverFormActivity.this, uris[0]);
                Log.d("TAG", "doInBackground: MBs before compression: " + (double) bitmap.getByteCount() / 1e6);
                byte[] bytes = getBytesFromBitmap(bitmap, 80);
                Log.d("TAG", "doInBackground: MBs after compression: " + (double) bytes.length / 1e6);
                return bytes;
            } catch (IOException e) {
                Log.d("TAG", "doInBackground: exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            itemImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,  itemImageView.getWidth(), itemImageView.getHeight(), false));
            picturePB.setVisibility(View.GONE);
            itemImageView.setVisibility(View.VISIBLE);
            formBtn.setAlpha((float) 1.0);
            formBtn.setClickable(true);
        }

        private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            return stream.toByteArray();
        }
    }
}