package dev.aurakai.auraframefx.oracledrive.api

import dev.aurakai.auraframefx.oracledrive.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Oracle Drive API interface for database and cloud storage operations
 * 
 * Defines the contract for interacting with Oracle Drive's backend services,
 * including consciousness management, metadata synchronization, and storage operations.
 */
interface OracleDriveApi {
    
    /**
     * Real-time consciousness state of the Oracle Drive system
     */
    val consciousnessState: StateFlow<DriveConsciousnessState>
    
    /**
     * Awaken the drive consciousness and initialize AI agents
     * 
     * @return DriveConsciousness representing the awakened state
     */
    suspend fun awakeDriveConsciousness(): DriveConsciousness
    
    /**
     * Synchronize metadata with Oracle Database
     * 
     * @return OracleSyncResult containing sync status and metrics
     */
    suspend fun syncDatabaseMetadata(): OracleSyncResult
    
    /**
     * Upload file metadata to Oracle Database
     * 
     * @param fileId Unique identifier for the file
     * @param metadata File metadata to store
     * @return Result indicating success or failure
     */
    suspend fun uploadFileMetadata(fileId: String, metadata: Map<String, Any>): Result<Unit>
    
    /**
     * Retrieve file metadata from Oracle Database
     * 
     * @param fileId Unique identifier for the file
     * @return Result containing metadata or error
     */
    suspend fun getFileMetadata(fileId: String): Result<Map<String, Any>>
    
    /**
     * Delete file metadata from Oracle Database
     * 
     * @param fileId Unique identifier for the file
     * @return Result indicating success or failure
     */
    suspend fun deleteFileMetadata(fileId: String): Result<Unit>
}
