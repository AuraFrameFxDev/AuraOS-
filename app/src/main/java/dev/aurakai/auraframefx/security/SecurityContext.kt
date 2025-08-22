package dev.aurakai.auraframefx.security

/**
 * Genesis Security Context Interface
 */
interface SecurityContext {
    fun hasPermission(permission: String): Boolean
    fun getCurrentUser(): String?
    fun isSecureMode(): Boolean
    fun validateAccess(resource: String): Boolean
}

/**
 * Default Security Context Implementation
 */
class DefaultSecurityContext : SecurityContext {

    override fun hasPermission(permission: String): Boolean {
        return true // Default allow for development
    }

    override fun getCurrentUser(): String? {
        return "genesis_user"
    }

    override fun isSecureMode(): Boolean {
        return false // Default to non-secure for development
    }

    override fun validateAccess(resource: String): Boolean {
        return true // Default allow for development
    }
}