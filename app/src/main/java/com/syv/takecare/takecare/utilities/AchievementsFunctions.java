package com.syv.takecare.takecare.utilities;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.syv.takecare.takecare.R;

public abstract class AchievementsFunctions {

    public static final int GOOD_NEIGHBOUR_BADGE_BAR = 5;
    public static final int ALTRUIST_BADGE_BAR = 15;
    public static final int PHILANTHROPIST_BADGE_BAR = 25;
    public static final int COMMUNITY_HERO_BADGE_BAR = 50;
    public static final int AUDIENCE_FAVORITE_BADGE_BAR = 15;
    public static final int LOCAL_CELEBRITY_BADGE_BAR = 25;
    public static final int LEGENDARY_SHARER = 50;
    public static final int CATEGORY_BRONZE_BADGE_BAR = 15;
    public static final int CATEGORY_SILVER_BADGE_BAR = 25;
    public static final int CATEGORY_GOLD_BADGE_BAR = 50;

    public static int checkForSharesBadgeEligibility(ImageView badge, Long totalGivenItems) {
        if (totalGivenItems == null) {
            return -1;
        }
        int SHARING_BADGE = -1;
        badge.setVisibility(View.VISIBLE);
        if (totalGivenItems >= COMMUNITY_HERO_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_superhero);
            SHARING_BADGE = 3;
        } else if (totalGivenItems >= PHILANTHROPIST_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_philanthropist);
            SHARING_BADGE = 2;
        } else if (totalGivenItems >= ALTRUIST_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_altruism);
            SHARING_BADGE = 1;
        } else if (totalGivenItems >= GOOD_NEIGHBOUR_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_good_neighbor);
            SHARING_BADGE = 0;
        } else {
            badge.setVisibility(View.GONE);
        }
        return SHARING_BADGE;
    }

    public static int checkForLikesBadgeEligibility(ImageView badge, Long likesCount) {
        if (likesCount == null) {
            return -1;
        }
        int LIKES_BADGE = -1;
        badge.setVisibility(View.VISIBLE);
        if (likesCount >= LEGENDARY_SHARER) {
            badge.setImageResource(R.drawable.ic_legendary);
            LIKES_BADGE = 2;
        } else if (likesCount >= LOCAL_CELEBRITY_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_celebrity);
            LIKES_BADGE = 1;
        } else if (likesCount >= AUDIENCE_FAVORITE_BADGE_BAR) {
            badge.setImageResource(R.drawable.ic_audience_favorite);
            LIKES_BADGE = 0;
        } else {
            badge.setVisibility(View.GONE);
        }
        return LIKES_BADGE;
    }

    public static boolean checkForCategorySharesBadgeEligibility(ImageView badge, String category, Long shares) {
        if (shares == null || shares < CATEGORY_BRONZE_BADGE_BAR) {
            return false;
        }
        badge.setVisibility(View.VISIBLE);
        if (shares >= CATEGORY_GOLD_BADGE_BAR) {
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_gold);
                    return true;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_gold);
                    return true;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_gold);
                    return true;
            }
        } else if (shares >= CATEGORY_SILVER_BADGE_BAR) {
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_silver);
                    return true;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_silver);
                    return true;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_silver);
                    return true;
            }
        } else { // shares >= CATEGORY_BRONZE_BADGE_BAR
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_bronze);
                    return true;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_bronze);
                    return true;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_bronze);
                    return true;
            }
        }
        return false;   // Should be unreachable given proper input
    }
}
