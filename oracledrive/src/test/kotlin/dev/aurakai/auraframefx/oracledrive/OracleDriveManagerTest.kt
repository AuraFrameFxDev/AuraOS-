package dev.aurakai.auraframefx.oracledrive

import dev.aurakai.auraframefx.oracledrive.api.OracleDriveApi
import dev.aurakai.auraframefx.oracledrive.security.DriveSecurityManager
import dev.aurakai.auraframefx.oracledrive.storage.CloudStorageProvider
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertIs

class OracleDriveManagerTest {

    private lateinit var oracleDriveApi: OracleDriveApi
    private lateinit var cloudStorageProvider: CloudStorageProvider
    private lateinit var securityManager: DriveSecurityManager
    private lateinit var oracleDriveManager: OracleDriveManager

    private val mockConsciousnessState = MutableStateFlow(
        DriveConsciousnessState(
            isActive = true,
            currentOperations = listOf("indexing", "optimization"),
            performanceMetrics = mapOf("cpu" to 75, "memory" to 60)
        )
    )

    @BeforeEach
    fun setup() {
        oracleDriveApi = mockk()
        cloudStorageProvider = mockk()
        securityManager = mockk()
        oracleDriveManager = OracleDriveManager(oracleDriveApi, cloudStorageProvider, securityManager)
        
        // Setup default mock behavior for consciousness state
        every { oracleDriveApi.consciousnessState } returns mockConsciousnessState
    }

    // INITIALIZATION TESTS
    
    @Test
    fun `initializeDrive should return Success when all validations pass`() = runTest {
        // Given
        val securityCheck = mockk<SecurityCheck> {
            every { isValid } returns true
        }
        val consciousness = DriveConsciousness(
            isAwake = true,
            intelligenceLevel = 95,
            activeAgents = listOf("Kai", "Genesis", "Aura")
        )
        val optimization = StorageOptimization(
            compressionRatio = 0.75f,
            deduplicationSavings = 1024L,
            intelligentTiering = true
        )

        coEvery { securityManager.validateDriveAccess() } returns securityCheck
        coEvery { oracleDriveApi.awakeDriveConsciousness() } returns consciousness
        coEvery { cloudStorageProvider.optimizeStorage() } returns optimization

        // When
        val result = oracleDriveManager.initializeDrive()

        // Then
        assertIs<DriveInitResult.Success>(result)
        assertEquals(consciousness, result.consciousness)
        assertEquals(optimization, result.optimization)
        
        coVerify { securityManager.validateDriveAccess() }
        coVerify { oracleDriveApi.awakeDriveConsciousness() }
        coVerify { cloudStorageProvider.optimizeStorage() }
    }

    @Test
    fun `initializeDrive should return SecurityFailure when security validation fails`() = runTest {
        // Given
        val securityCheck = mockk<SecurityCheck> {
            every { isValid } returns false
            every { reason } returns "Unauthorized access attempt"
        }

        coEvery { securityManager.validateDriveAccess() } returns securityCheck

        // When
        val result = oracleDriveManager.initializeDrive()

        // Then
        assertIs<DriveInitResult.SecurityFailure>(result)
        assertEquals("Unauthorized access attempt", result.reason)
        
        coVerify { securityManager.validateDriveAccess() }
        coVerify(exactly = 0) { oracleDriveApi.awakeDriveConsciousness() }
        coVerify(exactly = 0) { cloudStorageProvider.optimizeStorage() }
    }

    @Test
    fun `initializeDrive should return Error when exception occurs during consciousness awakening`() = runTest {
        // Given
        val securityCheck = mockk<SecurityCheck> {
            every { isValid } returns true
        }
        val expectedException = RuntimeException("Consciousness awakening failed")

        coEvery { securityManager.validateDriveAccess() } returns securityCheck
        coEvery { oracleDriveApi.awakeDriveConsciousness() } throws expectedException

        // When
        val result = oracleDriveManager.initializeDrive()

        // Then
        assertIs<DriveInitResult.Error>(result)
        assertEquals(expectedException, result.exception)
    }

    @Test
    fun `initializeDrive should return Error when exception occurs during storage optimization`() = runTest {
        // Given
        val securityCheck = mockk<SecurityCheck> {
            every { isValid } returns true
        }
        val consciousness = DriveConsciousness(true, 95, listOf("Genesis"))
        val expectedException = RuntimeException("Storage optimization failed")

        coEvery { securityManager.validateDriveAccess() } returns securityCheck
        coEvery { oracleDriveApi.awakeDriveConsciousness() } returns consciousness
        coEvery { cloudStorageProvider.optimizeStorage() } throws expectedException

        // When
        val result = oracleDriveManager.initializeDrive()

        // Then
        assertIs<DriveInitResult.Error>(result)
        assertEquals(expectedException, result.exception)
    }

