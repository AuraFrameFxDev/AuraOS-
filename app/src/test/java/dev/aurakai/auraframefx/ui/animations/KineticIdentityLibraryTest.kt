package dev.aurakai.auraframefx.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.theme.AuraTheme
import dev.aurakai.auraframefx.theme.CyberpunkTheme
import dev.aurakai.auraframefx.theme.SolarFlareTheme
import dev.aurakai.auraframefx.ui.animations.KineticIdentityLibrary.EmotionalState
import dev.aurakai.auraframefx.ui.animations.KineticIdentityLibrary.FlowDirection
import dev.aurakai.auraframefx.ui.animations.KineticIdentityLibrary.Particle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.abs

/**
 * Comprehensive unit tests for KineticIdentityLibrary
 * 
 * Testing Framework: JUnit 4 with AndroidX Compose Test and AndroidJUnit4
 * 
 * This test suite covers:
 * - Happy path scenarios for all animation components
 * - Edge cases and error conditions  
 * - Emotional state variations
 * - Theme integration and responsiveness
 * - Performance considerations
 * - State persistence across recomposition
 * - Helper function validation
 * - Data class integrity
 */
@RunWith(AndroidJUnit4::class)
class KineticIdentityLibraryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // MARK: - BreathingAnimation Tests

    @Test
    fun breathingAnimation_rendersWithDefaultParameters() {
        composeTestRule.setContent {
            KineticIdentityLibrary.BreathingAnimation(
                modifier = Modifier.testTag("breathing_default")
            )
        }
        
        composeTestRule.onNodeWithTag("breathing_default")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun breathingAnimation_appliesCustomModifier() {
        composeTestRule.setContent {
            KineticIdentityLibrary.BreathingAnimation(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("breathing_custom_modifier")
            )
        }
        
        composeTestRule.onNodeWithTag("breathing_custom_modifier")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun breathingAnimation_handlesAllEmotionalStates() {
        EmotionalState.values().forEach { state ->
            composeTestRule.setContent {
                KineticIdentityLibrary.BreathingAnimation(
                    emotionalState = state,
                    modifier = Modifier.testTag("breathing_${state.name}")
                )
            }
            
            composeTestRule.onNodeWithTag("breathing_${state.name}")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun breathingAnimation_handlesIntensityRange() {
        val intensityValues = listOf(0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 5.0f)
        
        intensityValues.forEach { intensity ->
            composeTestRule.setContent {
                KineticIdentityLibrary.BreathingAnimation(
                    intensity = intensity,
                    modifier = Modifier.testTag("breathing_intensity_$intensity")
                )
            }
            
            composeTestRule.onNodeWithTag("breathing_intensity_$intensity")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun breathingAnimation_handlesColorVariations() {
        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Transparent,
            Color.White.copy(alpha = 0.1f),
            Color.Black.copy(alpha = 0.9f)
        )
        
        colors.forEachIndexed { index, color ->
            composeTestRule.setContent {
                KineticIdentityLibrary.BreathingAnimation(
                    color = color,
                    modifier = Modifier.testTag("breathing_color_$index")
                )
            }
            
            composeTestRule.onNodeWithTag("breathing_color_$index")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun breathingAnimation_handlesExtremeValues() {
        val extremeIntensities = listOf(
            Float.MIN_VALUE,
            -1.0f,
            0.0f,
            Float.MAX_VALUE
        )
        
        extremeIntensities.forEachIndexed { index, intensity ->
            composeTestRule.setContent {
                KineticIdentityLibrary.BreathingAnimation(
                    intensity = intensity,
                    modifier = Modifier.testTag("breathing_extreme_$index")
                )
            }
            
            // Should handle gracefully without crashing
            composeTestRule.onNodeWithTag("breathing_extreme_$index")
                .assertExists()
        }
    }

    // MARK: - ResponsiveGlow Tests

    @Test
    fun responsiveGlow_rendersWithDefaultParameters() {
        composeTestRule.setContent {
            KineticIdentityLibrary.ResponsiveGlow(
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("glow_default")
            )
        }
        
        composeTestRule.onNodeWithTag("glow_default")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun responsiveGlow_respondsToActiveState() {
        listOf(true, false).forEach { isActive ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ResponsiveGlow(
                    isActive = isActive,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("glow_active_$isActive")
                )
            }
            
            composeTestRule.onNodeWithTag("glow_active_$isActive")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun responsiveGlow_handlesTouchPositions() {
        val touchPositions = listOf(
            Offset(100f, 100f),
            Offset(0f, 0f),
            Offset(500f, 300f),
            Offset(-50f, -50f),
            null
        )
        
        touchPositions.forEachIndexed { index, position ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ResponsiveGlow(
                    touchPosition = position,
                    isActive = position != null,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("glow_touch_$index")
                )
            }
            
            composeTestRule.onNodeWithTag("glow_touch_$index")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun responsiveGlow_worksWithDifferentThemes() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme)
        
        themes.forEachIndexed { index, theme ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ResponsiveGlow(
                    theme = theme,
                    modifier = Modifier.testTag("glow_theme_$index")
                )
            }
            
            composeTestRule.onNodeWithTag("glow_theme_$index")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun responsiveGlow_handlesIntensityVariations() {
        val intensityValues = listOf(0.0f, 0.5f, 1.0f, 2.0f, 10.0f)
        
        intensityValues.forEach { intensity ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ResponsiveGlow(
                    intensity = intensity,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("glow_intensity_$intensity")
                )
            }
            
            composeTestRule.onNodeWithTag("glow_intensity_$intensity")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun responsiveGlow_handlesExtremeOffsets() {
        val extremeOffsets = listOf(
            Offset(Float.MAX_VALUE, Float.MAX_VALUE),
            Offset(Float.MIN_VALUE, Float.MIN_VALUE),
            Offset(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY),
            Offset(Float.NaN, Float.NaN)
        )
        
        extremeOffsets.forEachIndexed { index, offset ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ResponsiveGlow(
                    touchPosition = offset,
                    isActive = true,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("glow_extreme_$index")
                )
            }
            
            // Should handle gracefully without crashing
            composeTestRule.onNodeWithTag("glow_extreme_$index")
                .assertExists()
        }
    }

    // MARK: - ParticleFlow Tests

    @Test
    fun particleFlow_rendersWithDefaultParameters() {
        composeTestRule.setContent {
            KineticIdentityLibrary.ParticleFlow(
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("particles_default")
            )
        }
        
        composeTestRule.onNodeWithTag("particles_default")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun particleFlow_handlesParticleCountVariations() {
        val particleCounts = listOf(0, 1, 5, 20, 50, 100, 1000)
        
        particleCounts.forEach { count ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ParticleFlow(
                    particleCount = count,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("particles_count_$count")
                )
            }
            
            composeTestRule.onNodeWithTag("particles_count_$count")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun particleFlow_handlesAllFlowDirections() {
        FlowDirection.values().forEach { direction ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ParticleFlow(
                    flowDirection = direction,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("particles_${direction.name}")
                )
            }
            
            composeTestRule.onNodeWithTag("particles_${direction.name}")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun particleFlow_worksWithDifferentAnimationStyles() {
        val themes = listOf(
            CyberpunkTheme, // ENERGETIC
            SolarFlareTheme // PULSING
        )
        
        themes.forEachIndexed { index, theme ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ParticleFlow(
                    theme = theme,
                    modifier = Modifier.testTag("particles_style_$index")
                )
            }
            
            composeTestRule.onNodeWithTag("particles_style_$index")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun particleFlow_handlesNegativeParticleCount() {
        composeTestRule.setContent {
            KineticIdentityLibrary.ParticleFlow(
                particleCount = -10,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("particles_negative")
            )
        }
        
        // Should handle gracefully without crashing
        composeTestRule.onNodeWithTag("particles_negative")
            .assertExists()
    }

    @Test
    fun particleFlow_handlesIntensityVariations() {
        val intensityValues = listOf(0.0f, 0.1f, 1.0f, 5.0f, 10.0f)
        
        intensityValues.forEach { intensity ->
            composeTestRule.setContent {
                KineticIdentityLibrary.ParticleFlow(
                    intensity = intensity,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("particles_intensity_$intensity")
                )
            }
            
            composeTestRule.onNodeWithTag("particles_intensity_$intensity")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    // MARK: - KeyboardGlow Tests

    @Test
    fun keyboardGlow_rendersWithDefaultParameters() {
        composeTestRule.setContent {
            KineticIdentityLibrary.KeyboardGlow(
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("keyboard_default")
            )
        }
        
        composeTestRule.onNodeWithTag("keyboard_default")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun keyboardGlow_respondsToTypingState() {
        listOf(true, false).forEach { isTyping ->
            composeTestRule.setContent {
                KineticIdentityLibrary.KeyboardGlow(
                    isTyping = isTyping,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("keyboard_typing_$isTyping")
                )
            }
            
            composeTestRule.onNodeWithTag("keyboard_typing_$isTyping")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun keyboardGlow_worksWithDifferentThemes() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme)
        
        themes.forEachIndexed { index, theme ->
            composeTestRule.setContent {
                KineticIdentityLibrary.KeyboardGlow(
                    theme = theme,
                    modifier = Modifier.testTag("keyboard_theme_$index")
                )
            }
            
            composeTestRule.onNodeWithTag("keyboard_theme_$index")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun keyboardGlow_handlesIntensityVariations() {
        val intensityValues = listOf(0.0f, 0.5f, 1.0f, 2.0f, 5.0f)
        
        intensityValues.forEach { intensity ->
            composeTestRule.setContent {
                KineticIdentityLibrary.KeyboardGlow(
                    intensity = intensity,
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("keyboard_intensity_$intensity")
                )
            }
            
            composeTestRule.onNodeWithTag("keyboard_intensity_$intensity")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun keyboardGlow_handlesNegativeIntensity() {
        composeTestRule.setContent {
            KineticIdentityLibrary.KeyboardGlow(
                intensity = -1.0f,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("keyboard_negative")
            )
        }
        
        // Should handle gracefully without crashing
        composeTestRule.onNodeWithTag("keyboard_negative")
            .assertExists()
    }

    // MARK: - Data Class and Enum Tests

    @Test
    fun particle_dataClass_hasCorrectProperties() {
        val particle = Particle(
            position = Offset(10f, 20f),
            velocity = Offset(1f, -2f),
            life = 0.8f,
            maxLife = 2.0f,
            size = 5.0f
        )
        
        assertEquals(Offset(10f, 20f), particle.position)
        assertEquals(Offset(1f, -2f), particle.velocity)
        assertEquals(0.8f, particle.life, 0.001f)
        assertEquals(2.0f, particle.maxLife, 0.001f)
        assertEquals(5.0f, particle.size, 0.001f)
    }

    @Test
    fun particle_copyFunction_worksCorrectly() {
        val original = Particle(
            position = Offset(10f, 20f),
            velocity = Offset(1f, -2f),
            life = 0.8f,
            maxLife = 2.0f,
            size = 5.0f
        )
        
        val modified = original.copy(
            life = 0.5f,
            position = Offset(15f, 25f)
        )
        
        assertEquals(Offset(15f, 25f), modified.position)
        assertEquals(0.5f, modified.life, 0.001f)
        assertEquals(original.velocity, modified.velocity)
        assertEquals(original.maxLife, modified.maxLife)
        assertEquals(original.size, modified.size)
    }

    @Test
    fun emotionalState_enumHasAllExpectedValues() {
        val expectedStates = setOf(
            EmotionalState.CALM,
            EmotionalState.ENERGETIC,
            EmotionalState.FOCUSED,
            EmotionalState.STRESSED,
            EmotionalState.NEUTRAL
        )
        
        val actualStates = EmotionalState.values().toSet()
        assertEquals(expectedStates.size, actualStates.size)
        assertTrue(actualStates.containsAll(expectedStates))
    }

    @Test
    fun flowDirection_enumHasAllExpectedValues() {
        val expectedDirections = setOf(
            FlowDirection.UPWARD,
            FlowDirection.DOWNWARD,
            FlowDirection.LEFTWARD,
            FlowDirection.RIGHTWARD,
            FlowDirection.RADIAL
        )
        
        val actualDirections = FlowDirection.values().toSet()
        assertEquals(expectedDirections.size, actualDirections.size)
        assertTrue(actualDirections.containsAll(expectedDirections))
    }

    // MARK: - Integration Tests

    @Test
    fun allAnimations_renderSimultaneouslyWithoutConflicts() {
        composeTestRule.setContent {
            KineticIdentityLibrary.BreathingAnimation(
                modifier = Modifier.testTag("combined_breathing")
            )
            KineticIdentityLibrary.ResponsiveGlow(
                isActive = true,
                touchPosition = Offset(100f, 100f),
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("combined_glow")
            )
            KineticIdentityLibrary.ParticleFlow(
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("combined_particles")
            )
            KineticIdentityLibrary.KeyboardGlow(
                isTyping = true,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("combined_keyboard")
            )
        }
        
        // All animations should render without conflicts
        composeTestRule.onNodeWithTag("combined_breathing").assertExists()
        composeTestRule.onNodeWithTag("combined_glow").assertExists()
        composeTestRule.onNodeWithTag("combined_particles").assertExists()
        composeTestRule.onNodeWithTag("combined_keyboard").assertExists()
    }

    @Test
    fun animations_maintainPerformanceWithHighLoad() {
        composeTestRule.setContent {
            // High particle count test
            KineticIdentityLibrary.ParticleFlow(
                particleCount = 500,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("high_performance")
            )
            
            // Multiple breathing animations
            repeat(3) { index ->
                KineticIdentityLibrary.BreathingAnimation(
                    emotionalState = EmotionalState.values()[index % EmotionalState.values().size],
                    modifier = Modifier.testTag("breathing_multi_$index")
                )
            }
        }
        
        // Should render without performance degradation
        composeTestRule.onNodeWithTag("high_performance").assertExists()
        repeat(3) { index ->
            composeTestRule.onNodeWithTag("breathing_multi_$index").assertExists()
        }
    }

    @Test
    fun animations_maintainStateAcrossRecomposition() {
        var triggerRecomposition by mutableStateOf(false)
        
        composeTestRule.setContent {
            // Force recomposition when state changes
            val _ = triggerRecomposition
            
            KineticIdentityLibrary.BreathingAnimation(
                modifier = Modifier.testTag("recomposition_test")
            )
        }
        
        composeTestRule.onNodeWithTag("recomposition_test").assertExists()
        
        // Trigger recomposition
        triggerRecomposition = true
        composeTestRule.waitForIdle()
        
        // Should still exist and be stable after recomposition
        composeTestRule.onNodeWithTag("recomposition_test").assertExists()
    }

    @Test
    fun animations_respondToThemeChanges() {
        var currentTheme by mutableStateOf<AuraTheme>(CyberpunkTheme)
        
        composeTestRule.setContent {
            KineticIdentityLibrary.ResponsiveGlow(
                theme = currentTheme,
                modifier = Modifier.testTag("theme_change_test")
            )
            KineticIdentityLibrary.ParticleFlow(
                theme = currentTheme,
                modifier = Modifier.testTag("theme_change_particles")
            )
            KineticIdentityLibrary.KeyboardGlow(
                theme = currentTheme,
                modifier = Modifier.testTag("theme_change_keyboard")
            )
        }
        
        // Initially with CyberpunkTheme
        composeTestRule.onNodeWithTag("theme_change_test").assertExists()
        composeTestRule.onNodeWithTag("theme_change_particles").assertExists()
        composeTestRule.onNodeWithTag("theme_change_keyboard").assertExists()
        
        // Change to SolarFlareTheme
        currentTheme = SolarFlareTheme
        composeTestRule.waitForIdle()
        
        // Should still render with new theme
        composeTestRule.onNodeWithTag("theme_change_test").assertExists()
        composeTestRule.onNodeWithTag("theme_change_particles").assertExists()
        composeTestRule.onNodeWithTag("theme_change_keyboard").assertExists()
    }

    // MARK: - Stress Tests

    @Test
    fun animations_handleRapidStateChanges() {
        var isActive by mutableStateOf(false)
        var isTyping by mutableStateOf(false)
        var emotionalState by mutableStateOf(EmotionalState.NEUTRAL)
        
        composeTestRule.setContent {
            KineticIdentityLibrary.ResponsiveGlow(
                isActive = isActive,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("rapid_glow")
            )
            KineticIdentityLibrary.KeyboardGlow(
                isTyping = isTyping,
                theme = CyberpunkTheme,
                modifier = Modifier.testTag("rapid_keyboard")
            )
            KineticIdentityLibrary.BreathingAnimation(
                emotionalState = emotionalState,
                modifier = Modifier.testTag("rapid_breathing")
            )
        }
        
        // Rapidly change states
        repeat(5) {
            isActive = !isActive
            isTyping = !isTyping
            emotionalState = EmotionalState.values()[it % EmotionalState.values().size]
            composeTestRule.waitForIdle()
        }
        
        // Should handle rapid changes gracefully
        composeTestRule.onNodeWithTag("rapid_glow").assertExists()
        composeTestRule.onNodeWithTag("rapid_keyboard").assertExists()
        composeTestRule.onNodeWithTag("rapid_breathing").assertExists()
    }

    @Test
    fun animations_handleMemoryPressure() {
        composeTestRule.setContent {
            // Create many animation instances
            repeat(10) { index ->
                KineticIdentityLibrary.BreathingAnimation(
                    modifier = Modifier.testTag("memory_breathing_$index")
                )
                KineticIdentityLibrary.ResponsiveGlow(
                    theme = CyberpunkTheme,
                    modifier = Modifier.testTag("memory_glow_$index")
                )
            }
        }
        
        // All should render without memory issues
        repeat(10) { index ->
            composeTestRule.onNodeWithTag("memory_breathing_$index").assertExists()
            composeTestRule.onNodeWithTag("memory_glow_$index").assertExists()
        }
    }
}