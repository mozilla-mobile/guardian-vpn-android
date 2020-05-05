package org.mozilla.firefox.vpn.splash

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.MainActivity
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

@LargeTest
@RunWith(AndroidJUnit4::class)
class OnboardingTest {

//    @Rule
//    @JvmField
//    var mActivityTestRule = ActivityTestRule(SplashActivity::class.java)
//
//    @ClassRule
//    @JvmField
//    val localeTestRule: LocaleTestRule = LocaleTestRule()
    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun onboardingTest() {

        activityRule.launchActivity(null)
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(3000)

        Screengrab.screenshot("001-onboarding")

        val materialTextView = onView(
            allOf(
                withId(R.id.btn_intro),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_host_fragment),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        materialTextView.perform(click())

        val onboardingView = onView(allOf(withId(R.id.intro_title), isDisplayed()))

        Thread.sleep(700)
        Screengrab.screenshot("002-onboarding")
        onboardingView.perform(ViewActions.swipeLeft())
        Thread.sleep(700)
        Screengrab.screenshot("003-onboarding")
        onboardingView.perform(ViewActions.swipeLeft())
        Thread.sleep(700)
        Screengrab.screenshot("004-onboarding")
        onboardingView.perform(ViewActions.swipeLeft())
        Thread.sleep(700)
        Screengrab.screenshot("005-onboarding")

        val materialTextView2 = onView(
            allOf(
                withId(R.id.intro_list), isDisplayed()
            )
        )
//        materialTextView2.perform(click())
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position)
            }
        }
    }
}
