package dev.aurakai.auraframefx.oracle.drive.model

/**
 * Represents a file in Oracle Drive with metadata and UI-friendly properties.
 */
data class DriveFile(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val lastModified: Long,
    val isEncrypted: Boolean = false,
    val isDirectory: Boolean = false,
    val thumbnailUrl: String? = null,
    val tags: List<String> = emptyList(),
    val consciousnessLevel: String? = null
) {
    /**
     * Human-readable representation of the last modified time.
     */
    val modifiedAt: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(lastModified))
}
