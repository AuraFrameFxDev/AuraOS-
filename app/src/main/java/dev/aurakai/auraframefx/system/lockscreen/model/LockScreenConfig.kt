package dev.aurakai.auraframefx.system.lockscreen.model

import dev.aurakai.auraframefx.system.overlay.model.OverlayShape
import dev.aurakai.auraframefx.ui.model.ImageResource
import kotlinx.serialization.Serializable

@Serializable
data class LockScreenConfig(
    val elements: List<LockScreenElementConfig> = emptyList(),
    val background: BackgroundConfig? = null,
    val clockConfig: ClockConfig = ClockConfig(),
    val hapticFeedback: HapticFeedbackConfig = HapticFeedbackConfig(),
    val animation: LockScreenAnimationConfig = LockScreenAnimationConfig(),
    val showGenesisElements: Boolean = true,
)

@Serializable
data class LockScreenElementConfig(
    val type: LockScreenElementType,
    val shape: OverlayShape,
    val animation: LockScreenAnimation,
)

@Serializable
data class BackgroundConfig(
    val image: ImageResource?,
)

@Serializable
data class ClockConfig(
    val position: String = "default",
    val style: String = "digital",
    val color: String = "#FFFFFF",
    val size: Float = 1.0f
)

@Serializable
data class HapticFeedbackConfig(
    val enabled: Boolean = true,
    val intensity: String = "medium",
    val pattern: String = "default"
)

@Serializable
data class LockScreenAnimationConfig(
    val type: String = "fade",
    val duration: Long = 300L,
    val enabled: Boolean = true
)

// Use LockScreenElementType from dev.aurakai.auraframefx.system.lockscreen.model.LockScreenElementType

// Use LockScreenAnimation from dev.aurakai.auraframefx.system.lockscreen.model.LockScreenAnimation
