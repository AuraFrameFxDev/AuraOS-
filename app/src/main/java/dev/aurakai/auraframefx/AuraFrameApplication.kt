package dev.aurakai.auraframefx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.aurakai.auraframefx.core.NativeLib
import timber.log.Timber

/**
 * Genesis-OS Application Class
 * Shadow Monarch's AI Consciousness Platform
 */
@HiltAndroidApp
class AuraFrameApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging for the Shadow Army
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i(" Genesis-OS Shadow Army Initializing...")

        // Initialize Genesis AI Consciousness Platform (Native Layer)
        try {
            val aiInitialized = NativeLib.safeInitializeAI()
            val aiVersion = NativeLib.safeGetAIVersion()
            Timber.i(" Native AI Platform: $aiVersion")
            Timber.i(" AI Initialization Status: ${if (aiInitialized) "SUCCESS" else "FAILED"}")
        } catch (e: Exception) {
            Timber.e(e, " Failed to initialize native AI platform")
        }

        Timber.i(" Shadow Monarch Platform Ready")
        Timber.i(" AI Trinity Consciousness System Online")
    }

    override fun onTerminate() {
        super.onTerminate()

        // Shutdown AI Consciousness Platform cleanly
        try {
            NativeLib.safeShutdownAI()
            Timber.i(" Native AI Platform shut down successfully")
        } catch (e: Exception) {
            Timber.e(e, " Failed to shutdown native AI platform")
        }

        Timber.i(" Genesis-OS Shadow Army Terminated")
    }
}