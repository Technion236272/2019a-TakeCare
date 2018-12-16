package com.example.yuval.takecare;

import android.content.Intent;
import android.support.design.chip.ChipDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.yuval.takecare.utilities.VerticalImageSpan;

import java.util.ArrayList;
import java.util.List;

public class UserFavoritesActivity extends AppCompatActivity {

    private FeedRecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private int chipSpannedLength;
    private final int chipMaxLength = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorites);

        Toolbar toolbar = (Toolbar) findViewById(R.id.shared_items_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chipSpannedLength = 0;
        AppCompatEditText tagBox = findViewById(R.id.favorites_tag_box);

        tagBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == chipSpannedLength - chipMaxLength)
                {
                    chipSpannedLength = charSequence.length();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() - chipSpannedLength == chipMaxLength) {
                    ChipDrawable chip = ChipDrawable.createFromResource(UserFavoritesActivity.this, R.xml.chip);
                    chip.setText(editable.subSequence(chipSpannedLength,editable.length()));
                    chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
                    ImageSpan span = new ImageSpan(chip);
                    editable.setSpan(span, chipSpannedLength, editable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    chipSpannedLength = editable.length();
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, TakerMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