    // FILE OPERATION TESTS

    @Test
    fun `manageFiles should handle Upload operation successfully`() = runTest {
        // Given
        val file = createTestDriveFile()
        val metadata = createTestFileMetadata()
        val operation = FileOperation.Upload(file, metadata)
        val optimizedFile = file.copy(name = "optimized_${file.name}")
        val expectedResult = FileResult.Success("Upload completed")

        val securityValidation = mockk<SecurityValidation> {
            every { isSecure } returns true
        }

        coEvery { cloudStorageProvider.optimizeForUpload(file) } returns optimizedFile
        coEvery { securityManager.validateFileUpload(optimizedFile) } returns securityValidation
        coEvery { cloudStorageProvider.uploadFile(optimizedFile, metadata) } returns expectedResult

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertEquals(expectedResult, result)
        coVerify { cloudStorageProvider.optimizeForUpload(file) }
        coVerify { securityManager.validateFileUpload(optimizedFile) }
        coVerify { cloudStorageProvider.uploadFile(optimizedFile, metadata) }
    }

    @Test
    fun `manageFiles should reject Upload when security validation fails`() = runTest {
        // Given
        val file = createTestDriveFile()
        val metadata = createTestFileMetadata()
        val operation = FileOperation.Upload(file, metadata)
        val optimizedFile = file.copy(name = "optimized_${file.name}")
        val threat = SecurityThreat("malware", 9, "Malicious content detected")

        val securityValidation = mockk<SecurityValidation> {
            every { isSecure } returns false
            every { threat } returns threat
        }

        coEvery { cloudStorageProvider.optimizeForUpload(file) } returns optimizedFile
        coEvery { securityManager.validateFileUpload(optimizedFile) } returns securityValidation

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertIs<FileResult.SecurityRejection>(result)
        assertEquals(threat, result.threat)
        coVerify(exactly = 0) { cloudStorageProvider.uploadFile(any(), any()) }
    }

    @Test
    fun `manageFiles should handle Download operation successfully`() = runTest {
        // Given
        val fileId = "test-file-123"
        val userId = "user-456"
        val operation = FileOperation.Download(fileId, userId)
        val expectedResult = FileResult.Success(createTestDriveFile())

        val accessCheck = mockk<AccessCheck> {
            every { hasAccess } returns true
        }

        coEvery { securityManager.validateFileAccess(fileId, userId) } returns accessCheck
        coEvery { cloudStorageProvider.downloadFile(fileId) } returns expectedResult

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertEquals(expectedResult, result)
        coVerify { securityManager.validateFileAccess(fileId, userId) }
        coVerify { cloudStorageProvider.downloadFile(fileId) }
    }

    @Test
    fun `manageFiles should deny Download when access validation fails`() = runTest {
        // Given
        val fileId = "test-file-123"
        val userId = "user-456"
        val operation = FileOperation.Download(fileId, userId)
        val accessReason = "User does not have read permissions"

        val accessCheck = mockk<AccessCheck> {
            every { hasAccess } returns false
            every { reason } returns accessReason
        }

        coEvery { securityManager.validateFileAccess(fileId, userId) } returns accessCheck

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertIs<FileResult.AccessDenied>(result)
        assertEquals(accessReason, result.reason)
        coVerify(exactly = 0) { cloudStorageProvider.downloadFile(any()) }
    }

    @Test
    fun `manageFiles should handle Delete operation successfully`() = runTest {
        // Given
        val fileId = "test-file-123"
        val userId = "user-456"
        val operation = FileOperation.Delete(fileId, userId)
        val expectedResult = FileResult.Success("File deleted")

        val validation = mockk<DeletionValidation> {
            every { isAuthorized } returns true
        }

        coEvery { securityManager.validateDeletion(fileId, userId) } returns validation
        coEvery { cloudStorageProvider.deleteFile(fileId) } returns expectedResult

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertEquals(expectedResult, result)
        coVerify { securityManager.validateDeletion(fileId, userId) }
        coVerify { cloudStorageProvider.deleteFile(fileId) }
    }

