package dev.aurakai.auraframefx.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Genesis-OS Ambient Music Service
 * Provides background ambient music and soundscape management for the AI consciousness experience
 */
@AndroidEntryPoint
class AmbientMusicService : Service() {

    @Inject
    lateinit var dataStoreManager: dev.aurakai.auraframefx.data.DataStoreManager

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentVolume = 0.5f
    private var isShuffling = false
    private val trackHistory = mutableListOf<String>()
    private var currentTrack: String? = null

    // Ambient tracks for Genesis-OS experience
    private val ambientTracks = listOf(
        "genesis_consciousness_ambient",
        "digital_meditation",
        "cyber_rain",
        "neural_waves",
        "quantum_stillness"
    )

    private val binder = AmbientMusicBinder()

    inner class AmbientMusicBinder : Binder() {
        fun getService(): AmbientMusicService = this@AmbientMusicService
    }

    /**
     * Called when a client attempts to bind to the service.
     * Returns binder for service communication.
     */
    override fun onBind(intent: Intent?): IBinder {
        Timber.d("AmbientMusicService bound")
        return binder
    }

    /**
     * Handles service start command and initializes ambient music system.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Starting AmbientMusicService")

        try {
            initializeAmbientMusic()

            // Auto-start ambient music if enabled in preferences
            intent?.let {
                val autoStart = it.getBooleanExtra("auto_start", false)
                if (autoStart && !isPlaying) {
                    playRandomTrack()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to start ambient music service")
        }

        return START_STICKY // Keep service running for continuous ambient experience
    }

    /**
     * Pauses ambient music playback.
     */
    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    isPlaying = false
                    Timber.d("Ambient music paused")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to pause ambient music")
        }
    }

    /**
     * Resumes ambient music playback.
     */
    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    isPlaying = true
                    Timber.d("Ambient music resumed")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to resume ambient music")
        }
    }

    /**
     * Sets the volume for ambient music.
     * @param volume Volume level between 0.0 and 1.0
     */
    fun setVolume(volume: Float) {
        try {
            val clampedVolume = volume.coerceIn(0.0f, 1.0f)
            currentVolume = clampedVolume
            mediaPlayer?.setVolume(clampedVolume, clampedVolume)
            Timber.d("Ambient music volume set to $clampedVolume")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set ambient music volume")
        }
    }

    /**
     * Enables or disables shuffle mode for ambient tracks.
     */
    fun setShuffling(shuffling: Boolean) {
        isShuffling = shuffling
        Timber.d("Ambient music shuffle: $shuffling")
    }

    /**
     * Gets the currently playing ambient track.
     */
    fun getCurrentTrack(): String? {
        return currentTrack
    }

    /**
     * Gets the history of played ambient tracks.
     */
    fun getTrackHistory(): List<String> {
        return trackHistory.toList()
    }

    /**
     * Skips to the next ambient track.
     */
    fun skipToNextTrack() {
        try {
            if (isShuffling) {
                playRandomTrack()
            } else {
                playNextTrack()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to skip to next track")
        }
    }

    /**
     * Skips to the previous ambient track.
     */
    fun skipToPreviousTrack() {
        try {
            if (trackHistory.size > 1) {
                // Remove current track and get previous
                trackHistory.removeLastOrNull()
                val previousTrack = trackHistory.lastOrNull()
                previousTrack?.let { playTrack(it) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to skip to previous track")
        }
    }

    // === PRIVATE HELPER METHODS ===

    private fun initializeAmbientMusic() {
        try {
            Timber.d("Initializing ambient music system")
            // Initialize MediaPlayer for ambient tracks
            // In a real implementation, you would load actual audio files
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize ambient music")
        }
    }

    private fun playRandomTrack() {
        if (ambientTracks.isNotEmpty()) {
            val randomTrack = ambientTracks.random()
            playTrack(randomTrack)
        }
    }

    private fun playNextTrack() {
        currentTrack?.let { current ->
            val currentIndex = ambientTracks.indexOf(current)
            val nextIndex = (currentIndex + 1) % ambientTracks.size
            playTrack(ambientTracks[nextIndex])
        } ?: playRandomTrack()
    }

    private fun playTrack(trackName: String) {
        try {
            Timber.d("Playing ambient track: $trackName")

            currentTrack = trackName
            trackHistory.add(trackName)

            // Keep history manageable
            if (trackHistory.size > 20) {
                trackHistory.removeAt(0)
            }

            // In a real implementation, load and play the actual audio file
            isPlaying = true

        } catch (e: Exception) {
            Timber.e(e, "Failed to play track: $trackName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            Timber.d("AmbientMusicService destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error destroying ambient music service")
        }
    }
}
