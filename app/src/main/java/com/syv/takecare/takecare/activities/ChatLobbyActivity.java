package com.syv.takecare.takecare.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.adapters.SectionsPageAdapter;
import com.syv.takecare.takecare.customViews.CustomViewPager;
import com.syv.takecare.takecare.fragments.GiverMessagesFragment;
import com.syv.takecare.takecare.fragments.TakerMessagesFragment;

public class ChatLobbyActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare/ChatLobby";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_lobby);
        Toolbar toolbar = findViewById(R.id.chat_lobby_toolbar);
        setToolbar(toolbar);

        CustomViewPager viewPager = findViewById(R.id.chat_container);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            viewPager.setPagingEnabled(false);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.chat_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new GiverMessagesFragment(), "My Items");
        adapter.addFragment(new TakerMessagesFragment(), "Requested Items");
        viewPager.setAdapter(adapter);
    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}
