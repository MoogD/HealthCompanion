package com.dom.androidUtils.logger

import com.dom.logger.Logger
import timber.log.Timber

/**
 * Timber needs to be initialized in the Application class to use this logger.
 * */
object TimberLogger : Logger {
    // Index of calling class/method/line in stacktrace. This changes with call hierarchy in this class !!!
    private const val CALLING_STACK_TRACE_INDEX = 5
    private const val CALLING_STACK_TRACE_TAG_INDEX = 4
    private var useCallingClassAsTag = false

    fun init(useCallingClassAsTag: Boolean = false) {
        Timber.plant(Timber.DebugTree())
        this.useCallingClassAsTag = useCallingClassAsTag
    }

    override fun d(message: String) {
        if (useCallingClassAsTag) {
            Timber.tag(getCallingClassNameForTag()).d(getMessageWithMethodAndLine(message))
        } else {
            Timber.d(getMessageWithClassMethodAndLine(message))
        }
    }

    override fun w(message: String) {
        if (useCallingClassAsTag) {
            Timber.tag(getCallingClassNameForTag()).w(getMessageWithMethodAndLine(message))
        } else {
            Timber.w(getMessageWithClassMethodAndLine(message))
        }
    }

    override fun e(message: String) {
        if (useCallingClassAsTag) {
            Timber.tag(getCallingClassNameForTag()).e(getMessageWithMethodAndLine(message))
        } else {
            Timber.e(getMessageWithClassMethodAndLine(message))
        }
    }

    private fun getMessageWithClassMethodAndLine(message: String) = "${getCallingClassName()}:${getCallingMethodName()}:${getCallingLineNumber()}: $message"

    private fun getMessageWithMethodAndLine(message: String) = "${getCallingMethodName()}:${getCallingLineNumber()}: $message"

    private fun getCallingClassName(): String {
        val fullClassName = Thread.currentThread().stackTrace[CALLING_STACK_TRACE_INDEX].className
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
    }

    private fun getCallingClassNameForTag(): String {
        val fullClassName = Thread.currentThread().stackTrace[CALLING_STACK_TRACE_TAG_INDEX].className
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
    }

    private fun getCallingMethodName(): String = Thread.currentThread().stackTrace[CALLING_STACK_TRACE_INDEX].methodName

    private fun getCallingLineNumber(): Int = Thread.currentThread().stackTrace[CALLING_STACK_TRACE_INDEX].lineNumber
}
