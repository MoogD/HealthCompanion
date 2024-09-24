package com.dom.testUtils

import com.dom.utils.DispatchersProvider
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider : DispatchersProvider {
    private val innerDispatcher = StandardTestDispatcher()

    private val dispatcher =
        object : CoroutineDispatcher() {
            var pause: Boolean = false

            override fun dispatch(
                context: CoroutineContext,
                block: Runnable,
            ) {
                if (pause) {
                    innerDispatcher.dispatch(context, block)
                } else {
                    innerDispatcher.dispatch(context, block)
                    innerDispatcher.scheduler.advanceUntilIdle()
                }
            }
        }

    override val main: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher

    fun pauseDispatcher() {
        dispatcher.pause = true
    }

    fun resumeDispatcher() {
        dispatcher.pause = false
        innerDispatcher.scheduler.advanceUntilIdle()
    }

    fun advanceBy(delayTimeMillis: Long) {
        innerDispatcher.scheduler.advanceTimeBy(delayTimeMillis)
    }

    fun runCurrent(delayTimeMillis: Long) {
        innerDispatcher.scheduler.runCurrent()
    }
}
