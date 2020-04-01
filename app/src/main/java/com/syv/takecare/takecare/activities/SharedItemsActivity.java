package com.syv.takecare.takecare.activities;

import android.content.res.Configuration;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.syv.takecare.takecare.customViews.CustomViewPager;
import com.syv.takecare.takecare.fragments.MyAvailableItemsFragment;
import com.syv.takecare.takecare.fragments.MyExpiredItemsFragment;
import com.syv.takecare.takecare.fragments.MyTakenItemsFragment;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.adapters.SectionsPageAdapter;

public class SharedItemsActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare/Login";

    private SectionsPageAdapter sectionsPageAdapter;
    private CustomViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_items);
        Log.d(TAG, "onCreate: Starting.");

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.shared_items_container);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            viewPager.setPagingEnabled(false);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.shared_items_tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.shared_items_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedItemsActivity.super.onBackPressed();
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
//        adapter.addFragment(new MyRequestedItemsFragment(), "Requested"); <- this is scrapped
        adapter.addFragment(new MyAvailableItemsFragment(), "Available");
        adapter.addFragment(new MyTakenItemsFragment(), "Taken");
        adapter.addFragment(new MyExpiredItemsFragment(), "Expired");
        viewPager.setAdapter(adapter);
    }
}