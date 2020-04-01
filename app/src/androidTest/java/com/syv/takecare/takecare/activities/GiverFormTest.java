package com.syv.takecare.takecare.activities;


import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.syv.takecare.takecare.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GiverFormTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.CAMERA");

    @Test
    public void giverFormTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction ix = onView(
                allOf(withText("Sign In"),
                        childAtPosition(
                                allOf(withId(R.id.google_login_button),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                3)),
                                0),
                        isDisplayed()));
        ix.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.drawer_layout),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.upload_picture_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.item_image_card),
                                        0),
                                1)));
        appCompatImageButton.perform(scrollTo(), click());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(android.R.id.title), withText("Camera"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.category_food_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_1),
                                        0),
                                1)));
        appCompatImageButton2.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.category_study_material_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_1),
                                        1),
                                1)));
        appCompatImageButton3.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.category_households_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_1),
                                        2),
                                1)));
        appCompatImageButton4.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton5 = onView(
                allOf(withId(R.id.category_lost_and_found_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_2),
                                        0),
                                1)));
        appCompatImageButton5.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton6 = onView(
                allOf(withId(R.id.category_hitchhikes_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_2),
                                        1),
                                1)));
        appCompatImageButton6.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton7 = onView(
                allOf(withId(R.id.category_other_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categories_buttons_2),
                                        2),
                                1)));
        appCompatImageButton7.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.title_input),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.description_card),
                                        0),
                                1)));
        appCompatEditText.perform(scrollTo(), replaceText("Title"), closeSoftKeyboard());

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.item_description),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("Description"), closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.pickup_method_spinner),
                        childAtPosition(
                                allOf(withId(R.id.pickup_info_layout),
                                        childAtPosition(
                                                withId(R.id.pickup_time_card),
                                                0)),
                                1)));
        appCompatSpinner.perform(scrollTo(), click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(2);
        linearLayout.perform(click());

        ViewInteraction appCompatSpinner2 = onView(
                allOf(withId(R.id.pickup_method_spinner),
                        childAtPosition(
                                allOf(withId(R.id.pickup_info_layout),
                                        childAtPosition(
                                                withId(R.id.pickup_time_card),
                                                0)),
                                1)));
        appCompatSpinner2.perform(scrollTo(), click());

        DataInteraction linearLayout2 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        linearLayout2.perform(click());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.item_time),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.text_input_layout_time),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("12:00"), closeSoftKeyboard());

        ViewInteraction textInputEditText3 = onView(
                allOf(withId(R.id.item_location),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.text_input_layout_location),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText3.perform(replaceText("Haifa"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.air_time_change), withText("Change"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.form_scroll),
                                        0),
                                8)));
        appCompatTextView2.perform(scrollTo(), click());

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.add_keywords_text), withText("Add keywords"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.form_scroll),
                                        0),
                                12)));
        appCompatTextView3.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction nachoTextView = onView(
                allOf(withId(R.id.keywords_tag_box),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.form_scroll),
                                        0),
                                15)));
        nachoTextView.perform(scrollTo(), click());

        ViewInteraction nachoTextView2 = onView(
                allOf(withId(R.id.keywords_tag_box),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.form_scroll),
                                        0),
                                15)));
        nachoTextView2.perform(scrollTo(), replaceText(" \u001Ftag1\u001F  \u001Ftag2\u001F "), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.add_location_button), withText("Choose a location"),
                        childAtPosition(
                                allOf(withId(R.id.pickup_info_layout),
                                        childAtPosition(
                                                withId(R.id.pickup_time_card),
                                                0)),
                                6)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton8 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.giver_form_toolbar),
                                        childAtPosition(
                                                withClassName(is("android.support.design.widget.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton8.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
