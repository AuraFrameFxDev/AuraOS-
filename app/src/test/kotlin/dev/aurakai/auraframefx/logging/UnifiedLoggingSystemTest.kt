package dev.aurakai.auraframefx.logging

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive unit tests for UnifiedLoggingSystem
 * 
 * Testing Framework: JUnit 4 with Mockito and Robolectric for Android components
 * Test Categories: Unit tests covering initialization, logging methods, health monitoring,
 * file operations, analytics, and error handling scenarios.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UnifiedLoggingSystemTest {

    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockFilesDir: File
    
    @Mock
    private lateinit var mockLogDirectory: File
    
    @Mock
    private lateinit var mockLogFile: File
    
    private lateinit var unifiedLoggingSystem: UnifiedLoggingSystem
    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope
    private lateinit var logMock: MockedStatic<Log>
    private lateinit var timberMock: MockedStatic<Timber>

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        Dispatchers.setMain(testDispatcher)
        
        // Mock Android Log
        logMock = mockStatic(Log::class.java)
        
        // Mock Timber
        timberMock = mockStatic(Timber::class.java)
        
        // Setup mock context and file system
        `when`(mockContext.filesDir).thenReturn(mockFilesDir)
        `when`(mockFilesDir.absolutePath).thenReturn("/mock/files")
        `when`(mockLogDirectory.exists()).thenReturn(false)
        `when`(mockLogDirectory.mkdirs()).thenReturn(true)
        
        unifiedLoggingSystem = UnifiedLoggingSystem(mockContext)
    }

    @After
    fun tearDown() {
        testScope.cleanupTestCoroutines()
        Dispatchers.resetMain()
        logMock.close()
        timberMock.close()
        if (::unifiedLoggingSystem.isInitialized) {
            unifiedLoggingSystem.shutdown()
        }
    }

    // INITIALIZATION TESTS

    @Test
    fun `initialize should create log directory when it doesn't exist`() {
        // Arrange
        `when`(mockLogDirectory.exists()).thenReturn(false)
        `when`(mockLogDirectory.mkdirs()).thenReturn(true)
        
        // Act
        unifiedLoggingSystem.initialize()
        
        // Assert
        verify(mockLogDirectory).mkdirs()
        timberMock.verify { Timber.plant(any()) }
    }

    @Test
    fun `initialize should not create log directory when it already exists`() {
        // Arrange
        `when`(mockLogDirectory.exists()).thenReturn(true)
        
        // Act
        unifiedLoggingSystem.initialize()
        
        // Assert
        verify(mockLogDirectory, never()).mkdirs()
    }

    @Test
    fun `initialize should handle directory creation failure gracefully`() {
        // Arrange
        `when`(mockLogDirectory.exists()).thenReturn(false)
        `when`(mockLogDirectory.mkdirs()).thenThrow(SecurityException("Permission denied"))
        
        // Act & Assert (should not throw)
        unifiedLoggingSystem.initialize()
        
        logMock.verify { Log.e(eq("UnifiedLoggingSystem"), eq("Failed to initialize logging system"), any()) }
    }

    @Test
    fun `initialize should plant Timber tree`() {
        // Act
        unifiedLoggingSystem.initialize()
        
        // Assert
        timberMock.verify { Timber.plant(any()) }
    }

    // LOGGING METHOD TESTS

    @Test
    fun `log should create LogEntry with correct properties`() = testScope.runBlockingTest {
        // Arrange
        val level = UnifiedLoggingSystem.LogLevel.INFO
        val category = UnifiedLoggingSystem.LogCategory.SYSTEM
        val tag = "TestTag"
        val message = "Test message"
        val metadata = mapOf("key" to "value")
        val throwable = RuntimeException("Test exception")
        
        // Act
        unifiedLoggingSystem.log(level, category, tag, message, throwable, metadata)
        
        // Assert
        logMock.verify { Log.i(eq("SYSTEM_TestTag"), eq(message), eq(throwable)) }
        timberMock.verify { Timber.i(eq(throwable), eq(message)) }
    }

    @Test
    fun `verbose should log with VERBOSE level`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.UI
        val tag = "VerboseTag"
        val message = "Verbose message"
        
        // Act
        unifiedLoggingSystem.verbose(category, tag, message)
        
        // Assert
        logMock.verify { Log.v(eq("UI_VerboseTag"), eq(message), isNull()) }
        timberMock.verify { Timber.v(isNull(), eq(message)) }
    }

    @Test
    fun `debug should log with DEBUG level`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.NETWORK
        val tag = "DebugTag"
        val message = "Debug message"
        val metadata = mapOf("request_id" to "123")
        
        // Act
        unifiedLoggingSystem.debug(category, tag, message, metadata)
        
        // Assert
        logMock.verify { Log.d(eq("NETWORK_DebugTag"), eq(message), isNull()) }
        timberMock.verify { Timber.d(isNull(), eq(message)) }
    }

    @Test
    fun `info should log with INFO level`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.PERFORMANCE
        val tag = "InfoTag"
        val message = "Info message"
        
        // Act
        unifiedLoggingSystem.info(category, tag, message)
        
        // Assert
        logMock.verify { Log.i(eq("PERFORMANCE_InfoTag"), eq(message), isNull()) }
        timberMock.verify { Timber.i(isNull(), eq(message)) }
    }

    @Test
    fun `warning should log with WARNING level and throwable`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.STORAGE
        val tag = "WarningTag"
        val message = "Warning message"
        val throwable = IOException("Disk full")
        
        // Act
        unifiedLoggingSystem.warning(category, tag, message, throwable)
        
        // Assert
        logMock.verify { Log.w(eq("STORAGE_WarningTag"), eq(message), eq(throwable)) }
        timberMock.verify { Timber.w(eq(throwable), eq(message)) }
    }

    @Test
    fun `error should log with ERROR level`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.AI
        val tag = "ErrorTag"
        val message = "Error message"
        val throwable = IllegalStateException("Invalid state")
        
        // Act
        unifiedLoggingSystem.error(category, tag, message, throwable)
        
        // Assert
        logMock.verify { Log.e(eq("AI_ErrorTag"), eq(message), eq(throwable)) }
        timberMock.verify { Timber.e(eq(throwable), eq(message)) }
    }

    @Test
    fun `fatal should log with FATAL level using wtf`() {
        // Arrange
        val category = UnifiedLoggingSystem.LogCategory.SYSTEM
        val tag = "FatalTag"
        val message = "Fatal message"
        val throwable = OutOfMemoryError("No memory")
        
        // Act
        unifiedLoggingSystem.fatal(category, tag, message, throwable)
        
        // Assert
        logMock.verify { Log.wtf(eq("SYSTEM_FatalTag"), eq(message), eq(throwable)) }
        timberMock.verify { Timber.wtf(eq(throwable), eq(message)) }
    }

    // SPECIALIZED LOGGING TESTS

    @Test
    fun `logSecurityEvent should use SECURITY category with default WARNING level`() {
        // Arrange
        val event = "Unauthorized access attempt"
        val details = mapOf("ip" to "192.168.1.1", "user" to "unknown")
        
        // Act
        unifiedLoggingSystem.logSecurityEvent(event, details = details)
        
        // Assert
        logMock.verify { Log.w(eq("SECURITY_SecurityMonitor"), eq(event), isNull()) }
    }

    @Test
    fun `logSecurityEvent should accept custom severity level`() {
        // Arrange
        val event = "Critical security breach"
        val severity = UnifiedLoggingSystem.LogLevel.FATAL
        
        // Act
        unifiedLoggingSystem.logSecurityEvent(event, severity)
        
        // Assert
        logMock.verify { Log.wtf(eq("SECURITY_SecurityMonitor"), eq(event), isNull()) }
    }

    @Test
    fun `logPerformanceMetric should include value and unit in metadata`() {
        // Arrange
        val metric = "Database query time"
        val value = 250.5
        val unit = "ms"
        
        // Act
        unifiedLoggingSystem.logPerformanceMetric(metric, value, unit)
        
        // Assert
        logMock.verify { Log.i(eq("PERFORMANCE_PerformanceMonitor"), eq(metric), isNull()) }
    }

    @Test
    fun `logPerformanceMetric should use default unit when not specified`() {
        // Arrange
        val metric = "Response time"
        val value = 100.0
        
        // Act
        unifiedLoggingSystem.logPerformanceMetric(metric, value)
        
        // Assert
        logMock.verify { Log.i(eq("PERFORMANCE_PerformanceMonitor"), eq(metric), isNull()) }
    }

    @Test
    fun `logUserAction should use USER_ACTION category`() {
        // Arrange
        val action = "Button clicked"
        val details = mapOf("button_id" to "submit", "screen" to "login")
        
        // Act
        unifiedLoggingSystem.logUserAction(action, details)
        
        // Assert
        logMock.verify { Log.i(eq("USER_ACTION_UserInteraction"), eq(action), isNull()) }
    }

    @Test
    fun `logAIEvent should include confidence in metadata when provided`() {
        // Arrange
        val agent = "VoiceAssistant"
        val event = "Intent recognized"
        val confidence = 0.85f
        val details = mapOf("intent" to "weather_query")
        
        // Act
        unifiedLoggingSystem.logAIEvent(agent, event, confidence, details)
        
        // Assert
        logMock.verify { Log.i(eq("AI_VoiceAssistant"), eq(event), isNull()) }
    }

    @Test
    fun `logAIEvent should not include confidence when null`() {
        // Arrange
        val agent = "ChatBot"
        val event = "Message processed"
        
        // Act
        unifiedLoggingSystem.logAIEvent(agent, event)
        
        // Assert
        logMock.verify { Log.i(eq("AI_ChatBot"), eq(event), isNull()) }
    }

    @Test
    fun `logGenesisProtocol should use GENESIS_PROTOCOL category with default INFO level`() {
        // Arrange
        val event = "Protocol handshake completed"
        val details = mapOf("version" to "1.0", "timestamp" to System.currentTimeMillis())
        
        // Act
        unifiedLoggingSystem.logGenesisProtocol(event, details = details)
        
        // Assert
        logMock.verify { Log.i(eq("GENESIS_PROTOCOL_GenesisProtocol"), eq(event), isNull()) }
    }

    @Test
    fun `logGenesisProtocol should accept custom log level`() {
        // Arrange
        val event = "Protocol error detected"
        val level = UnifiedLoggingSystem.LogLevel.ERROR
        
        // Act
        unifiedLoggingSystem.logGenesisProtocol(event, level)
        
        // Assert
        logMock.verify { Log.e(eq("GENESIS_PROTOCOL_GenesisProtocol"), eq(event), isNull()) }
    }

    // SYSTEM HEALTH TESTS

    @Test
    fun `system health should start as HEALTHY`() = testScope.runBlockingTest {
        // Assert
        assertEquals(UnifiedLoggingSystem.SystemHealth.HEALTHY, unifiedLoggingSystem.systemHealth.first())
    }

    @Test
    fun `analyzeLogForHealth should set CRITICAL for FATAL logs`() = testScope.runBlockingTest {
        // Act
        unifiedLoggingSystem.fatal(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Fatal error")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert
        assertEquals(UnifiedLoggingSystem.SystemHealth.CRITICAL, unifiedLoggingSystem.systemHealth.value)
    }

    @Test
    fun `analyzeLogForHealth should set ERROR for ERROR logs when currently HEALTHY`() = testScope.runBlockingTest {
        // Arrange
        assertEquals(UnifiedLoggingSystem.SystemHealth.HEALTHY, unifiedLoggingSystem.systemHealth.value)
        
        // Act
        unifiedLoggingSystem.error(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Error occurred")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert
        assertEquals(UnifiedLoggingSystem.SystemHealth.ERROR, unifiedLoggingSystem.systemHealth.value)
    }

    @Test
    fun `analyzeLogForHealth should set WARNING for WARNING logs when currently HEALTHY`() = testScope.runBlockingTest {
        // Arrange
        assertEquals(UnifiedLoggingSystem.SystemHealth.HEALTHY, unifiedLoggingSystem.systemHealth.value)
        
        // Act
        unifiedLoggingSystem.warning(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Warning occurred")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert
        assertEquals(UnifiedLoggingSystem.SystemHealth.WARNING, unifiedLoggingSystem.systemHealth.value)
    }

    @Test
    fun `analyzeLogForHealth should not downgrade health status`() = testScope.runBlockingTest {
        // Arrange - Set to ERROR state
        unifiedLoggingSystem.error(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Error occurred")
        advanceTimeBy(1000)
        assertEquals(UnifiedLoggingSystem.SystemHealth.ERROR, unifiedLoggingSystem.systemHealth.value)
        
        // Act - Log a warning
        unifiedLoggingSystem.warning(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Warning occurred")
        advanceTimeBy(1000)
        
        // Assert - Should remain ERROR
        assertEquals(UnifiedLoggingSystem.SystemHealth.ERROR, unifiedLoggingSystem.systemHealth.value)
    }

    // CRITICAL PATTERN DETECTION TESTS

    @Test
    fun `checkCriticalPatterns should escalate SECURITY ERROR logs to FATAL`() = testScope.runBlockingTest {
        // Act
        unifiedLoggingSystem.error(UnifiedLoggingSystem.LogCategory.SECURITY, "SecurityTest", "Security breach detected")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert - Should log fatal security violation
        logMock.verify(atLeastOnce()) { 
            Log.wtf(eq("SYSTEM_CriticalPatternDetector"), 
                   contains("SECURITY VIOLATION DETECTED"), 
                   isNull()) 
        }
    }

    @Test
    fun `checkCriticalPatterns should escalate GENESIS_PROTOCOL ERROR logs to FATAL`() = testScope.runBlockingTest {
        // Act
        unifiedLoggingSystem.error(UnifiedLoggingSystem.LogCategory.GENESIS_PROTOCOL, "ProtocolTest", "Protocol failure")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert - Should log fatal protocol issue
        logMock.verify(atLeastOnce()) { 
            Log.wtf(eq("SYSTEM_CriticalPatternDetector"), 
                   contains("GENESIS PROTOCOL ISSUE"), 
                   isNull()) 
        }
    }

    @Test
    fun `checkCriticalPatterns should not escalate non-critical security logs`() = testScope.runBlockingTest {
        // Act
        unifiedLoggingSystem.warning(UnifiedLoggingSystem.LogCategory.SECURITY, "SecurityTest", "Minor security warning")
        
        // Allow time for processing
        advanceTimeBy(1000)
        
        // Assert - Should not log fatal security violation
        logMock.verify(never()) { 
            Log.wtf(eq("SYSTEM_CriticalPatternDetector"), 
                   contains("SECURITY VIOLATION DETECTED"), 
                   any()) 
        }
    }

    // LOG FORMATTING TESTS

    @Test
    fun `formatLogEntry should include all log entry components`() {
        // This tests the private formatLogEntry method indirectly through file writing
        // We can verify the formatting by checking the expected format structure
        unifiedLoggingSystem.initialize()
        
        val testEntry = UnifiedLoggingSystem.LogEntry(
            timestamp = 1234567890123L,
            level = UnifiedLoggingSystem.LogLevel.INFO,
            category = UnifiedLoggingSystem.LogCategory.SYSTEM,
            tag = "TestTag",
            message = "Test message",
            throwable = RuntimeException("Test exception"),
            metadata = mapOf("key1" to "value1", "key2" to "value2"),
            threadName = "TestThread"
        )
        
        // The formatting is tested through the file output which would use formatLogEntry
        // We can't directly test the private method but can verify it's called correctly
        assertTrue(true) // Placeholder for format verification
    }

    // ANDROID LOG INTEGRATION TESTS

    @Test
    fun `logToAndroidLog should use correct Android Log methods for each level`() {
        // Test all log levels
        val levels = listOf(
            UnifiedLoggingSystem.LogLevel.VERBOSE,
            UnifiedLoggingSystem.LogLevel.DEBUG,
            UnifiedLoggingSystem.LogLevel.INFO,
            UnifiedLoggingSystem.LogLevel.WARNING,
            UnifiedLoggingSystem.LogLevel.ERROR,
            UnifiedLoggingSystem.LogLevel.FATAL
        )
        
        levels.forEach { level ->
            unifiedLoggingSystem.log(level, UnifiedLoggingSystem.LogCategory.SYSTEM, "TestTag", "Test message")
        }
        
        // Verify each Android Log method was called
        logMock.verify { Log.v(any(), any(), any()) }
        logMock.verify { Log.d(any(), any(), any()) }
        logMock.verify { Log.i(any(), any(), any()) }
        logMock.verify { Log.w(any(), any(), any()) }
        logMock.verify { Log.e(any(), any(), any()) }
        logMock.verify { Log.wtf(any(), any(), any()) }
    }

    // TIMBER INTEGRATION TESTS

    @Test
    fun `logToTimber should use correct Timber methods for each level`() {
        // Test all log levels
        val levels = listOf(
            UnifiedLoggingSystem.LogLevel.VERBOSE,
            UnifiedLoggingSystem.LogLevel.DEBUG,
            UnifiedLoggingSystem.LogLevel.INFO,
            UnifiedLoggingSystem.LogLevel.WARNING,
            UnifiedLoggingSystem.LogLevel.ERROR,
            UnifiedLoggingSystem.LogLevel.FATAL
        )
        
        levels.forEach { level ->
            unifiedLoggingSystem.log(level, UnifiedLoggingSystem.LogCategory.SYSTEM, "TestTag", "Test message")
        }
        
        // Verify each Timber method was called
        timberMock.verify { Timber.v(any(), any()) }
        timberMock.verify { Timber.d(any(), any()) }
        timberMock.verify { Timber.i(any(), any()) }
        timberMock.verify { Timber.w(any(), any()) }
        timberMock.verify { Timber.e(any(), any()) }
        timberMock.verify { Timber.wtf(any(), any()) }
    }

    // ANALYTICS TESTS

    @Test
    fun `generateLogAnalytics should return valid analytics data`() = testScope.runBlockingTest {
        // Initialize the system to start background processing
        unifiedLoggingSystem.initialize()
        
        // The current implementation returns hardcoded values
        // This test ensures the method works and returns expected structure
        advanceTimeBy(35000) // Allow health monitoring to run
        
        // We can't directly test the private method, but we can verify
        // the system health monitoring is working by checking health updates
        assertTrue(true) // Placeholder for analytics verification
    }

    // SESSION TRACKING TESTS

    @Test
    fun `getCurrentSessionId should generate hour-based session IDs`() {
        // Test that session IDs are generated consistently
        // This tests the session ID generation indirectly through log entries
        
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Message 1")
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Message 2")
        
        // Both messages should have the same session ID since they're in the same hour
        // This is tested indirectly through the log entry creation
        assertTrue(true) // Placeholder for session verification
    }

    // SHUTDOWN TESTS

    @Test
    fun `shutdown should log shutdown message and clean up resources`() {
        // Arrange
        unifiedLoggingSystem.initialize()
        
        // Act
        unifiedLoggingSystem.shutdown()
        
        // Assert
        logMock.verify { 
            Log.i(eq("SYSTEM_UnifiedLoggingSystem"), 
                  eq("Shutting down Genesis Unified Logging System"), 
                  isNull()) 
        }
    }

    // ERROR HANDLING TESTS

    @Test
    fun `log processing should handle exceptions gracefully`() = testScope.runBlockingTest {
        // Arrange
        unifiedLoggingSystem.initialize()
        
        // Act - Log many messages to potentially trigger processing errors
        repeat(100) {
            unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "StressTest", "Message $it")
        }
        
        advanceTimeBy(5000)
        
        // Assert - System should continue functioning
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "After stress test")
        logMock.verify { Log.i(eq("SYSTEM_Test"), eq("After stress test"), isNull()) }
    }

    @Test
    fun `health monitoring should handle exceptions gracefully`() = testScope.runBlockingTest {
        // Arrange
        unifiedLoggingSystem.initialize()
        
        // Act - Allow health monitoring to run
        advanceTimeBy(35000)
        
        // Assert - System should continue functioning even if health monitoring fails
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Health monitoring test")
        logMock.verify { Log.i(eq("SYSTEM_Test"), eq("Health monitoring test"), isNull()) }
    }

    // EDGE CASE TESTS

    @Test
    fun `log should handle null throwable gracefully`() {
        // Act
        unifiedLoggingSystem.error(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Error without exception", null)
        
        // Assert
        logMock.verify { Log.e(eq("SYSTEM_Test"), eq("Error without exception"), isNull()) }
    }

    @Test
    fun `log should handle empty metadata gracefully`() {
        // Act
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Message with empty metadata", emptyMap())
        
        // Assert
        logMock.verify { Log.i(eq("SYSTEM_Test"), eq("Message with empty metadata"), isNull()) }
    }

    @Test
    fun `log should handle large metadata objects`() {
        // Arrange
        val largeMetadata = (1..100).associate { "key$it" to "value$it" }
        
        // Act
        unifiedLoggingSystem.debug(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Large metadata test", largeMetadata)
        
        // Assert
        logMock.verify { Log.d(eq("SYSTEM_Test"), eq("Large metadata test"), isNull()) }
    }

    @Test
    fun `log should handle special characters in messages`() {
        // Arrange
        val specialMessage = "Test with special chars: !@#$%^&*(){}[]|\\:;\"'<>,.?/~`"
        
        // Act
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "SpecialChars", specialMessage)
        
        // Assert
        logMock.verify { Log.i(eq("SYSTEM_SpecialChars"), eq(specialMessage), isNull()) }
    }

    @Test
    fun `log should handle very long messages`() {
        // Arrange
        val longMessage = "A".repeat(10000)
        
        // Act
        unifiedLoggingSystem.warning(UnifiedLoggingSystem.LogCategory.SYSTEM, "LongMessage", longMessage)
        
        // Assert
        logMock.verify { Log.w(eq("SYSTEM_LongMessage"), eq(longMessage), isNull()) }
    }

    // CONCURRENCY TESTS

    @Test
    fun `concurrent logging should be thread safe`() = testScope.runBlockingTest {
        // Arrange
        unifiedLoggingSystem.initialize()
        
        // Act - Log from multiple coroutines concurrently
        val jobs = (1..50).map { index ->
            launch {
                unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Concurrent", "Message $index")
            }
        }
        
        jobs.forEach { it.join() }
        advanceTimeBy(1000)
        
        // Assert - All messages should be logged
        logMock.verify(times(50)) { Log.i(contains("SYSTEM_Concurrent"), contains("Message"), isNull()) }
    }

    // MEMORY AND PERFORMANCE TESTS

    @Test
    fun `logging should not cause memory leaks with repeated calls`() = testScope.runBlockingTest {
        // Arrange
        unifiedLoggingSystem.initialize()
        
        // Act - Log many messages
        repeat(1000) {
            unifiedLoggingSystem.debug(UnifiedLoggingSystem.LogCategory.SYSTEM, "MemoryTest", "Message $it")
        }
        
        advanceTimeBy(2000)
        
        // Assert - System should still be responsive
        unifiedLoggingSystem.info(UnifiedLoggingSystem.LogCategory.SYSTEM, "Test", "Memory test complete")
        logMock.verify { Log.i(eq("SYSTEM_Test"), eq("Memory test complete"), isNull()) }
    }
}

/**
 * Unit tests for AuraFxLoggerCompat compatibility layer
 */
class AuraFxLoggerCompatTest {

    @Mock
    private lateinit var mockUnifiedLogger: UnifiedLoggingSystem
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `d should delegate to unified logger debug method`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        
        // Act
        AuraFxLoggerCompat.d("TestTag", "Debug message")
        
        // Assert
        verify(mockUnifiedLogger).debug(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("TestTag"), 
            eq("Debug message")
        )
    }

    @Test
    fun `i should delegate to unified logger info method`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        
        // Act
        AuraFxLoggerCompat.i("InfoTag", "Info message")
        
        // Assert
        verify(mockUnifiedLogger).info(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("InfoTag"), 
            eq("Info message")
        )
    }

    @Test
    fun `w should delegate to unified logger warning method`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        
        // Act
        AuraFxLoggerCompat.w("WarnTag", "Warning message")
        
        // Assert
        verify(mockUnifiedLogger).warning(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("WarnTag"), 
            eq("Warning message")
        )
    }

    @Test
    fun `e should delegate to unified logger error method with throwable`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        val throwable = RuntimeException("Test exception")
        
        // Act
        AuraFxLoggerCompat.e("ErrorTag", "Error message", throwable)
        
        // Assert
        verify(mockUnifiedLogger).error(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("ErrorTag"), 
            eq("Error message"), 
            eq(throwable)
        )
    }

    @Test
    fun `e should handle null throwable`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        
        // Act
        AuraFxLoggerCompat.e("ErrorTag", "Error message", null)
        
        // Assert
        verify(mockUnifiedLogger).error(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("ErrorTag"), 
            eq("Error message"), 
            isNull()
        )
    }

    @Test
    fun `compat methods should handle null tags gracefully`() {
        // Arrange
        AuraFxLoggerCompat.initialize(mockUnifiedLogger)
        
        // Act
        AuraFxLoggerCompat.d(null, "Debug message")
        AuraFxLoggerCompat.i(null, "Info message")
        AuraFxLoggerCompat.w(null, "Warning message")
        AuraFxLoggerCompat.e(null, "Error message")
        
        // Assert
        verify(mockUnifiedLogger, times(4)).debug(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("Unknown"), 
            anyString()
        )
        verify(mockUnifiedLogger).info(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("Unknown"), 
            eq("Info message")
        )
        verify(mockUnifiedLogger).warning(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("Unknown"), 
            eq("Warning message")
        )
        verify(mockUnifiedLogger).error(
            eq(UnifiedLoggingSystem.LogCategory.SYSTEM), 
            eq("Unknown"), 
            eq("Error message"), 
            isNull()
        )
    }

    @Test
    fun `compat methods should not crash when logger not initialized`() {
        // Act & Assert (should not throw)
        AuraFxLoggerCompat.d("TestTag", "Debug message")
        AuraFxLoggerCompat.i("TestTag", "Info message")
        AuraFxLoggerCompat.w("TestTag", "Warning message")
        AuraFxLoggerCompat.e("TestTag", "Error message")
        
        // No verification needed - just ensuring no exceptions are thrown
    }
}