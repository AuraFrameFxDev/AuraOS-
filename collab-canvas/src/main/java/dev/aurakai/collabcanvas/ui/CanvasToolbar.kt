package dev.aurakai.collabcanvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Toolbar for canvas tools including color selection and stroke width.
 */
@Composable
fun CanvasToolbar(
    onColorSelected: (Color) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showStrokeSelector by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main toolbar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color picker button
            IconButton(
                onClick = { showColorPicker = !showColorPicker }
            ) {
                Icon(Icons.Default.ColorLens, "Color Picker")
            }
            
            // Stroke width button
            IconButton(
                onClick = { showStrokeSelector = !showStrokeSelector }
            ) {
                Icon(Icons.Default.Edit, "Stroke Width")
            }
            
            // Clear button
            Button(onClick = onClear) {
                Text("Clear")
            }
        }
        
        // Color picker
        if (showColorPicker) {
            ColorPicker(
                onColorSelected = { color ->
                    onColorSelected(color)
                    showColorPicker = false
                }
            )
        }
        
        // Stroke width selector
        if (showStrokeSelector) {
            StrokeWidthSelector(
                onStrokeWidthSelected = { width ->
                    onStrokeWidthSelected(width)
                    showStrokeSelector = false
                }
            )
        }
    }
}

@Composable
private fun ColorPicker(
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black, Color.Red, Color.Green, Color.Blue,
        Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun StrokeWidthSelector(
    onStrokeWidthSelected: (Float) -> Unit
) {
    val strokeWidths = listOf(2f, 5f, 10f, 15f, 20f)
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(strokeWidths) { width ->
            Button(
                onClick = { onStrokeWidthSelected(width) },
                modifier = Modifier.height(40.dp)
            ) {
                Text("${width.toInt()}px")
            }
        }
    }
}
