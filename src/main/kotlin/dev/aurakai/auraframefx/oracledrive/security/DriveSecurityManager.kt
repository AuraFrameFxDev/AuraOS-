package dev.aurakai.auraframefx.oracledrive.security

import dev.aurakai.auraframefx.oracledrive.*

/**
 * Security manager for Oracle Drive operations
 * Integrates with AuraShield security framework and consciousness validation
 */
interface DriveSecurityManager {
    
    /**
 * Performs a security validation to determine if access to the Oracle Drive system is permitted.
 *
 * @return A SecurityCheck representing the result of the access validation.
 */
    fun validateDriveAccess(): SecurityCheck
    
    /**
 * Performs AI-based threat detection to validate the security of a file upload.
 *
 * @param file The file to be assessed for potential security threats.
 * @return A SecurityValidation containing the results of the threat assessment.
 */
    fun validateFileUpload(file: DriveFile): SecurityValidation
    
    /**
 * Validates whether a user has permission to access a specified file.
 *
 * @param fileId The unique identifier of the file to access.
 * @param userId The unique identifier of the user requesting access.
 * @return An AccessCheck indicating whether access is permitted.
 */
    fun validateFileAccess(fileId: String, userId: String): AccessCheck
    
    /**
 * Validates whether a user is authorized to delete a specified file.
 *
 * @param fileId The identifier of the file to be deleted.
 * @param userId The identifier of the user requesting the deletion.
 * @return A DeletionValidation object indicating whether the deletion is authorized.
 */
    fun validateDeletion(fileId: String, userId: String): DeletionValidation
}