package com.dom.healthcompanion.ui.main

import app.cash.turbine.test
import com.dom.healthcompanion.utils.TextString
import com.dom.logger.Logger
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.navigation.NavItem
import com.dom.healthcompanion.utils.IconState
import io.mockk.clearAllMocks
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    // region test cases

    // 1- When initialized, then set title to app name and hide back button

    // 2- When onNavigationDestinationChanged invoked, then update title to correct destination
    // 3- When onNavigationDestinationChanged invoked, given destination is not feature list, then show back button
    // 4- When onNavigationDestinationChanged invoked, given destination is feature list, then hide back button

    // 5- When onBackPressed invoked, then send event to go back

    // endregion
    private lateinit var sut: MainViewModel

    private val logger: Logger = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sut = MainViewModel(logger)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `1- When initialized, then set title to app name and hide back button`() {
        // Assert
        assertThat(sut.topBarTitleFlow.value, `is`(TextString.Res(R.string.app_name)))
        assertThat(sut.topBarIconFlow.value.isVisible, `is`(false))
    }

    @Nested
    @DisplayName("When onNavigationDestinationChanged invoked")
    inner class OnNavigationDestinationChanged {
        @ParameterizedTest
        @EnumSource(NavItem::class)
        fun `2- then update title to correct destination`(navItem: NavItem) =
            runTest {
                // Arrange
                sut.topBarTitleFlow.test {
                    // catch initial value
                    awaitItem()
                    // Act
                    sut.onNavigationDestinationChanged(navItem)
                    // Assert
                    assertThat(awaitItem(), `is`(navItem.title))
                }
            }

        @ParameterizedTest
        @EnumSource(value = NavItem::class, names = ["FEATURE_LIST"], mode = EnumSource.Mode.EXCLUDE)
        fun `3- given destination is not feature list, then show back button`(navItem: NavItem) =
            runTest {
                // Arrange
                sut.topBarIconFlow.test {
                    // catch initial value
                    awaitItem()
                    // Act
                    sut.onNavigationDestinationChanged(navItem)
                    // Assert
                    val icon = awaitItem()
                    assertThat(icon.isVisible, `is`(true))
                    assertThat(icon.iconType, `is`(IconState.Type.BACK))
                }
            }

        @Test
        fun `4- given destination is feature list, then hide back button`() =
            runTest {
                // Arrange
                sut.topBarIconFlow.test {
                    // Act
                    sut.onNavigationDestinationChanged(NavItem.FEATURE_LIST)
                    // Assert
                    assertThat(awaitItem().isVisible, `is`(false))
                }
            }
    }

    @Test
    fun `5- When onBackPressed invoked, then send event to go back`() =
        runTest {
            sut.screenEventFlow.test {
                // Arrange
                // show back button
                sut.onNavigationDestinationChanged(NavItem.BREATHING)
                // Act
                sut.topBarIconFlow.value.onClick()
                // Assert
                assertThat(awaitItem(), `is`(MainScreenEvent.OnBackPressed))
            }
        }
}
