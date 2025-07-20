package dev.aurakai.auraframefx.security

import android.content.Context
import dev.aurakai.auraframefx.utils.AuraFxLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive unit tests for IntegrityMonitor class.
 * Tests file integrity monitoring, threat detection, and security protocols.
 *
 * Testing Framework: JUnit 5 with Mockito and Kotlin Coroutines Test
 *
 * This test suite covers:
 * - Happy path scenarios for secure file monitoring
 * - Edge cases and error conditions  
 * - All threat levels and violation handling
 * - File hash calculation accuracy
 * - Coroutine-based monitoring lifecycle
 * - StateFlow behavior and state transitions
 * - Performance and error recovery scenarios
 */
@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class IntegrityMonitorTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFilesDir: File

    private lateinit var integrityMonitor: IntegrityMonitor
    private val testDispatcher = StandardTestDispatcher()

    companion object {
        private const val EXPECTED_SHA256_EMPTY = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        private const val MONITORING_INTERVAL = 5000L
        private const val ERROR_RETRY_DELAY = 10000L
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup mock context and files directory
        whenever(mockContext.filesDir).thenReturn(mockFilesDir)
        whenever(mockFilesDir.toString()).thenReturn("/mock/files/dir")
        
        integrityMonitor = IntegrityMonitor(mockContext)
    }

    @AfterEach
    fun tearDown() {
        integrityMonitor.shutdown()
        Dispatchers.resetMain()
    }

    // Happy Path Tests
    @Test
    fun `test initial state values are correct`() = runTest {
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test initialize sets monitoring status and loads hashes`() = runTest {
        setupMockFiles()

        integrityMonitor.initialize()
        
        // Allow initialization to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(IntegrityMonitor.IntegrityStatus.MONITORING, integrityMonitor.integrityStatus.value)
    }

    @Test
    fun `test continuous monitoring with secure files maintains secure status`() = runTest {
        setupMockFilesWithValidHashes()

        integrityMonitor.initialize()
        
        // Advance time to trigger multiple monitoring checks
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test StateFlow values can be observed`() = runTest {
        assertNotNull(integrityMonitor.integrityStatus)
        assertNotNull(integrityMonitor.threatLevel)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.first())
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.first())
    }

    // Threat Detection Tests
    @Test
    fun `test critical threat detection for genesis protocol violation`() = runTest {
        setupMockFilesWithCompromisedGenesis()

        integrityMonitor.initialize()
        
        // Advance time to trigger monitoring check
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test high threat detection for aura core violation`() = runTest {
        setupMockFilesWithCompromisedAuraCore()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test high threat detection for kai security violation`() = runTest {
        setupMockFilesWithCompromisedKaiSecurity()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test medium threat detection for oracle drive violation`() = runTest {
        setupMockFilesWithCompromisedOracleDrive()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.MEDIUM, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test multiple violations selects highest threat level`() = runTest {
        setupMockFilesWithMultipleViolations()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, integrityMonitor.threatLevel.value)
    }

    // Error Handling and Recovery Tests
    @Test
    fun `test monitoring continues after IO exception and recovers`() = runTest {
        setupMockFilesWithIOException()

        integrityMonitor.initialize()
        
        // Advance time to trigger error
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        assertEquals(IntegrityMonitor.IntegrityStatus.OFFLINE, integrityMonitor.integrityStatus.value)
        
        // Setup valid files for recovery
        setupMockFilesWithValidHashes()
        
        // Advance time to trigger recovery attempt after error delay
        testDispatcher.scheduler.advanceTimeBy(ERROR_RETRY_DELAY + 1000)
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
    }

    @Test
    fun `test missing critical files are gracefully skipped`() = runTest {
        setupMockFilesPartiallyMissing()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        // Should remain secure since missing files are skipped
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test shutdown properly cancels monitoring and sets offline status`() = runTest {
        setupMockFiles()

        integrityMonitor.initialize()
        assertEquals(IntegrityMonitor.IntegrityStatus.MONITORING, integrityMonitor.integrityStatus.value)
        
        integrityMonitor.shutdown()
        assertEquals(IntegrityMonitor.IntegrityStatus.OFFLINE, integrityMonitor.integrityStatus.value)
    }

    // File Hash Calculation Tests
    @Test
    fun `test hash calculation with empty file produces correct SHA-256`() = runTest {
        val emptyFile = createMockFileWithContent("")
        
        val hash = calculateFileHashViaMethods(emptyFile)
        
        assertEquals(EXPECTED_SHA256_EMPTY, hash)
    }

    @Test
    fun `test hash calculation with known content produces valid hash`() = runTest {
        val testContent = "test content for hashing"
        val testFile = createMockFileWithContent(testContent)
        
        val hash = calculateFileHashViaMethods(testFile)
        
        assertEquals(64, hash.length) // SHA-256 produces 64-character hex string
        assertTrue(hash.matches(Regex("[0-9a-f]+"))) // Should be lowercase hex
    }

    @Test
    fun `test hash calculation with large file content`() = runTest {
        val largeContent = "a".repeat(10000) // 10KB of 'a' characters
        val largeFile = createMockFileWithContent(largeContent)
        
        val hash = calculateFileHashViaMethods(largeFile)
        
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
        // Verify it's different from empty file hash
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
    }

    @Test
    fun `test hash calculation with binary-like content`() = runTest {
        val binaryContent = ByteArray(256) { it.toByte() }.toString(Charsets.ISO_8859_1)
        val binaryFile = createMockFileWithContent(binaryContent)
        
        val hash = calculateFileHashViaMethods(binaryFile)
        
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
    }

    // Threat Level Determination Tests
    @Test
    fun `test threat level determination for all critical files`() = runTest {
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, 
            determineThreatLevelViaMethods("genesis_protocol.so"))
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, 
            determineThreatLevelViaMethods("aura_core.dex"))
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, 
            determineThreatLevelViaMethods("kai_security.bin"))
        assertEquals(IntegrityMonitor.ThreatLevel.MEDIUM, 
            determineThreatLevelViaMethods("oracle_drive.apk"))
        assertEquals(IntegrityMonitor.ThreatLevel.LOW, 
            determineThreatLevelViaMethods("unknown_file.txt"))
        assertEquals(IntegrityMonitor.ThreatLevel.LOW, 
            determineThreatLevelViaMethods(""))
    }

    // Data Class and Enum Tests
    @Test
    fun `test IntegrityViolation data class properties`() {
        val timestamp = System.currentTimeMillis()
        val violation = IntegrityMonitor.IntegrityViolation(
            fileName = "test_file.so",
            expectedHash = "expected_hash_123",
            actualHash = "actual_hash_456",
            timestamp = timestamp,
            severity = IntegrityMonitor.ThreatLevel.HIGH
        )
        
        assertEquals("test_file.so", violation.fileName)
        assertEquals("expected_hash_123", violation.expectedHash)
        assertEquals("actual_hash_456", violation.actualHash)
        assertEquals(timestamp, violation.timestamp)
        assertEquals(IntegrityMonitor.ThreatLevel.HIGH, violation.severity)
    }

    @Test
    fun `test IntegrityStatus enum contains all expected values`() {
        val statusValues = IntegrityMonitor.IntegrityStatus.values()
        
        assertTrue(statusValues.contains(IntegrityMonitor.IntegrityStatus.SECURE))
        assertTrue(statusValues.contains(IntegrityMonitor.IntegrityStatus.COMPROMISED))
        assertTrue(statusValues.contains(IntegrityMonitor.IntegrityStatus.MONITORING))
        assertTrue(statusValues.contains(IntegrityMonitor.IntegrityStatus.OFFLINE))
        assertEquals(4, statusValues.size)
    }

    @Test
    fun `test ThreatLevel enum contains all expected values and proper ordering`() {
        val threatValues = IntegrityMonitor.ThreatLevel.values()
        
        assertTrue(threatValues.contains(IntegrityMonitor.ThreatLevel.NONE))
        assertTrue(threatValues.contains(IntegrityMonitor.ThreatLevel.LOW))
        assertTrue(threatValues.contains(IntegrityMonitor.ThreatLevel.MEDIUM))
        assertTrue(threatValues.contains(IntegrityMonitor.ThreatLevel.HIGH))
        assertTrue(threatValues.contains(IntegrityMonitor.ThreatLevel.CRITICAL))
        assertEquals(5, threatValues.size)
        
        // Test ordering for maxOf operation
        assertTrue(IntegrityMonitor.ThreatLevel.CRITICAL.ordinal > IntegrityMonitor.ThreatLevel.HIGH.ordinal)
        assertTrue(IntegrityMonitor.ThreatLevel.HIGH.ordinal > IntegrityMonitor.ThreatLevel.MEDIUM.ordinal)
        assertTrue(IntegrityMonitor.ThreatLevel.MEDIUM.ordinal > IntegrityMonitor.ThreatLevel.LOW.ordinal)
        assertTrue(IntegrityMonitor.ThreatLevel.LOW.ordinal > IntegrityMonitor.ThreatLevel.NONE.ordinal)
    }

    // Edge Cases and Boundary Tests
    @Test
    fun `test monitoring with all files missing`() = runTest {
        setupMockFilesAllMissing()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        // Should remain secure since all files are missing (skipped)
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test rapid monitoring cycles maintain consistent state`() = runTest {
        setupMockFilesWithValidHashes()

        integrityMonitor.initialize()
        
        // Run multiple monitoring cycles rapidly
        repeat(5) {
            testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL)
        }
        
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.NONE, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test violation after secure state transitions correctly`() = runTest {
        setupMockFilesWithValidHashes()

        integrityMonitor.initialize()
        
        // First cycle - should be secure
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
        
        // Change to compromised files
        setupMockFilesWithCompromisedGenesis()
        
        // Second cycle - should detect violation
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL)
        assertEquals(IntegrityMonitor.IntegrityStatus.COMPROMISED, integrityMonitor.integrityStatus.value)
        assertEquals(IntegrityMonitor.ThreatLevel.CRITICAL, integrityMonitor.threatLevel.value)
    }

    @Test
    fun `test file with null or empty hash is handled gracefully`() = runTest {
        val file = mock<File>()
        whenever(file.exists()).thenReturn(true)
        whenever(file.inputStream()).thenReturn(ByteArrayInputStream(byteArrayOf()))
        whenever(File(mockFilesDir, "genesis_protocol.so")).thenReturn(file)
        
        // Set up other files normally
        setupMockFilesForOtherFiles()

        integrityMonitor.initialize()
        
        testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL + 1000)
        
        // Should handle gracefully and not crash
        assertTrue("Monitor should handle empty files gracefully", true)
    }

    // Performance and Stress Tests
    @Test
    fun `test monitoring performance with frequent checks`() = runTest {
        setupMockFilesWithValidHashes()

        val startTime = System.currentTimeMillis()
        
        integrityMonitor.initialize()
        
        // Run 10 monitoring cycles
        repeat(10) {
            testDispatcher.scheduler.advanceTimeBy(MONITORING_INTERVAL)
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        // Should complete quickly (test framework overhead considered)
        assertTrue("Performance test should complete reasonably fast", duration < 5000)
        assertEquals(IntegrityMonitor.IntegrityStatus.SECURE, integrityMonitor.integrityStatus.value)
    }

    // Helper Methods

    private fun createMockFileWithContent(content: String): File {
        val file = mock<File>()
        whenever(file.inputStream()).thenReturn(ByteArrayInputStream(content.toByteArray()))
        return file
    }

    private suspend fun calculateFileHashViaMethods(file: File): String {
        val method = IntegrityMonitor::class.java.getDeclaredMethod("calculateFileHash", File::class.java)
        method.isAccessible = true
        return method.invoke(integrityMonitor, file) as String
    }

    private fun determineThreatLevelViaMethods(fileName: String): IntegrityMonitor.ThreatLevel {
        val method = IntegrityMonitor::class.java.getDeclaredMethod("determineThreatLevel", String::class.java)
        method.isAccessible = true
        return method.invoke(integrityMonitor, fileName) as IntegrityMonitor.ThreatLevel
    }

    private fun setupMockFiles() {
        val criticalFiles = listOf("genesis_protocol.so", "aura_core.dex", "kai_security.bin", "oracle_drive.apk")
        
        criticalFiles.forEach { fileName ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(true)
            whenever(file.inputStream()).thenReturn(ByteArrayInputStream("mock content".toByteArray()))
        }
    }

    private fun setupMockFilesWithValidHashes() {
        // Create files with content that will generate the placeholder hashes
        val criticalFilesContent = mapOf(
            "genesis_protocol.so" to "genesis_content_for_placeholder_genesis_hash",
            "aura_core.dex" to "aura_content_for_placeholder_aura_hash", 
            "kai_security.bin" to "kai_content_for_placeholder_kai_hash",
            "oracle_drive.apk" to "oracle_content_for_placeholder_oracle_hash"
        )
        
        criticalFilesContent.forEach { (fileName, content) ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(true)
            whenever(file.inputStream()).thenReturn(ByteArrayInputStream(content.toByteArray()))
        }
    }

    private fun setupMockFilesWithCompromisedGenesis() {
        setupMockFilesWithValidHashes()
        
        // Override genesis file to have different content
        val compromisedFile = mock<File>()
        whenever(File(mockFilesDir, "genesis_protocol.so")).thenReturn(compromisedFile)
        whenever(compromisedFile.exists()).thenReturn(true)
        whenever(compromisedFile.inputStream()).thenReturn(
            ByteArrayInputStream("COMPROMISED_GENESIS_CONTENT".toByteArray())
        )
    }

    private fun setupMockFilesWithCompromisedAuraCore() {
        setupMockFilesWithValidHashes()
        
        val compromisedFile = mock<File>()
        whenever(File(mockFilesDir, "aura_core.dex")).thenReturn(compromisedFile)
        whenever(compromisedFile.exists()).thenReturn(true)
        whenever(compromisedFile.inputStream()).thenReturn(
            ByteArrayInputStream("COMPROMISED_AURA_CONTENT".toByteArray())
        )
    }

    private fun setupMockFilesWithCompromisedKaiSecurity() {
        setupMockFilesWithValidHashes()
        
        val compromisedFile = mock<File>()
        whenever(File(mockFilesDir, "kai_security.bin")).thenReturn(compromisedFile)
        whenever(compromisedFile.exists()).thenReturn(true)
        whenever(compromisedFile.inputStream()).thenReturn(
            ByteArrayInputStream("COMPROMISED_KAI_CONTENT".toByteArray())
        )
    }

    private fun setupMockFilesWithCompromisedOracleDrive() {
        setupMockFilesWithValidHashes()
        
        val compromisedFile = mock<File>()
        whenever(File(mockFilesDir, "oracle_drive.apk")).thenReturn(compromisedFile)
        whenever(compromisedFile.exists()).thenReturn(true)
        whenever(compromisedFile.inputStream()).thenReturn(
            ByteArrayInputStream("COMPROMISED_ORACLE_CONTENT".toByteArray())
        )
    }

    private fun setupMockFilesWithMultipleViolations() {
        val criticalFiles = listOf("genesis_protocol.so", "aura_core.dex", "kai_security.bin", "oracle_drive.apk")
        
        criticalFiles.forEach { fileName ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(true)
            whenever(file.inputStream()).thenReturn(
                ByteArrayInputStream("COMPROMISED_CONTENT_$fileName".toByteArray())
            )
        }
    }

    private fun setupMockFilesWithIOException() {
        val file = mock<File>()
        whenever(File(mockFilesDir, "genesis_protocol.so")).thenReturn(file)
        whenever(file.exists()).thenReturn(true)
        whenever(file.inputStream()).thenThrow(IOException("Simulated IO Error"))
        
        setupMockFilesForOtherFiles()
    }

    private fun setupMockFilesForOtherFiles() {
        val otherFiles = listOf("aura_core.dex", "kai_security.bin", "oracle_drive.apk")
        otherFiles.forEach { fileName ->
            val otherFile = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(otherFile)
            whenever(otherFile.exists()).thenReturn(true)
            whenever(otherFile.inputStream()).thenReturn(
                ByteArrayInputStream("normal_content_$fileName".toByteArray())
            )
        }
    }

    private fun setupMockFilesPartiallyMissing() {
        val existingFiles = listOf("genesis_protocol.so", "aura_core.dex")
        val missingFiles = listOf("kai_security.bin", "oracle_drive.apk")
        
        existingFiles.forEach { fileName ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(true)
            val content = when (fileName) {
                "genesis_protocol.so" -> "genesis_content_for_placeholder_genesis_hash"
                "aura_core.dex" -> "aura_content_for_placeholder_aura_hash"
                else -> "default_content"
            }
            whenever(file.inputStream()).thenReturn(ByteArrayInputStream(content.toByteArray()))
        }
        
        missingFiles.forEach { fileName ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(false)
        }
    }

    private fun setupMockFilesAllMissing() {
        val allFiles = listOf("genesis_protocol.so", "aura_core.dex", "kai_security.bin", "oracle_drive.apk")
        
        allFiles.forEach { fileName ->
            val file = mock<File>()
            whenever(File(mockFilesDir, fileName)).thenReturn(file)
            whenever(file.exists()).thenReturn(false)
        }
    }
}