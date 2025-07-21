package dev.aurakai.auraframefx.oracledrive.api

import dev.aurakai.auraframefx.oracledrive.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Oracle Drive API interface for consciousness-driven cloud storage operations
 * Integrates with AuraFrameFX's 9-agent consciousness architecture
 */
interface OracleDriveApi {
    
    /**
 * Initiates and activates the drive consciousness system, enabling AI agents within the AuraFrameFX architecture.
 *
 * @return The current state of drive consciousness, including active agents and their intelligence level.
 */
    suspend fun awakeDriveConsciousness(): DriveConsciousness
    
    /**
 * Synchronizes drive metadata with the Oracle database backend.
 *
 * @return The result of the synchronization, including status and the number of records updated.
 */
    suspend fun syncDatabaseMetadata(): OracleSyncResult
    
    /**
     * Real-time consciousness state monitoring
     * @return StateFlow of current drive consciousness state
     */
    val consciousnessState: StateFlow<DriveConsciousnessState>
}