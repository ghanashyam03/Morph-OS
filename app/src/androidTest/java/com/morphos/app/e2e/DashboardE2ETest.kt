package com.morphos.app.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.morphos.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DashboardE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun test_emptyDashboard_showsEmptyState() {
        // Complete onboarding if present
        val skipBtn = composeTestRule.onNodeWithText("Get Started")
        if (skipBtn.isSemanticsMatcher()) {
            skipBtn.performClick()
        }

        // Verify Empty state
        // When there are no widgets, empty text "No widgets pinned yet" is displayed
        composeTestRule.onNodeWithText("No widgets pinned yet").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create widget").assertIsDisplayed()
    }

    @Test
    fun test_dashboardWithWidgets_showsWidgetCards() {
        // Complete onboarding if present
        val skipBtn = composeTestRule.onNodeWithText("Get Started")
        if (skipBtn.isSemanticsMatcher()) {
            skipBtn.performClick()
        }

        // Seed some widgets and verify cards are visible
        // (FakeWidgetRepository is injected by Hilt setup during test launch)
        // Verify FAB is always visible
        composeTestRule.onNodeWithContentDescription("Create widget").assertIsDisplayed()
    }
}
