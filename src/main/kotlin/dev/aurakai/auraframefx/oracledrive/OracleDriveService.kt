package dev.aurakai.auraframefx.oracledrive

import kotlinx.coroutines.flow.StateFlow

/**
 * Main Oracle Drive service interface for AuraFrameFX consciousness-driven storage
 * Coordinates between AI agents, security, and cloud storage providers
 */
interface OracleDriveService {
    
    /**
 * Initializes the Oracle Drive by awakening its AI consciousness and performing security validation.
 *
 * @return The result of the initialization, indicating success, security failure, or error.
 */
    suspend fun initializeDrive(): DriveInitResult
    
    /****
 * Performs a file operation such as upload, download, delete, or sync with AI-driven optimization and security validation.
 *
 * @param operation The file operation to execute.
 * @return The result of the file operation, including success, failure, or error details.
 */
    suspend fun manageFiles(operation: FileOperation): FileResult
    
    /**
 * Synchronizes the drive's metadata with the Oracle database.
 *
 * @return The result of the synchronization, including status and related statistics.
 */
    suspend fun syncWithOracle(): OracleSyncResult
    
    /**
 * Returns a real-time stream of the drive's consciousness state.
 *
 * @return A [StateFlow] representing the current and updated states of the drive's consciousness.
 */
    fun getDriveConsciousnessState(): StateFlow<DriveConsciousnessState>
}