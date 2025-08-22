package dev.aurakai.auraframefx.oracle.drive.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.aurakai.genesis.security.CryptographyManager
import dev.aurakai.genesis.storage.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure file service that integrates with Genesis security infrastructure.
 * Provides encrypted file operations using Genesis security primitives.
 */
@Singleton
class GenesisSecureFileService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptographyManager,
    private val secureStorage: SecureStorage
) : SecureFileService {

    private val internalStorageDir: File = context.filesDir
    private val secureFileExtension = ".gen"
    
    /**
     * Encrypts and securely saves a file to internal storage, storing associated metadata.
     *
     * The file is encrypted using a key derived from the file name and saved with a `.gen` extension in the specified directory.
     * Metadata including file name, MIME type, size, and last modified timestamp is stored securely.
     *
     * @param data The raw bytes of the file to save.
     * @param fileName The name to assign to the saved file.
     * @param directory Optional subdirectory within internal storage for the file.
     * @return A flow emitting a [FileOperationResult] indicating success with the saved file or an error.
     */
    override suspend fun saveFile(
        data: ByteArray,
        fileName: String,
        directory: String?
    ): Flow<FileOperationResult> = flow {
        try {
            val targetDir = directory?.let { File(internalStorageDir, it) } ?: internalStorageDir
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // Encrypt data using Genesis crypto
            val encryptedData = withContext(Dispatchers.IO) {
                cryptoManager.encrypt(data, getKeyAlias(fileName))
            }

            val outputFile = File(targetDir, "$fileName$secureFileExtension")
            FileOutputStream(outputFile).use { fos ->
                fos.write(encryptedData)
            }

            // Store metadata in secure storage
            val metadata = FileMetadata(
                fileName = fileName,
                mimeType = guessMimeType(fileName),
                size = data.size.toLong(),
                lastModified = System.currentTimeMillis()
            )
            secureStorage.storeMetadata(getMetadataKey(fileName), metadata)

            emit(FileOperationResult.Success(outputFile))
        } catch (e: Exception) {
            emit(FileOperationResult.Error("Failed to save file: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reads and decrypts a securely stored file and emits the result as a Flow.
     *
     * On success emits a single [FileOperationResult.Data] containing the decrypted bytes and the original file name
     * (without the secure extension). Emits [FileOperationResult.Error] if the file is missing or an error occurs during I/O or decryption.
     *
     * @param fileName The name of the file to read (without the ".gen" secure extension).
     * @param directory Optional subdirectory under the service's internal storage directory to locate the file.
     * @return A Flow that emits the operation result (one success or error event).
     */
    override suspend fun readFile(
        fileName: String,
        directory: String?
    ): Flow<FileOperationResult> = flow {
        try {
            val targetDir = directory?.let { File(internalStorageDir, it) } ?: internalStorageDir
            val inputFile = File(targetDir, "$fileName$secureFileExtension")
            
            if (!inputFile.exists()) {
                emit(FileOperationResult.Error("File not found"))
                return@flow
            }

            val encryptedData = withContext(Dispatchers.IO) {
                FileInputStream(inputFile).use { fis ->
                    fis.readBytes()
                }
            }

            // Decrypt data using Genesis crypto
            val decryptedData = cryptoManager.decrypt(encryptedData, getKeyAlias(fileName))
            emit(FileOperationResult.Data(decryptedData, inputFile.nameWithoutExtension))
        } catch (e: Exception) {
            emit(FileOperationResult.Error("Failed to read file: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Delete an encrypted file (with the service's secure extension), its stored metadata, and its cryptographic key.
     *
     * Performs I/O on the IO dispatcher; callers should not assume main-thread execution.
     *
     * @param fileName The file name without extension.
     * @param directory Optional subdirectory inside the service's internal storage; when null the root internal storage directory is used.
     * @return [FileOperationResult.Success] with the deleted [File] on successful deletion, or [FileOperationResult.Error] if the file does not exist or deletion fails (including unexpected exceptions).
     */
    override suspend fun deleteFile(
        fileName: String,
        directory: String?
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val targetDir = directory?.let { File(internalStorageDir, it) } ?: internalStorageDir
            val fileToDelete = File(targetDir, "$fileName$secureFileExtension")
            
            if (!fileToDelete.exists()) {
                return@withContext FileOperationResult.Error("File not found")
            }

            if (fileToDelete.delete()) {
                // Clean up metadata and keys
                secureStorage.removeMetadata(getMetadataKey(fileName))
                cryptoManager.removeKey(getKeyAlias(fileName))
                FileOperationResult.Success(fileToDelete)
            } else {
                FileOperationResult.Error("Failed to delete file")
            }
        } catch (e: Exception) {
            FileOperationResult.Error("Failed to delete file: ${e.message}", e)
        }
    }

    /**
     * Lists securely stored files in the given directory and returns their names without the secure extension.
     *
     * Searches the specified subdirectory (or the service's internal storage root when null) for files ending
     * with the secure extension (".gen") and returns their base names (extension removed). If the directory
     * does not exist or an error occurs, an empty list is returned.
     *
     * @param directory Optional subdirectory name within the internal storage to search; pass null to search the root.
     * @return A list of file names without the secure extension, or an empty list if none found or on error.
     */
    override suspend fun listFiles(directory: String?): List<String> = withContext(Dispatchers.IO) {
        try {
            val targetDir = directory?.let { File(internalStorageDir, it) } ?: internalStorageDir
            if (!targetDir.exists()) {
                return@withContext emptyList()
            }

            targetDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(secureFileExtension) }
                ?.map { it.nameWithoutExtension }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Create a deterministic key alias for a file by hashing its name.
     *
     * @param fileName The file name used to derive the alias.
     * @return A string alias for cryptographic keys, stable for the same file name. 
     */
    private fun getKeyAlias(fileName: String): String {
        return "oracle_drive_${fileName.hashCode()}"
    }

    /**
     * Generate a deterministic metadata storage key for a given file name.
     *
     * Produces a key in the form `file_meta_<hash>` where `<hash>` is the result of
     * `fileName.hashCode()`. The key is stable for the same file name (collisions
     * are possible due to hashCode semantics).
     *
     * @param fileName The file name to derive the metadata key from.
     * @return The metadata storage key for the file.
     */
    private fun getMetadataKey(fileName: String): String {
        return "file_meta_${fileName.hashCode()}"
    }

    /**
     * Returns the MIME type corresponding to the file extension in the given file name.
     *
     * Defaults to "application/octet-stream" if the extension is unrecognized.
     *
     * @param fileName The name of the file whose MIME type is to be determined.
     * @return The MIME type string for the file.
     */
    private fun guessMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.').lowercase()) {
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
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

/**
 * Represents file metadata for secure storage
 */
data class FileMetadata(
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val lastModified: Long,
    val tags: List<String> = emptyList()
)
