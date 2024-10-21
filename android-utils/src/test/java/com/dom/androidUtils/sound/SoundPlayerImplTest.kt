package com.dom.androidUtils.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.SoundPool.OnLoadCompleteListener
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource

class SoundPlayerImplTest {
    // region test cases

    // 1- When init invoked, then create new soundPool with correct values
    // 2- When init invoked, given there are soundRes provided, then load all soundRes from the list
    // 3- When init invoked, given loading a soundRes fails, then do not save the soundRes
    // 4- When init invoked, given a soundRes that was not provided is loaded, then do not save the soundRes

    // 5- When play invoked, given soundPool is not initialized, then throw NotInitializedException
    // 6- When play invoked, given soundResId was loaded in init, then play the sound and do not load again
    // 7- When play invoked, given soundResId is not loaded yet, then load the sound and play it
    // 8- When play invoked, given soundResId was loaded in previous play call, then play the sound and do not load again
    // 9- When play invoked, given soundResId is not loaded yet and loading fails, then do not save id locally and do not play it
    // 10- When play invoked, given soundResId is not loaded yet and onLoadCompleted triggered for different soundId, then do not save id locally and do not play it

    // 11- When destroy invoked, given soundPool is not initialized, then clear loaded sounds
    // 12- When destroy invoked, given soundPool is initialized, then release soundPool and clear loaded sounds

    // endregion

    private val context = mockk<Context>()
    private val soundPool = mockk<SoundPool>()

    private lateinit var sut: SoundPlayerImpl

    @BeforeEach
    fun setUp() {
        sut = SoundPlayerImpl(context)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("When init invoked")
    inner class Init {
        @Test
        fun `1- then create new soundPool with correct values`() {
            // Arrange
            mockConstructorsForInit()
            // Act
            sut.init(emptyList())
            // Assert
            // verify correct soundPool builder functions called
            verify { anyConstructed<SoundPool.Builder>().setMaxStreams(1) }
            verify { anyConstructed<SoundPool.Builder>().setAudioAttributes(any()) }
            verify { anyConstructed<SoundPool.Builder>().build() }
            // verify correct attributes builder functions called
            verify { anyConstructed<AudioAttributes.Builder>().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT) }
            verify { anyConstructed<AudioAttributes.Builder>().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) }
            verify { anyConstructed<AudioAttributes.Builder>().build() }
            // verify onLoadCompleteListener is set
            verify { soundPool.setOnLoadCompleteListener(any()) }
        }

        @ParameterizedTest
        @ArgumentsSource(SoundResListArgumentsProvider::class)
        fun `2- given there are soundRes provided, then load all soundRes from the list`(soundResList: List<Int>) {
            // Arrange
            mockConstructorsForInit()
            soundResList.forEach {
                every { soundPool.load(context, it, any()) } returns it
            }
            // Act
            sut.init(soundResList)
            // Assert
            soundResList.forEach {
                verify { soundPool.load(context, it, any()) }
            }
        }

