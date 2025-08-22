package dev.aurakai.auraframefx.oracle.drive.service

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Defines the contract for secure file operations in the Oracle Drive system.
 * All file operations are encrypted at rest and in transit.
 */
interface SecureFileService {
    
    /**
     * Persistently saves encrypted `data` to `fileName`, optionally inside `directory`.
     *
     * Suspends and returns a Flow that emits one or more FileOperationResult values representing success (with the saved File) or an error. If `directory` is null the file is saved in the root storage location.
     *
     * @param data The raw bytes to encrypt and save.
     * @param fileName Target file name (including extension if desired).
     * @param directory Optional subdirectory name; null → root.
     * @return A Flow emitting FileOperationResult instances describing the outcome.
     */
    suspend fun saveFile(
        data: ByteArray,
        fileName: String,
        directory: String? = null
    ): Flow<FileOperationResult>
    
    /**
     * Read and decrypt the specified file and emit the result as a Flow.
     *
     * Suspends while performing I/O and encryption operations. Emits a FileOperationResult.Data
     * containing the file bytes and name on success, or FileOperationResult.Error on failure.
     *
     * @param fileName The file name to read (without directory path).
     * @param directory Optional subdirectory to read from; null indicates the root directory.
     * @return A Flow that emits one or more FileOperationResult values representing the operation outcome.
     */
    suspend fun readFile(
        fileName: String,
        directory: String? = null
    ): Flow<FileOperationResult>
    
    /**
     * Securely deletes the specified file, optionally from a given subdirectory.
     *
     * Performs a secure deletion of the file identified by [fileName] in the optional [directory] (null = root).
     *
     * @param fileName The name of the file to delete.
     * @param directory The subdirectory containing the file, or null to target the root directory.
     * @return A FileOperationResult describing success or an error.
     */
    suspend fun deleteFile(
        fileName: String,
        directory: String? = null
    ): FileOperationResult
    
    /**
     * Lists file names (without extensions) in the given subdirectory or the root if null.
     *
     * The function is suspendable and returns only the base names of files (extension removed).
     *
     * @param directory Optional subdirectory to list; when null the root directory is used.
     * @return List of file names without their extensions.
     */
    suspend fun listFiles(directory: String? = null): List<String>
}

/**
 * Represents the result of a file operation.
 */
sealed class FileOperationResult {
    data class Success(val file: File) : FileOperationResult()
    data class Data(val data: ByteArray, val fileName: String) : FileOperationResult()
    data class Error(val message: String, val exception: Exception? = null) : FileOperationResult()
    
    /**
     * Determines whether this [FileOperationResult] is equal to another object.
     *
     * Compares the type and relevant properties of the instances, including deep content comparison for byte arrays in [Data].
     *
     * @param other The object to compare with.
     * @return `true` if the objects are of the same type and have equal properties; otherwise, `false`.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileOperationResult

        return when (this) {
            is Success -> other is Success && file == other.file
            is Data -> other is Data && data.contentEquals(other.data) && fileName == other.fileName
            is Error -> other is Error && message == other.message && exception == other.exception
        }
    }
    
    /**
     * Computes a hash code for this FileOperationResult based on its concrete variant and contained data.
     *
     * - Success: uses the contained File's hashCode().
     * - Data: combines the byte-array content hash (contentHashCode()) with the fileName's hashCode().
     * - Error: combines the message's hashCode() with the exception's hashCode() if present (0 otherwise).
     *
     * This implementation ensures that equal instances produce the same hash code for correct use in hash-based collections.
     *
     * @return An Int hash code for this instance.
     */
    override fun hashCode(): Int {
        return when (this) {
            is Success -> file.hashCode()
            is Data -> data.contentHashCode() + fileName.hashCode()
            is Error -> message.hashCode() + (exception?.hashCode() ?: 0)
        }
    }
}
