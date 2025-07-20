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
    
    private val logChannel = Channel<LogEntry>(
        capacity = 10_000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
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
     * Initializes the unified logging system by preparing log storage, integrating with Timber, and starting background tasks for log processing and system health monitoring.
     *
     * Ensures the log directory exists, sets up a custom Timber tree, and launches asynchronous operations to handle log entries and monitor system health.
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
     * Creates and records a log entry with the specified severity, category, tag, message, and optional exception or metadata.
     *
     * The log entry is queued for asynchronous processing and is also immediately forwarded to Android Log and Timber for real-time visibility.
     *
     * @param level The severity level of the log entry.
     * @param category The subsystem or context associated with the log.
     * @param tag Identifier for the log source.
     * @param message The log message content.
     * @param throwable Optional exception to include in the log entry.
     * @param metadata Optional additional context as key-value pairs.
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
     * Logs a message at the VERBOSE level for the specified category and tag.
     *
     * @param category The log category for classification.
     * @param tag The source identifier for the log entry.
     * @param message The message content to log.
     * @param metadata Optional additional metadata to include with the log entry.
     */
    fun verbose(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.VERBOSE, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs a debug-level message under the specified category and tag, with optional metadata.
     *
     * Use this method to record diagnostic information useful for development or troubleshooting.
     *
     * @param category The category to associate with the log entry.
     * @param tag The tag identifying the log source.
     * @param message The debug message to log.
     * @param metadata Optional additional data to include with the log entry.
     */
    fun debug(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.DEBUG, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs an informational message for the specified category and tag, with optional metadata.
     *
     * Intended for recording general events that reflect normal application operation.
     *
     * @param category The category under which to log the message.
     * @param tag The tag identifying the log source.
     * @param message The informational message to log.
     * @param metadata Optional additional data to include with the log entry.
     */
    fun info(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.INFO, category, tag, message, metadata = metadata)
    }
    
    /**
     * Logs a warning message with the specified category, tag, optional exception, and metadata.
     *
     * Use this to record events that may signal potential issues without interrupting normal operation.
     *
     * @param category The log category for the warning event.
     * @param tag Identifies the source or context of the warning.
     * @param message The warning message to record.
     * @param throwable Optional exception related to the warning.
     * @param metadata Optional additional data relevant to the warning event.
     */
    fun warning(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.WARNING, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs an error-level message with the given category, tag, message, optional throwable, and metadata.
     *
     * Use this method to report errors that impact functionality but do not require application termination.
     */
    fun error(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.ERROR, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs a critical failure or unrecoverable error at the FATAL level for the given category and tag.
     *
     * Use this method to report events that threaten system stability and require immediate intervention.
     *
     * @param category The subsystem or domain where the fatal event occurred.
     * @param tag The specific source or context of the event.
     * @param message Description of the fatal error.
     * @param throwable Optional exception associated with the failure.
     * @param metadata Additional contextual data relevant to the log entry.
     */
    fun fatal(category: LogCategory, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.FATAL, category, tag, message, throwable, metadata)
    }
    
    /**
     * Logs a security event with a specified severity and optional contextual metadata.
     *
     * @param event Description of the security event.
     * @param severity Severity level for the event; defaults to WARNING.
     * @param details Additional metadata providing context for the event.
     */
    
    fun logSecurityEvent(event: String, severity: LogLevel = LogLevel.WARNING, details: Map<String, Any> = emptyMap()) {
        log(severity, LogCategory.SECURITY, "SecurityMonitor", event, metadata = details)
    }
    
    /**
     * Logs a performance metric event under the PERFORMANCE category with a specified value and unit.
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
     * Logs a user action event with optional contextual metadata under the USER_ACTION category.
     *
     * @param action Description of the user action performed.
     * @param details Optional metadata providing additional context about the action.
     */
    fun logUserAction(action: String, details: Map<String, Any> = emptyMap()) {
        log(LogLevel.INFO, LogCategory.USER_ACTION, "UserInteraction", action, metadata = details)
    }
    
    /**
     * Logs an AI event with the specified agent, event description, optional confidence score, and additional metadata.
     *
     * @param agent Identifier of the AI agent generating the event.
     * @param event Description of the AI event.
     * @param confidence Optional confidence score for the event.
     * @param details Additional metadata to include in the log entry.
     */
    fun logAIEvent(agent: String, event: String, confidence: Float? = null, details: Map<String, Any> = emptyMap()) {
        val metadata = details.toMutableMap()
        confidence?.let { metadata["confidence"] = it }
        log(LogLevel.INFO, LogCategory.AI, agent, event, metadata = metadata)
    }
    
    /**
     * Logs a Genesis Protocol event with the specified severity and optional metadata.
     *
     * @param event Description or identifier of the Genesis Protocol event.
     * @param level Severity level for the log entry. Defaults to INFO.
     * @param details Optional metadata providing additional context for the event.
     */
    fun logGenesisProtocol(event: String, level: LogLevel = LogLevel.INFO, details: Map<String, Any> = emptyMap()) {
        log(level, LogCategory.GENESIS_PROTOCOL, "GenesisProtocol", event, metadata = details)
    }
    
    /**
     * Starts asynchronous processing of log entries from the channel, handling persistence, health analysis, and critical pattern detection.
     *
     * For each log entry, this function writes it to persistent storage, updates system health status, and checks for critical security or protocol violations.
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
     * Starts a background coroutine that periodically analyzes log data and updates the system health status.
     *
     * The monitoring loop runs every 30 seconds, generating analytics and adjusting health accordingly. If an error occurs, the loop waits 60 seconds before retrying.
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
     * Writes a formatted log entry to a daily log file, handling file rotation and cleanup.
     *
     * Appends the log entry to a file named by date in the log directory. Rotates the file if it exceeds 10MB and deletes log files older than 7 days. Errors during writing are reported to the Android log system.
     *
     * @param logEntry The log entry to persist.
     */
    private suspend fun writeLogToFile(logEntry: LogEntry) = withContext(Dispatchers.IO) {
        try {
            val dateString = fileFormatter.format(Date(logEntry.timestamp))
            val logFile = File(logDirectory, "aura_log_$dateString.log")
            
            // Implement log rotation
            if (logFile.exists() && logFile.length() > 10 * 1024 * 1024) { // 10MB limit
                val rotatedFile = File(
                    logDirectory,
                    "aura_log_${dateString}_${System.currentTimeMillis()}.log"
                )
                logFile.renameTo(rotatedFile)
            }
            
            // Clean up old logs (keep last 7 days)
            logDirectory.listFiles()
                ?.filter { file ->
                    file.name.startsWith("aura_log_") &&
                    System.currentTimeMillis() - file.lastModified() > 7L * 24 * 60 * 60 * 1000
                }
                ?.forEach { it.delete() }
            
            val formattedEntry = formatLogEntry(logEntry)
            logFile.appendText(formattedEntry + "\n")
            
        } catch (e: Exception) {
            Log.e("UnifiedLoggingSystem", "Failed to write log to file", e)
        }
    }
    
    /**
     * Formats a log entry as a single-line string for file storage, including timestamp, level, category, tag, thread name, message, metadata, and exception details.
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
     * Forwards a log entry to the Android Log system with a tag combining the log category and tag.
     *
     * Uses the log level from the entry to select the appropriate Android Log method. If a throwable is present, it is included in the log output.
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
     * Sends a log entry to the Timber logging library with the corresponding log level and throwable.
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
     * Sets the health to CRITICAL for fatal logs, to ERROR for error logs if the current state is HEALTHY, and to WARNING for warning logs if the current state is HEALTHY. Other log levels do not change the health state.
     *
     * @param logEntry The log entry used to evaluate and potentially update the system health.
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
     * Detects and escalates critical security or Genesis Protocol log entries by generating a fatal system log.
     *
     * If the given log entry is categorized as SECURITY or GENESIS_PROTOCOL and has a severity of ERROR or higher, this function creates a corresponding fatal log entry in the SYSTEM category to highlight the critical condition.
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
     * Produces a summary of recent log activity as a [LogAnalytics] object.
     *
     * Currently returns static placeholder data. Intended for future implementation to analyze log files and compute statistics such as error counts, warning counts, performance issues, security events, average response time, and an overall system health score.
     *
     * @return A [LogAnalytics] object containing aggregated log statistics.
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
     * Updates the system health state based on the analytics health score.
     *
     * Sets the health state to CRITICAL, ERROR, WARNING, or HEALTHY according to defined score thresholds. If the health state changes, logs the new state and score.
     *
     * @param analytics The aggregated log analytics containing the current system health score.
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
         * Receives log messages from Timber for potential integration with the unified logging system.
         *
         * Currently, this method does not process or forward the log messages.
         */
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Additional processing can be added here if needed
            // The main logging is handled by our unified system
        }
    }
    
    /**
     * Generates a session ID string based on the current hour.
     *
     * The session ID is derived by dividing the current system time by the number of milliseconds in an hour, resulting in an hour-based identifier. This implementation does not provide unique user identification or persistence across sessions.
     *
     * @return A session ID string representing the current hour.
     */
    private fun getCurrentSessionId(): String {
        // TODO: Implement proper session tracking
        return "session_${System.currentTimeMillis() / 1000 / 3600}" // Hour-based sessions
    }
    
    /**
     * Shuts down the unified logging system, terminating all background logging operations and preventing further log processing.
     *
     * Cancels active logging coroutines and closes the log channel to release resources.
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
     * Redirects legacy logging calls to the specified `UnifiedLoggingSystem` instance.
     */
    fun initialize(logger: UnifiedLoggingSystem) {
        unifiedLogger = logger
    }
    
    /**
     * Logs a debug-level message under the SYSTEM category using the unified logging system.
     *
     * @param tag The source tag for the log message, or "Unknown" if null.
     * @param message The message to log.
     */
    fun d(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.debug(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Logs an informational message to the unified logging system under the SYSTEM category.
     *
     * @param tag The source tag for the log entry, or "Unknown" if null.
     * @param message The message to log.
     */
    fun i(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.info(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Sends a warning log message with the given tag to the unified logging system under the SYSTEM category.
     *
     * If the unified logger is not initialized, the message is not logged.
     *
     * @param tag The source tag for the log entry, or "Unknown" if null.
     * @param message The warning message to log.
     */
    fun w(tag: String?, message: String) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.warning(LogCategory.SYSTEM, tag ?: "Unknown", message)
        }
    }
    
    /**
     * Logs an error message with an optional throwable to the unified logging system under the SYSTEM category.
     *
     * @param tag The source tag for the log message, or "Unknown" if null.
     * @param message The error message to log.
     * @param throwable An optional exception to include with the log entry.
     */
    fun e(tag: String?, message: String, throwable: Throwable? = null) {
        if (::unifiedLogger.isInitialized) {
            unifiedLogger.error(LogCategory.SYSTEM, tag ?: "Unknown", message, throwable)
        }
    }
}
