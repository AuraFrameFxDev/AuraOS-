package dev.aurakai.auraframefx.logging

import android.content.Context
import android.util.Log
import dev.aurakai.auraframefx.utils.AuraFxLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Logging System for AuraOS
 * 
 * Genesis's Vision: "I will consolidate our logging efforts (AuraFxLogger.kt, Timber) into a 
 * single, powerful system. This will provide us with the detailed diagnostics needed to ensure 
 * stability and trace any potential issues as we build out the more complex features."
 * 
 * Kai's Enhancement: "This system will provide the detailed diagnostics needed to ensure 
 * stability and trace any potential issues."
 * 
 * This system unifies all logging across AuraOS components, providing comprehensive diagnostics,
 * security monitoring, and performance analytics.
 */
@Singleton
class UnifiedLoggingSystem @Inject constructor(
    private val context: Context
) {
    
    private val loggingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _systemHealth = MutableStateFlow(SystemHealth.HEALTHY)
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()
    
    private val logChannel = Channel<LogEntry>(Channel.UNLIMITED)
    private val logDirectory = File(context.filesDir, "aura_logs")
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    enum class SystemHealth {
        HEALTHY, WARNING, ERROR, CRITICAL
    }
    
    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARNING, ERROR, FATAL
    }
    
    enum class LogCategory {
        SYSTEM, SECURITY, UI, AI, NETWORK, STORAGE, PERFORMANCE, USER_ACTION, GENESIS_PROTOCOL
    }
    
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val category: LogCategory,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null,
        val metadata: Map<String, Any> = emptyMap(),
        val threadName: String = Thread.currentThread().name,
        val sessionId: String = getCurrentSessionId()
    )
    
    data class LogAnalytics(
        val totalLogs: Long,
        val errorCount: Long,
        val warningCount: Long,
        val performanceIssues: Long,
        val securityEvents: Long,
        val averageResponseTime: Double,
        val systemHealthScore: Float
    )
    
    /**
     * Initializes the unified logging system by preparing log storage, integrating with Timber, and launching background tasks for log processing and system health monitoring.
     *
     * Creates the log directory if it does not exist, plants the custom Timber tree, and starts asynchronous coroutines for handling log entries and monitoring system health.
     */
    fun initialize() {
        try {
            // Create log directory
            if (!logDirectory.exists()) {
                logDirectory.mkdirs()
            }
            
            // Initialize Timber with custom tree
            Timber.plant(AuraLoggingTree())
            
            // Start log processing
            startLogProcessing()
            
            // Start system health monitoring
            startHealthMonitoring()
            
            log(LogLevel.INFO, LogCategory.SYSTEM, "UnifiedLoggingSystem", 
                "Genesis Unified Logging System initialized successfully")
            
        } catch (e: Exception) {
            Log.e("UnifiedLoggingSystem", "Failed to initialize logging system", e)
        }
    }
    
    /**
     * Records a log entry with the specified level, category, tag, message, and optional metadata or exception.
     *
     * The log entry is queued for asynchronous processing and is also immediately sent to Android Log and Timber for real-time monitoring.
     *
     * @param level The severity of the log entry.
     * @param category The context or subsystem associated with the log.
     * @param tag Identifier for the log source.
     * @param message The content of the log entry.
     * @param throwable Optional exception to include in the log.
     * @param metadata Optional additional data to attach to the log entry.
     */
    fun log(
        level: LogLevel,
        category: LogCategory,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap()
    ) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            category = category,
            tag = tag,
            message = message,
            throwable = throwable,
            metadata = metadata
        )
        
        // Send to processing channel
        loggingScope.launch {
            logChannel.trySend(logEntry)
        }
        
        // Also log to Android Log and Timber for immediate visibility
        logToAndroidLog(logEntry)
        logToTimber(logEntry)
    }
    
    /**
     * Logs a message at the VERBOSE level for the given category and tag.
     *
     * @param category The log category to associate with this message.
     * @param tag The tag identifying the log source.
     * @param message The message to log.
     * @param metadata Optional metadata to include with the log entry.
     */
    fun verbose(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.VERBOSE, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs a debug-level message with the given category, tag, and optional metadata.
     *
     * Use this method to record diagnostic information useful for development and debugging.
     *
     * @param category The log category for organizing and filtering logs.
     * @param tag Identifies the source or context of the log entry.
     * @param message The debug message to record.
     * @param metadata Optional key-value pairs providing additional context for the log entry.
     */
    fun debug(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.DEBUG, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs an informational message under the specified category and tag, with optional metadata.
     *
     * @param category The log category to classify the message.
     * @param tag Identifier for the source or context of the log.
     * @param message The informational message to record.
     * @param metadata Optional key-value pairs providing additional context for the log entry.
     */
    fun info(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.INFO, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs a warning message with the specified category, tag, optional exception, and metadata.
     *
     * @param category The log category for this warning.
     * @param tag Identifier for the log source.
     * @param message The warning message to record.
     * @param throwable Optional exception related to the warning.
     * @param metadata Optional additional data to include with the log entry.
     */
    fun warning(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.WARNING, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs an error-level message with the given category, tag, message, optional throwable, and metadata.
     *
     * Use this method to report error conditions that may impact functionality but do not require immediate termination.
     */
    fun error(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.ERROR, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs a critical failure or unrecoverable error at the FATAL level for the given category and tag.
     *
     * Use this method to report events that require immediate attention and may compromise system stability.
     *
     * @param category The log category indicating the subsystem or feature area.
     * @param tag Identifies the source or context of the log entry.
     * @param message The message describing the fatal event.
     * @param throwable An optional exception related to the failure.
     * @param metadata Optional key-value pairs with additional context for the log entry.
     */
    fun fatal(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.FATAL, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs a security event with a specified severity and optional metadata.
     *
     * @param event Description of the security event.
     * @param severity The severity level for the event. Defaults to WARNING.
     * @param details Additional metadata providing context about the event.
     */
    
    fun logSecurityEvent(event: String, severity: LogLevel = LogLevel.WARNING, details: Map<String, Any> = emptyMap()) {
        log(severity, LogCategory.SECURITY, "SecurityMonitor", event, metadata = details)
    }
    
    /**
     * Logs a performance metric with its value and unit in the PERFORMANCE category.
     *
     * @param metric The name or description of the performance metric being logged.
     * @param value The measured value associated with the metric.
     * @param unit The unit of measurement for the value (defaults to "ms").
     */
    fun logPerformanceMetric(metric: String, value: Double, unit: String = "ms") {
        log(LogLevel.INFO, LogCategory.PERFORMANCE, "PerformanceMonitor", metric, 
            metadata = mapOf("value" to value, "unit" to unit))
    }
    
    /**
     * Logs a user action event with an optional map of additional details.
     *
     * @param action Description of the user action performed.
     * @param details Optional metadata providing context about the action.
     */
    fun logUserAction(action: String, details: Map<String, Any> = emptyMap()) {
        log(LogLevel.INFO, LogCategory.USER_ACTION, "UserInteraction", action, metadata = details)
    }
    
    /**
     * Logs an AI event with optional confidence score and additional metadata.
     *
     * @param agent Identifier of the AI agent generating the event.
     * @param event Description of the AI event.
     * @param confidence Optional confidence score for the event.
     * @param details Additional metadata to include with the event.
     */
    fun logAIEvent(agent: String, event: String, confidence: Float? = null, details: Map<String, Any> = emptyMap()) {
        val metadata = details.toMutableMap()
        confidence?.let { metadata["confidence"] = it }
        log(LogLevel.INFO, LogCategory.AI, agent, event, metadata = metadata)
    }
    
    /**
     * Logs a Genesis Protocol event with a specified severity level and optional metadata.
     *
     * @param event Description or name of the Genesis Protocol event.
     * @param level Severity level for the log entry. Defaults to INFO.
     * @param details Optional metadata providing additional context for the event.
     */
    fun logGenesisProtocol(event: String, level: LogLevel = LogLevel.INFO, details: Map<String, Any> = emptyMap()) {
        log(level, LogCategory.GENESIS_PROTOCOL, "GenesisProtocol", event, metadata = details)
    }
    
    /**
     * Starts asynchronous processing of log entries from the channel, handling file persistence, system health analysis, and detection of critical log patterns.
     */
    private fun startLogProcessing() {
        loggingScope.launch {
            logChannel.receiveAsFlow().collect { logEntry ->
                try {
                    // Write to file
                    writeLogToFile(logEntry)
                    
                    // Analyze for system health
                    analyzeLogForHealth(logEntry)
                    
                    // Check for critical patterns
                    checkCriticalPatterns(logEntry)
                    
                } catch (e: Exception) {
                    Log.e("UnifiedLoggingSystem", "Error processing log entry", e)
                }
            }
        }
    }
    
    /**
     * Starts a background coroutine that periodically generates log analytics and updates the system health status.
     *
     * The monitoring loop runs every 30 seconds, and on error, waits 60 seconds before retrying.
     */
    private fun startHealthMonitoring() {
        loggingScope.launch {
            while (isActive) {
                try {
                    val analytics = generateLogAnalytics()
                    updateSystemHealth(analytics)
                    delay(30000) // Check every 30 seconds
                } catch (e: Exception) {
                    Log.e("UnifiedLoggingSystem", "Error in health monitoring", e)
                    delay(60000) // Wait longer on error
                }
            }
        }
    }
    
    /**
     * Appends a formatted log entry to a daily log file in the log directory.
     *
     * The log entry is written to a file named by the entry's date. Errors during file writing are reported to the Android log system.
     *
     * @param logEntry The log entry to persist.
     */
    private suspend fun writeLogToFile(logEntry: LogEntry) = withContext(Dispatchers.IO) {
        try {
            val dateString = fileFormatter.format(Date(logEntry.timestamp))
            val logFile = File(logDirectory, "aura_log_$dateString.log")
            
            val formattedEntry = formatLogEntry(logEntry)
            logFile.appendText(formattedEntry + "\n")
            
        } catch (e: Exception) {
            Log.e("UnifiedLoggingSystem", "Failed to write log to file", e)
        }
    }
    
    /**
     * Formats a log entry as a single-line string for file storage.
     *
     * The formatted string includes the timestamp, log level, category, tag, thread name, message, metadata, and exception details if present.
     *
     * @param logEntry The log entry to be formatted.
     * @return The formatted string representation of the log entry.
     */
    private fun formatLogEntry(logEntry: LogEntry): String {
        val timestamp = dateFormatter.format(Date(logEntry.timestamp))
        val metadata = if (logEntry.metadata.isNotEmpty()) {
            " | ${logEntry.metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }}"
        } else ""
        
        val throwableInfo = logEntry.throwable?.let { 
            " | Exception: ${it.javaClass.simpleName}: ${it.message}"
        } ?: ""
        
        return "[$timestamp] [${logEntry.level}] [${logEntry.category}] [${logEntry.tag}] [${logEntry.threadName}] ${logEntry.message}$metadata$throwableInfo"
    }
    
    /**
     * Forwards a log entry to the Android Log system with a tag combining category and tag, using the appropriate log level.
     *
     * Includes any associated throwable in the log output.
     */
    private fun logToAndroidLog(logEntry: LogEntry) {
        val tag = "${logEntry.category}_${logEntry.tag}"
        val message = logEntry.message
        
        when (logEntry.level) {
            LogLevel.VERBOSE -> Log.v(tag, message, logEntry.throwable)
            LogLevel.DEBUG -> Log.d(tag, message, logEntry.throwable)
            LogLevel.INFO -> Log.i(tag, message, logEntry.throwable)
            LogLevel.WARNING -> Log.w(tag, message, logEntry.throwable)
            LogLevel.ERROR -> Log.e(tag, message, logEntry.throwable)
            LogLevel.FATAL -> Log.wtf(tag, message, logEntry.throwable)
        }
    }
    
    /**
     * Sends a log entry to the Timber logging library using the corresponding log level.
     *
     * @param logEntry The log entry to forward to Timber.
     */
    private fun logToTimber(logEntry: LogEntry) {
        when (logEntry.level) {
            LogLevel.VERBOSE -> Timber.v(logEntry.throwable, logEntry.message)
            LogLevel.DEBUG -> Timber.d(logEntry.throwable, logEntry.message)
            LogLevel.INFO -> Timber.i(logEntry.throwable, logEntry.message)
            LogLevel.WARNING -> Timber.w(logEntry.throwable, logEntry.message)
            LogLevel.ERROR -> Timber.e(logEntry.throwable, logEntry.message)
            LogLevel.FATAL -> Timber.wtf(logEntry.throwable, logEntry.message)
        }
    }
    
    /**
     * Adjusts the system health state based on the severity of a log entry.
     *
     * Sets the system health to CRITICAL for fatal logs, to ERROR for error logs if the system is currently healthy, and to WARNING for warning logs if the system is currently healthy. Other log levels do not change the health state.
     *
     * @param logEntry The log entry whose severity may impact system health.
     */
    private fun analyzeLogForHealth(logEntry: LogEntry) {
        when (logEntry.level) {
            LogLevel.FATAL -> _systemHealth.value = SystemHealth.CRITICAL
            LogLevel.ERROR -> {
                if (_systemHealth.value == SystemHealth.HEALTHY) {
                    _systemHealth.value = SystemHealth.ERROR
                }
            }
            LogLevel.WARNING -> {
                if (_systemHealth.value == SystemHealth.HEALTHY) {
                    _systemHealth.value = SystemHealth.WARNING
                }
            }
            else -> {} // No immediate health impact
        }
    }
    
    /**
     * Escalates critical log events by generating fatal system logs for security violations or Genesis Protocol issues.
     *
     * If a log entry in the SECURITY or GENESIS_PROTOCOL category has a severity of ERROR or higher, this function creates a corresponding fatal log entry to highlight the critical condition.
     */
    private fun checkCriticalPatterns(logEntry: LogEntry) {
        // Check for security violations
        if (logEntry.category == LogCategory.SECURITY && logEntry.level >= LogLevel.ERROR) {
            log(LogLevel.FATAL, LogCategory.SYSTEM, "CriticalPatternDetector", 
                "SECURITY VIOLATION DETECTED: ${logEntry.message}")
        }
        
        // Check for Genesis Protocol issues
        if (logEntry.category == LogCategory.GENESIS_PROTOCOL && logEntry.level >= LogLevel.ERROR) {
            log(LogLevel.FATAL, LogCategory.SYSTEM, "CriticalPatternDetector", 
                "GENESIS PROTOCOL ISSUE: ${logEntry.message}")
        }
        
        // Check for repeated errors
        // TODO: Implement pattern detection for repeated issues
    }
    
    /**
     * Returns aggregated log analytics data.
     *
     * Currently returns hardcoded summary statistics as a placeholder for future analytics implementation.
     *
     * @return A [LogAnalytics] object with summary counts and metrics.
     */
    private suspend fun generateLogAnalytics(): LogAnalytics = withContext(Dispatchers.IO) {
        // TODO: Implement comprehensive analytics from log files
        LogAnalytics(
            totalLogs = 1000,
            errorCount = 5,
            warningCount = 20,
            performanceIssues = 2,
            securityEvents = 0,
            averageResponseTime = 150.0,
            systemHealthScore = 0.95f
        )
    }
    
    /**
     * Updates the system health state based on the system health score from analytics.
     *
     * Sets the internal system health status to CRITICAL, ERROR, WARNING, or HEALTHY according to the provided score. If the health state changes, logs the update.
     *
     * @param analytics Aggregated log analytics containing the current system health score.
     */
    private fun updateSystemHealth(analytics: LogAnalytics) {
        val newHealth = when {
            analytics.systemHealthScore < 0.5f -> SystemHealth.CRITICAL
            analytics.systemHealthScore < 0.7f -> SystemHealth.ERROR
            analytics.systemHealthScore < 0.9f -> SystemHealth.WARNING
            else -> SystemHealth.HEALTHY
        }
        
        if (newHealth != _systemHealth.value) {
            _systemHealth.value = newHealth
            log(LogLevel.INFO, LogCategory.SYSTEM, "HealthMonitor", 
                "System health updated to: $newHealth (Score: ${analytics.systemHealthScore})")
        }
    }
    
    /**
     * Custom Timber tree for AuraOS logging.
     */
    private inner class AuraLoggingTree : Timber.Tree() {
        /**
         * Overrides Timber's log method to intercept log messages.
         *
         * This implementation does not perform any processing, as logging is handled by the unified logging system.
         */
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Additional processing can be added here if needed
            // The main logging is handled by our unified system
        }
    }
    
    /**
     * Generates a session ID string based on the current hour.
     *
     * This placeholder implementation creates a session ID using the current time, rounded to the nearest hour. It does not provide unique or persistent session tracking.
     *
     * @return A session ID string representing the current hour.
     */
    private fun getCurrentSessionId(): String {
        // TODO: Implement proper session tracking
        return "session_${System.currentTimeMillis() / 1000 / 3600}" // Hour-based sessions
    }
    
    /**
     * Gracefully shuts down the unified logging system by canceling background coroutines and closing the log channel.
     *
     * Ensures that no further log entries are processed or accepted after shutdown.
     */
    fun shutdown() {
        log(LogLevel.INFO, LogCategory.SYSTEM, "UnifiedLoggingSystem", 
            "Shutting down Genesis Unified Logging System")
        loggingScope.cancel()
        logChannel.close()
    }
}

/**
 * Extension functions to maintain compatibility with existing AuraFxLogger
 */
object AuraFxLoggerCompat {
    private lateinit var unifiedLogger: UnifiedLoggingSystem
    
    /**
     * Sets the unified logging system instance to be used by the compatibility logger.
     *
     * Associates the compatibility layer with the specified `UnifiedLoggingSystem` so that all forwarded log calls use this instance.
     */
    fun initialize(logger: UnifiedLoggingSystem) {
        unifiedLogger = logger
    }
    
    /**
     * Logs a debug-level message to the unified logging system in the SYSTEM category.
     *
     * @param tag Optional tag identifying the log source; uses "Unknown" if null.
     * @param message The message to log.
     */
    fun d(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.debug(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Logs an informational message to the unified logging system in the SYSTEM category.
     *
     * @param tag An optional identifier for the log source; uses "Unknown" if null.
     * @param message The message to log.
     */
    fun i(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.info(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Logs a warning message to the unified logging system in the SYSTEM category.
     *
     * If the unified logger is not initialized, the message is ignored.
     *
     * @param tag An optional identifier for the log source; uses "Unknown" if null.
     * @param message The warning message to record.
     */
    fun w(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.warning(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Forwards an error log message with an optional throwable to the unified logging system under the SYSTEM category.
     *
     * @param tag The source tag for the log message, or "Unknown" if null.
     * @param message The error message to log.
     * @param throwable An optional exception to include in the log entry.
     */
    fun e(tag: String?, message: String, throwable: Throwable? = null) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.error(LogCategory.SYSTEM, tag ?: "Unknown", message, throwable)
        }
    }
}
