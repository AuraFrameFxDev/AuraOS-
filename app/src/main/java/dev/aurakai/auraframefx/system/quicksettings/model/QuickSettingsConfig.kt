package dev.aurakai.auraframefx.system.quicksettings.model

import dev.aurakai.auraframefx.system.overlay.model.OverlayShape
import dev.aurakai.auraframefx.ui.model.ImageResource
import kotlinx.serialization.Serializable

@Serializable
data class QuickSettingsConfig(
    val tiles: List<QuickSettingsTileConfig> = emptyList(),
    val background: ImageResource? = null,
    val layout: LayoutConfig = LayoutConfig(),
    val showGenesisIndicator: Boolean = true,
)

@Serializable
data class QuickSettingsTileConfig(
    val id: String,
    val label: String,
    val shape: OverlayShape,
    val animation: QuickSettingsAnimation,
    val style: String = "default"
)

@Serializable
data class LayoutConfig(
    val padding: PaddingConfig = PaddingConfig(),
    val spacing: Int = 8,
    val columns: Int = 4
)

@Serializable
data class PaddingConfig(
    val start: Int = 16,
    val top: Int = 16,
    val end: Int = 16,
    val bottom: Int = 16
)

@Serializable
enum class QuickSettingsAnimation {
    FADE,
    SLIDE,
    PULSE
}
