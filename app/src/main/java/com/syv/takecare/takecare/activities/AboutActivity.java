package com.syv.takecare.takecare.activities;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class AboutActivity extends TakeCareActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.credits_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        TextView termsOfUse = findViewById(R.id.icon_credits_text);
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());

        setTitle(R.string.about_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
