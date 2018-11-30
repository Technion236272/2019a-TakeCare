package com.example.yuval.takecare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RequestedItemsActivity extends AppCompatActivity {

    private FeedRecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requested_items);

        Toolbar toolbar = (Toolbar) findViewById(R.id.shared_items_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setUpRecyclerView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        android.R.id.home
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_user_settings:
                intent = new Intent(this, UserProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.action_my_items:
                intent = new Intent(this, SharedItemsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_requested_items:
                intent = new Intent(this, RequestedItemsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_favorites:
                intent = new Intent(this, UserFavoritesActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        recyclerView = (FeedRecyclerView) findViewById(R.id.taker_feed_list);
        View emptyFeedView = findViewById(R.id.shared_items_empty_feed_view);
        //Optimizing recycler view's performance:
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //TODO: properly implement factory with input stream once database is ready

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<FeedCardInformation> cards = new ArrayList<>();
        adapter = new TakerRVAdapter(cards); //List is still empty
        recyclerView.setAdapter(adapter);
        //Set the view to be displayed when the FeedRecyclerView is empty!
        recyclerView.setEmptyView(emptyFeedView);
    }
}
