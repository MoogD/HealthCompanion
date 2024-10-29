package com.dom.healthcompanion

import android.app.Application
import com.dom.androidUtils.logger.TimberLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthCompanionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // enable calling class name as tags when adding remote logger or similar
            // keep TimberLogger as tag (useCallingClassAsTag = false) for easier filtering for custom logs
            TimberLogger.init(useCallingClassAsTag = true)
        }
    }
}
