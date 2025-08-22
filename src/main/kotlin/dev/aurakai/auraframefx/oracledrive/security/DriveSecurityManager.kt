package dev.aurakai.auraframefx.oracledrive.security

import dev.aurakai.auraframefx.oracledrive.*

/**
<<<<<<< HEAD
* Security manager for Oracle Drive operations
* Integrates with AuraShield security framework and consciousness validation
*/
interface DriveSecurityManager {

   /**
    * Validates access to Oracle Drive system
    * @return SecurityCheck with validation result
    */
   fun validateDriveAccess(): SecurityCheck

   /**
    * Validates file upload security with AI threat detection
    * @param file The drive file to validate
    * @return SecurityValidation with threat assessment
    */
   fun validateFileUpload(file: DriveFile): SecurityValidation

   /**
 * Determine whether a specific user is allowed to access a given file.
 *
 * @param fileId The identifier of the file to check access for.
 * @param userId The identifier of the user requesting access.
 * @return An [AccessCheck] describing whether access is permitted and any relevant constraints. 
 */
   fun validateFileAccess(fileId: String, userId: String): AccessCheck

   /**
 * Validate whether the specified user is authorized to delete the given file.
 *
 * Performs an authorization check and returns a DeletionValidation describing whether
 * deletion is permitted and any relevant reasons or policy details.
 *
 * @param fileId Identifier of the file to delete.
 * @param userId Identifier of the user requesting deletion.
 * @return DeletionValidation indicating whether deletion is authorized and associated metadata.
 */
   fun validateDeletion(fileId: String, userId: String): DeletionValidation
}
=======
 * Security manager for Oracle Drive operations
 * Integrates with AuraShield security framework and consciousness validation
 */
interface DriveSecurityManager {
    
    /**
 * Validates whether access to the Oracle Drive system is permitted.
 *
 * @return A SecurityCheck indicating the result of the access validation.
 */
    fun validateDriveAccess(): SecurityCheck
    
    /**
 * Analyze a DriveFile using AI-based threat detection to determine whether it is safe to upload.
 *
 * Performs file content and metadata assessment (e.g., malware, hidden payloads, policy violations) and returns a SecurityValidation summarizing detected risks and recommended disposition.
 *
 * @param file The DriveFile to analyze.
 * @return SecurityValidation with the threat assessment result and any associated metadata (risk level, reasons, remediation suggestions).
 */
    fun validateFileUpload(file: DriveFile): SecurityValidation
    
    /**
 * Determine whether a specific user is allowed to access a given file.
 *
 * @param fileId The identifier of the file to check access for.
 * @param userId The identifier of the user requesting access.
 * @return An [AccessCheck] describing whether access is permitted and any relevant constraints. 
 */
    fun validateFileAccess(fileId: String, userId: String): AccessCheck
    
    /**
 * Validate whether the specified user is authorized to delete the given file.
 *
 * Performs an authorization check and returns a DeletionValidation describing whether
 * deletion is permitted and any relevant reasons or policy details.
 *
 * @param fileId Identifier of the file to delete.
 * @param userId Identifier of the user requesting deletion.
 * @return DeletionValidation indicating whether deletion is authorized and associated metadata.
 */
    fun validateDeletion(fileId: String, userId: String): DeletionValidation
}
>>>>>>> origin/coderabbitai/chat/e19563d
