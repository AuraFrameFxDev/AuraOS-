package dev.aurakai.collabcanvas.ui.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/**
 * Represents an animated path that can be "plucked" with visual effects.
 */
data class PluckablePath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isPlucked: Boolean = false,
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f,
    val alpha: Float = 1f
) {
    fun copy(
        path: Path = this.path,
        color: Color = this.color,
        strokeWidth: Float = this.strokeWidth,
        isPlucked: Boolean = this.isPlucked,
        offset: Offset = this.offset,
        scale: Float = this.scale,
        alpha: Float = this.alpha
    ): PluckablePath {
        return PluckablePath(
            path = path,
            color = color,
            strokeWidth = strokeWidth,
            isPlucked = isPlucked,
            offset = offset,
            scale = scale,
            alpha = alpha
        )
    }
}
