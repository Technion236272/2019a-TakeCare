package com.syv.takecare.takecare.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.adapters.SectionsPageAdapter;
import com.syv.takecare.takecare.customViews.CustomViewPager;
import com.syv.takecare.takecare.fragments.FeedListFragment;
import com.syv.takecare.takecare.fragments.FeedMapFragment;
import com.syv.takecare.takecare.fragments.GiverMessagesFragment;
import com.syv.takecare.takecare.fragments.MyAvailableItemsFragment;
import com.syv.takecare.takecare.fragments.MyExpiredItemsFragment;
import com.syv.takecare.takecare.fragments.MyRequestedItemsFragment;
import com.syv.takecare.takecare.fragments.MyTakenItemsFragment;
import com.syv.takecare.takecare.fragments.TakerMessagesFragment;

public class ChatLobbyActivity extends TakeCareActivity {

    private static final String TAG = "TakeCare/ChatLobby";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_lobby);
        Toolbar toolbar = findViewById(R.id.chat_lobby_toolbar);
        toolbar.setTitle("Chat");

        CustomViewPager viewPager = findViewById(R.id.chat_container);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            viewPager.setPagingEnabled(false);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.chat_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new GiverMessagesFragment(), "My Items");
        adapter.addFragment(new TakerMessagesFragment(), "Others' Items");
        viewPager.setAdapter(adapter);
    }
}
