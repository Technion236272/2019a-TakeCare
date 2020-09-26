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

    public static int SHARING_BADGE = -1;
    public static int LIKES_BADGE = -1;
    public static boolean IN_PERSON_BADGE = false;
    public static boolean GIVEAWAY_BADGE = false;
    public static boolean RACE_BADGE = false;

    public static void checkForSharesBadgeEligibility(ImageView badge, long totalGivenItems) {
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
    }

    public static void checkForLikesBadgeEligibility(ImageView badge, long likesCount) {
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
    }

    public static void checkForCategorySharesBadgeEligibility(ImageView badge, String category, long shares) {
        badge.setVisibility(View.VISIBLE);
        if (shares >= CATEGORY_GOLD_BADGE_BAR) {
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_gold);
                    IN_PERSON_BADGE = true;
                    break;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_gold);
                    GIVEAWAY_BADGE = true;
                    break;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_gold);
                    RACE_BADGE = true;
                    break;
            }
        } else if (shares >= CATEGORY_SILVER_BADGE_BAR) {
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_silver);
                    IN_PERSON_BADGE = true;
                    break;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_silver);
                    GIVEAWAY_BADGE = true;
                    break;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_silver);
                    RACE_BADGE = true;
                    break;
            }
        } else if (shares >= CATEGORY_BRONZE_BADGE_BAR) {
            switch(category) {
                case "In Person":
                    badge.setImageResource(R.drawable.ic_personal_touch_bronze);
                    IN_PERSON_BADGE = true;
                    break;
                case "Giveaway":
                    badge.setImageResource(R.drawable.ic_one_for_all_bronze);
                    GIVEAWAY_BADGE = true;
                    break;
                case "Race":
                    badge.setImageResource(R.drawable.ic_time_files_bronze);
                    RACE_BADGE = true;
                    break;
            }
        } else {
            badge.setVisibility(View.GONE);
        }
    }
}
