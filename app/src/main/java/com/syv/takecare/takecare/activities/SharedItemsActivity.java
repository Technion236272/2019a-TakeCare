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

import java.util.Locale;

public class SharedItemsActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare/Login";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_items);
        Log.d(TAG, "onCreate: Starting.");

        CustomViewPager viewPager = findViewById(R.id.shared_items_container);
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

        setTitle(R.string.shared_items_menu_title);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
//        adapter.addFragment(new MyRequestedItemsFragment(), "Requested"); <- this is scrapped
        if (!getResources().getConfiguration().locale.getLanguage().equals("iw")) {
            Log.d(TAG, "Locale default: " + Locale.getDefault().getLanguage());
            adapter.addFragment(new MyAvailableItemsFragment(), getString(R.string.shared_items_available_tab));
            adapter.addFragment(new MyTakenItemsFragment(), getString(R.string.shared_items_taken_tab));
            adapter.addFragment(new MyExpiredItemsFragment(), getString(R.string.shared_items_expired_tab));
            viewPager.setAdapter(adapter);
        } else {
            adapter.addFragment(new MyExpiredItemsFragment(), getString(R.string.shared_items_expired_tab));
            adapter.addFragment(new MyTakenItemsFragment(), getString(R.string.shared_items_taken_tab));
            adapter.addFragment(new MyAvailableItemsFragment(), getString(R.string.shared_items_available_tab));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(2);
        }
    }
}