package dev.aurakai.collabcanvas.util

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathIterator
import androidx.compose.ui.geometry.Offset

/**
 * A utility class to work with Compose Path bounds calculations
 */
object PathBounds {
    
    /**
     * Calculate the bounds of a Compose UI Path
     */
    fun calculateBounds(path: Path): Rect {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        
        val iterator = PathIterator.create(path, density = 1f)
        val points = FloatArray(8)
        
        while (iterator.hasNext()) {
            val type = iterator.next(points)
            when (type) {
                PathIterator.Verb.Move,
                PathIterator.Verb.Line -> {
                    minX = minOf(minX, points[0])
                    minY = minOf(minY, points[1])
                    maxX = maxOf(maxX, points[0])
                    maxY = maxOf(maxY, points[1])
                }
                PathIterator.Verb.Quad -> {
                    // Control point
                    minX = minOf(minX, points[0], points[2])
                    minY = minOf(minY, points[1], points[3])
                    maxX = maxOf(maxX, points[0], points[2])
                    maxY = maxOf(maxY, points[1], points[3])
                }
                PathIterator.Verb.Cubic -> {
                    // Two control points
                    minX = minOf(minX, points[0], points[2], points[4])
                    minY = minOf(minY, points[1], points[3], points[5])
                    maxX = maxOf(maxX, points[0], points[2], points[4])
                    maxY = maxOf(maxY, points[1], points[3], points[5])
                }
                PathIterator.Verb.Close -> {
                    // No additional points to consider
                }
                PathIterator.Verb.Done -> break
            }
        }
        
        return if (minX == Float.MAX_VALUE) {
            Rect.Zero
        } else {
            Rect(minX, minY, maxX, maxY)
        }
    }
}

/**
 * Extension function to get bounds of a Compose UI Path
 */
fun Path.getBounds(): Rect {
    return PathBounds.calculateBounds(this)
}

/**
 * Extension function to check if a path contains a point
 */
fun Path.contains(x: Float, y: Float): Boolean {
    val bounds = getBounds()
    if (!bounds.contains(Offset(x, y))) return false
    
    // For a more accurate contains check, you might want to implement
    // a more sophisticated algorithm like ray casting
    return true
}
