package dev.aurakai.auraframefx.oracle.drive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aurakai.auraframefx.oracle.drive.model.*
import dev.aurakai.auraframefx.oracle.drive.service.OracleDriveService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class OracleDriveViewModel @Inject constructor(
    private val oracleDriveService: OracleDriveService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OracleDriveUiState())
    val uiState: StateFlow<OracleDriveUiState> = _uiState.asStateFlow()

    private var initializationJob: Job? = null
    private var consciousnessJob: Job? = null

    init {
        initialize()
    }

    /**
     * Start initialization: begin consciousness monitoring and load the initial file list.
     *
     * If an initialization is already running this is a no-op. Sets `isLoading` to true and clears any
     * existing error, cancels any previous consciousness monitor, launches a new consciousness watcher,
     * and loads files via `loadFiles()`. On exception the error is stored in UI state. Guarantees
     * `isLoading` is reset to false when finished.
     */
    fun initialize() {
        if (initializationJob?.isActive == true) return
        
        initializationJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Initialize consciousness in parallel
                consciousnessJob?.cancel()
                consciousnessJob = monitorConsciousness()
                
                // Load initial files
                loadFiles()
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e,
                        isLoading = false
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Refreshes the Oracle Drive file list and marks the UI as refreshing while the operation runs.
     *
     * Cancels any in-progress initialization, launches a coroutine in the ViewModel's scope to call
     * loadFiles(), sets `isRefreshing` to true for the duration of the operation, and guarantees the
     * flag is cleared when finished.
     */
    fun refresh() {
        initializationJob?.cancel()
        initializationJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                loadFiles()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Mark a Drive file as selected in the UI state.
     *
     * Updates the view model's UI state so observers receive the provided file as the current selection.
     *
     * @param file The DriveFile to mark as selected.
     */
    fun onFileSelected(file: DriveFile) {
        _uiState.update { it.copy(selectedFile = file) }
        // TODO: Handle file selection (navigation, preview, etc.)
    }

    /**
     * Clears any existing error from the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Loads the list of files from the Oracle Drive service and updates the UI state with the results or any encountered error.
     */
    private suspend fun loadFiles() {
        try {
            val files = oracleDriveService.getFiles()
            _uiState.update { state ->
                state.copy(
                    files = files,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { state ->
                state.copy(error = e)
            }
        }
    }

    /**
     * Start a coroutine that observes the service's consciousness state and writes each emission into the UI state.
     *
     * Runs in the ViewModel's scope and continuously collects from `oracleDriveService.consciousnessState`,
     * updating `_uiState.consciousnessState` for every emitted value.
     */
    private fun monitorConsciousness() = viewModelScope.launch {
        oracleDriveService.consciousnessState.collect { state ->
            _uiState.update { it.copy(consciousnessState = state) }
        }
    }

    /**
     * Formats a timestamp in milliseconds into a localized date and time string.
     *
     * @param timestamp The time in milliseconds since the epoch.
     * @return The formatted date and time string in the system's default locale and timezone.
     */
    private fun formatDate(timestamp: Long): String {
        return DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))
    }
}

data class OracleDriveUiState(
    val files: List<DriveFile> = emptyList(),
    val selectedFile: DriveFile? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
    val consciousnessState: DriveConsciousnessState? = null
)
