package com.morphos.app.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.morphos.app.MainActivity
import com.morphos.app.core.testing.FakeWidgetRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateWidgetE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun test_createWidget_fromNLInput_toSuccessScreen() {
        // Complete onboarding if present
        val skipBtn = composeTestRule.onNodeWithText("Get Started")
        if (skipBtn.isSemanticsMatcher()) {
            skipBtn.performClick()
        }

        // 1. Click FAB "New Widget"
        composeTestRule.onNodeWithContentDescription("Create widget").performClick()

        // 2. Type "Build me a study dashboard"
        composeTestRule.onNodeWithText("e.g. Build me a study dashboard").performTextReplacement("Build me a study dashboard")

        // 3. Click "Generate Widget"
        composeTestRule.onNodeWithText("Generate Widget").performClick()

        // 4. Wait for processing (isProcessing loader is handled by Compose UI test thread synchronization automatically)
        composeTestRule.waitForIdle()

        // 5. Verify WidgetPreviewScreen is shown
        composeTestRule.onNodeWithText("Widget Preview").assertIsDisplayed()

        // 6. Click "Create Widget"
        composeTestRule.onNodeWithText("Create Widget").performClick()

        // 7. Verify "Widget Created!" text is shown
        composeTestRule.onNodeWithText("Widget Created!").assertIsDisplayed()

        // 8. Go back to Dashboard
        composeTestRule.onNodeWithText("Go to Dashboard").performClick()

        // 9. Verify widget card is visible
        composeTestRule.onNodeWithText("Adaptive Dashboard").assertIsDisplayed()
    }
}
