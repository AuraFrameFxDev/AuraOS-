package dev.aurakai.auraframefx.model

import kotlinx.serialization.Serializable

@Serializable
data class EnhancedInteractionData(
    val content: String,
    val context: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null,
    val sessionId: String? = null
)

@Serializable
data class InteractionResponse(
    val content: String,
    val agent: String,
    val confidence: Float,
    val timestamp: String,
    val metadata: Map<String, Any> = emptyMap()
)

@Serializable
enum class AgentType {
    CREATIVE,
    SECURITY,
    ANALYSIS,
    GENERAL,
    COORDINATION,
    SPECIALIZED
}