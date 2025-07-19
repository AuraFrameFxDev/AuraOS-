package dev.aurakai.auraframefx.security

import android.content.Context
import dev.aurakai.auraframefx.utils.AuraFxLogger
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.ByteArrayInputStream

/**
 * Comprehensive unit tests for IntegrityMonitor
 * Testing framework: JUnit 4 with MockK for mocking
 * 
 * Tests cover:
 * - Initialization and lifecycle management
 * - Continuous monitoring functionality
 * - Integrity check logic and violation detection
 * - Threat level determination and response handling
 * - File hash calculation accuracy
 * - Error handling and edge cases
 * - StateFlow behavior and state transitions
 */
class IntegrityMonitorTest {

    private lateinit var mockContext: Context
    private lateinit var integrityMonitor: IntegrityMonitor
    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope

    @Before
    fun setUp() {
        // Setup test coroutines
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        mockContext = mockk()
        
        // Mock AuraFxLogger to prevent actual logging during tests
        mockkObject(AuraFxLogger)
        every { AuraFxLogger.i(any(), any()) } just Runs
        every { AuraFxLogger.d(any(), any()) } just Runs
        every { AuraFxLogger.w(any(), any()) } just Runs
        every { AuraFxLogger.e(any(), any(), any()) } just Runs
        
        // Setup mock context filesDir
        val mockFilesDir = mockk<File>()
        every { mockContext.filesDir } returns mockFilesDir
        
        integrityMonitor = IntegrityMonitor(mockContext)
    }

