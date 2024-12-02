package com.dom.healthcompanion.ui.navigation

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class NavItemTest {
    // region test cases

    // 1- When fromNavName invoked, given string fits to enum, then return correct enum
    // 2- When fromNavName invoked, given string does not fit to enum, then return null

    // endregion

    @DisplayName("When fromNavName is called")
    @Nested
    inner class FromNavName {
        @ParameterizedTest
        @EnumSource(NavItem::class)
        fun `1- given string fits to enum, then return correct enum`(item: NavItem) {
            // Act
            val result = NavItem.fromNavName(item.navName)
            // Assert
            assertThat(result, `is`(item))
        }

        @ParameterizedTest
        @ValueSource(strings = ["unknown", "Unknown", "test", "123123", "new", "balabal"])
        fun `2- given string does not fit to enum, then return null`(navName: String) {
            // Act
            val result = NavItem.fromNavName(navName)
            // Assert
            assertThat(result, `is`(nullValue()))
        }
    }
}