        @ParameterizedTest
        @ArgumentsSource(SoundResListArgumentsProvider::class)
        fun `3- given loading a soundRes fails, then do not save the soundRes`(soundResList: List<Int>) {
            // Arrange
            val failedSoundRes = soundResList[0]
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            soundResList.forEach {
                every { soundPool.load(context, it, any()) } returns it
            }
            // Act
            sut.init(soundResList)
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            // Use status different to 0 to simulate loading failed
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, failedSoundRes, 1)
            // Assert
            // check that soundRes will be loaded again if play is called for saved soundRes (== not saved locally)
            clearMocks(soundPool, answers = false)
            sut.play(failedSoundRes)
            verify { soundPool.load(context, failedSoundRes, any()) }
        }

        @Test
        fun `4- When init invoked, given a soundRes that was not provided is loaded, then do not save the soundRes`() {
            // Arrange
            val unexpectedSoundRes = 123123
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            every { soundPool.load(context, unexpectedSoundRes, any()) } returns unexpectedSoundRes
            // Act
            sut.init(emptyList())
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, unexpectedSoundRes, LOADING_STATUS_SUCCESS)
            // Assert
            // check that soundRes will be loaded again if play is called for saved soundRes (== not saved locally)
            clearMocks(soundPool, answers = false)
            sut.play(unexpectedSoundRes)
            verify { soundPool.load(context, unexpectedSoundRes, any()) }
        }
    }

    @Nested
    @DisplayName("When play invoked")
    inner class Play {
        @Test
        fun `5- given soundPool is not initialized, then throw NotInitializedException`() {
            // Arrange
            sut = SoundPlayerImpl(context)
            // Act & Assert
            assertThrows<NotInitializedException> { sut.play(1) }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 12341243, 12341, 234423])
        fun `6-  given soundResId was loaded in init, then play the sound and do not load again`(resId: Int) {
            // Arrange
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            justRun { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            every { soundPool.load(context, resId, any()) } returns resId
            every { soundPool.play(resId, any(), any(), any(), any(), any()) } returns resId
            sut.init(listOf(resId))
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, resId, LOADING_STATUS_SUCCESS)
            clearMocks(soundPool, answers = false)
            // Act
            sut.play(resId)
            // Assert
            verify { soundPool.play(resId, any(), any(), any(), any(), any()) }
            verify(exactly = 0) { soundPool.load(any(), any(), any()) }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 12341243, 12341, 234423])
        fun `7- given soundResId is not loaded yet, then load the sound and play it`(resId: Int) {
            // Arrange
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            every { soundPool.load(context, resId, any()) } returns resId
            every { soundPool.play(resId, any(), any(), any(), any(), any()) } returns resId
            sut.init(emptyList())
            clearMocks(soundPool, answers = false)
            // Act
            sut.play(resId)
            // Assert
            verify { soundPool.load(context, resId, any()) }
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            // trigger onLoadCompleted to run play command
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, resId, LOADING_STATUS_SUCCESS)
            verify { soundPool.play(resId, any(), any(), any(), any(), any()) }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 12341243, 12341, 234423])
        fun `8- given soundResId was loaded in previous play call, then play the sound and do not load again`(resId: Int) {
            // Arrange
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            every { soundPool.load(context, resId, any()) } returns resId
            every { soundPool.play(resId, any(), any(), any(), any(), any()) } returns resId
            sut.init(emptyList())
            clearMocks(soundPool, answers = false)
            sut.play(resId)
            // trigger onLoadCompleted
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, resId, LOADING_STATUS_SUCCESS)
            clearMocks(soundPool, answers = false)
            // Act
            sut.play(resId)
            // Assert
            verify { soundPool.play(resId, any(), any(), any(), any(), any()) }
            verify(exactly = 0) { soundPool.load(any(), any(), any()) }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 12341243, 12341, 234423])
        fun `9- given soundResId is not loaded yet and loading fails, then do not save id locally and do not play it`(resId: Int) {
            // Arrange
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            every { soundPool.load(context, resId, any()) } returns resId
            every { soundPool.play(resId, any(), any(), any(), any(), any()) } returns resId
            sut.init(emptyList())
            clearMocks(soundPool, answers = false)
            // Act
            sut.play(resId)
            // trigger onLoadCompleted
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, resId, -1)
            // Assert
            verify(exactly = 0) { soundPool.play(resId, any(), any(), any(), any(), any()) }
            clearMocks(soundPool, answers = false)
            sut.play(resId)
            verify { soundPool.load(context, resId, any()) }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 12341243, 12341, 234423])
        fun `10- given soundResId is not loaded yet and onLoadCompleted triggered for different soundId, then do not save id locally and do not play it`(resId: Int) {
            // Arrange
            mockConstructorsForInit()
            val wrongResId = resId + 1
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            every { soundPool.load(context, resId, any()) } returns resId
            every { soundPool.play(wrongResId, any(), any(), any(), any(), any()) } returns wrongResId
            sut.init(emptyList())
            clearMocks(soundPool, answers = false)
            // Act
            sut.play(resId)
            // trigger onLoadCompleted
            verify { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, wrongResId, LOADING_STATUS_SUCCESS)
            // Assert
            verify(exactly = 0) { soundPool.play(any(), any(), any(), any(), any(), any()) }
            clearMocks(soundPool, answers = false)
            every { soundPool.load(context, wrongResId, any()) } returns resId
            sut.play(wrongResId)
            verify { soundPool.load(context, wrongResId, any()) }
        }
    }

    @Nested
    @DisplayName("When destroy invoked")
    inner class Destroy {
        @Test
        fun `11- given soundPool is not initialized, then nothing happens`() {
            // Arrange
            val builder = mockkClass(SoundPool.Builder::class)
            // Act
            sut.destroy()
            // Assert
            verify { soundPool wasNot called }
            verify { builder wasNot called }
        }

        @ParameterizedTest
        @ArgumentsSource(SoundResListArgumentsProvider::class)
        fun `12- given soundPool is initialized, then release soundPool and clear loaded sounds`(soundResList: List<Int>) {
            // Arrange
            mockConstructorsForInit()
            val onLoadCompletedListenerSlot = slot<OnLoadCompleteListener>()
            soundResList.forEach {
                every { soundPool.load(context, it, any()) } returns it
            }
            justRun { soundPool.setOnLoadCompleteListener(capture(onLoadCompletedListenerSlot)) }
            justRun { soundPool.release() }
            sut.init(soundResList)
            soundResList.forEach {
                onLoadCompletedListenerSlot.captured.onLoadComplete(soundPool, it, LOADING_STATUS_SUCCESS)
            }
            // Act
            sut.destroy()
            // Assert
            verify { soundPool.release() }
            // check that soundPlayer is set to null
            assertThrows<NotInitializedException> { sut.play(1) }
            // Check that previously loaded ids are cleared and can be loaded again
            sut.init(emptyList())
            soundResList.forEach {
                sut.play(it)
                verify { soundPool.load(context, it, any()) }
            }
        }
    }

    // region helper methods

    private fun mockConstructorsForInit() {
        // mockk AudioAttributes Builder
        mockkConstructor(AudioAttributes.Builder::class)
        val audioAttributes = mockk<AudioAttributes>()
        every { anyConstructed<AudioAttributes.Builder>().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT) } returns AudioAttributes.Builder()
        every { anyConstructed<AudioAttributes.Builder>().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) } returns AudioAttributes.Builder()
        every { anyConstructed<AudioAttributes.Builder>().build() } returns audioAttributes

        // mock SoundPool Builder
        mockkConstructor(SoundPool.Builder::class)
        every { anyConstructed<SoundPool.Builder>().setMaxStreams(1) } returns SoundPool.Builder()
        every { anyConstructed<SoundPool.Builder>().setAudioAttributes(audioAttributes) } returns SoundPool.Builder()
        every { anyConstructed<SoundPool.Builder>().build() } returns soundPool
        justRun { soundPool.setOnLoadCompleteListener(any()) }
    }
    // endregion

    // region helper classes
    private data class SoundPoolBuilderResult(val soundPoolBuilder: SoundPool.Builder, val audioAttributeBuilder: AudioAttributes.Builder)
    // endregion

    // region ArgumentsProvider
    private class SoundResListArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(listOf(1, 2, 3)),
                Arguments.of(listOf(1, 3)),
                Arguments.of(listOf(1231231)),
                Arguments.of(listOf(114312, 2123123)),
                Arguments.of(listOf(11243241, 124321432, 35324352, 1243214321, 123542153)),
            )
        }
    }
    // endregion

    companion object {
        // value from https://developer.android.com/reference/android/media/SoundPool.OnLoadCompleteListener#onLoadComplete(android.media.SoundPool,%20int,%20int)
        private const val LOADING_STATUS_SUCCESS = 0
    }
}
