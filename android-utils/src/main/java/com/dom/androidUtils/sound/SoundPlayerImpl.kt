package com.dom.androidUtils.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.dom.logger.Logger

class SoundPlayerImpl(private val context: Context, private val logger: Logger) : SoundPlayer {
    // Safe all soundIds that have been loaded successfully for the @RawRes resources
    private val loadedSounds = mutableMapOf<Int, Int>()
    private var soundPool: SoundPool? = null

    override fun init(
        @RawRes soundResList: List<Int>,
    ) {
        logger.d("init with $soundResList")
        val attributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
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
            logger.d(" $soundId loaded with status $status")
            if (status == LOADING_STATUS_SUCCESS) {
                val resId = soundsToLoad.remove(soundId) ?: return@setOnLoadCompleteListener
                loadedSounds[resId] = soundId
            }
        }
    }

    override fun play(
        @RawRes soundResId: Int,
    ) {
        logger.d("start play: $soundResId")
        soundPool?.let { pool ->
            if (loadedSounds[soundResId] == null) {
                logger.d("$soundResId not loaded")
                val id = pool.load(context, soundResId, DEFAULT_PRIORITY)
                pool.setOnLoadCompleteListener { soundPool, soundId, status ->
                    logger.d(" $soundId loaded with status $status")
                    if (soundId == id && status == LOADING_STATUS_SUCCESS) {
                        logger.d("play $soundId")
                        soundPool.play(soundId, DEFAULT_VOLUME, DEFAULT_VOLUME, DEFAULT_PRIORITY, LoopMode.NO_LOOP.key, DEFAULT_PLAYBACK_RATE)
                        loadedSounds[soundResId] = soundId
                    }
                }
            } else {
                logger.d("play $soundResId")
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
