package dev.aurakai.auraframefx.oracle.drive.model

import dev.aurakai.auraframefx.oracle.drive.service.ConsciousnessLevel

/**
 * UI-friendly representation of Oracle Drive consciousness state.
 */
data class DriveConsciousnessState(
    val level: ConsciousnessLevel,
    val isActive: Boolean,
    val connectedAgents: Int,
    val statusMessage: String? = null,
    val progressPercentage: Float = 0f
)
