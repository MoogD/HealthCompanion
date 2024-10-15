package com.dom.healthcompanion.ui.navigation

import app.cash.turbine.test
import io.mockk.clearAllMocks
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class NavigatorTest {
    // region test cases

    // 1- When initialized, then navTarget is empty

    // 2- When navigateTo is called, then navTarget is updated with provided NavItem

    // endregion

    private lateinit var sut: Navigator

    @BeforeEach
    fun setUp() {
        sut = Navigator()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `1- When initialized, then navTarget is empty`() {
        // Assert
        assertThat(sut.navTarget.replayCache, `is`(emptyList()))
    }

    @ParameterizedTest
    @EnumSource(NavItem::class)
    fun `2- When navigateTo is called, then navTarget is updated with provided NavItem`(item: NavItem) =
        runTest {
            // Arrange
            sut.navTarget.test {
                // Act
                sut.navigateTo(item)
                // Assert
                assertThat(awaitItem(), `is`(item))
            }
        }
}
