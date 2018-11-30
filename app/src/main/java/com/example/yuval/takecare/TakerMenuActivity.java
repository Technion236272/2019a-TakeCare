package com.example.yuval.takecare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SoundEffectConstants;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TakerMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    MenuItem[] prevNavGroupItem;
    private FeedRecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker_menu);

        //Set the toolbar as the AppBar for this activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up the onClick listener for the giver form button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakerMenuActivity.this, GiverMenuActivity.class);
                startActivity(intent);
            }
        });

        //Set up the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initialize three default checked items from the pickup drawer
        //Manipulating these items ensures that one item is always checked from each menu
        prevNavGroupItem = new MenuItem[3];
        prevNavGroupItem[0] = navigationView.getMenu().findItem(R.id.nav_feed_display);
        prevNavGroupItem[0].setChecked(true);
        prevNavGroupItem[1] = navigationView.getMenu().findItem(R.id.nav_show_all);
        prevNavGroupItem[1].setChecked(true);
        prevNavGroupItem[2] = navigationView.getMenu().findItem(R.id.nav_any_pickup);
        prevNavGroupItem[2].setChecked(true);

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        recyclerView = (FeedRecyclerView) findViewById(R.id.taker_feed_list);
        View emptyFeedView = findViewById(R.id.empty_feed_view);
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

    /*
    private List<FeedCardInformation> initData() {
        List<FeedCardInformation> list = new ArrayList<>();
        list.add(new FeedCardInformation("Yummy Muffins For All!", R.drawable.photo_muffin, R.drawable.photo_mcgiverface, "Giver McGiverFace", R.drawable.ic_pizza_slice_purple, R.drawable.ic_giveaway_purple));
        list.add(new FeedCardInformation("Driving to Tel-Aviv at Approx 7pm", R.drawable.photo_hittchhiker, R.drawable.ic_user_purple, "Israel M. Shalom", R.drawable.ic_car_purple, R.drawable.ic_race_purple));
        list.add(new FeedCardInformation("I Found An Umbrella Near Ullman", R.drawable.photo_umbrella, R.drawable.ic_user_purple, "Noa", R.drawable.ic_lost_and_found_purple, R.drawable.ic_in_person_purple));
        list.add(new FeedCardInformation("FREE PIZZAS IN TAUB'S BALCONY!! GET OVER HERE QUICKLY!!", R.drawable.photo_pizza, R.drawable.ic_user_purple, "Yuval", R.drawable.ic_pizza_slice_purple, R.drawable.ic_giveaway_purple));
        list.add(new FeedCardInformation("This Cool Nightstand!", R.drawable.photo_nightstand, R.drawable.ic_user_purple, "Tzvika", R.drawable.ic_lamp_purple, R.drawable.ic_race_purple));
        return list;
    }*/


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.taker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int groupId = item.getGroupId();
        if (groupId == R.id.display_group && prevNavGroupItem[0] != item) {
            prevNavGroupItem[0].setChecked(false);
            prevNavGroupItem[0] = item;
        } else if (groupId == R.id.categories_group && prevNavGroupItem[1] != item) {
            prevNavGroupItem[1].setChecked(false);
            prevNavGroupItem[1] = item;
        } else if (groupId == R.id.pickup_group && prevNavGroupItem[2] != item) {
            prevNavGroupItem[2].setChecked(false);
            prevNavGroupItem[2] = item;
        }
        item.setChecked(true);

        //TODO: Implement handlers for each drawer item
        switch (item.getItemId()) {
            case R.id.nav_map_display:
                break;
            case R.id.nav_show_all:
                break;
            case R.id.nav_food:
                break;
            case R.id.nav_study_material:
                break;
            case R.id.nav_furniture:
                break;
            case R.id.nav_lost_and_found:
                break;
            case R.id.nav_hitchhike:
                break;
            case R.id.nav_any_pickup:
                break;
            case R.id.nav_in_person:
                break;
            case R.id.nav_giveaway:
                break;
            case R.id.nav_race:
                break;
        }

        return false;
    }

    public void onItemCategoryPress(View view) {
        String str = "";
        if (view.getId() == R.id.item_category) {
            switch ((int) view.getTag()) {
                case R.drawable.ic_pizza_slice_purple:
                    str = "This item's category is food";
                    break;
                case R.drawable.ic_book_purple:
                    str = "This item's category is study material";
                    break;
                case R.drawable.ic_lamp_purple:
                    str = "This item's category is household objects";
                    break;
                case R.drawable.ic_lost_and_found_purple:
                    str = "This item's category is lost&founds";
                    break;
                case R.drawable.ic_car_purple:
                    str = "This item's category is hitchhiking";
                    break;
                default:
                    str = "This item is in a category of its own";
                    break;
            }
        } else {
            switch ((int) view.getTag()) {
                case R.drawable.ic_in_person_purple:
                    str = "This item is available for pick-up in person";
                    break;
                case R.drawable.ic_giveaway_purple:
                    str = "This item is available in a public giveaway";
                    break;
                case R.drawable.ic_race_purple:
                    str = "Race to get this item before anyone else!";
                    break;
                default:
                    break;
            }
        }
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    public void onReportPress(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.report_menu, menu.getMenu());
        menu.show();
    }

    public void tempFillItems(View view) {
        List<FeedCardInformation> list = new ArrayList<>();
        list.add(new FeedCardInformation("Yummy Muffins For All!", R.drawable.photo_muffin, R.drawable.photo_mcgiverface, "Giver McGiverFace", R.drawable.ic_pizza_slice_purple, R.drawable.ic_giveaway_purple));
        list.add(new FeedCardInformation("Driving to Tel-Aviv at Approx 7pm", R.drawable.photo_hittchhiker, R.drawable.ic_user_purple, "Israel M. Shalom", R.drawable.ic_car_purple, R.drawable.ic_race_purple));
        list.add(new FeedCardInformation("I Found An Umbrella Near Ullman", R.drawable.photo_umbrella, R.drawable.ic_user_purple, "Noa", R.drawable.ic_lost_and_found_purple, R.drawable.ic_in_person_purple));
        list.add(new FeedCardInformation("FREE PIZZAS IN TAUB'S BALCONY!! GET OVER HERE QUICKLY!!", R.drawable.photo_pizza, R.drawable.ic_user_purple, "Yuval", R.drawable.ic_pizza_slice_purple, R.drawable.ic_giveaway_purple));
        list.add(new FeedCardInformation("This Cool Nightstand!", R.drawable.photo_nightstand, R.drawable.ic_user_purple, "Tzvika", R.drawable.ic_lamp_purple, R.drawable.ic_race_purple));

        List<FeedCardInformation> cards = list;
        adapter = new TakerRVAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

    public void onTakerCardSelected(View view) {
        //TODO: add extra information to intent
        Intent intent = new Intent(this, ItemInfoActivity.class);
        startActivity(intent);
    }
}
