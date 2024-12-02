package com.dom.utils

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class EitherTest {
    // region test cases

    // 1- When value invoked, then return Either.Value
    // 2- When error invoked, then return Either.Error

    // endregion

    @Test
    fun `1- When value invoked, then return Either Value`() {
        // Arrange
        val value = "test"
        // Act
        val result = value(value)
        // Assert
        assertThat(result, `is`(instanceOf(Either.Value::class.java)))
        assertThat((result as Either.Value).value, `is`(value))
    }

    @Test
    fun `2- When error invoked, then return Either Error`() {
        // Arrange
        val error = RuntimeException()
        // Act
        val result = error(error)
        // Assert
        assertThat(result, `is`(instanceOf(Either.Error::class.java)))
        assertThat((result as Either.Error).error, `is`(error))
    }
}
