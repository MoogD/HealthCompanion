package com.dom.healthcompanion.ui.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.dom.logger.Logger
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavComponentKtTest {
    // region test cases

    // 1- When initialized, then show feature list screen with featureListViewModel items

    // 2- When navigator navTarget updated, given new target is FEATURE_LIST, then show FeatureListScreen with featureListViewModel items
    // 3- When navigator navTarget updated, given new target is BREATHING, then show BreathingScreen with BreathingViewModel flows

    // endregion

    @get:Rule(order = 0)
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private val mockNavigator = mockk<Navigator>()
    private val logger = mockk<Logger>(relaxed = true)
    private val navTargetFlow = MutableSharedFlow<NavItem>(extraBufferCapacity = 1)

    // Create spyk to test correct navigation route invoked without actually calling the connected composable (to avoid issues with hiltViewModel call)
    private val mockkComposeNavigator: ComposeNavigator = spyk(ComposeNavigator())

    @Before
    fun setup() {
        every { mockNavigator.navTarget } returns navTargetFlow
        // prevent the actual navigation to be called so hiltViewModel call is not executed
        justRun { mockkComposeNavigator.navigate(any(), any(), any()) }
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(mockkComposeNavigator)
            NavigationComponent(navController, mockNavigator, logger)
        }
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `1- When initialized, then show feature list screen with featureListViewModel items`() {
        // Assert
        val backStackEntrySlot = slot<List<NavBackStackEntry>>()
        verify { mockkComposeNavigator.navigate(capture(backStackEntrySlot), any(), any()) }
        assertThat(backStackEntrySlot.captured.first().destination.route, `is`(NavItem.FEATURE_LIST.navName))
    }

    @Test
    fun `2- When navigator navTarget updated, given new target is FEATURE_LIST, then show FeatureListScreen with featureListViewModel items`() =
        runTest {
            // Arrange
            clearMocks(mockkComposeNavigator, answers = false)
            // Act
            navTargetFlow.emit(NavItem.BREATHING)
            // Assert
            val backStackEntrySlot = slot<List<NavBackStackEntry>>()
            verify { mockkComposeNavigator.navigate(capture(backStackEntrySlot), any(), any()) }
            assertThat(backStackEntrySlot.captured.first().destination.route, `is`(NavItem.BREATHING.navName))
        }

    @Test
    fun `3- When navigator navTarget updated, given new target is BREATHING, then show BreathingScreen with BreathingViewModel flows`() =
        runTest {
            // Arrange
            clearMocks(mockkComposeNavigator, answers = false)
            // Act
            navTargetFlow.emit(NavItem.FEATURE_LIST)
            // Assert
            val backStackEntrySlot = slot<List<NavBackStackEntry>>()
            verify { mockkComposeNavigator.navigate(capture(backStackEntrySlot), any(), any()) }
            assertThat(backStackEntrySlot.captured.first().destination.route, `is`(NavItem.FEATURE_LIST.navName))
        }
}
