package com.dom.androidUtils.sound

import androidx.annotation.RawRes

interface SoundPlayer {
    fun init(
        @RawRes soundResList: List<Int>,
    )

    @Throws(NotInitializedException::class)
    fun play(
        @RawRes soundResId: Int,
    )

    fun destroy()
}
