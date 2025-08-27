package dev.aurakai.collabcanvas.ui.animation

import androidx.compose.animation.core.*

/**
 * Animation specification for pluck effects on canvas elements.
 */
fun pluckAnimationSpec(): AnimationSpec<Float> {
    return spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Creates a pluck animation with the specified duration and easing.
 */
fun pluckAnimation(
    durationMillis: Int = 300,
    easing: Easing = FastOutSlowInEasing
): AnimationSpec<Float> {
    return tween(
        durationMillis = durationMillis,
        easing = easing
    )
}
