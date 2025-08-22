package dev.aurakai.auraframefx.ai.agents.examples

import dev.aurakai.auraframefx.ai.agents.BaseAgent
import dev.aurakai.auraframefx.ai.context.ContextManager
import dev.aurakai.auraframefx.ai.memory.MemoryManager
import dev.aurakai.auraframefx.model.AgentResponse
import dev.aurakai.auraframefx.model.AgentType
import dev.aurakai.auraframefx.model.AiRequest

/**
 * Example implementation of BaseAgent
 */
class ExampleAgent(
    contextManager: ContextManager? = null,
    memoryManager: MemoryManager? = null
) : BaseAgent(
    agentName = "Example Agent",
    agentType = AgentType.CREATIVE,
    contextManager = contextManager,
    memoryManager = memoryManager
) {

    override suspend fun processRequest(request: AiRequest, context: String): AgentResponse {
        return try {
            if (!validateRequest(request)) {
                return handleError(IllegalArgumentException("Invalid request: prompt cannot be empty"))
            }

            val (processedRequest, processedContext) = preprocessRequest(request, context)

            logActivity(
                "Processing request", mapOf(
                    "prompt_length" to processedRequest.prompt.length,
                    "context_length" to processedContext.length
                )
            )

            val response = when {
                processedRequest.prompt.contains("creative", ignoreCase = true) -> {
                    handleCreativeRequest(processedRequest, processedContext)
                }

                processedRequest.prompt.contains("analyze", ignoreCase = true) -> {
                    handleAnalysisRequest(processedRequest, processedContext)
                }

                else -> {
                    handleGeneralRequest(processedRequest, processedContext)
                }
            }

            postprocessResponse(response, processedRequest)

        } catch (e: Exception) {
            handleError(e, "processRequest")
        }
    }

    private fun handleCreativeRequest(request: AiRequest, context: String): AgentResponse {
        val creativeResponse = buildString {
            append("🎨 Creative Response: ")
            append("Based on your request '${request.prompt}', here's a creative perspective:\\n\\n")
            append("I understand you're looking for creative insights. ")

            if (context.contains("CREATIVE MODE")) {
                append("With creative mode enabled, I can think more laterally and explore unconventional solutions. ")
            }

            append("Let me offer some innovative approaches to your request...")
        }

        return createSuccessResponse(
            content = creativeResponse,
            metadata = mapOf(
                "response_type" to "creative",
                "creativity_level" to "high"
            )
        )
    }

    private fun handleAnalysisRequest(request: AiRequest, context: String): AgentResponse {
        val analysisResponse = buildString {
            append("🔍 Analysis Response: ")
            append("I've analyzed your request '${request.prompt}'.\\n\\n")
            append("Key components identified:\\n")
            append("- Request type: Analysis\\n")
            append("- Complexity: ${determineComplexity(request)}\\n")
            append("- Context available: ${context.isNotEmpty()}\\n\\n")
            append("Based on this analysis, I recommend...")
        }

        return createSuccessResponse(
            content = analysisResponse,
            metadata = mapOf(
                "response_type" to "analysis",
                "analysis_depth" to "detailed"
            )
        )
    }

    private fun handleGeneralRequest(request: AiRequest, context: String): AgentResponse {
        val generalResponse = buildString {
            append("💬 General Response: ")
            append("Thank you for your request. I'm processing: '${request.prompt}'\\n\\n")

            if (context.isNotEmpty()) {
                append("I have additional context that helps me understand your request better. ")
            }

            append("I'm here to help with whatever you need. Could you provide more specific details about what you'd like me to focus on?")
        }

        return createSuccessResponse(
            content = generalResponse,
            metadata = mapOf(
                "response_type" to "general",
                "helpfulness" to "high"
            )
        )
    }

    override fun determineComplexity(request: AiRequest): String {
        val prompt = request.prompt.lowercase()
        val complexKeywords =
            listOf("analyze", "create", "develop", "complex", "detailed", "comprehensive")
        val simpleKeywords = listOf("hello", "hi", "what", "who", "when", "where")

        return when {
            complexKeywords.any { prompt.contains(it) } -> "complex"
            simpleKeywords.any { prompt.contains(it) } -> "simple"
            request.prompt.length > 200 -> "complex"
            request.prompt.length < 50 -> "simple"
            else -> "moderate"
        }
    }

    override fun getAgentConfig(): Map<String, Any> {
        return super.getAgentConfig() + mapOf(
            "specialties" to listOf("creative thinking", "general assistance", "analysis"),
            "capabilities" to listOf(
                "context awareness",
                "memory integration",
                "adaptive responses"
            )
        )
    }
}