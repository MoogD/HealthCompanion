package com.dom.androidUtils.logger

import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import timber.log.Timber
import timber.log.Timber.Forest

class TimberLoggerTest {
    // region test cases

    // 1- When init invoked, then plant debug Timber tree
    // 2- When init invoked, given no useCallingClassAsTag value provided, then set useCallingClassAsTag to false

    // 3- When d invoked, given init not invoked, then call Timber d without additional tag
    // 4- When d invoked, given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag
    // 5- When d invoked, given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag

    // 6- When w invoked, given init not invoked, then call Timber d without additional tag
    // 7- When w invoked, given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag
    // 8- When w invoked, given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag

    // 9- When e invoked, given init not invoked, then call Timber d without additional tag
    // 10- When e invoked, given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag
    // 11- When e invoked, given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag

    // endregion

    private lateinit var sut: TimberLogger

    @BeforeEach
    fun setUp() {
        // Mock timber companion object for static calls
        mockkObject(Forest)
        mockkConstructor(Timber.DebugTree::class)
        sut = TimberLogger
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("When init invoked")
    inner class Init {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `1- then plant debug Timber tree`(useCallingClassAsTag: Boolean) {
            // Act
            sut.init(useCallingClassAsTag)

            // Assert
            verify { Timber.plant(any<Timber.DebugTree>()) }
        }

        @Test
        fun `2- given no useCallingClassAsTag value provided, then set useCallingClassAsTag to false`() {
            // Arrange
            justRun { Timber.d(any<String>()) }

            // Act
            sut.init()

            // Assert
            verify { Timber.plant(any<Timber.DebugTree>()) }
            sut.d("Test")
            verify(exactly = 0) { Timber.tag(any()) }
        }
    }
//    class TestCallingClass {
//        fun test() {
//            TimberLogger.d("test")
//        }
//    }
//
//    @Test
//    fun test() {
//        // Arrange
//
//        val testCallingClass = TestCallingClass()
//        val msgSlot = slot<String>()
//        justRun { Timber.d(any<String>()) }
//        // Act
// //        sut.d("logMsg")
//        testCallingClass.test()
//        // Assert
//        verify { Timber.d(capture(msgSlot)) }
//        val splitMsg = msgSlot.captured.split(":")
//        assertThat(splitMsg.size, `is`(4))
//        assertThat(splitMsg[0], `is`(this.javaClass.name))
//        assertThat(splitMsg[1], `is`(this::test.name))
//        assertThat(splitMsg[3], `is`(" logMsg"))
//    }

    @Nested
    @DisplayName("When d invoked")
    inner class D {
        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `3- given init not invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@D.javaClass.name.split(".").last()
            val methodName = this::`3- given init not invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.d(any<String>()) }
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.d(logMsg)
            // Assert
            verify(exactly = 1) { Timber.d(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `4- given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@D.javaClass.name.split(".").last()
            val methodName = this::`4- given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.d(any<String>()) }
            sut.init(false)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.d(logMsg)
            // Assert
            verify(exactly = 1) { Timber.d(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `5- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`(logMsg: String) {
            // Arrange
            val className = this@D.javaClass.name.split(".").last()
            val methodName = this::`5- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`.name

            val msgSlot = slot<String>()
            justRun { Timber.d(any<String>()) }
            every { Timber.tag(className) } returns Timber
            sut.init(true)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.d(logMsg)
            // Assert
            verify(exactly = 1) { Timber.d(capture(msgSlot)) }
            verify(exactly = 1) { Timber.tag(className) }
            verifyLoggedMsgWithoutClass(msgSlot, logMsg, methodName)
        }
    }

    @Nested
    @DisplayName("When w invoked")
    inner class W {
        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `6- When w invoked, given init not invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@W.javaClass.name.split(".").last()
            val methodName = this::`6- When w invoked, given init not invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.w(any<String>()) }
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.w(logMsg)
            // Assert
            verify(exactly = 1) { Timber.w(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `7- given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@W.javaClass.name.split(".").last()
            val methodName = this::`7- given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.w(any<String>()) }
            sut.init(false)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.w(logMsg)
            // Assert
            verify(exactly = 1) { Timber.w(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `8- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`(logMsg: String) {
            // Arrange
            val className = this@W.javaClass.name.split(".").last()
            val methodName = this::`8- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`.name

            val msgSlot = slot<String>()
            justRun { Timber.w(any<String>()) }
            every { Timber.tag(className) } returns Timber
            sut.init(true)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.w(logMsg)
            // Assert
            verify(exactly = 1) { Timber.w(capture(msgSlot)) }
            verify(exactly = 1) { Timber.tag(className) }
            verifyLoggedMsgWithoutClass(msgSlot, logMsg, methodName)
        }
    }

    @Nested
    @DisplayName("When e invoked")
    inner class E {
        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `9- When e invoked, given init not invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@E.javaClass.name.split(".").last()
            val methodName = this::`9- When e invoked, given init not invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.e(any<String>()) }
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.e(logMsg)
            // Assert
            verify(exactly = 1) { Timber.e(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `10- When e invoked, given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`(logMsg: String) {
            // Arrange
            val className = this@E.javaClass.name.split(".").last()
            val methodName = this::`10- When e invoked, given useCallingClassAsTag was false when init invoked, then call Timber d without additional tag`.name
            val msgSlot = slot<String>()
            justRun { Timber.e(any<String>()) }
            sut.init(false)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.e(logMsg)
            // Assert
            verify(exactly = 1) { Timber.e(capture(msgSlot)) }
            verifyLoggedMsg(msgSlot, logMsg, className, methodName)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "logs 123", "xyz invoked", "", "asdfasgasdf"])
        fun `11- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`(logMsg: String) {
            // Arrange
            val className = this@E.javaClass.name.split(".").last()
            val methodName = this::`11- given useCallingClassAsTag was true when init invoked, then call Timber d with calling class as tag additional tag`.name

            val msgSlot = slot<String>()
            justRun { Timber.e(any<String>()) }
            every { Timber.tag(className) } returns Timber
            sut.init(true)
            // use wrapper to get correct calling hierarchy
            val callingClassWrapper = CallingClassWrapper(sut)
            // Act
            callingClassWrapper.e(logMsg)
            // Assert
            verify(exactly = 1) { Timber.e(capture(msgSlot)) }
            verify(exactly = 1) { Timber.tag(className) }
            verifyLoggedMsgWithoutClass(msgSlot, logMsg, methodName)
        }
    }

    // region helper functions
    private fun verifyLoggedMsg(
        msgSlot: CapturingSlot<String>,
        logMsg: String,
        className: String,
        methodName: String,
    ) {
        val splitMsg = msgSlot.captured.split(":")
        assertThat(splitMsg.size, `is`(4))
        val integerMappedFromLineNumberIndex = splitMsg[2].toIntOrNull()
        assertThat(integerMappedFromLineNumberIndex, `is`(not(nullValue())))
        assertThat(splitMsg[0], `is`(className))
        assertThat(splitMsg[1], `is`(methodName))
        assertThat(splitMsg[3], `is`(" $logMsg"))
    }

    private fun verifyLoggedMsgWithoutClass(
        msgSlot: CapturingSlot<String>,
        logMsg: String,
        methodName: String,
    ) {
        val splitMsg = msgSlot.captured.split(":")
        assertThat(splitMsg.size, `is`(3))
        val integerMappedFromLineNumberIndex = splitMsg[1].toIntOrNull()
        assertThat(integerMappedFromLineNumberIndex, `is`(not(nullValue())))
        assertThat(splitMsg[0], `is`(methodName))
        assertThat(splitMsg[2], `is`(" $logMsg"))
    }

    // endregion

    // region helper classes
    // Needed to simulate call hierarchy from running app.
    class CallingClassWrapper(val timberLogger: TimberLogger) {
        fun d(msg: String) {
            timberLogger.d(msg)
        }

        fun e(msg: String) {
            timberLogger.e(msg)
        }

        fun w(msg: String) {
            timberLogger.w(msg)
        }
    }
    // endregion
}
