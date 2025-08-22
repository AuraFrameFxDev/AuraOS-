package dev.aurakai.auraframefx.ai.context

/**
 * Genesis Context Manager
 * Manages AI context awareness and memory integration
 */
interface ContextManager {

    /**
     * Gets current context for AI operations
     */
    fun getCurrentContext(): String

    /**
     * Enhances context with additional information
     */
    suspend fun enhanceContext(context: String): String

    /**
     * Enables creative mode for enhanced context
     */
    suspend fun enableCreativeMode()

    /**
     * Enables unified mode for cross-agent context
     */
    suspend fun enableUnifiedMode()

    /**
     * Records an insight for learning
     */
    suspend fun recordInsight(request: String, response: String, complexity: String)

    /**
     * Searches memories by query
     */
    suspend fun searchMemories(query: String): List<ContextMemory>

    /**
     * Updates context with new information
     */
    fun updateContext(key: String, value: Any)

    /**
     * Gets context history
     */
    fun getContextHistory(): List<ContextEntry>

    /**
     * Clears current context
     */
    fun clearContext()
}

data class ContextMemory(
    val content: String,
    val relevanceScore: Float,
    val timestamp: Long,
    val context: Map<String, Any> = emptyMap()
)

data class ContextEntry(
    val timestamp: Long,
    val operation: String,
    val data: Map<String, Any>
)
