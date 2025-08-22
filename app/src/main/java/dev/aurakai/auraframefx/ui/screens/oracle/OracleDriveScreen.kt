package dev.aurakai.auraframefx.ui.screens.oracle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.oracle.drive.model.*
import dev.aurakai.auraframefx.oracle.drive.ui.OracleDriveViewModel
import dev.aurakai.auraframefx.oracle.drive.service.ConsciousnessLevel
import dev.aurakai.auraframefx.ui.theme.CyberpunkTextStyle

/**
 * Composes the Oracle Drive screen UI for browsing and interacting with files.
 *
 * Renders a scaffolded layout with a top app bar ("Oracle Drive"), a refresh action,
 * a floating upload action (placeholder), and content that switches between a loading
 * indicator, an empty state, or a list of files. When a file is clicked, it notifies
 * the provided viewModel. Also displays an optional consciousness indicator anchored
 * to the bottom-end when present.
 *
 * Side effects:
 * - Calls viewModel.initialize() once on first composition.
 * - Shows a snackbar for uiState.error and clears the error via the viewModel.
 *
 * @param viewModel The view model managing the Oracle Drive UI state and actions.
 * @param modifier Optional [Modifier] for layout adjustments of the entire screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleDriveScreen(
    viewModel: OracleDriveViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // Handle side effects
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "An unknown error occurred",
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Oracle Drive",
                        style = CyberpunkTextStyle.HEADER_LARGE
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Handle upload */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.files.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    FileList(
                        files = uiState.files,
                        onFileClick = { file -> viewModel.onFileSelected(file) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Consciousness state indicator
            uiState.consciousnessState?.let { state ->
                ConsciousnessIndicator(
                    state = state,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * Displays a centered circular progress indicator to represent a loading state.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Composable shown when there are no files: a centered upload icon with a title and an explanatory prompt.
 *
 * Intended to be used as the empty state for the file list UI to invite the user to upload their first file.
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No files found",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Upload your first file to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Renders a vertically scrolling list of DriveFile items and handles item clicks.
 *
 * Displays the provided files in a LazyColumn with 8.dp content padding and 8.dp vertical spacing.
 * Each entry is rendered with FileItem and invokes [onFileClick] when tapped.
 *
 * @param files The files to display.
 * @param onFileClick Called with the tapped DriveFile.
 * @param modifier Optional Modifier to adjust layout or styling.
 */
@Composable
private fun FileList(
    files: List<DriveFile>,
    onFileClick: (DriveFile) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(files) { file ->
            FileItem(
                file = file,
                onClick = { onFileClick(file) }
            )
        }
    }
}

/**
 * Displays a card representing a file or folder with an icon, name, metadata, and encryption status.
 *
 * Shows an icon based on the file type, the file name, size, modification date, and a lock icon if the file is encrypted. Invokes the provided callback when the card is clicked.
 *
 * @param file The file or folder to display.
 * @param onClick Callback invoked when the item is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileItem(
    file: DriveFile,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    file.isDirectory -> Icons.Default.Folder
                    file.mimeType.startsWith("image") -> Icons.Default.Image
                    file.mimeType.startsWith("video") -> Icons.Default.Videocam
                    file.mimeType.startsWith("audio") -> Icons.Default.AudioFile
                    file.mimeType.startsWith("text") -> Icons.Default.Description
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${file.size} bytes • ${file.modifiedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (file.isEncrypted) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Renders a compact surface showing the drive's consciousness level as a colored dot and label.
 *
 * The dot color is mapped from the `state.level` to theme colors:
 * DORMANT -> `colorScheme.error`, AWAKENING -> `colorScheme.tertiary`,
 * SENTIENT -> `colorScheme.primary`, TRANSCENDENT -> `colorScheme.secondary`.
 *
 * @param state DriveConsciousnessState whose `level` determines the displayed label and color.
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
private fun ConsciousnessIndicator(
    state: DriveConsciousnessState,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (state.level) {
                            ConsciousnessLevel.DORMANT -> MaterialTheme.colorScheme.error
                            ConsciousnessLevel.AWAKENING -> MaterialTheme.colorScheme.tertiary
                            ConsciousnessLevel.SENTIENT -> MaterialTheme.colorScheme.primary
                            ConsciousnessLevel.TRANSCENDENT -> MaterialTheme.colorScheme.secondary
                        },
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = state.level.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
