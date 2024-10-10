package com.dom.utils

import kotlinx.coroutines.Dispatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DispatchersProviderImplTest {
    // region test cases

    // 1- When main called, then return Dispatchers.Main
    // 2- When default called, then return Dispatchers.Default
    // 3- When io called, then return Dispatchers.IO
    // 4- When unconfined called, then return Dispatchers.Unconfined

    // endregion

    private lateinit var sut: DispatchersProviderImpl

    @BeforeEach
    fun setUp() {
        sut = DispatchersProviderImpl()
    }

    @Test
    fun `1- When main called, then return Dispatchers Main`() {
        // Assert
        assertThat(sut.main, `is`(Dispatchers.Main))
    }

    @Test
    fun `2- When default called, then return Dispatchers Default`() {
        // Assert
        assertThat(sut.default, `is`(Dispatchers.Default))
    }

    @Test
    fun `3- When io called, then return Dispatchers IO`() {
        // Assert
        assertThat(sut.io, `is`(Dispatchers.IO))
    }

    @Test
    fun `4- When unconfined called, then return Dispatchers Unconfined`() {
        // Assert
        assertThat(sut.unconfined, `is`(Dispatchers.Unconfined))
    }
}
