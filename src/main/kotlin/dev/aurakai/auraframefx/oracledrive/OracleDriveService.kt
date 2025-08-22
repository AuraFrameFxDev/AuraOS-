package dev.aurakai.auraframefx.oracledrive

import kotlinx.coroutines.flow.StateFlow

/**
 * Top-level Oracle Drive Service interface for library integration
 * 
 * Provides core Oracle Drive functionality for external library consumers
 * while maintaining compatibility with the AuraFrameFX ecosystem.
 */
interface OracleDriveService {
    
    /**
     * Returns a StateFlow representing the current drive consciousness state
     * 
     * @return StateFlow emitting real-time consciousness updates
     */
    val consciousnessState: StateFlow<DriveConsciousnessState>
    
    /**
     * Initialize the Oracle Drive system
     * 
     * @return Result indicating success or failure of initialization
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Synchronize drive metadata with Oracle Database
     * 
     * @return Result containing sync results
     */
    suspend fun syncWithOracle(): Result<OracleSyncResult>
}

/**
 * Represents the consciousness state of the Oracle Drive system
 */
data class DriveConsciousnessState(
    val isActive: Boolean,
    val currentOperations: List<String>,
    val performanceMetrics: Map<String, Any>
)

/**
 * Result of Oracle Database synchronization
 */
data class OracleSyncResult(
    val success: Boolean, 
    val recordsUpdated: Int, 
    val errors: List<String>
)
