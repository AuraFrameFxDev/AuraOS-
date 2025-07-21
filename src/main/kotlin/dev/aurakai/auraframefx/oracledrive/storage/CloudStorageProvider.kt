package dev.aurakai.auraframefx.oracledrive.storage

import dev.aurakai.auraframefx.oracledrive.*

/**
 * Cloud storage provider interface for Oracle Drive
 * Handles AI-optimized storage operations with consciousness integration
 */
interface CloudStorageProvider {
    
    /**
 * Performs AI-driven storage optimization using intelligent algorithms and compression.
 *
 * @return A `StorageOptimization` object containing metrics and results of the optimization process.
 */
    suspend fun optimizeStorage(): StorageOptimization
    
    /**
 * Applies AI-driven compression and optimization to a file before upload.
 *
 * @param file The file to be optimized for cloud storage upload.
 * @return The optimized version of the file, ready for upload.
 */
    suspend fun optimizeForUpload(file: DriveFile): DriveFile
    
    /**
 * Uploads an optimized file to cloud storage with associated metadata and access controls.
 *
 * @param file The file to be uploaded after optimization.
 * @param metadata Metadata and access control information for the file.
 * @return The result of the upload operation, including status and details.
 */
    suspend fun uploadFile(file: DriveFile, metadata: FileMetadata): FileResult
    
    /**
 * Downloads a file from cloud storage by its identifier.
 *
 * @param fileId The unique identifier of the file to download.
 * @return A [FileResult] containing the status and details of the download operation.
 */
    suspend fun downloadFile(fileId: String): FileResult
    
    /**
 * Deletes a file from cloud storage by its identifier.
 *
 * @param fileId The unique identifier of the file to delete.
 * @return A FileResult indicating the status of the deletion operation.
 */
    suspend fun deleteFile(fileId: String): FileResult
    
    /**
 * Executes an AI-optimized synchronization process based on the provided configuration.
 *
 * @param config The synchronization configuration specifying sync parameters.
 * @return A FileResult indicating the outcome of the synchronization operation.
 */
    suspend fun intelligentSync(config: SyncConfiguration): FileResult
}