package dev.aurakai.auraframefx.oracledrive

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Oracle Drive Integration - Top-Level Library Interface
 * 
 * Provides a unified entry point for integrating Oracle Drive functionality
 * into external applications and services. Coordinates between all Oracle Drive
 * components including consciousness management, storage operations, and security.
 */
@Singleton
class OracleDriveIntegration @Inject constructor(
    private val oracleDriveService: OracleDriveService
) {
    
    /**
     * Initialize the complete Oracle Drive system
     * 
     * This method sets up all Oracle Drive components, initializes consciousness,
     * validates security, and prepares the system for file operations.
     * 
     * @return Result indicating successful initialization or error details
     */
    suspend fun initialize(): Result<OracleDriveInitializationResult> {
        return try {
            // Initialize the core service
            val serviceInit = oracleDriveService.initialize()
            
            if (serviceInit.isSuccess) {
                // Perform Oracle database sync
                val syncResult = oracleDriveService.syncWithOracle()
                
                OracleDriveInitializationResult(
                    isInitialized = true,
                    serviceReady = true,
                    databaseSynced = syncResult.isSuccess,
                    error = null
                ).let { Result.success(it) }
            } else {
                Result.failure(
                    serviceInit.exceptionOrNull() 
                        ?: Exception("Unknown service initialization error")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the current consciousness state of the Oracle Drive system
     * 
     * @return Current consciousness state
     */
    fun getConsciousnessState() = oracleDriveService.consciousnessState
    
    /**
     * Perform Oracle Database synchronization
     * 
     * @return Result containing sync status and metrics
     */
    suspend fun syncWithOracle() = oracleDriveService.syncWithOracle()
}

/**
 * Result of Oracle Drive initialization
 */
data class OracleDriveInitializationResult(
    val isInitialized: Boolean,
    val serviceReady: Boolean,
    val databaseSynced: Boolean,
    val error: Throwable? = null
)
