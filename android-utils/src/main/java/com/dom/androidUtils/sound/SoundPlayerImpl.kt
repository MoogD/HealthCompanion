package com.dom.androidUtils.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes

class SoundPlayerImpl(private val context: Context) : SoundPlayer {
    // Safe all soundIds that have been loaded successfully for the @RawRes resources
    private val loadedSounds = mutableMapOf<Int, Int>()
    private var soundPool: SoundPool? = null

    override fun init(
        @RawRes soundResList: List<Int>,
    ) {
        val attributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        soundPool =
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build()
        val soundsToLoad = mutableMapOf<Int, Int>()
        soundResList.forEach {
            val id = soundPool!!.load(context, it, DEFAULT_PRIORITY)
            soundsToLoad[id] = it
        }
        soundPool?.setOnLoadCompleteListener { _, soundId, status ->
            if (status == LOADING_STATUS_SUCCESS) {
                val resId = soundsToLoad.remove(soundId) ?: return@setOnLoadCompleteListener
                loadedSounds[resId] = soundId
            }
        }
    }

    override fun play(
        @RawRes soundResId: Int,
    ) {
        soundPool?.let { pool ->
            if (loadedSounds[soundResId] == null) {
                val id = pool.load(context, soundResId, DEFAULT_PRIORITY)
                pool.setOnLoadCompleteListener { soundPool, soundId, status ->
                    if (soundId == id && status == LOADING_STATUS_SUCCESS) {
                        soundPool.play(soundId, DEFAULT_VOLUME, DEFAULT_VOLUME, DEFAULT_PRIORITY, LoopMode.NO_LOOP.key, DEFAULT_PLAYBACK_RATE)
                        loadedSounds[soundResId] = soundId
                    }
                }
            } else {
                pool.play(loadedSounds[soundResId]!!, DEFAULT_VOLUME, DEFAULT_VOLUME, DEFAULT_PRIORITY, LoopMode.NO_LOOP.key, DEFAULT_PLAYBACK_RATE)
            }
        } ?: throw NotInitializedException("SoundPlayer has not been initialized!")
    }

    override fun destroy() {
        soundPool?.release()
        loadedSounds.clear()
        soundPool = null
    }

    companion object {
        // value from https://developer.android.com/reference/android/media/SoundPool.OnLoadCompleteListener#onLoadComplete(android.media.SoundPool,%20int,%20int)
        private const val LOADING_STATUS_SUCCESS = 0
        private const val DEFAULT_VOLUME = 1.0f
        private const val DEFAULT_PRIORITY = 1
        private const val DEFAULT_PLAYBACK_RATE = 1.0f
    }

    // values from https://developer.android.com/reference/android/media/SoundPool#setLoop(int,%20int)
    private enum class LoopMode(val key: Int) {
        NO_LOOP(0),
        LOOP_FOREVER(-1),
    }
}