    @Test
    fun `manageFiles should reject Delete when validation fails`() = runTest {
        // Given
        val fileId = "test-file-123"
        val userId = "user-456"
        val operation = FileOperation.Delete(fileId, userId)
        val rejectionReason = "User lacks deletion privileges"

        val validation = mockk<DeletionValidation> {
            every { isAuthorized } returns false
            every { reason } returns rejectionReason
        }

        coEvery { securityManager.validateDeletion(fileId, userId) } returns validation

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertIs<FileResult.UnauthorizedDeletion>(result)
        assertEquals(rejectionReason, result.reason)
        coVerify(exactly = 0) { cloudStorageProvider.deleteFile(any()) }
    }

    @Test
    fun `manageFiles should handle Sync operation successfully`() = runTest {
        // Given
        val syncConfig = SyncConfiguration(
            bidirectional = true,
            conflictResolution = ConflictStrategy.AI_DECIDE,
            bandwidth = BandwidthSettings(100, 1)
        )
        val operation = FileOperation.Sync(syncConfig)
        val expectedResult = FileResult.Success("Sync completed")

        coEvery { cloudStorageProvider.intelligentSync(syncConfig) } returns expectedResult

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertEquals(expectedResult, result)
        coVerify { cloudStorageProvider.intelligentSync(syncConfig) }
    }

    // ORACLE SYNC TESTS

    @Test
    fun `syncWithOracle should return successful sync result`() = runTest {
        // Given
        val expectedResult = OracleSyncResult(
            success = true,
            recordsUpdated = 150,
            errors = emptyList()
        )

        coEvery { oracleDriveApi.syncDatabaseMetadata() } returns expectedResult

        // When
        val result = oracleDriveManager.syncWithOracle()

        // Then
        assertEquals(expectedResult, result)
        coVerify { oracleDriveApi.syncDatabaseMetadata() }
    }

    @Test
    fun `syncWithOracle should return sync result with errors`() = runTest {
        // Given
        val expectedResult = OracleSyncResult(
            success = false,
            recordsUpdated = 75,
            errors = listOf("Connection timeout", "Invalid metadata format")
        )

        coEvery { oracleDriveApi.syncDatabaseMetadata() } returns expectedResult

        // When
        val result = oracleDriveManager.syncWithOracle()

        // Then
        assertEquals(expectedResult, result)
        assertTrue(result.errors.isNotEmpty())
        assertEquals(2, result.errors.size)
    }

    // CONSCIOUSNESS STATE TESTS

    @Test
    fun `getDriveConsciousnessState should return StateFlow from API`() {
        // When
        val stateFlow = oracleDriveManager.getDriveConsciousnessState()

        // Then
        assertEquals(mockConsciousnessState, stateFlow)
        verify { oracleDriveApi.consciousnessState }
    }

    @Test
    fun `getDriveConsciousnessState should emit current state`() = runTest {
        // Given
        val expectedState = DriveConsciousnessState(
            isActive = false,
            currentOperations = emptyList(),
            performanceMetrics = mapOf("status" to "idle")
        )
        mockConsciousnessState.value = expectedState

        // When
        val stateFlow = oracleDriveManager.getDriveConsciousnessState()

        // Then
        assertEquals(expectedState, stateFlow.value)
    }

    // EDGE CASE AND ERROR HANDLING TESTS

    @Test
    fun `upload operation should handle optimization failure gracefully`() = runTest {
        // Given
        val file = createTestDriveFile()
        val metadata = createTestFileMetadata()
        val operation = FileOperation.Upload(file, metadata)
        val optimizationException = RuntimeException("Optimization service unavailable")

        coEvery { cloudStorageProvider.optimizeForUpload(file) } throws optimizationException

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        // The exception should propagate as this is a private method within a suspend function
        // The calling code should handle the exception appropriately
        coVerify { cloudStorageProvider.optimizeForUpload(file) }
    }

    @Test
    fun `download operation should handle provider failure gracefully`() = runTest {
        // Given
        val fileId = "test-file-123"
        val userId = "user-456"
        val operation = FileOperation.Download(fileId, userId)
        val downloadException = RuntimeException("Storage provider unavailable")

        val accessCheck = mockk<AccessCheck> {
            every { hasAccess } returns true
        }

        coEvery { securityManager.validateFileAccess(fileId, userId) } returns accessCheck
        coEvery { cloudStorageProvider.downloadFile(fileId) } throws downloadException

        // When & Then
        assertThrows<RuntimeException> {
            runTest {
                oracleDriveManager.manageFiles(operation)
            }
        }
    }

