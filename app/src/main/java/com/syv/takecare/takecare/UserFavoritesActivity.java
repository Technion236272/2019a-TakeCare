package com.syv.takecare.takecare;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hootsuite.nachos.ChipConfiguration;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;

public class UserFavoritesActivity extends AppCompatActivity {
    private NachoTextView tagsBox;

//    private int chipSpannedLength;
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

        /*chipSpannedLength = 0;
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
        });*/

        tagsBox = (NachoTextView) findViewById(R.id.favorites_tag_box);
        tagsBox.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        tagsBox.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        tagsBox.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        tagsBox.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        tagsBox.setChipTokenizer(new SpanChipTokenizer<>(this, new ChipSpanChipCreator() {
            @Override
            public ChipSpan createChip(@NonNull Context context, @NonNull CharSequence text, Object data) {
                return new ChipSpan(context, text, ContextCompat.getDrawable(UserFavoritesActivity.this, R.drawable.ic_close), data);
            }

            @Override
            public void configureChip(@NonNull ChipSpan chip, @NonNull ChipConfiguration chipConfiguration) {
                super.configureChip(chip, chipConfiguration);
                chip.setShowIconOnLeft(true);
                chip.setIconBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }, ChipSpan.class));

        tagsBox.enableEditChipOnTouch(false, true);
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
