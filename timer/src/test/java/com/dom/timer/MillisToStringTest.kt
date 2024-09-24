package com.dom.timer

import java.util.stream.Stream
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource

class MillisToStringTest {
    // region test cases

    // 1- When millisToMinutesAndSeconds is called, given millis is smaller 0, then return 00:00
    // 2- When millisToMinutesAndSeconds is called, given millis is bigger or equal 0, then return correct string

    // 3- When millisToHoursAndMinutesAndSeconds is called, given millis is smaller 0, then return 00:00:00
    // 4- When millisToHoursAndMinutesAndSeconds is called, given millis is bigger or equal 0, then return correct string

    // endregion

    @Nested
    @DisplayName("When millisToMinutesAndSeconds is called")
    inner class MillisToMinutesAndSeconds {
        @ParameterizedTest
        @ValueSource(longs = [Long.MIN_VALUE, -12321432143213, -1231, -12, 0])
        fun `1-  given millis is smaller 0, then return 00 00`(millis: Long) {
            // Act
            val result = millis.millisToMinutesAndSeconds()
            // Assert
            assertThat(result, `is`("00:00"))
        }

        @ParameterizedTest
        @ArgumentsSource(LongToMinutesAndSecondsArgumentsProvider::class)
        fun `2-  given millis is bigger or equal 0, then return correct string`(
            millis: Long,
            expected: String,
        ) {
            // Act
            val result = millis.millisToMinutesAndSeconds()
            // Assert
            assertThat(result, `is`(expected))
        }
    }

    @Nested
    @DisplayName("When millisToHoursAndMinutesAndSeconds is called")
    inner class MillisToHoursAndMinutesAndSeconds {
        @ParameterizedTest
        @ValueSource(longs = [Long.MIN_VALUE, -12321432143213, -1231, -12, 0])
        fun `3- given millis is smaller 0, then return 00 00 00`(millis: Long) {
            // Act
            val result = millis.millisToHoursAndMinutesAndSeconds()
            // Assert
            assertThat(result, `is`("00:00:00"))
        }

        @ParameterizedTest
        @ArgumentsSource(LongToHoursAndMinutesAndSecondsArgumentsProvider::class)
        fun `4- given millis is bigger or equal 0, then return correct string`(
            millis: Long,
            expected: String,
        ) {
            // Act
            val result = millis.millisToHoursAndMinutesAndSeconds()
            // Assert
            assertThat(result, `is`(expected))
        }
    }

    class LongToMinutesAndSecondsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.arguments(0, "00:00"),
                Arguments.arguments(100, "00:00"),
                Arguments.arguments(1000, "00:01"),
                Arguments.arguments(12431, "00:12"),
                Arguments.arguments(90000, "01:30"),
                Arguments.arguments(90012, "01:30"),
                Arguments.arguments(60000, "01:00"),
                Arguments.arguments(600000, "10:00"),
                Arguments.arguments(605000, "10:05"),
                Arguments.arguments(605012, "10:05"),
                Arguments.arguments(6000000, "100:00"),
                Arguments.arguments(6012123, "100:12"),
            )
        }
    }

    class LongToHoursAndMinutesAndSecondsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.arguments(0, "00:00:00"),
                Arguments.arguments(100, "00:00:00"),
                Arguments.arguments(1000, "00:00:01"),
                Arguments.arguments(12431, "00:00:12"),
                Arguments.arguments(90000, "00:01:30"),
                Arguments.arguments(90012, "00:01:30"),
                Arguments.arguments(60000, "00:01:00"),
                Arguments.arguments(600000, "00:10:00"),
                Arguments.arguments(605000, "00:10:05"),
                Arguments.arguments(605012, "00:10:05"),
                Arguments.arguments(6000000, "01:40:00"),
                Arguments.arguments(6012123, "01:40:12"),
                Arguments.arguments(90000000, "25:00:00"),
                Arguments.arguments(94000000, "26:06:40"),
                Arguments.arguments(360000000, "100:00:00"),
                Arguments.arguments(360000000, "100:00:00"),
            )
        }
    }
}
