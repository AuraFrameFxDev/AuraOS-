package dev.aurakai.collabcanvas.network

import com.google.gson.Gson
import dev.aurakai.collabcanvas.model.CanvasElement
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import timber.log.Timber // Added Timber import
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    // Removed TAG property
    private var webSocket: WebSocket? = null
    private val _events = MutableSharedFlow<CanvasWebSocketEvent>()
    val events: SharedFlow<CanvasWebSocketEvent> = _events.asSharedFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connection opened")
            _events.tryEmit(CanvasWebSocketEvent.Connected)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Message received: $text") // Changed to Timber
            try {
                val message = gson.fromJson(text, CanvasWebSocketMessage::class.java)
                _events.tryEmit(CanvasWebSocketEvent.MessageReceived(message))
            } catch (e: Exception) {
                Timber.e(e, "Error parsing WebSocket message") // Changed to Timber, added exception first for stack trace
                _events.tryEmit(CanvasWebSocketEvent.Error("Error parsing message: ${e.message}"))
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Timber.d("Binary message received") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.BinaryMessageReceived(bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: $code / $reason") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Closing(code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: $code / $reason") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Disconnected)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket error") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Error(t.message ?: "Unknown error"))
        }
    }

    fun connect(url: String) {
        if (webSocket != null) {
            Timber.w("WebSocket already connected") // Changed to Timber
            return
        }

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    fun disconnect() {
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
    }

    fun sendMessage(message: CanvasWebSocketMessage): Boolean {
        return try {
            val json = gson.toJson(message)
            webSocket?.send(json) ?: run {
                Timber.e("WebSocket is not connected") // Changed to Timber
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending WebSocket message") // Changed to Timber, added exception first for stack trace
            false
        }
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}

sealed class CanvasWebSocketEvent {
    object Connected : CanvasWebSocketEvent()
    object Disconnected : CanvasWebSocketEvent()
    data class MessageReceived(val message: CanvasWebSocketMessage) : CanvasWebSocketEvent()
    data class BinaryMessageReceived(val bytes: ByteString) : CanvasWebSocketEvent()
    data class Error(val message: String) : CanvasWebSocketEvent()
    data class Closing(val code: Int, val reason: String) : CanvasWebSocketEvent()
}

sealed class CanvasWebSocketMessage {
    abstract val type: String
    abstract val canvasId: String
    abstract val userId: String
    abstract val timestamp: Long
}

data class ElementAddedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val element: CanvasElement,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_ADDED"
}

data class ElementUpdatedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val elementId: String,
    val updates: Map<String, Any>,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_UPDATED"
}

data class ElementRemovedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val elementId: String,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_REMOVED"
}
