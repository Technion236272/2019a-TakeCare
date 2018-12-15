package com.example.yuval.takecare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GatewayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gateway);
    }

    public void openTakerMenu(View view) {
        //Create intent to navigate to the taker menu
        Intent intent = new Intent(this, TakerMenuActivity.class);
        startActivity(intent);
    }

    public void openGiverForm(View view) {
        //Create intent to navigate to the giver menu
        Intent intent = new Intent(this, GiverMenuActivity.class);
        startActivity(intent);
    }
}
