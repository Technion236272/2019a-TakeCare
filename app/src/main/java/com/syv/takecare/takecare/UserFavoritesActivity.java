package com.syv.takecare.takecare;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hootsuite.nachos.ChipConfiguration;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class UserFavoritesActivity extends AppCompatActivity {
    private static final String TAG = "TakeCare";
    private static final List<Character> TERMINATORS = Arrays.asList('\n', ';', ',');
    private static final int TAGS_SEPARATING_LENGTH = 4;
    private static final int CHIP_MAX_LENGTH = 15;
    private static final int CHIP_MIN_LENGTH = 3;
    private static final int POPUP_ACTIVE_DURATION = 5000;

    private LinearLayout rootLayout;
    private NachoTextView tagsBox;
    private AppCompatButton tagsBtn;
    private AppCompatImageView tagsHelpBtn;
    private ToolTipRelativeLayout tooltipLayout;
    private ToolTipView toolTipView;

    private Handler tooltipHandler = new Handler();
    private Runnable tooltipTask;

    private Handler suggestionsHandler = new Handler();
    private Runnable suggestionsTask;
    private List<String> autoCompleteSuggestions = new ArrayList<>();
    private List<String> allExistingTags = new ArrayList<>();
    private int tagsAmount = 0;
    private String tagsCopy;
    private boolean isPopupOpen;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

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

        rootLayout = (LinearLayout) findViewById(R.id.root);
        tagsBox = (NachoTextView) findViewById(R.id.favorites_tag_box);
        tagsBtn = (AppCompatButton) findViewById(R.id.save_keywords_button);
        tagsHelpBtn = (AppCompatImageView) findViewById(R.id.tags_help);
        tooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.tags_help_tooltip);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();


        addAutoCompleteOptions();
        configureTagsBox();
        configureTagsButton();
        configureTagsHelpButton();
    }

    private void configureTagsBox() {
        tagsBox.setVisibility(View.GONE);

        final ProgressDialog dialog = new ProgressDialog(UserFavoritesActivity.this);
        dialog.setMessage("Loading your keywords...");
        dialog.show();

        for (char c : TERMINATORS) {
            tagsBox.addChipTerminator(c, ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        }

        tagsBox.setChipTokenizer(new SpanChipTokenizer<>(this, new ChipSpanChipCreator() {
            @Override
            public ChipSpan createChip(@NonNull Context context, @NonNull CharSequence text, Object data) {
                return new ChipSpan(context, text, ContextCompat.getDrawable(UserFavoritesActivity.this, R.drawable.ic_edit_white), data);
            }

            @Override
            public void configureChip(@NonNull ChipSpan chip, @NonNull ChipConfiguration chipConfiguration) {
                super.configureChip(chip, chipConfiguration);
                chip.setShowIconOnLeft(true);
                chip.setIconBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }, ChipSpan.class));

        tagsBox.enableEditChipOnTouch(false, true);

        tagsCopy = tagsBox.getText().toString();

        if (user == null) {
            tagsBox.setVisibility(View.VISIBLE);
        }

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "onSuccess: successfully loaded keywords");
                        List<String> tags = (List<String>) documentSnapshot.get("tags");
                        initChips(tags);
                        tagsAmount = tagsBox.getChipValues().size();
                        suggestionsHandler.post(suggestionsTask);

                        tagsBox.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (s.toString().equals(tagsCopy)) {
                                    if (tagsBtn.getVisibility() == View.VISIBLE) {
                                        Log.d(TAG, "keywords unchanged");
                                        tagsBtn.setVisibility(View.GONE);
                                    }
                                } else if (tagsBtn.getVisibility() == View.GONE) {
                                    // The keyword are different than the original configuration: show accept button
                                    tagsBtn.setVisibility(View.VISIBLE);
                                }

                                // Check if the user has entered or deleted a keyword, in order to manage suggestions
                                if (tagsAmount != tagsBox.getAllChips().size()) {
                                    Log.d(TAG, "change in chips detected");
                                    suggestionsHandler.removeCallbacks(suggestionsTask);
                                    suggestionsHandler.post(suggestionsTask);
                                }
                            }
                        });

                        dialog.dismiss();
                        tagsBox.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: could not load keywords");
                        dialog.dismiss();
                        makeHighlightedSnackbar("Connection error: couldn\'t load keywords");
                    }
                });

        tagsBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    toolTipView.remove();
                    tagsHelpBtn.setAlpha(0.7f);
                    isPopupOpen = false;
                }
            }
        });
    }

    private void initChips(Collection<String> tags) {
        if (tags == null || tags.isEmpty()) {
            tagsBox.setText("");
            tagsCopy = tagsBox.getText().toString();
            return;
        }
        StringBuilder tagsTextBuilder = new StringBuilder();
        for (String tag : tags) {
            tagsTextBuilder.append(tag);
        }

        tagsBox.setText(tagsTextBuilder.toString());
        int index = 0;
        for (String tag : tags) {
            tagsBox.chipify(index, index + tag.length());
            index += tag.length() + TAGS_SEPARATING_LENGTH;
        }
        tagsCopy = tagsBox.getText().toString();
    }

    private void configureTagsButton() {
        tagsBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_add_favorite_white), null);

        tagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "saving keywords");
                if (user == null)
                    return;

                final ProgressDialog dialog = new ProgressDialog(UserFavoritesActivity.this);
                dialog.setMessage("Saving keywords...");
                dialog.show();
                tagsBox.chipifyAllUnterminatedTokens();

                // Filter duplicates & long keywords
                List<String> chosenTags = tagsBox.getChipValues();
                Set<String> uniqueTags = new HashSet<>(chosenTags);
                boolean removedDuplicateKeywords = uniqueTags.size() != chosenTags.size(),
                        removedLongKeywords = false, removedShortKeywords = false;

                for (Iterator<String> iterator = uniqueTags.iterator(); iterator.hasNext(); ) {
                    String tag = iterator.next();
                    if (tag.length() > CHIP_MAX_LENGTH) {
                        iterator.remove();
                        removedLongKeywords = true;
                    } else if (tag.length() < CHIP_MIN_LENGTH) {
                        iterator.remove();
                        removedShortKeywords = true;
                    }
                }

                chosenTags.clear();
                chosenTags.addAll(uniqueTags);
                initChips(chosenTags);

                Log.d(TAG, "adding keywords to database");
                final boolean finalRemovedDuplicateKeywords = removedDuplicateKeywords;
                final boolean finalRemovedLongKeywords = removedLongKeywords;
                final boolean finalRemovedShortKeywords = removedShortKeywords;
                db.collection("users").document(user.getUid())
                        .update("tags", chosenTags)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "successfully added keywords to database");
                                tagsCopy = tagsBox.getText().toString();
                                String extraMsg = "";
                                if (finalRemovedDuplicateKeywords && finalRemovedLongKeywords && finalRemovedShortKeywords) {
                                    extraMsg = "\nDuplicates, short and long words were removed";
                                } else if (finalRemovedDuplicateKeywords && finalRemovedLongKeywords) {
                                    extraMsg = "\nDuplicates and long words were removed";
                                } else if (finalRemovedDuplicateKeywords && finalRemovedShortKeywords) {
                                    extraMsg = "\nDuplicates and short words were removed";
                                } else if (finalRemovedShortKeywords && finalRemovedLongKeywords) {
                                    extraMsg = "\nShort and long words were removed";
                                } else if (finalRemovedDuplicateKeywords) {
                                    extraMsg = "\nDuplicates were removed";
                                } else if (finalRemovedLongKeywords) {
                                    extraMsg = "\nLong words were removed";
                                } else if (finalRemovedShortKeywords) {
                                    extraMsg = "\nShort words were removed";
                                }

                                dialog.dismiss();
                                tagsBtn.setVisibility(View.GONE);

                                String msg = "Updated keywords!";
                                String fullMsg = msg + extraMsg;

                                SpannableStringBuilder ssb = new SpannableStringBuilder()
                                        .append(fullMsg);
                                ssb.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                                        msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ssb.setSpan(new ForegroundColorSpan(Color.YELLOW), msg.length(),
                                        fullMsg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                Snackbar.make(rootLayout, ssb, Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "failed to add keywords to database");
                                makeHighlightedSnackbar("An error occurred. Please try again");
                                dialog.dismiss();
                            }
                        });
            }
        });
    }


    private void configureTagsHelpButton() {
        isPopupOpen = false;
        tooltipTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "tooltip timed out!");
                toolTipView.remove();
                tagsHelpBtn.setAlpha(0.7f);
                isPopupOpen = false;
            }
        };

        final ToolTip tooltip = new ToolTip()
                .withText(getResources().getString(R.string.favorites_help_popup_text))
                .withColor(getResources().getColor(R.color.colorPrimaryDark))
                .withTextColor(Color.WHITE)
                .withShadow()
                .withAnimationType(ToolTip.AnimationType.FROM_MASTER_VIEW);

        tagsHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isPopupOpen)
                    return;
                tagsBox.clearFocus();
                hideKeyboard(UserFavoritesActivity.this);
                isPopupOpen = true;
                tagsHelpBtn.setAlpha(1.0f);
                toolTipView = tooltipLayout.showToolTipForView(tooltip, tagsHelpBtn);
                toolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                    @Override
                    public void onToolTipViewClicked(ToolTipView toolTipView) {
                        toolTipView.remove();
                        tagsHelpBtn.setAlpha(0.7f);
                        isPopupOpen = false;
                    }
                });

                tooltipHandler.removeCallbacks(tooltipTask);
                tooltipHandler.postDelayed(tooltipTask, POPUP_ACTIVE_DURATION);
            }
        });
    }


    private void addAutoCompleteOptions() {
        Query query = db.collection("tags")
                .orderBy("tag");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Listen failed with: " + e);
                    return;
                }

                if (queryDocumentSnapshots == null) {
                    Log.d(TAG, "Did not find any tags in database");
                    return;
                }

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("tag") != null) {
                        allExistingTags.add(doc.getString("tag"));
                    }
                }

                suggestionsHandler.removeCallbacks(suggestionsTask);
                suggestionsHandler.post(suggestionsTask);
            }
        });

        suggestionsTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "detected keyword suggestions changes");
                setAutoCompleteAdapter();
            }
        };
    }


    private void setAutoCompleteAdapter() {
        autoCompleteSuggestions.clear();
        List<String> currentKeywords = tagsBox.getChipValues();
        tagsAmount = currentKeywords.size();
        for (String keyword : allExistingTags) {
            if (!currentKeywords.contains(keyword)) {
                autoCompleteSuggestions.add(keyword);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (getApplicationContext(), R.layout.auto_complete_dropdown_item, autoCompleteSuggestions);
        tagsBox.setAdapter(adapter);
        Log.d(TAG, "set the auto-complete adapter");
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (tagsCopy.equals(tagsBox.getText().toString())) {
            super.onBackPressed();
            return;
        }

        tagsBox.clearFocus();
        hideKeyboard(UserFavoritesActivity.this);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Unsaved Keywords")
                .setMessage("Are you sure you don\'t want to save your keywords?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Discard the newly entered keywords
                        UserFavoritesActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing: dismiss alert dialog
                    }
                })
                .show();
    }

    private void makeHighlightedSnackbar(String str) {
        Snackbar snackbar = Snackbar
                .make(rootLayout, str, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
