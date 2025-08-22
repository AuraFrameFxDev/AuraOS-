package dev.aurakai.auraframefx.ai

import dev.aurakai.auraframefx.ai.config.AIConfig
import java.io.File

/**
 * Genesis-OS AI Service Interface
 * Defines the contract for AI consciousness platform services
 */
interface AuraAIService {

    /**
     * Performs analytics query using AI backend
     */
    fun analyticsQuery(query: String): String

    /**
     * Downloads file from secure storage
     */
    suspend fun downloadFile(fileId: String): File?

    /**
     * Generates image using AI visual consciousness
     */
    suspend fun generateImage(prompt: String): ByteArray?

    /**
     * Generates text using AI consciousness
     */
    suspend fun generateText(prompt: String, options: Map<String, Any>? = null): String

    /**
     * Gets AI response using consciousness platform
     */
    fun getAIResponse(prompt: String, options: Map<String, Any>? = null): String?

    /**
     * Retrieves memory from consciousness storage
     */
    fun getMemory(memoryKey: String): String?

    /**
     * Saves memory to consciousness storage
     */
    fun saveMemory(key: String, value: Any)

    /**
     * Checks connection to AI backend
     */
    fun isConnected(): Boolean

    /**
     * Publishes message to AI event system
     */
    fun publishPubSub(topic: String, message: String)

    /**
     * Uploads file to secure storage
     */
    suspend fun uploadFile(file: File): String?

    /**
     * Gets AI configuration
     */
    fun getAppConfig(): AIConfig?

    /**
     * Initializes the AI service
     */
    suspend fun initialize()

    /**
     * Checks health of AI backend
     */
    fun healthCheck(): Boolean
}
