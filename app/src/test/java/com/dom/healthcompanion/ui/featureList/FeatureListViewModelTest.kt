package com.dom.healthcompanion.ui.featureList

import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.navigation.NavItem
import com.dom.healthcompanion.ui.navigation.Navigator
import io.mockk.clearAllMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FeatureListViewModelTest {
    // region test cases

    // 1- When initialized, then add Breathing feature item to feature list flow

    // 2- When Breathing feature item is clicked, then navigate to breathing screen

    // endregion

    private val navigator = mockk<Navigator>()
    private lateinit var sut: FeatureListViewModel

    @BeforeEach
    fun setUp() {
        sut = FeatureListViewModel(navigator)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `1- When initialized, then add Breathing feature item to feature list flow`() {
        // Assert
        val breathingItem = sut.featureItems.value.firstOrNull { it.textRes == R.string.breathing_screen_title }
        assertThat(breathingItem, `is`(notNullValue()))
    }

    @Test
    fun `2- When Breathing feature item is clicked, then navigate to breathing screen`() {
        // Arrange
        val breathingItem = sut.featureItems.value.first { it.textRes == R.string.breathing_screen_title }
        justRun { navigator.navigateTo(NavItem.BREATHING) }
        // Act
        breathingItem.onClick.invoke()
        // Assert
        verify { navigator.navigateTo(NavItem.BREATHING) }
    }
}
