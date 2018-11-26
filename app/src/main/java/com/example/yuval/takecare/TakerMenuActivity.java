package com.example.yuval.takecare;

import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Set;

public class TakerMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    MenuItem[] prevNavGroupItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prevNavGroupItem = new MenuItem[3];
        prevNavGroupItem[0] = navigationView.getMenu().findItem(R.id.nav_feed_display);
        prevNavGroupItem[0].setChecked(true);
        prevNavGroupItem[1] = navigationView.getMenu().findItem(R.id.nav_show_all);
        prevNavGroupItem[1].setChecked(true);
        prevNavGroupItem[2] = navigationView.getMenu().findItem(R.id.nav_in_person);
        prevNavGroupItem[2].setChecked(true);
    }

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int groupId = item.getGroupId();
        if(groupId == R.id.display_group && prevNavGroupItem[0] != item) {
            prevNavGroupItem[0].setChecked(false);
            prevNavGroupItem[0] = item;
        } else if(groupId == R.id.categories_group && prevNavGroupItem[1] != item) {
            prevNavGroupItem[1].setChecked(false);
            prevNavGroupItem[1] = item;
        } else if(groupId == R.id.pickup_group && prevNavGroupItem[2] != item) {
            prevNavGroupItem[2].setChecked(false);
            prevNavGroupItem[2] = item;
        }
        item.setChecked(true);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map_display) {
            // Handle the camera action
        } else if (id == R.id.nav_show_all) {

        } else if (id == R.id.nav_food) {

        } else if (id == R.id.nav_study_material) {

        } else if (id == R.id.nav_furniture) {

        } else if (id == R.id.nav_lost_and_found) {

        } else if (id == R.id.nav_hitchhike) {

        }
        
        return false;
    }
}
