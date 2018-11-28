package com.example.yuval.takecare;

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

public class SharedItemsActivity extends AppCompatActivity {

    private FeedRecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_items);

        Toolbar toolbar = (Toolbar) findViewById(R.id.shared_items_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setUpRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //TODO: create intent to move back to taker feed
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.taker_menu, menu);
        return true;
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
