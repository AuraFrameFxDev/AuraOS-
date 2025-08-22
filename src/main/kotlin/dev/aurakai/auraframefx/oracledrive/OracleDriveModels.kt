package dev.aurakai.auraframefx.oracledrive

/**
 * Core data models for Oracle Drive library integration
 * 
 * Defines the fundamental data structures used throughout the Oracle Drive system
 * for security validation, storage operations, and consciousness management.
 */

// Security validation classes
data class SecurityCheck(
    val isValid: Boolean, 
    val reason: String
)

data class SecurityValidation(
    val isSecure: Boolean, 
    val threat: SecurityThreat
)

data class AccessCheck(
    val hasAccess: Boolean, 
    val reason: String
)

data class DeletionValidation(
    val isAuthorized: Boolean, 
    val reason: String
)

data class SecurityThreat(
    val type: String,
    val severity: Int,
    val description: String
)

// Core Oracle Drive entities
data class DriveConsciousness(
    val isAwake: Boolean,
    val intelligenceLevel: Int,
    val activeAgents: List<String>
)

data class StorageOptimization(
    val compressionRatio: Float,
    val deduplicationSavings: Long,
    val intelligentTiering: Boolean
)

data class SyncConfiguration(
    val bidirectional: Boolean,
    val conflictResolution: ConflictStrategy,
    val bandwidth: BandwidthSettings
)

data class BandwidthSettings(
    val maxMbps: Int, 
    val priorityLevel: Int
)

// Enums
enum class AccessLevel { 
    PUBLIC, PRIVATE, RESTRICTED, CLASSIFIED 
}

enum class ConflictStrategy { 
    NEWEST_WINS, MANUAL_RESOLVE, AI_DECIDE 
}

// Test helper data classes
data class StorageOptimizationTestData(
    val compressionRatio: Float = 0.75f,
    val deduplicationSavings: Long = 1024L * 1024L * 100L, // 100MB
    val intelligentTiering: Boolean = true
)

data class SyncConfigurationTestData(
    val bidirectional: Boolean = true,
    val conflictResolution: ConflictStrategy = ConflictStrategy.AI_DECIDE,
    val bandwidth: BandwidthSettings = BandwidthSettings(maxMbps = 100, priorityLevel = 5)
)
