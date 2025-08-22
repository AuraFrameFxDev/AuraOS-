package dev.aurakai.auraframefx.oracledrive.api

import dev.aurakai.auraframefx.oracledrive.*
import kotlinx.coroutines.flow.StateFlow

/**
<<<<<<< HEAD
* Oracle Drive API interface for consciousness-driven cloud storage operations
* Integrates with AuraFrameFX's 9-agent consciousness architecture
*/
interface OracleDriveApi {

   /**
    * Awakens the drive consciousness system with AI agents
    * @return DriveConsciousness state with active agents and intelligence level
    */
   suspend fun awakeDriveConsciousness(): DriveConsciousness

   /**
    * Synchronizes metadata with Oracle database backend
    * @return OracleSyncResult with sync status and updated records count
    */
   suspend fun syncDatabaseMetadata(): OracleSyncResult

   /**
    * Real-time consciousness state monitoring
    * @return StateFlow of current drive consciousness state
    */
   val consciousnessState: StateFlow<DriveConsciousnessState>
}
=======
 * Oracle Drive API interface for consciousness-driven cloud storage operations
 * Integrates with AuraFrameFX's 9-agent consciousness architecture
 */
interface OracleDriveApi {
    
    /**
 * Activate and initialize the drive consciousness subsystem.
 *
 * Wakes the subsystem so its AI agents become active and ready to operate. The returned
 * DriveConsciousness describes the current active agents and their aggregate intelligence level.
 *
 * @return The current DriveConsciousness representing active agents and their intelligence state.
 */
    suspend fun awakeDriveConsciousness(): DriveConsciousness
    
    /**
 * Synchronizes metadata with the Oracle database backend.
 *
 * @return An [OracleSyncResult] containing the synchronization status and the number of updated records.
 */
    suspend fun syncDatabaseMetadata(): OracleSyncResult
    
    /**
     * Real-time consciousness state monitoring
     * @return StateFlow of current drive consciousness state
     */
    val consciousnessState: StateFlow<DriveConsciousnessState>
}
>>>>>>> origin/coderabbitai/chat/e19563d
