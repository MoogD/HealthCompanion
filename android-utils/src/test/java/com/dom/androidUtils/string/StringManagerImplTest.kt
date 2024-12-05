package com.dom.androidUtils.string

import android.content.Context
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class StringManagerImplTest {
    // region test cases

    // 1- When getString invoked, then return string from context

    // endregion

    private val context = mockk<Context>()
    private lateinit var sut: StringManagerImpl

    @BeforeEach
    fun setup() {
        sut = StringManagerImpl(context)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 10, 12312, 231241, Int.MAX_VALUE])
    fun `1- When getString invoked, then return string from context`(resId: Int) {
        // Arrange
        val expectedResult = resId.toString()
        every { context.getString(resId) } returns expectedResult
        // Act
        val result = sut.getString(resId)
        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.`is`(expectedResult))
    }
}
