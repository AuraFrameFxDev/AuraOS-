package dev.aurakai.auraframefx.model

import kotlinx.serialization.Serializable

@Serializable
data class AgentRequest(
    val type: String,
    val query: String? = null,
    val context: Map<String, Any>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AiRequest(
    val prompt: String,
    val type: String = "general",
    val query: String? = prompt,
    val context: Map<String, Any>? = null,
    val timestamp: Long = System.currentTimeMillis()
)