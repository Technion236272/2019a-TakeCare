package com.syv.takecare.takecare;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_CONTACTS");

    @Test
    public void loginActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.sign_up_with_email_button), withText("Sign up with E-mail"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.login_screen_fragment),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.user_name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.user_name_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("name$"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.user_name), withText("name$"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.user_name_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatAutoCompleteTextView = onView(
                allOf(withId(R.id.email),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatAutoCompleteTextView.perform(replaceText("name.email.com"), closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView2 = onView(
                allOf(withId(R.id.email), withText("name.email.com"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatAutoCompleteTextView2.perform(pressImeActionButton());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("111"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.password), withText("111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(pressImeActionButton());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.confirm_password),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("123"), closeSoftKeyboard());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.confirm_password), withText("123"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText6.perform(pressImeActionButton());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.password), withText("111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText7.perform(click());

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.password), withText("111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText8.perform(replaceText("111111"));

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.password), withText("111111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText9.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.password), withText("111111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText10.perform(pressImeActionButton());

        ViewInteraction appCompatEditText11 = onView(
                allOf(withId(R.id.confirm_password), withText("123"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText11.perform(pressImeActionButton());

        ViewInteraction appCompatEditText12 = onView(
                allOf(withId(R.id.confirm_password), withText("123"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText12.perform(replaceText("111111"));

        ViewInteraction appCompatEditText13 = onView(
                allOf(withId(R.id.confirm_password), withText("111111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText13.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.confirm_password), withText("111111"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.confirm_password_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText14.perform(pressImeActionButton());

        ViewInteraction appCompatAutoCompleteTextView6 = onView(
                allOf(withId(R.id.email), withText("name.email.com"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatAutoCompleteTextView6.perform(replaceText("name@email.com"));

        ViewInteraction appCompatAutoCompleteTextView10 = onView(
                allOf(withId(R.id.email), withText("name@email.com"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatAutoCompleteTextView10.perform(closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView11 = onView(
                allOf(withId(R.id.email), withText("name@email.com"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatAutoCompleteTextView11.perform(pressImeActionButton());

        pressBack();

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.email_sign_in_button), withText("Continue"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.login_screen_fragment),
                                        0),
                                5),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatEditText15 = onView(
                allOf(withId(R.id.user_name), withText("name$"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.user_name_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText15.perform(click());

        ViewInteraction appCompatEditText16 = onView(
                allOf(withId(R.id.user_name), withText("name$"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.user_name_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText16.perform(replaceText("name"));

        ViewInteraction appCompatEditText17 = onView(
                allOf(withId(R.id.user_name), withText("name"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.user_name_input_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText17.perform(closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.email_sign_in_button), withText("Continue"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.login_screen_fragment),
                                        0),
                                5),
                        isDisplayed()));
        appCompatButton3.perform(click());
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
