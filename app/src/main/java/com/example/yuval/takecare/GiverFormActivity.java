package com.example.yuval.takecare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.firestore.FieldValue.serverTimestamp;

public class GiverFormActivity extends AppCompatActivity {
    String category;
    String[] spinnerNames;
    int[] spinnerIcons;
    Calendar calander;
    FirebaseFirestore db;
    FirebaseAuth auth;

    EditText title;
    EditText description;
    Spinner pickup;
    EditText pickupDescription;

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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

    public void addPicture(View view) {
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
        Map<String, Object> itemInfo = new HashMap<>();
        itemInfo.put("timestamp", serverTimestamp());
        Log.d("TAG", "filled timestamp");
        //Air Time is in hours
        itemInfo.put("airTime", 72); //TODO: change this when the layout file changes
        Log.d("TAG", "filled airtime");
        itemInfo.put("category", category);
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
        itemInfo.put("title", title.getText().toString());
        Log.d("TAG", "filled title");
        itemInfo.put("description", description.getText().toString());
        Log.d("TAG", "filled description");
        if (!pickupDescription.getText().toString().isEmpty()) {
            itemInfo.put("pickupDescription", pickupDescription.getText().toString());
            Log.d("TAG", "filled pickup description");
        }
        final FirebaseUser user = auth.getCurrentUser();
        Log.d("TAG", "1");
        if (user != null) {
            Log.d("TAG", "2");
            itemInfo.put("publisher", user.getUid());
            Log.d("TAG", "filled publisher");

        }

        db.collection("items")
                .add(itemInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(final DocumentReference documentReference) {
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Map<String, Object> itemRef = new HashMap<>();
                        itemRef.put("itemRef", documentReference);

                        // Item created successfully: need to link item with its publisher

                        db.collection("users").document(user.getUid()).collection("publishedItems")
                                .add(itemRef)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Everything worked!
                                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                        Toast.makeText(GiverFormActivity.this, "Shared successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(GiverFormActivity.this, GatewayActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to add entry to the user's published items: delete entry altogether!
                                        Log.w("TAG", "Error adding document", e);
                                        db.collection("items").document(documentReference.getId()).delete();
                                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                        Toast.makeText(GiverFormActivity.this, "An error has occurred. Please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        Log.d("TAG", "finished form method");
    }
}