package dev.aurakai.auraframefx.oracle.drive.utils

import android.content.Context
import android.util.Log
import dev.aurakai.genesis.logging.Logger
import dev.aurakai.genesis.monitoring.PerformanceMonitor
import java.io.*
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Utility class for common file operations with proper error handling and logging.
 * Follows Genesis patterns for monitoring and logging.
 */
internal object FileOperationUtils {
    private const val TAG = "FileOperationUtils"
    private val logger = Logger.getLogger(TAG)
    
    /**
     * Ensures that the specified directory exists, creating it if necessary.
     *
     * Attempts to create the directory and any missing parent directories if they do not already exist.
     * Returns a [Result] indicating success or containing an [IOException] if creation fails.
     *
     * @param directory The directory to check or create.
     * @param coroutineContext The coroutine dispatcher to use for IO operations.
     * @return [Result.success] if the directory exists or was created successfully, or [Result.failure] with an [IOException] on failure.
     */
    suspend fun ensureDirectoryExists(
        directory: File,
        coroutineContext: CoroutineDispatcher = Dispatchers.IO
    ): Result<Unit> = withContext(coroutineContext) {
        return@withContext try {
            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (!created) {
                    throw IOException("Failed to create directory: ${directory.absolutePath}")
                }
                logger.debug("Created directory: ${directory.absolutePath}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = "Error ensuring directory exists: ${e.message}"
            logger.error(errorMsg, e)
            Result.failure(IOException(errorMsg, e))
        }
    }
    
    /**
     * Recursively deletes the given file or directory and all its contents.
     *
     * If the target does not exist this function returns a successful Result.
     * The operation runs on the supplied coroutine dispatcher (defaults to Dispatchers.IO).
     *
     * @param file File or directory to delete. If a directory, its children are deleted recursively.
     * @param coroutineContext Dispatcher on which the IO work will be performed.
     * @return Result.success(Unit) on successful deletion (or if the file did not exist),
     *         or Result.failure(IOException) if the deletion fails.
    suspend fun deleteFileOrDirectory(
        file: File,
        coroutineContext: CoroutineDispatcher = Dispatchers.IO
    ): Result<Unit> = withContext(coroutineContext) {
        return@withContext try {
            if (file.exists()) {
                if (file.isDirectory) {
                    file.listFiles()?.forEach { deleteFileOrDirectory(it).getOrThrow() }
                }
                val deleted = file.delete()
                if (!deleted) {
                    throw IOException("Failed to delete: ${file.absolutePath}")
                }
                logger.debug("Deleted: ${file.absolutePath}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = "Error deleting ${file.absolutePath}: ${e.message}"
            logger.error(errorMsg, e)
            Result.failure(IOException(errorMsg, e))
        }
    }
    
    /**
     * Copy a file to a destination with optional progress callbacks, executing on the provided coroutine dispatcher.
     *
     * The operation runs on the given `coroutineContext`. Progress (bytes copied, total bytes) is reported after each write
     * when `progressCallback` is provided. On success returns `Result.success(Unit)`. On failure (including a missing source
     * file or any IO error) returns `Result.failure(IOException)` containing the error.
     *
     * @param bufferSize Size of the internal buffer, in bytes, used for each read/write iteration.
     * @param progressCallback Optional callback invoked after each write with `bytesCopied` and `totalBytes`.
     * @return `Result.success(Unit)` if the copy completes; otherwise `Result.failure(IOException)` with details.
     */
    suspend fun copyFileWithProgress(
        source: File,
        destination: File,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        coroutineContext: CoroutineDispatcher = Dispatchers.IO,
        progressCallback: ((bytesCopied: Long, totalBytes: Long) -> Unit)? = null
    ): Result<Unit> = withContext(coroutineContext) {
        val monitor = PerformanceMonitor.start("file_copy")
        
        return@withContext try {
            if (!source.exists()) {
                throw FileNotFoundException("Source file not found: ${source.absolutePath}")
            }
            
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(bufferSize)
                    var bytesCopied = 0L
                    val totalBytes = source.length()
                    
                    while (true) {
                        val bytes = input.read(buffer)
                        if (bytes <= 0) break
                        
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        
                        // Update progress if callback provided
                        progressCallback?.invoke(bytesCopied, totalBytes)
                    }
                }
            }
            
            monitor.stop()
            logger.debug("Copied ${source.absolutePath} to ${destination.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            monitor.fail(e)
            val errorMsg = "Error copying ${source.absolutePath} to ${destination.absolutePath}: ${e.message}"
            logger.error(errorMsg, e)
            Result.failure(IOException(errorMsg, e))
        }
    }
    
    /**
     * Validates a file name to ensure it does not contain unsafe or disallowed patterns.
     *
     * Checks for directory traversal sequences, path separators, null characters, and empty or whitespace-only names.
     * Returns a successful result with the file name if valid, or a failure with an exception if invalid.
     *
     * @param fileName The file name to validate.
     * @return A Result containing the valid file name or a failure with the validation exception.
     */
    fun validateFileName(fileName: String): Result<String> {
        return try {
            // Basic validation - prevent directory traversal and other unsafe patterns
            if (fileName.contains("..") || 
                fileName.contains("/") || 
                fileName.contains("\\") ||
                fileName.contains("\0") ||
                fileName.trim().isEmpty()) {
                throw SecurityException("Invalid file name: $fileName")
            }
            
            // Additional security checks can be added here
            
            Result.success(fileName)
        } catch (e: Exception) {
            logger.error("File name validation failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Determine the MIME type for a file name based on its extension.
     *
     * The lookup is case-insensitive. If the file name has no extension or the extension
     * is not recognized, this returns "application/octet-stream".
     *
     * @param fileName The file name (or path) to inspect.
     * @return The corresponding MIME type as a String.
     */
    fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.').lowercase()) {
            "txt", "log", "json", "xml", "html", "css", "js" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "zip" -> "application/zip"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            else -> "application/octet-stream"
        }
    }
}
