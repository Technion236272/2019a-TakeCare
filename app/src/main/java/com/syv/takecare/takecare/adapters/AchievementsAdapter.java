package com.syv.takecare.takecare.adapters;

import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.syv.takecare.takecare.BuildConfig;
import com.syv.takecare.takecare.R;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.syv.takecare.takecare.utilities.AchievementsFunctions.ALTRUIST_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.AUDIENCE_FAVORITE_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_BRONZE_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_GOLD_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.CATEGORY_SILVER_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.COMMUNITY_HERO_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.GIVEAWAY_BADGE;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.GOOD_NEIGHBOUR_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.IN_PERSON_BADGE;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.LEGENDARY_SHARER;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.LIKES_BADGE;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.LOCAL_CELEBRITY_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.PHILANTHROPIST_BADGE_BAR;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.RACE_BADGE;
import static com.syv.takecare.takecare.utilities.AchievementsFunctions.SHARING_BADGE;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementsViewHolder> {
    private static final int SHARING_BADGES = 4;
    private static final int LIKES_BADGES = 3;
    private static final int PICKUP_METHOD_BADGES = 3;

    Resources res;
    int[] achievementsIconsResources;
    String[] achievementsTitles;
    String[] achievementsRequirements;
    String[] achievementsDescriptions;
    boolean[] openDescriptions;
    boolean[] activeAchievements;

    public static class AchievementsViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layout;
        ImageView badgeIcon;
        TextView badgeName;
        TextView badgeRequirements;
        TextView badgeDescription;

        public AchievementsViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (FrameLayout)itemView;
            badgeIcon = itemView.findViewById(R.id.badge_icon);
            badgeName = itemView.findViewById(R.id.badge_name);
            badgeRequirements = itemView.findViewById(R.id.badge_requirement);
            badgeDescription = itemView.findViewById(R.id.badge_description);
        }
    }

    public AchievementsAdapter(View view) {
        res = view.getResources();
        achievementsIconsResources = new int[] {
                R.drawable.ic_good_neighbor,
                R.drawable.ic_altruism,
                R.drawable.ic_philanthropist,
                R.drawable.ic_superhero,
                R.drawable.ic_audience_favorite,
                R.drawable.ic_celebrity,
                R.drawable.ic_legendary,
                R.drawable.ic_personal_touch_grouped,
                R.drawable.ic_one_for_all_grouped,
                R.drawable.ic_time_files_grouped
        };
        achievementsTitles = res.getStringArray(R.array.achievements_titles);
        String categoryAllBars = "" + CATEGORY_GOLD_BADGE_BAR + "/" + CATEGORY_SILVER_BADGE_BAR + "/" + CATEGORY_BRONZE_BADGE_BAR;
        achievementsRequirements = new String[] {
                "" + GOOD_NEIGHBOUR_BADGE_BAR + " " + res.getString(R.string.given_items_requirement),
                "" + ALTRUIST_BADGE_BAR + " " + res.getString(R.string.given_items_requirement),
                "" + PHILANTHROPIST_BADGE_BAR + " " + res.getString(R.string.given_items_requirement),
                "" + COMMUNITY_HERO_BADGE_BAR + " " + res.getString(R.string.given_items_requirement),
                "" + AUDIENCE_FAVORITE_BADGE_BAR + " " + res.getString(R.string.likes_requirement),
                "" + LOCAL_CELEBRITY_BADGE_BAR + " " + res.getString(R.string.likes_requirement),
                "" + LEGENDARY_SHARER + " " + res.getString(R.string.likes_requirement),
                categoryAllBars + " " + res.getString(R.string.in_person_requirement),
                categoryAllBars + " " + res.getString(R.string.giveaway_requirement),
                categoryAllBars + " " + res.getString(R.string.race_requirement),
        };
        achievementsDescriptions = view.getResources().getStringArray(R.array.achievements_descriptions);
        openDescriptions = new boolean[getItemCount()];
        activeAchievements = new boolean[getItemCount()];
        if (SHARING_BADGE >= 0) activeAchievements[SHARING_BADGE] = true;
        if (LIKES_BADGE >= 0) activeAchievements[SHARING_BADGES + LIKES_BADGE] = true;
        activeAchievements[SHARING_BADGES + LIKES_BADGES] = IN_PERSON_BADGE;
        activeAchievements[SHARING_BADGES + LIKES_BADGES + 1] = GIVEAWAY_BADGE;
        activeAchievements[SHARING_BADGES + LIKES_BADGES + 2] = RACE_BADGE;
    }

    @NonNull
    @Override
    public AchievementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final FrameLayout layout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.achievement_info, parent, false);
        return new AchievementsViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull final AchievementsViewHolder holder, final int position) {
        holder.badgeIcon.setImageDrawable(ResourcesCompat.getDrawable(res, achievementsIconsResources[position], null));
        holder.badgeName.setText(achievementsTitles[position]);
        holder.badgeRequirements.setText(achievementsRequirements[position]);
        holder.badgeDescription.setText(achievementsDescriptions[position]);
        if (position >= 7) {
            holder.badgeRequirements.setTextSize(14);
        }
        if (!activeAchievements[position]) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.badgeIcon.setColorFilter(filter);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openDescriptions[position]) {
                    holder.badgeDescription.setVisibility(View.GONE);
                    openDescriptions[position] = false;
                } else {
                    holder.badgeDescription.setVisibility(View.VISIBLE);
                    openDescriptions[position] = true;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG && !(achievementsIconsResources.length == achievementsTitles.length &&
                achievementsTitles.length == achievementsRequirements.length &&
                achievementsRequirements.length == achievementsDescriptions.length)) {
            throw new AssertionError("Assertion failed");
        }
        return achievementsIconsResources.length;
    }
}
