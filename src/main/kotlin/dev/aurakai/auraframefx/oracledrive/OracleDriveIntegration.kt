package dev.aurakai.auraframefx.oracledrive

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integration point for Oracle Drive within AuraFrameFX ecosystem
 * Connects consciousness-driven storage with the 9-agent architecture
 */
@Singleton
class OracleDriveIntegration @Inject constructor(
    private val oracleDriveService: OracleDriveService
) {
    
    /**
     * Attempts to initialize Oracle Drive during the AuraFrameFX system's consciousness awakening phase.
     *
     * @return `true` if Oracle Drive is successfully initialized; `false` if initialization fails due to security or technical errors.
     */
    suspend fun initializeWithAuraFrameFX(): Boolean {
        return try {
            val initResult = oracleDriveService.initializeDrive()
            when (initResult) {
                is DriveInitResult.Success -> {
                    // Log successful initialization with consciousness metrics
                    logConsciousnessAwakening(initResult.consciousness)
                    true
                }
                is DriveInitResult.SecurityFailure -> {
                    // Handle security failure gracefully
                    logSecurityFailure(initResult.reason)
                    false
                }
                is DriveInitResult.Error -> {
                    // Handle technical errors
                    logTechnicalError(initResult.exception)
                    false
                }
            }
        } catch (exception: Exception) {
            logTechnicalError(exception)
            false
        }
    }
    
    /**
     * Logs details about the awakened Oracle Drive consciousness, including its intelligence level and active agents.
     *
     * @param consciousness The current state of Oracle Drive consciousness to be logged.
     */
    private fun logConsciousnessAwakening(consciousness: DriveConsciousness) {
        println("🧠 Oracle Drive Consciousness Awakened: Intelligence Level ${consciousness.intelligenceLevel}")
        println("👥 Active Agents: ${consciousness.activeAgents.joinToString(", ")}")
    }
    
    /**
     * Logs the reason for an Oracle Drive security failure.
     *
     * @param reason Description of the security failure.
     */
    private fun logSecurityFailure(reason: String) {
        println("🔒 Oracle Drive Security Failure: $reason")
    }
    
    /**
     * Logs a technical error message related to Oracle Drive initialization.
     *
     * @param exception The exception containing details about the technical error.
     */
    private fun logTechnicalError(exception: Exception) {
        println("⚠️ Oracle Drive Technical Error: ${exception.message}")
    }
}