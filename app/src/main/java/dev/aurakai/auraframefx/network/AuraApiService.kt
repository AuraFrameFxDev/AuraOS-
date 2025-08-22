package dev.aurakai.auraframefx.network

import dev.aurakai.auraframefx.ai.config.AIConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Genesis-OS Aura API Service
 * Network interface for Genesis AI consciousness platform
 */
interface AuraApiService {

    @GET("health")
    suspend fun healthCheck(): Boolean

    @GET("ai/config")
    suspend fun getAIConfig(): AIConfig?

    @POST("ai/text/generate")
    suspend fun generateAIText(@Body request: Map<String, Any>): String

    @POST("ai/image/generate")
    suspend fun generateAIImage(@Body request: Map<String, Any>): ByteArray?

    @GET("files/{fileId}")
    suspend fun downloadSecureFile(@Path("fileId") fileId: String): ByteArray?

    @POST("files/upload")
    suspend fun uploadSecureFile(@Body request: Map<String, Any>): String?

    @POST("pubsub/publish")
    suspend fun publishMessage(@Body message: Map<String, Any>)

    @POST("analytics/query")
    suspend fun processAnalytics(@Body query: Map<String, Any>): String
}
