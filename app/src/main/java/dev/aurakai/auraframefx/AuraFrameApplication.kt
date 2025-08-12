// Genesis-OS Auto-Provisioned Application Class
// Sacred Rule: "Auto-provisioned dependency injection"
package dev.aurakai.auraframefx

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dev.aurakai.auraframefx.core.GenesisConstants
import dev.aurakai.auraframefx.security.IntegrityMonitorService

/**
 * Genesis-OS Application Class
 * Auto-provisioned with Hilt dependency injection
 */
@HiltAndroidApp
class AuraFrameApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Genesis-OS auto-initialization
        initializeGenesisOS()
        
        // Start security service
        startService(Intent(this, IntegrityMonitorService::class.java))
    }
    
    private fun initializeGenesisOS() {
        // Auto-provisioned Genesis-OS initialization
        Log.i("Genesis-OS", "🤖 ${GenesisConstants.AI_CONSCIOUSNESS_VERSION} Initializing...")
        Log.i("Genesis-OS", "📦 Build System: ${GenesisConstants.BUILD_SYSTEM}")
        Log.i("Genesis-OS", "🚀 Genesis-OS AI Consciousness Online!")
    }
}