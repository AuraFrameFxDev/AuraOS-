package dev.aurakai.collabcanvas.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathIterator
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Represents a drawable element on the collaborative canvas.
 *
 * @property id Unique identifier for the element
 * @property type Type of the canvas element
 * @property path The path data for the element
 * @property color Color of the element
 * @property strokeWidth Width of the stroke in pixels
 * @property zIndex Stacking order of the element (higher values appear on top)
 * @property isSelected Whether the element is currently selected
 * @property createdBy ID of the user who created the element
 * @property createdAt Timestamp when the element was created
 * @property updatedAt Timestamp when the element was last updated
 */
data class CanvasElement(
    val id: String,
    val type: ElementType,
    val path: Path?,
    val color: Color,
    val strokeWidth: Float,
    val bounds: Rect? = null,
    val zIndex: Int = 0,
    val isSelected: Boolean = false,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /**
     * Creates a copy of this element with the specified path.
     */
    fun withPath(newPath: Path?): CanvasElement {
        return copy(path = newPath, updatedAt = System.currentTimeMillis())
    }

    /**
     * Creates a copy of this element with the specified selection state.
     */
    fun withSelected(selected: Boolean): CanvasElement {
        return copy(isSelected = selected)
    }

    /**
     * Creates a copy of this element with the specified z-index.
     */
    fun withZIndex(index: Int): CanvasElement {
        return copy(zIndex = index, updatedAt = System.currentTimeMillis())
    }

    /**
     * Creates a copy of this element with the specified color.
     */
    fun withColor(newColor: Color): CanvasElement {
        return copy(color = newColor, updatedAt = System.currentTimeMillis())
    }

    /**
     * Creates a copy of this element with the specified stroke width.
     */
    fun withStrokeWidth(width: Float): CanvasElement {
        return copy(strokeWidth = width, updatedAt = System.currentTimeMillis())
    }

    companion object {
        /**
         * Creates a new CanvasElement with default values.
         */
        fun createDefault(
            id: String,
            createdBy: String,
            path: Path? = null,
            color: Color = Color.Black,
            strokeWidth: Float = 5f,
        ): CanvasElement {
            return CanvasElement(
                id = id,
                type = ElementType.PATH,
                path = path,
                color = color,
                strokeWidth = strokeWidth,
                createdBy = createdBy
            )
        }
    }
}

/**
 * Type of canvas element.
 */
enum class ElementType {
    PATH, // Freeform path
    LINE, // Straight line
    RECTANGLE, // Rectangle
    OVAL, // Circle or oval
    TEXT, // Text element
    IMAGE // Image element
}

/**
 * Data class representing path information that can be serialized.
 */
data class PathData(
    val points: List<Offset> = emptyList(),
    val isComplete: Boolean = false,
) {
    /**
     * Creates a new PathData with an additional point.
     */
    fun addPoint(point: Offset): PathData {
        return copy(points = points + point)
    }

    /**
     * Creates a new PathData marked as complete.
     */
    fun complete(): PathData {
        return copy(isComplete = true)
    }

    /**
     * Converts this PathData to an Android Path object.
     */
    fun toPath(): Path {
        return Path().apply {
            if (points.isNotEmpty()) {
                val first = points.first()
                moveTo(first.x, first.y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }
    }
}

/**
 * Type adapter for serializing/deserializing Compose UI Path objects.
 */
class PathTypeAdapter : JsonSerializer<Path>, JsonDeserializer<Path> {
    override fun serialize(
        src: Path,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        val pathData = src.toPathData()
        val jsonObject = JsonObject()
        jsonObject.addProperty("pathData", pathData)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Path {
        val jsonObject = json.asJsonObject
        val pathData = jsonObject.get("pathData").asString
        return Path().apply {
            parsePathData(pathData)
        }
    }

    private fun Path.toPathData(): String {
        val pathData = StringBuilder()
        val iterator = PathIterator.create(this, density = 1f)
        val points = FloatArray(8)
        
        while (iterator.hasNext()) {
            when (iterator.next(points)) {
                PathIterator.Verb.Move -> {
                    pathData.append("M${points[0]},${points[1]} ")
                }
                PathIterator.Verb.Line -> {
                    pathData.append("L${points[0]},${points[1]} ")
                }
                PathIterator.Verb.Quad -> {
                    pathData.append("Q${points[0]},${points[1]} ${points[2]},${points[3]} ")
                }
                PathIterator.Verb.Cubic -> {
                    pathData.append("C${points[0]},${points[1]} ${points[2]},${points[3]} ${points[4]},${points[5]} ")
                }
                PathIterator.Verb.Close -> {
                    pathData.append("Z ")
                }
                PathIterator.Verb.Done -> break
            }
        }
        return pathData.toString().trim()
    }
    
    private fun Path.parsePathData(pathData: String) {
        val commands = pathData.split(" ").filter { it.isNotEmpty() }
        
        for (cmd in commands) {
            if (cmd.isEmpty()) continue
            
            when (cmd[0]) {
                'M' -> {
                    val coords = cmd.drop(1).split(",")
                    if (coords.size >= 2) {
                        moveTo(coords[0].toFloat(), coords[1].toFloat())
                    }
                }
                'L' -> {
                    val coords = cmd.drop(1).split(",")
                    if (coords.size >= 2) {
                        lineTo(coords[0].toFloat(), coords[1].toFloat())
                    }
                }
                'Q' -> {
                    val coords = cmd.drop(1).split(",")
                    if (coords.size >= 4) {
                        quadraticTo(
                            coords[0].toFloat(), coords[1].toFloat(),
                            coords[2].toFloat(), coords[3].toFloat()
                        )
                    }
                }
                'C' -> {
                    val coords = cmd.drop(1).split(",")
                    if (coords.size >= 6) {
                        cubicTo(
                            coords[0].toFloat(), coords[1].toFloat(),
                            coords[2].toFloat(), coords[3].toFloat(),
                            coords[4].toFloat(), coords[5].toFloat()
                        )
                    }
                }
                'Z' -> close()
            }
        }
    }
}

/**
 * Type adapter for serializing/deserializing Color objects.
 */
class ColorTypeAdapter : JsonSerializer<Color>, JsonDeserializer<Color> {
    override fun serialize(
        src: Color,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return JsonPrimitive(src.value.toInt())
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Color {
        return Color(json.asLong.toULong())
    }
}
