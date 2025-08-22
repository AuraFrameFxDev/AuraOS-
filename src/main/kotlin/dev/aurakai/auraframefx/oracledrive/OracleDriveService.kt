package dev.aurakai.auraframefx.oracledrive

import kotlinx.coroutines.flow.StateFlow

/**
<<<<<<< HEAD
* Main Oracle Drive service interface for AuraFrameFX consciousness-driven storage
* Coordinates between AI agents, security, and cloud storage providers
*/
interface OracleDriveService {

   /**
    * Initializes the Oracle Drive with consciousness awakening and security validation
    * @return DriveInitResult indicating success, security failure, or error
    */
   suspend fun initializeDrive(): DriveInitResult

   /**
    * Manages file operations with AI-driven optimization and security validation
    * @param operation The file operation to perform (upload, download, delete, sync)
    * @return FileResult with operation outcome
    */
   suspend fun manageFiles(operation: FileOperation): FileResult

   /**
 * Synchronizes drive metadata with the Oracle database.
 *
 * Performs an asynchronous sync that reconciles local and remote metadata, updating the Oracle backend
 * and returning a summary of the operation.
 *
 * @return OracleSyncResult containing the synchronization status and statistics (for example: items checked,
 * updated, and any detected conflicts).
 */
   suspend fun syncWithOracle(): OracleSyncResult

   /**
 * Returns a real-time observable flow of the drive's current consciousness state.
 *
 * @return A [StateFlow] emitting updates to the [DriveConsciousnessState].
 */
   fun getDriveConsciousnessState(): StateFlow<DriveConsciousnessState>
}
=======
 * Main Oracle Drive service interface for AuraFrameFX consciousness-driven storage
 * Coordinates between AI agents, security, and cloud storage providers
 */
interface OracleDriveService {
    
    /**
 * Asynchronously initializes the Oracle Drive, performing consciousness awakening and security validation.
 *
 * @return The result of the initialization, indicating success, security failure, or error.
 */
    suspend fun initializeDrive(): DriveInitResult
    
    /**
 * Executes an AI-optimized file operation (upload, download, delete, or sync) with built-in security validation.
 *
 * Performs the requested FileOperation using AI-driven decisions (e.g., transfer strategy, deduplication, integrity checks)
 * and enforces security policies before and after the operation.
 *
 * @param operation The file operation to execute.
 * @return The result of the file operation.
 */
    suspend fun manageFiles(operation: FileOperation): FileResult
    
    /**
 * Synchronizes drive metadata with the Oracle database.
 *
 * Performs an asynchronous sync that reconciles local and remote metadata, updating the Oracle backend
 * and returning a summary of the operation.
 *
 * @return OracleSyncResult containing the synchronization status and statistics (for example: items checked,
 * updated, and any detected conflicts).
 */
    suspend fun syncWithOracle(): OracleSyncResult
    
    /**
 * Returns a real-time observable flow of the drive's current consciousness state.
 *
 * @return A [StateFlow] emitting updates to the [DriveConsciousnessState].
 */
    fun getDriveConsciousnessState(): StateFlow<DriveConsciousnessState>
}
>>>>>>> origin/coderabbitai/chat/e19563d