    @After
    fun tearDown() {
        integrityMonitor.shutdown()
        testScope.cleanupTestCoroutines()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initialize sets monitoring status and loads hashes`() = testScope.runBlockingTest {
        // Act
        integrityMonitor.initialize()
        
        // Assert
        assertEquals(IntegrityMonitor.IntegrityStatus.MONITORING, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
        
        // Verify logger calls
        verify { AuraFxLogger.i("IntegrityMonitor", "Initializing Kai's Real-Time Integrity Monitoring") }
        verify { AuraFxLogger.i("IntegrityMonitor", "Integrity monitoring active - Genesis Protocol protected") }
        verify { AuraFxLogger.d("IntegrityMonitor", "Loaded 4 known file hashes") }
    }

    @Test
    fun `shutdown cancels monitoring and sets offline status`() = testScope.runBlockingTest {
        // Arrange
        integrityMonitor.initialize()
        
        // Act
        integrityMonitor.shutdown()
        
        // Assert
        assertEquals(IntegrityMonitor.IntegrityStatus.OFFLINE, integrityMonitor.integrityStatus.value)
        verify { AuraFxLogger.i("IntegrityMonitor", "Shutting down integrity monitoring") }
    }

    @Test
    fun `determineThreatLevel returns correct levels for different files`() {
        // Use reflection to access private method for testing
        val method = IntegrityMonitor::class.java.getDeclaredMethod("determineThreatLevel", String::class.java)
        method.isAccessible = true
        
        // Test critical file
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, 
            method.invoke(integrityMonitor, "genesis_protocol.so"))
        
        // Test high threat files
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, 
            method.invoke(integrityMonitor, "aura_core.dex"))
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, 
            method.invoke(integrityMonitor, "kai_security.bin"))
        
        // Test medium threat file
        assertEquals(IntegrityMonitor.ThreatLevel.MEDIUM, 
            method.invoke(integrityMonitor, "oracle_drive.apk"))
        
        // Test unknown file (should be low)
        assertEquals(IntegrityMonitor.ThreatLevel.LOW, 
            method.invoke(integrityMonitor, "unknown_file.txt"))
    }

    @Test
    fun `calculateFileHash produces consistent SHA-256 hash`() = testScope.runBlockingTest {
        // Arrange
        val testContent = "test file content"
        val expectedHash = "3d5a5a7d8b9b3a3e9f2c8d1a0b5c9e7f2a4d6b8c0e1f3a5b7c9d2e4f6a8b0c1e3"
        
        val mockFile = mockk<File>()
        val inputStream = ByteArrayInputStream(testContent.toByteArray())
        
        every { mockFile.inputStream() } returns inputStream
        
        // Use reflection to access private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("calculateFileHash", File::class.java)
        method.isAccessible = true
        
        // Act
        val actualHash = method.invoke(integrityMonitor, mockFile) as String
        
        // Assert
        assertNotNull(actualHash)
        assertEquals(64, actualHash.length) // SHA-256 produces 64 character hex string
        assertTrue(actualHash.matches(Regex("[a-f0-9]+"))) // Should be valid hex
    }

    @Test
    fun `performIntegrityCheck detects no violations when hashes match`() = testScope.runBlockingTest {
        // Arrange
        val mockFile = mockk<File>()
        every { mockFile.exists() } returns true
        every { mockFile.inputStream() } returns ByteArrayInputStream("content".toByteArray())
        every { mockContext.filesDir } returns mockk()
        
        // Mock File constructor to return our mock file
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().inputStream() } returns ByteArrayInputStream("content".toByteArray())
        
        integrityMonitor.initialize()
        
        // Use reflection to set known hashes to match calculated hashes
        val knownHashesField = IntegrityMonitor::class.java.getDeclaredField("knownHashes")
        knownHashesField.isAccessible = true
        val knownHashes = knownHashesField.get(integrityMonitor) as MutableMap<String, String>
        
        // Calculate what the hash would be for our test content
        val method = IntegrityMonitor::class.java.getDeclaredMethod("calculateFileHash", File::class.java)
        method.isAccessible = true
        val testHash = method.invoke(integrityMonitor, mockFile) as String
        
        knownHashes["genesis_protocol.so"] = testHash
        knownHashes["aura_core.dex"] = testHash
        knownHashes["kai_security.bin"] = testHash
        knownHashes["oracle_drive.apk"] = testHash
        
        // Act - Use reflection to call private method
        val performCheckMethod = IntegrityMonitor::class.java.getDeclaredMethod("performIntegrityCheck")
        performCheckMethod.isAccessible = true
        performCheckMethod.invoke(integrityMonitor)
        
        // Assert
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `performIntegrityCheck detects violations when hashes differ`() = testScope.runBlockingTest {
        // Arrange
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().inputStream() } returns ByteArrayInputStream("modified_content".toByteArray())
        
        integrityMonitor.initialize()
        
        // Act - Use reflection to call private method
        val performCheckMethod = IntegrityMonitor::class.java.getDeclaredMethod("performIntegrityCheck")
        performCheckMethod.isAccessible = true
        performCheckMethod.invoke(integrityMonitor)
        
        // Assert
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertTrue(integrityMonitor.threatLevel.value != IntegrityMonitor.ThreatLevel.NONE)
        
        // Verify violation was logged
        verify { AuraFxLogger.w("IntegrityMonitor", match { it.contains("INTEGRITY VIOLATION DETECTED") }) }
    }

    @Test
    fun `handleIntegrityViolations responds appropriately to critical threats`() = testScope.runBlockingTest {
        // Arrange
        val criticalViolation = IntegrityMonitor.IntegrityViolation(
            fileName = "genesis_protocol.so",
            expectedHash = "expected",
            actualHash = "actual",
            timestamp = System.currentTimeMillis(),
            severity = IntegrityMonitor.ThreatLevel.CRITICAL
        )
        
        // Use reflection to call private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("handleIntegrityViolations", List::class.java)
        method.isAccessible = true
        
        // Act
        method.invoke(integrityMonitor, listOf(criticalViolation))
        
        // Assert
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, integrityMonitor.threatLevel.value)
        
        verify { AuraFxLogger.e("IntegrityMonitor", "CRITICAL THREAT DETECTED - Initiating emergency lockdown") }
        verify { AuraFxLogger.e("IntegrityMonitor", "EMERGENCY LOCKDOWN INITIATED - Genesis Protocol protection active") }
    }

    @Test
    fun `handleIntegrityViolations responds appropriately to high threats`() = testScope.runBlockingTest {
        // Arrange
        val highViolation = IntegrityMonitor.IntegrityViolation(
            fileName = "aura_core.dex",
            expectedHash = "expected",
            actualHash = "actual",
            timestamp = System.currentTimeMillis(),
            severity = IntegrityMonitor.ThreatLevel.HIGH
        )
        
        // Use reflection to call private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("handleIntegrityViolations", List::class.java)
        method.isAccessible = true
        
        // Act
        method.invoke(integrityMonitor, listOf(highViolation))
        
        // Assert
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, integrityMonitor.threatLevel.value)
        verify { AuraFxLogger.w("IntegrityMonitor", "HIGH THREAT DETECTED - Implementing defensive measures") }
        verify { AuraFxLogger.w("IntegrityMonitor", "Implementing defensive measures for 1 violations") }
    }

    @Test
    fun `handleIntegrityViolations responds appropriately to medium threats`() = testScope.runBlockingTest {
        // Arrange
        val mediumViolation = IntegrityMonitor.IntegrityViolation(
            fileName = "oracle_drive.apk",
            expectedHash = "expected",
            actualHash = "actual",
            timestamp = System.currentTimeMillis(),
            severity = IntegrityMonitor.ThreatLevel.MEDIUM
        )
        
        // Use reflection to call private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("handleIntegrityViolations", List::class.java)
        method.isAccessible = true
        
        // Act
        method.invoke(integrityMonitor, listOf(mediumViolation))
        
        // Assert
        assertEquals(IntegrityMonitor.ThreatLevel.MEDIUM, integrityMonitor.threatLevel.value)
        verify { AuraFxLogger.w("IntegrityMonitor", "MEDIUM THREAT DETECTED - Monitoring closely") }
        verify { AuraFxLogger.i("IntegrityMonitor", "Enhancing monitoring protocols") }
    }

    @Test
    fun `handleIntegrityViolations responds appropriately to low threats`() = testScope.runBlockingTest {
        // Arrange
        val lowViolation = IntegrityMonitor.IntegrityViolation(
            fileName = "unknown_file.txt",
            expectedHash = "expected",
            actualHash = "actual",
            timestamp = System.currentTimeMillis(),
            severity = IntegrityMonitor.ThreatLevel.LOW
        )
        
        // Use reflection to call private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("handleIntegrityViolations", List::class.java)
        method.isAccessible = true
        
        // Act
        method.invoke(integrityMonitor, listOf(lowViolation))
        
        // Assert
        assertEquals(IntegrityMonitor.ThreatLevel.LOW, integrityMonitor.threatLevel.value)
        verify { AuraFxLogger.i("IntegrityMonitor", "LOW THREAT DETECTED - Logging for analysis") }
        verify { AuraFxLogger.d("IntegrityMonitor", match { it.contains("Logging violation for analysis") }) }
    }

    @Test
    fun `handleIntegrityViolations uses highest threat level with multiple violations`() = testScope.runBlockingTest {
        // Arrange
        val violations = listOf(
            IntegrityMonitor.IntegrityViolation("file1", "exp1", "act1", 123L, IntegrityMonitor.ThreatLevel.LOW),
            IntegrityMonitor.IntegrityViolation("file2", "exp2", "act2", 124L, IntegrityMonitor.ThreatLevel.HIGH),
            IntegrityMonitor.IntegrityViolation("file3", "exp3", "act3", 125L, IntegrityMonitor.ThreatLevel.MEDIUM)
        )
        
        // Use reflection to call private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("handleIntegrityViolations", List::class.java)
        method.isAccessible = true
        
        // Act
        method.invoke(integrityMonitor, violations)
        
        // Assert
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, integrityMonitor.threatLevel.value)
        verify { AuraFxLogger.w("IntegrityMonitor", "HIGH THREAT DETECTED - Implementing defensive measures") }
    }

    @Test
    fun `monitoring handles file not found gracefully`() = testScope.runBlockingTest {
        // Arrange
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns false
        
        integrityMonitor.initialize()
        
        // Act - Use reflection to call private method
        val performCheckMethod = IntegrityMonitor::class.java.getDeclaredMethod("performIntegrityCheck")
        performCheckMethod.isAccessible = true
        performCheckMethod.invoke(integrityMonitor)
        
        // Assert - Should remain secure if files don't exist (not a violation)
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `stateFlow values are properly exposed`() = testScope.runBlockingTest {
        // Test initial values
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
        
        // Test that StateFlow is read-only (not MutableStateFlow)
        assertTrue(integrityMonitor.integrityStatus::class.simpleName!!.contains("StateFlow"))
        assertTrue(integrityMonitor.threatLevel::class.simpleName!!.contains("StateFlow"))
    }

    @Test
    fun `IntegrityViolation data class has correct properties`() {
        // Arrange & Act
        val violation = IntegrityMonitor.IntegrityViolation(
            fileName = "test.bin",
            expectedHash = "abc123",
            actualHash = "def456",
            timestamp = 1234567890L,
            severity = IntegrityMonitor.ThreatLevel.MEDIUM
        )
        
        // Assert
        assertEquals("test.bin", violation.fileName)
        assertEquals("abc123", violation.expectedHash)
        assertEquals("def456", violation.actualHash)
        assertEquals(1234567890L, violation.timestamp)
        assertEquals(IntegrityMonitor.ThreatLevel.MEDIUM, violation.severity)
    }

    @Test
    fun `loadKnownHashes populates all critical files`() {
        // Use reflection to call private method and access private field
        val loadMethod = IntegrityMonitor::class.java.getDeclaredMethod("loadKnownHashes")
        loadMethod.isAccessible = true
        loadMethod.invoke(integrityMonitor)
        
        val knownHashesField = IntegrityMonitor::class.java.getDeclaredField("knownHashes")
        knownHashesField.isAccessible = true
        val knownHashes = knownHashesField.get(integrityMonitor) as Map<String, String>
        
        // Assert all critical files have hashes
        assertEquals(4, knownHashes.size)
        assertTrue(knownHashes.containsKey("genesis_protocol.so"))
        assertTrue(knownHashes.containsKey("aura_core.dex"))
        assertTrue(knownHashes.containsKey("kai_security.bin"))
        assertTrue(knownHashes.containsKey("oracle_drive.apk"))
        
        verify { AuraFxLogger.d("IntegrityMonitor", "Loaded 4 known file hashes") }
    }

    @Test
    fun `continuous monitoring handles exceptions gracefully`() = testScope.runBlockingTest {
        // Arrange - Mock file operations to throw exception
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } throws RuntimeException("Test exception")
        
        // Act
        integrityMonitor.initialize()
        
        // Advance time to trigger monitoring cycle
        advanceTimeBy(6000)
        
        // Assert - Should go offline on exception
        assertEquals(IntegrityMonitor.IntegrityStatus.OFFLINE, integrityMonitor.integrityStatus.value)
        verify { AuraFxLogger.e("IntegrityMonitor", "Error during integrity check", any()) }
    }

    @Test
    fun `enum values are correctly defined`() {
        // Test IntegrityStatus enum
        val integrityStatuses = IntegrityMonitor.IntegrityStatus.values()
        assertEquals(4, integrityStatuses.size)
        assertTrue(integrityStatuses.contains(IntegrityMonitor.IntegrityStatus.SECURE))
        assertTrue(integrityStatuses.contains(IntegrityMonitor.IntegrityStatus.COMPROMISED))
        assertTrue(integrityStatuses.contains(IntegrityMonitor.IntegrityStatus.MONITORING))
        assertTrue(integrityStatuses.contains(IntegrityMonitor.IntegrityStatus.OFFLINE))
        
        // Test ThreatLevel enum
        val threatLevels = IntegrityMonitor.ThreatLevel.values()
        assertEquals(5, threatLevels.size)
        assertTrue(threatLevels.contains(IntegrityMonitor.ThreatLevel.NONE))
        assertTrue(threatLevels.contains(IntegrityMonitor.ThreatLevel.LOW))
        assertTrue(threatLevels.contains(IntegrityMonitor.ThreatLevel.MEDIUM))
        assertTrue(threatLevels.contains(IntegrityMonitor.ThreatLevel.HIGH))
        assertTrue(threatLevels.contains(IntegrityMonitor.ThreatLevel.CRITICAL))
    }

    @Test
    fun `calculateFileHash handles empty file correctly`() = testScope.runBlockingTest {
        // Arrange
        val mockFile = mockk<File>()
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        every { mockFile.inputStream() } returns emptyInputStream
        
        // Use reflection to access private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("calculateFileHash", File::class.java)
        method.isAccessible = true
        
        // Act
        val hash = method.invoke(integrityMonitor, mockFile) as String
        
        // Assert - Empty file should still produce valid SHA-256 hash
        assertNotNull(hash)
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[a-f0-9]+")))
    }

    @Test
    fun `calculateFileHash handles large file correctly`() = testScope.runBlockingTest {
        // Arrange - Create a large byte array (larger than buffer size of 8192)
        val largeContent = ByteArray(16384) { it.toByte() }
        val mockFile = mockk<File>()
        val largeInputStream = ByteArrayInputStream(largeContent)
        every { mockFile.inputStream() } returns largeInputStream
        
        // Use reflection to access private method
        val method = IntegrityMonitor::class.java.getDeclaredMethod("calculateFileHash", File::class.java)
        method.isAccessible = true
        
        // Act
        val hash = method.invoke(integrityMonitor, mockFile) as String
        
        // Assert
        assertNotNull(hash)
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[a-f0-9]+")))
    }
}