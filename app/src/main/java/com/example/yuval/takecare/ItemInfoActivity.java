package com.example.yuval.takecare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

public class ItemInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

        /*
        final TextView distanceBox = findViewById(R.id.distance_text);
        final TextView hoursBox = findViewById(R.id.pickup_time_text);
        final ViewTreeObserver observer= hoursBox.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int hoursBoxHeight = hoursBox.getHeight();
                distanceBox.setHeight(hoursBoxHeight);
                hoursBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        */
    }
}
