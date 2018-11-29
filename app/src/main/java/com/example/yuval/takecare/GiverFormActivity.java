package com.example.yuval.takecare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GiverFormActivity extends AppCompatActivity {
    String[] spinnerNames;
    int[] spinnerIcons;
    Calendar calander;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver_form);

        // Pickup method spinner initialization
        spinnerNames = new String[]{"In Person","Giveaway","Race"};
        spinnerIcons = new int[]{R.drawable.ic_in_person,R.drawable.ic_giveaway,R.drawable.ic_race};
        IconTextAdapter ita = new IconTextAdapter(this,spinnerNames,spinnerIcons);
        Spinner spinner = (Spinner) findViewById(R.id.pickup_method_spinner);
        spinner.setAdapter(ita);

        calander = Calendar.getInstance();



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
        }, cHour, cMinute,true);
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
                c.set(year,month,dayOfMonth);
                SimpleDateFormat dateFormat= new SimpleDateFormat("EEEE, MMMM d");
                time_slot_text.append(dateFormat.format(c.getTime()));
                int cHour = calander.get(Calendar.HOUR);
                int cMinute = calander.get(Calendar.MINUTE);
                TimePickerDialog startTime = new TimePickerDialog(GiverFormActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time_slot_text.append(" " + (hourOfDay < 10 ? "0":"") + hourOfDay + ":" + (minute < 10 ? "0":"")+ minute + " - ");
                        TimePickerDialog endTime = new TimePickerDialog(GiverFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                time_slot_text.append((hourOfDay < 10 ? "0":"") + hourOfDay + ":" + (minute < 10 ? "0":"") + minute + "\n");
                            }
                        },hourOfDay,minute,true);
                        endTime.show();
                    }
                }, cHour, cMinute,true);
                startTime.show();
            }
        }, cYear, cMonth, cDay);
        dpd.setTitle("Pick a Time");
        dpd.show();
    }

    public void deadlinePressed(View view) {
        EditText e = findViewById(R.id.flexible_text);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.VISIBLE);
        e.setVisibility(View.GONE);

    }

    public void flexiblePressed(View view) {
        EditText e = findViewById(R.id.flexible_text);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.GONE);
        e.setVisibility(View.VISIBLE);
    }

    public void nowPressed(View view) {
        EditText e = findViewById(R.id.flexible_text);
        SeekBar s = findViewById(R.id.seekBar);
        s.setVisibility(View.GONE);
        e.setVisibility(View.GONE);
    }
}
