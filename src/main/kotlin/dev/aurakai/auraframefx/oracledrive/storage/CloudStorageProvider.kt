package dev.aurakai.auraframefx.oracledrive.storage

import dev.aurakai.auraframefx.oracledrive.*

/**
<<<<<<< HEAD
* Cloud storage provider interface for Oracle Drive
* Handles AI-optimized storage operations with consciousness integration
*/
interface CloudStorageProvider {

   /**
    * Optimizes storage with intelligent algorithms and compression
    * @return StorageOptimization with optimization metrics
    */
   suspend fun optimizeStorage(): StorageOptimization

   /**
    * Optimizes file for upload with AI-driven compression
    * @param file The file to optimize
    * @return Optimized DriveFile
    */
   suspend fun optimizeForUpload(file: DriveFile): DriveFile

   /**
    * Uploads file to cloud storage with metadata
    * @param file The optimized file to upload
    * @param metadata File metadata and access controls
    * @return FileResult with upload status
    */
   suspend fun uploadFile(file: DriveFile, metadata: FileMetadata): FileResult

   /**
    * Downloads file from cloud storage
    * @param fileId The file identifier
    * @return FileResult with download status
    */
   suspend fun downloadFile(fileId: String): FileResult

   /**
 * Delete a file from cloud storage by its identifier.
 *
 * Performs an asynchronous deletion and returns a FileResult describing the outcome.
 *
 * @param fileId The unique identifier of the file to delete.
 * @return A FileResult containing the deletion status and any related metadata.
 */
   suspend fun deleteFile(fileId: String): FileResult

   /**
 * Performs intelligent file synchronization using the provided synchronization configuration.
 *
 * Executes an AI-driven synchronization pass that applies configured rules (filters, conflict resolution,
 * bandwidth and scheduling constraints) and returns the outcome for the operation.
 *
 * @param config Synchronization parameters and rules that control what to sync and how conflicts, bandwidth,
 *               and scheduling are handled.
 * @return A FileResult describing the synchronization outcome, including status, updated file metadata, and any errors. 
 */
   suspend fun intelligentSync(config: SyncConfiguration): FileResult
}
=======
 * Cloud storage provider interface for Oracle Drive
 * Handles AI-optimized storage operations with consciousness integration
 */
interface CloudStorageProvider {
    
    /**
 * Performs intelligent storage optimization using AI-driven algorithms and compression techniques.
 *
 * @return A [StorageOptimization] object containing metrics and results of the optimization process.
 */
    suspend fun optimizeStorage(): StorageOptimization
    
    /**
 * Optimize a DriveFile using AI-driven compression so it's ready for upload.
 *
 * Implementations return the optimized file (may be a new instance); the result is intended for upload and should have reduced size or improved transfer characteristics.
 *
 * @return The optimized DriveFile ready for upload.
 */
    suspend fun optimizeForUpload(file: DriveFile): DriveFile
    
    /**
 * Uploads an already-optimized DriveFile to cloud storage and associates metadata and access controls.
 *
 * Upload is performed asynchronously (suspend). The provided `file` is expected to have been
 * prepared for upload (e.g., compressed/optimized). `metadata` supplies descriptive properties
 * and access control settings to attach to the stored file.
 *
 * @param file The DriveFile to upload (should be pre-optimized for storage).
 * @param metadata FileMetadata containing metadata and access control information for the upload.
 * @return A FileResult describing the outcome of the upload, including status and any server-side details.
 */
    suspend fun uploadFile(file: DriveFile, metadata: FileMetadata): FileResult
    
    /**
 * Downloads a file from cloud storage using its identifier.
 *
 * @param fileId The unique identifier of the file to download.
 * @return A [FileResult] containing the status and details of the download operation.
 */
    suspend fun downloadFile(fileId: String): FileResult
    
    /**
 * Delete a file from cloud storage by its identifier.
 *
 * Performs an asynchronous deletion and returns a FileResult describing the outcome.
 *
 * @param fileId The unique identifier of the file to delete.
 * @return A FileResult containing the deletion status and any related metadata.
 */
    suspend fun deleteFile(fileId: String): FileResult
    
    /**
 * Performs intelligent file synchronization using the provided synchronization configuration.
 *
 * Executes an AI-driven synchronization pass that applies configured rules (filters, conflict resolution,
 * bandwidth and scheduling constraints) and returns the outcome for the operation.
 *
 * @param config Synchronization parameters and rules that control what to sync and how conflicts, bandwidth,
 *               and scheduling are handled.
 * @return A FileResult describing the synchronization outcome, including status, updated file metadata, and any errors. 
 */
    suspend fun intelligentSync(config: SyncConfiguration): FileResult
}
>>>>>>> origin/coderabbitai/chat/e19563d
