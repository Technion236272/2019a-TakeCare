package com.syv.takecare.takecare.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.adapters.SectionsPageAdapter;
import com.syv.takecare.takecare.customViews.CustomViewPager;
import com.syv.takecare.takecare.fragments.AcceptedRequestsFragment;
import com.syv.takecare.takecare.fragments.PendingRequestsFragment;

import java.util.Locale;

public class RequestedItemsActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare/RequestedItems";

    private SectionsPageAdapter sectionsPageAdapter;
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requested_items);
        Log.d(TAG, "onCreate: Starting.");

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.requested_items_container);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            viewPager.setPagingEnabled(false);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.requested_items_tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.requested_items_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestedItemsActivity.super.onBackPressed();
            }
        });

        setTitle(R.string.requested_items_menu_title);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        if (!getResources().getConfiguration().locale.getLanguage().equals("iw")) {
            adapter.addFragment(new PendingRequestsFragment(), getString(R.string.requested_items_pending_tab));
            adapter.addFragment(new AcceptedRequestsFragment(), getString(R.string.requested_items_accepted_tab));
            // This is scrapped:
//        adapter.addFragment(new RejectedRequestsFragment(), "Rejected");
            viewPager.setAdapter(adapter);
        } else {
            adapter.addFragment(new AcceptedRequestsFragment(), getString(R.string.requested_items_accepted_tab));
            adapter.addFragment(new PendingRequestsFragment(), getString(R.string.requested_items_pending_tab));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(1);
        }
    }
}