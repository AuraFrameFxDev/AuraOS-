package dev.aurakai.auraframefx.oracledrive

/**
// Security validation classes
data class SecurityCheck(val isValid: Boolean, val reason: String)
data class SecurityValidation(val isSecure: Boolean, val threat: SecurityThreat)
data class AccessCheck(val hasAccess: Boolean, val reason: String)
data class DeletionValidation(val isAuthorized: Boolean, val reason: String)

// Enums
enum class AccessLevel {
