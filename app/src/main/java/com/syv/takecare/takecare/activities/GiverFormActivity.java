package com.syv.takecare.takecare.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hootsuite.nachos.ChipConfiguration;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.syv.takecare.takecare.fragments.WorkaroundMapFragment;
import com.syv.takecare.takecare.adapters.IconTextAdapter;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.utilities.RotateBitmap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class GiverFormActivity extends TakeCareActivity implements OnMapReadyCallback{

    private final static String TAG = "TakeCare/GiverForm";
    private static final int POPUP_ACTIVE_DURATION = 6000;
    private static final String changeText = "Change";
    private static final String hideText = "Hide";
    private static final List<Character> TERMINATORS = Arrays.asList('\n', ';', ',');
    private static final int CHIP_MAX_LENGTH = 15;
    private static final int CHIP_MIN_LENGTH = 3;
    private static final int TAGS_SEPARATING_LENGTH = 4;

    int APP_PERMISSION_REQUEST_CAMERA;

    private String pickupMethod;
    private String category;

    private ScrollView scrollView;

    private EditText title;
    private EditText description;
    private EditText pickupDescription;
    private EditText pickupLocation;
    private Spinner pickup;
    private ProgressDialog dialog;

    private TextView airTimeText;
    private TextView airTimeToggler;
    private SeekBar airTimePicker;
    private AppCompatImageView airTimeHelpBtn;
    private ToolTipRelativeLayout airTimeTooltipLayout;
    private ToolTipView airTimeToolTipView;
    private Handler airTimeTooltipHandler = new Handler();
    private Runnable airTimeTooltipTask;
    private boolean isAirtimeTooltipOpen;

    private TextView tagsToggler;
    private NachoTextView tagsBox;
    private AppCompatImageView tagsHelpBtn;
    private ToolTipRelativeLayout tagsTooltipLayout;
    private ToolTipView tagsToolTipView;
    private Handler tagsTooltipHandler = new Handler();
    private Runnable tagsTooltipTask;
    private boolean isTagsTooltipOpen;

    private View tooltipsPlaceholder;

    private ImageView itemImageView;
    final static private int REQUEST_CAMERA = 1;
    final static private int SELECT_IMAGE = 2;
    private File selectedImageFile;
    private Uri selectedImage;
    private byte[] uploadBytes;
    private ProgressBar picturePB;
    private Button formBtn;
    private AppCompatButton addLocationButton;
    private AppCompatImageButton chosenCategory = null;
    private int airTime = 0;

    private Handler suggestionsHandler = new Handler();
    private Runnable suggestionsTask;
    private List<String> autoCompleteSuggestions = new ArrayList<>();
    private List<String> allExistingTags = new ArrayList<>();
    private int tagsAmount = 0;

    private boolean mLocationPermissionGranted;
    private FrameLayout mapWrapper;
    private GoogleMap mMap;
    private Marker marker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(32.777751, 35.021508);
    enum locationButtonStateEnum{
        ENTER_LOCATION,
        EDIT_LOCATION,
        SAVE_LOCATION
    }
    private locationButtonStateEnum locationButtonState;
    enum formResult {
        ERROR_UNKNOWN,
        ERROR_TITLE,
        ERROR_PICTURE_NOT_INCLUDED,
        ERROR_NO_CATEGORY,
        PICTURE_UPLOADED,
        PICTURE_MISSING
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver_form);
        Toolbar toolbar = findViewById(R.id.giver_form_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initWidgets();

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

        selectedImage = null;
        selectedImageFile = null;
        uploadBytes = null;

        AppCompatImageButton[] buttonsCategories = new AppCompatImageButton[6];
        buttonsCategories[0] = findViewById(R.id.category_food_btn);
        buttonsCategories[1] = findViewById(R.id.category_study_material_btn);
        buttonsCategories[2] = findViewById(R.id.category_households_btn);
        buttonsCategories[3] = findViewById(R.id.category_lost_and_found_btn);
        buttonsCategories[4] = findViewById(R.id.category_hitchhikes_btn);
        buttonsCategories[5] = findViewById(R.id.category_other_btn);
        for (int i = 0; i < 6; i++) {
            buttonsCategories[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCategorySelect(v);
                }
            });
        }
        formBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSendForm(v);
            }
        });
        pickup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        pickupMethod = "In Person";
                        pickupDescription.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        pickupMethod = "Giveaway";
                        pickupDescription.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        pickupMethod = "Race";
                        pickupDescription.setVisibility(View.GONE);
                        break;
                    default:
                        Log.d(TAG, "Error in spinner position: " + pickup.getSelectedItemPosition());
                        pickupMethod = "ERROR";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        initTooltips();
        configureTagsBox();
        addLocationButton = findViewById(R.id.add_location_button);
        mapWrapper = findViewById(R.id.choose_map_wrapper);
        addLocationButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_map_display), null, null, null);
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLocationPermissionGranted) {
                    getLocationPermission();
                }
                switch (locationButtonState){
                    case ENTER_LOCATION:
                        mapWrapper.setVisibility(View.VISIBLE);
                        locationButtonState = locationButtonStateEnum.SAVE_LOCATION;
                        ViewCompat.setBackgroundTintList(addLocationButton, getResources().getColorStateList(R.color.colorAccent));
                        addLocationButton.setText(getString(R.string.save_location));
                        break;
                    case SAVE_LOCATION:
                        mapWrapper.setVisibility(View.GONE);
                        locationButtonState = locationButtonStateEnum.EDIT_LOCATION;
                        ViewCompat.setBackgroundTintList(addLocationButton, getResources().getColorStateList(R.color.colorPrimary));
                        addLocationButton.setText(getString(R.string.edit_location));
                        break;
                    case EDIT_LOCATION:
                        mapWrapper.setVisibility(View.VISIBLE);
                        locationButtonState = locationButtonStateEnum.SAVE_LOCATION;
                        ViewCompat.setBackgroundTintList(addLocationButton, getResources().getColorStateList(R.color.colorAccent));
                        addLocationButton.setText(getString(R.string.save_location));
                        break;
                }
            }
        });
        try {
            WorkaroundMapFragment mapFragment =
                    (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.choose_map);
            mapFragment.getMapAsync(this);
            ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.choose_map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "Activity destroyed");
        }
        locationButtonState = locationButtonStateEnum.ENTER_LOCATION;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            return;
        }
        updateLocationUI();
        getDeviceLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(marker != null){
                    marker.remove();
                }
                MarkerOptions selection = new MarkerOptions();
                selection.position(latLng).draggable(true);
                marker = mMap.addMarker(selection);
            }
        });
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation == null) {
                            return;
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 15));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, 15));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }
    private void initWidgets() {
        scrollView = findViewById(R.id.form_scroll);
        pickup = findViewById(R.id.pickup_method_spinner);
        title = findViewById(R.id.title_input);
        description = findViewById(R.id.item_description);
        pickupDescription = findViewById(R.id.item_time);
        pickupLocation = findViewById(R.id.item_location);
        itemImageView = findViewById(R.id.item_picture);
        picturePB = findViewById(R.id.picture_pb);
        airTimeText = findViewById(R.id.air_time_text);
        airTimeToggler = findViewById(R.id.air_time_change);
        airTimePicker = findViewById(R.id.air_time_seek_bar);
        airTimeHelpBtn = findViewById(R.id.air_time_help);
        airTimeTooltipLayout = findViewById(R.id.air_time_help_tooltip);
        airTimeHelpBtn = findViewById(R.id.air_time_help);
        tagsToggler = findViewById(R.id.add_keywords_text);
        tagsHelpBtn = findViewById(R.id.keywords_help);
        tagsBox = findViewById(R.id.keywords_tag_box);
        tagsTooltipLayout = findViewById(R.id.keywords_help_tooltip);
        formBtn = findViewById(R.id.send_form_button);
        tooltipsPlaceholder = findViewById(R.id.placeholder);
        airTimePicker.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        airTimePicker.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        // Pickup method spinner initialization
        String[] spinnerNames = new String[]{"In Person", "Giveaway", "Race"};
        int[] spinnerIcons = new int[]{R.drawable.ic_in_person, R.drawable.ic_giveaway, R.drawable.ic_race};
        IconTextAdapter ita = new IconTextAdapter(this, spinnerNames, spinnerIcons);
        pickup.setAdapter(ita);
    }


    private void initTooltips() {
        isAirtimeTooltipOpen = false;
        isTagsTooltipOpen = false;

        airTimeTooltipTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "tooltip timed out!");
                if (airTimeToolTipView != null)
                    airTimeToolTipView.remove();
                airTimeHelpBtn.setAlpha(0.7f);
                isAirtimeTooltipOpen = false;
                if (!isAirtimeTooltipOpen && !isTagsTooltipOpen) {
                    tooltipsPlaceholder.setVisibility(View.GONE);
                }
            }
        };

        tagsTooltipTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "tooltip timed out!");
                if (tagsToolTipView != null)
                    tagsToolTipView.remove();
                tagsHelpBtn.setAlpha(0.7f);
                isTagsTooltipOpen = false;
                if (!isAirtimeTooltipOpen && !isTagsTooltipOpen) {
                    tooltipsPlaceholder.setVisibility(View.GONE);
                }
            }
        };

        final ToolTip airtimeTooltip = new ToolTip()
                .withText(getResources().getString(R.string.air_time_tooltip_text))
                .withColor(getResources().getColor(R.color.colorPrimary))
                .withTextColor(Color.WHITE)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);

        final ToolTip tagsTooltip = new ToolTip()
                .withText(getResources().getString(R.string.tags_tooltip_text))
                .withColor(getResources().getColor(R.color.colorPrimary))
                .withTextColor(Color.WHITE)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);

        airTimeHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAirtimeTooltipOpen)
                    return;
                isAirtimeTooltipOpen = true;
                airTimeHelpBtn.setAlpha(1.0f);
                tooltipsPlaceholder.setVisibility(View.INVISIBLE);
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, scrollView.getScrollY() + 144);
                    }
                }, 100);
                airTimeToolTipView = airTimeTooltipLayout
                        .showToolTipForView(airtimeTooltip, airTimeHelpBtn);
                airTimeToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                    @Override
                    public void onToolTipViewClicked(ToolTipView toolTipView) {
                        airTimeTooltipTask.run();
                    }
                });

                tagsTooltipTask.run();
                tagsTooltipHandler.removeCallbacks(tagsTooltipTask);
                airTimeTooltipHandler.removeCallbacks(airTimeTooltipTask);
                airTimeTooltipHandler.postDelayed(airTimeTooltipTask, POPUP_ACTIVE_DURATION);
            }
        });

        tagsHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTagsTooltipOpen)
                    return;
                isTagsTooltipOpen = true;
                tagsHelpBtn.setAlpha(1.0f);
                tooltipsPlaceholder.setVisibility(View.INVISIBLE);
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, scrollView.getScrollY() + 144);
                    }
                }, 100);
                tagsToolTipView = tagsTooltipLayout
                        .showToolTipForView(tagsTooltip, tagsHelpBtn);
                tagsToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                    @Override
                    public void onToolTipViewClicked(ToolTipView toolTipView) {
                        tagsTooltipTask.run();
                    }
                });

                airTimeTooltipTask.run();
                airTimeTooltipHandler.removeCallbacks(airTimeTooltipTask);
                tagsTooltipHandler.removeCallbacks(tagsTooltipTask);
                tagsTooltipHandler.postDelayed(tagsTooltipTask, POPUP_ACTIVE_DURATION);
            }
        });
    }


    private void configureTagsBox() {
        for (char c : TERMINATORS) {
            tagsBox.addChipTerminator(c, ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        }

        tagsBox.setChipTokenizer(new SpanChipTokenizer<>(this, new ChipSpanChipCreator() {
            @Override
            public ChipSpan createChip(@NonNull Context context, @NonNull CharSequence text, Object data) {
                return new ChipSpan(context, text, ContextCompat.getDrawable(GiverFormActivity.this, R.drawable.ic_edit_white), data);
            }

            @Override
            public void configureChip(@NonNull ChipSpan chip, @NonNull ChipConfiguration chipConfiguration) {
                super.configureChip(chip, chipConfiguration);
                chip.setShowIconOnLeft(true);
                chip.setIconBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }, ChipSpan.class));

        tagsBox.enableEditChipOnTouch(false, true);

        suggestionsTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "detected keyword suggestions changes");
                setAutoCompleteAdapter();
            }
        };

        addAutoCompleteOptions();

        tagsBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if the user has entered or deleted a keyword, in order to manage suggestions
                if (tagsAmount != tagsBox.getAllChips().size()) {
                    Log.d(TAG, "change in chips detected");
                    suggestionsHandler.removeCallbacks(suggestionsTask);
                    suggestionsHandler.post(suggestionsTask);
                }
            }
        });
    }

    private void addAutoCompleteOptions() {
        Query query = db.collection("tags")
                .orderBy("tag");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Listen failed with: " + e);
                    return;
                }

                if (queryDocumentSnapshots == null) {
                    Log.d(TAG, "Did not find any tags in database");
                    return;
                }

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("tag") != null) {
                        allExistingTags.add(doc.getString("tag"));
                    }
                }

                suggestionsHandler.removeCallbacks(suggestionsTask);
                suggestionsHandler.post(suggestionsTask);
            }
        });

        suggestionsTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "detected keyword suggestions changes");
                setAutoCompleteAdapter();
            }
        };
    }

    private void setAutoCompleteAdapter() {
        autoCompleteSuggestions.clear();
        List<String> currentKeywords = tagsBox.getChipValues();
        tagsAmount = currentKeywords.size();
        for (String keyword : allExistingTags) {
            if (!currentKeywords.contains(keyword)) {
                autoCompleteSuggestions.add(keyword);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (getApplicationContext(), R.layout.auto_complete_dropdown_item, autoCompleteSuggestions);
        tagsBox.setAdapter(adapter);
        Log.d(TAG, "set the auto-complete adapter");
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
        if(marker != null){
            itemInfo.put("location",new GeoPoint(marker.getPosition().latitude,marker.getPosition().longitude));
        }
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
                showAlertMessage("Please include a picture of the item"); //TODO: change this
//                showAlertMessage("Please include a picture of the item when posting for pick-up in person");
                break;
            case ERROR_NO_CATEGORY:
                dialog.dismiss();
                showAlertMessage("Please select the item's category");
                break;
            case PICTURE_MISSING:
                assert user != null;
                uploadItemDataNoPicture(itemInfo);
                break;
            case PICTURE_UPLOADED:
                assert user != null;
                uploadItemDataWithPicture(itemInfo, user);
                break;
        }
    }

    private void uploadItemDataNoPicture(final Map<String, Object> itemInfo) {
        Log.d(TAG, "uploadItemAndPictureData: starting data upload ");
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
                        Intent intent = new Intent(GiverFormActivity.this, TakerMenuActivity.class);
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
                                                        Intent intent = new Intent(GiverFormActivity.this, TakerMenuActivity.class);
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

        Log.d(TAG, "pickup method poition: " + pickup.getSelectedItemPosition());
        itemInfo.put("pickupMethod", pickupMethod);
        Log.d(TAG, "filled pickup method");

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
            if (category.equals("Hitchhikes")) {
                try {
                    String to = ((TextInputEditText) (findViewById(R.id.item_location_to))).getText().toString();
                    itemInfo.put("pickupLocation", "From: " + pickupLocation.getText().toString() + "\nTo: " + to);
                } catch (NullPointerException e) {
                    Log.d(TAG, "formStatus: Hitchhike getText from to");
                }
            } else {
                itemInfo.put("pickupLocation", pickupLocation.getText().toString());
                Log.d(TAG, "filled pickup location");
            }
        }

        // Status 1 = available
        itemInfo.put("status", 1);
        Log.d(TAG, "filled item's status");

        // displayStatus means whether or not the item should be displayed in the feed
        itemInfo.put("displayStatus", true);
        Log.d(TAG, "filled item's display status");

        List<String> keywords = getAllKeywords();
        if (!keywords.isEmpty()) {
            itemInfo.put("tags", keywords);
        }

        //TODO: allow users to not upload a picture under some circumstances later
        /*if (selectedImage == null && !category.equals("Hitchhikes") && !category.equals("Other"))
            return formResult.ERROR_PICTURE_NOT_INCLUDED; //TODO: change this error code if upload is legal*/
        if (selectedImage == null) {
            return formResult.PICTURE_MISSING;
        } else {
            return formResult.PICTURE_UPLOADED;
        }
    }

    public List<String> getAllKeywords() {
        tagsBox.chipifyAllUnterminatedTokens();

        // Filter duplicates & long keywords
        List<String> chosenTags = tagsBox.getChipValues();
        Set<String> uniqueTags = new HashSet<>(chosenTags);

        for (Iterator<String> iterator = uniqueTags.iterator(); iterator.hasNext(); ) {
            String tag = iterator.next();
            if (tag.length() > CHIP_MAX_LENGTH || tag.length() < CHIP_MIN_LENGTH) {
                iterator.remove();
            }
        }

        chosenTags.clear();
        chosenTags.addAll(uniqueTags);
        initChips(chosenTags);
        return chosenTags;
    }

    private void initChips(Collection<String> tags) {
        if (tags == null || tags.isEmpty()) {
            tagsBox.setText("");
            return;
        }
        StringBuilder tagsTextBuilder = new StringBuilder();
        for (String tag : tags) {
            tagsTextBuilder.append(tag);
        }

        tagsBox.setText(tagsTextBuilder.toString());
        int index = 0;
        for (String tag : tags) {
            tagsBox.chipify(index, index + tag.length());
            index += tag.length() + TAGS_SEPARATING_LENGTH;
        }
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
                    if (selectedImageFile != null && selectedImage != null) {
                        Log.d(TAG, "About to set image");
                        itemImageView.setVisibility(View.INVISIBLE);
                        uploadPhoto(selectedImage);
                    }
                    break;
                case SELECT_IMAGE:
                    Log.d(TAG, "Fetching gallery's image");
                    selectedImage = data.getData();
                    Log.d(TAG, "About to set image");
                    uploadPhoto(selectedImage);
                    break;
                default:
                    Log.d(TAG, "Error: unknown request code");
            }
        } else {
            selectedImage = null;
            selectedImageFile = null;
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
        Log.d(TAG, "onCategorySelect: invoke");
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
                pickupMethod = "In Person";
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
        if (category.equals("Hitchhikes") || category.equals("Lost & Found")) {
            pickupMethod = "In Person";
            pickupDescription.setVisibility(View.VISIBLE);
            pickup.setVisibility(View.GONE);
            findViewById(R.id.type_time_separator).setVisibility(View.GONE);
        } else {
            switch (pickup.getSelectedItemPosition()) {
                case 0:
                    pickupMethod = "In Person";
                    pickupDescription.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    pickupMethod = "Giveaway";
                    pickupDescription.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    pickupMethod = "Race";
                    pickupDescription.setVisibility(View.GONE);
                    break;
                default:
                    Log.d(TAG, "formStatus: Error in spinner position: " + pickup.getSelectedItemPosition());
                    pickupMethod = "ERROR";
                    break;
            }
            pickup.setVisibility(View.VISIBLE);
            findViewById(R.id.type_time_separator).setVisibility(View.VISIBLE);
        }
        if (category.equals("Hitchhikes")) {
            findViewById(R.id.text_input_layout_location_hitchhiking_extension).setVisibility(View.VISIBLE);
            ((TextInputLayout) findViewById(R.id.text_input_layout_location)).setHint(getResources().getString(R.string.enter_location_from_hint));
        } else {
            findViewById(R.id.text_input_layout_location_hitchhiking_extension).setVisibility(View.GONE);
            ((TextInputLayout) findViewById(R.id.text_input_layout_location)).setHint(getResources().getString(R.string.enter_location_hint));
        }
        setDefaultAirTime();
        setDefaultTags();
    }

    private void setDefaultTags() {
        tagsToggler.setVisibility(View.VISIBLE);
        tagsHelpBtn.setVisibility(View.VISIBLE);
    }

    private void setDefaultAirTime() {
        setPostAirTimeText();
        airTimeText.setVisibility(View.VISIBLE);
        airTimeToggler.setVisibility(View.VISIBLE);
        airTimeToggler.setText(changeText);
        airTimeHelpBtn.setVisibility(View.VISIBLE);
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

    public void onTagsTogglerClick(View view) {
        TextView togglerText = (TextView) view;
        if (togglerText.getText().toString().equals(getResources().getString
                (R.string.tags_box_show_text))) {
            // Show the tags box
            togglerText.setText(getResources().getString
                    (R.string.tags_box_hide_text));
            tagsBox.setVisibility(View.VISIBLE);

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 300);

        } else if (togglerText.getText().toString().equals(getResources().getString
                (R.string.tags_box_hide_text))) {
            // Hide the tags box
            togglerText.setText(getResources().getString
                    (R.string.tags_box_show_text));
            tagsBox.setVisibility(View.GONE);
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