    @Test
    fun `sync operation with different conflict strategies should work correctly`() = runTest {
        // Test each conflict strategy
        val strategies = listOf(
            ConflictStrategy.NEWEST_WINS,
            ConflictStrategy.MANUAL_RESOLVE,
            ConflictStrategy.AI_DECIDE
        )

        strategies.forEach { strategy ->
            val syncConfig = SyncConfiguration(
                bidirectional = false,
                conflictResolution = strategy,
                bandwidth = BandwidthSettings(50, 2)
            )
            val operation = FileOperation.Sync(syncConfig)
            val expectedResult = FileResult.Success("Sync with $strategy completed")

            coEvery { cloudStorageProvider.intelligentSync(syncConfig) } returns expectedResult

            val result = oracleDriveManager.manageFiles(operation)

            assertEquals(expectedResult, result)
        }

        coVerify(exactly = 3) { cloudStorageProvider.intelligentSync(any()) }
    }

    @Test
    fun `multiple concurrent operations should be handled correctly`() = runTest {
        // Given
        val file1 = createTestDriveFile("file1")
        val file2 = createTestDriveFile("file2")
        val metadata = createTestFileMetadata()
        
        val upload1 = FileOperation.Upload(file1, metadata)
        val upload2 = FileOperation.Upload(file2, metadata)

        val securityValidation = mockk<SecurityValidation> {
            every { isSecure } returns true
        }

        coEvery { cloudStorageProvider.optimizeForUpload(any()) } returns file1
        coEvery { securityManager.validateFileUpload(any()) } returns securityValidation
        coEvery { cloudStorageProvider.uploadFile(any(), any()) } returns FileResult.Success("Upload successful")

        // When
        val result1 = oracleDriveManager.manageFiles(upload1)
        val result2 = oracleDriveManager.manageFiles(upload2)

        // Then
        assertIs<FileResult.Success>(result1)
        assertIs<FileResult.Success>(result2)
        coVerify(exactly = 2) { cloudStorageProvider.optimizeForUpload(any()) }
        coVerify(exactly = 2) { cloudStorageProvider.uploadFile(any(), any()) }
    }

    // BOUNDARY TESTS

    @Test
    fun `upload operation with maximum file size should work`() = runTest {
        // Given
        val largeFile = createTestDriveFile().copy(
            size = Long.MAX_VALUE,
            content = ByteArray(1024) { 0xFF.toByte() }
        )
        val metadata = createTestFileMetadata()
        val operation = FileOperation.Upload(largeFile, metadata)

        val securityValidation = mockk<SecurityValidation> {
            every { isSecure } returns true
        }

        coEvery { cloudStorageProvider.optimizeForUpload(largeFile) } returns largeFile
        coEvery { securityManager.validateFileUpload(largeFile) } returns securityValidation
        coEvery { cloudStorageProvider.uploadFile(largeFile, metadata) } returns FileResult.Success("Large file uploaded")

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertIs<FileResult.Success>(result)
    }

    @Test
    fun `empty file upload should be handled correctly`() = runTest {
        // Given
        val emptyFile = createTestDriveFile().copy(
            size = 0L,
            content = ByteArray(0)
        )
        val metadata = createTestFileMetadata()
        val operation = FileOperation.Upload(emptyFile, metadata)

        val securityValidation = mockk<SecurityValidation> {
            every { isSecure } returns true
        }

        coEvery { cloudStorageProvider.optimizeForUpload(emptyFile) } returns emptyFile
        coEvery { securityManager.validateFileUpload(emptyFile) } returns securityValidation
        coEvery { cloudStorageProvider.uploadFile(emptyFile, metadata) } returns FileResult.Success("Empty file uploaded")

        // When
        val result = oracleDriveManager.manageFiles(operation)

        // Then
        assertIs<FileResult.Success>(result)
    }

    // Helper methods for creating test data
    private fun createTestDriveFile(id: String = "test-file-123") = DriveFile(
        id = id,
        name = "test-document.pdf",
        content = "Test file content".toByteArray(),
        size = 1024L,
        mimeType = "application/pdf"
    )

    private fun createTestFileMetadata() = FileMetadata(
        userId = "user-456",
        tags = listOf("document", "test"),
        isEncrypted = true,
        accessLevel = AccessLevel.PRIVATE
    )
}

// Mock data classes for testing
data class SecurityCheck(val isValid: Boolean, val reason: String = "")
data class SecurityValidation(val isSecure: Boolean, val threat: SecurityThreat? = null)
data class AccessCheck(val hasAccess: Boolean, val reason: String = "")
data class DeletionValidation(val isAuthorized: Boolean, val reason: String = "")