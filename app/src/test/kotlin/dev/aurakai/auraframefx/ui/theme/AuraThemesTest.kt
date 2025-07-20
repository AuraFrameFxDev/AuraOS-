package dev.aurakai.auraframefx.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Comprehensive unit tests for AuraThemes
 * Testing Framework: JUnit 4 (based on project dependencies using libs.bundles.testing.unit)
 * 
 * Tests cover:
 * - Theme interface implementation validation
 * - Color scheme properties and consistency
 * - Animation style enum behavior
 * - Extension function correctness
 * - Cross-theme comparison and uniqueness
 * - Edge cases and invariants
 * - Color value validation
 */
class AuraThemesTest {

    private lateinit var cyberpunkTheme: AuraTheme
    private lateinit var solarFlareTheme: AuraTheme
    private lateinit var forestTheme: AuraTheme

    @Before
    fun setup() {
        cyberpunkTheme = CyberpunkTheme
        solarFlareTheme = SolarFlareTheme
        forestTheme = ForestTheme
    }

    // === CyberpunkTheme Tests ===

    @Test
    fun `CyberpunkTheme should have correct basic properties`() {
        assertEquals("Cyberpunk", cyberpunkTheme.name)
        assertEquals("High-energy neon aesthetics for a futuristic feel", cyberpunkTheme.description)
        assertEquals(Color(0xFF00FFFF), cyberpunkTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.ENERGETIC, cyberpunkTheme.animationStyle)
    }

    @Test
    fun `CyberpunkTheme light color scheme should have neon cyberpunk colors`() {
        val lightScheme = cyberpunkTheme.lightColorScheme
        
        assertEquals(Color(0xFF00FFFF), lightScheme.primary)
        assertEquals(Color(0xFF000000), lightScheme.onPrimary)
        assertEquals(Color(0xFF004D4D), lightScheme.primaryContainer)
        assertEquals(Color(0xFF00FFFF), lightScheme.onPrimaryContainer)
        assertEquals(Color(0xFFFF0080), lightScheme.secondary)
        assertEquals(Color(0xFF000000), lightScheme.onSecondary)
        assertEquals(Color(0xFF4D0026), lightScheme.secondaryContainer)
        assertEquals(Color(0xFFFF0080), lightScheme.onSecondaryContainer)
        assertEquals(Color(0xFF8000FF), lightScheme.tertiary)
        assertEquals(Color(0xFF000000), lightScheme.onTertiary)
        assertEquals(Color(0xFF26004D), lightScheme.tertiaryContainer)
        assertEquals(Color(0xFF8000FF), lightScheme.onTertiaryContainer)
        assertEquals(Color(0xFF0A0A0A), lightScheme.background)
        assertEquals(Color(0xFF00FFFF), lightScheme.onBackground)
        assertEquals(Color(0xFF1A1A1A), lightScheme.surface)
        assertEquals(Color(0xFF00FFFF), lightScheme.onSurface)
    }

    @Test
    fun `CyberpunkTheme dark color scheme should have cyberpunk colors with darker background`() {
        val darkScheme = cyberpunkTheme.darkColorScheme
        
        assertEquals(Color(0xFF00FFFF), darkScheme.primary)
        assertEquals(Color(0xFF000000), darkScheme.onPrimary)
        assertEquals(Color(0xFF004D4D), darkScheme.primaryContainer)
        assertEquals(Color(0xFF00FFFF), darkScheme.onPrimaryContainer)
        assertEquals(Color(0xFFFF0080), darkScheme.secondary)
        assertEquals(Color(0xFF000000), darkScheme.onSecondary)
        assertEquals(Color(0xFF4D0026), darkScheme.secondaryContainer)
        assertEquals(Color(0xFFFF0080), darkScheme.onSecondaryContainer)
        assertEquals(Color(0xFF8000FF), darkScheme.tertiary)
        assertEquals(Color(0xFF000000), darkScheme.onTertiary)
        assertEquals(Color(0xFF26004D), darkScheme.tertiaryContainer)
        assertEquals(Color(0xFF8000FF), darkScheme.onTertiaryContainer)
        assertEquals(Color(0xFF000000), darkScheme.background)
        assertEquals(Color(0xFF00FFFF), darkScheme.onBackground)
        assertEquals(Color(0xFF0A0A0A), darkScheme.surface)
        assertEquals(Color(0xFF00FFFF), darkScheme.onSurface)
    }

    @Test
    fun `CyberpunkTheme should maintain neon accent color consistency`() {
        val accentColor = cyberpunkTheme.accentColor
        val lightPrimary = cyberpunkTheme.lightColorScheme.primary
        val darkPrimary = cyberpunkTheme.darkColorScheme.primary
        
        assertEquals(accentColor, lightPrimary)
        assertEquals(accentColor, darkPrimary)
    }

    // === SolarFlareTheme Tests ===

    @Test
    fun `SolarFlareTheme should have correct basic properties`() {
        assertEquals("Solar Flare", solarFlareTheme.name)
        assertEquals("Warm, energizing colors to brighten your day", solarFlareTheme.description)
        assertEquals(Color(0xFFFFB000), solarFlareTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.PULSING, solarFlareTheme.animationStyle)
    }

    @Test
    fun `SolarFlareTheme light color scheme should have warm energizing colors`() {
        val lightScheme = solarFlareTheme.lightColorScheme
        
        assertEquals(Color(0xFFFFB000), lightScheme.primary)
        assertEquals(Color(0xFF000000), lightScheme.onPrimary)
        assertEquals(Color(0xFFFFE0B3), lightScheme.primaryContainer)
        assertEquals(Color(0xFF4D3300), lightScheme.onPrimaryContainer)
        assertEquals(Color(0xFFFF6B35), lightScheme.secondary)
        assertEquals(Color(0xFF000000), lightScheme.onSecondary)
        assertEquals(Color(0xFFFFD6CC), lightScheme.secondaryContainer)
        assertEquals(Color(0xFF4D1A0F), lightScheme.onSecondaryContainer)
        assertEquals(Color(0xFFFFD700), lightScheme.tertiary)
        assertEquals(Color(0xFF000000), lightScheme.onTertiary)
        assertEquals(Color(0xFFFFF5B3), lightScheme.tertiaryContainer)
        assertEquals(Color(0xFF4D4000), lightScheme.onTertiaryContainer)
        assertEquals(Color(0xFFFFFBF5), lightScheme.background)
        assertEquals(Color(0xFF4D3300), lightScheme.onBackground)
        assertEquals(Color(0xFFFFF8F0), lightScheme.surface)
        assertEquals(Color(0xFF4D3300), lightScheme.onSurface)
    }

    @Test
    fun `SolarFlareTheme dark color scheme should have warm colors with darker background`() {
        val darkScheme = solarFlareTheme.darkColorScheme
        
        assertEquals(Color(0xFFFFB000), darkScheme.primary)
        assertEquals(Color(0xFF000000), darkScheme.onPrimary)
        assertEquals(Color(0xFF664400), darkScheme.primaryContainer)
        assertEquals(Color(0xFFFFE0B3), darkScheme.onPrimaryContainer)
        assertEquals(Color(0xFFFF6B35), darkScheme.secondary)
        assertEquals(Color(0xFF000000), darkScheme.onSecondary)
        assertEquals(Color(0xFF661A0F), darkScheme.secondaryContainer)
        assertEquals(Color(0xFFFFD6CC), darkScheme.onSecondaryContainer)
        assertEquals(Color(0xFFFFD700), darkScheme.tertiary)
        assertEquals(Color(0xFF000000), darkScheme.onTertiary)
        assertEquals(Color(0xFF664400), darkScheme.tertiaryContainer)
        assertEquals(Color(0xFFFFF5B3), darkScheme.onTertiaryContainer)
        assertEquals(Color(0xFF1A1000), darkScheme.background)
        assertEquals(Color(0xFFFFE0B3), darkScheme.onBackground)
        assertEquals(Color(0xFF2D1F00), darkScheme.surface)
        assertEquals(Color(0xFFFFE0B3), darkScheme.onSurface)
    }

    @Test
    fun `SolarFlareTheme should have golden orange accent color consistency`() {
        val accentColor = solarFlareTheme.accentColor
        val lightPrimary = solarFlareTheme.lightColorScheme.primary
        val darkPrimary = solarFlareTheme.darkColorScheme.primary
        
        assertEquals(accentColor, lightPrimary)
        assertEquals(accentColor, darkPrimary)
    }

    // === ForestTheme Tests ===

    @Test
    fun `ForestTheme should have correct basic properties`() {
        assertEquals("Forest", forestTheme.name)
        assertEquals("Natural, calming colors for peace and focus", forestTheme.description)
        assertEquals(Color(0xFF4CAF50), forestTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.FLOWING, forestTheme.animationStyle)
    }

    @Test
    fun `ForestTheme light color scheme should have natural calming colors`() {
        val lightScheme = forestTheme.lightColorScheme
        
        assertEquals(Color(0xFF4CAF50), lightScheme.primary)
        assertEquals(Color(0xFFFFFFFF), lightScheme.onPrimary)
        assertEquals(Color(0xFFC8E6C9), lightScheme.primaryContainer)
        assertEquals(Color(0xFF1B5E20), lightScheme.onPrimaryContainer)
        assertEquals(Color(0xFF8BC34A), lightScheme.secondary)
        assertEquals(Color(0xFF000000), lightScheme.onSecondary)
        assertEquals(Color(0xFFDCEDC8), lightScheme.secondaryContainer)
        assertEquals(Color(0xFF33691E), lightScheme.onSecondaryContainer)
        assertEquals(Color(0xFF795548), lightScheme.tertiary)
        assertEquals(Color(0xFFFFFFFF), lightScheme.onTertiary)
        assertEquals(Color(0xFFD7CCC8), lightScheme.tertiaryContainer)
        assertEquals(Color(0xFF3E2723), lightScheme.onTertiaryContainer)
        assertEquals(Color(0xFFF1F8E9), lightScheme.background)
        assertEquals(Color(0xFF1B5E20), lightScheme.onBackground)
        assertEquals(Color(0xFFF8FFF8), lightScheme.surface)
        assertEquals(Color(0xFF1B5E20), lightScheme.onSurface)
    }

    @Test
    fun `ForestTheme dark color scheme should have natural colors with darker background`() {
        val darkScheme = forestTheme.darkColorScheme
        
        assertEquals(Color(0xFF4CAF50), darkScheme.primary)
        assertEquals(Color(0xFF000000), darkScheme.onPrimary)
        assertEquals(Color(0xFF2E7D32), darkScheme.primaryContainer)
        assertEquals(Color(0xFFC8E6C9), darkScheme.onPrimaryContainer)
        assertEquals(Color(0xFF8BC34A), darkScheme.secondary)
        assertEquals(Color(0xFF000000), darkScheme.onSecondary)
        assertEquals(Color(0xFF558B2F), darkScheme.secondaryContainer)
        assertEquals(Color(0xFFDCEDC8), darkScheme.onSecondaryContainer)
        assertEquals(Color(0xFF795548), darkScheme.tertiary)
        assertEquals(Color(0xFFFFFFFF), darkScheme.onTertiary)
        assertEquals(Color(0xFF5D4037), darkScheme.tertiaryContainer)
        assertEquals(Color(0xFFD7CCC8), darkScheme.onTertiaryContainer)
        assertEquals(Color(0xFF0D1F0D), darkScheme.background)
        assertEquals(Color(0xFFC8E6C9), darkScheme.onBackground)
        assertEquals(Color(0xFF1A2E1A), darkScheme.surface)
        assertEquals(Color(0xFFC8E6C9), darkScheme.onSurface)
    }

    @Test
    fun `ForestTheme should have forest green accent color consistency`() {
        val accentColor = forestTheme.accentColor
        val lightPrimary = forestTheme.lightColorScheme.primary
        val darkPrimary = forestTheme.darkColorScheme.primary
        
        assertEquals(accentColor, lightPrimary)
        assertEquals(accentColor, darkPrimary)
    }

    // === Animation Style Enum Tests ===

    @Test
    fun `AnimationStyle enum should contain all expected values`() {
        val animationStyles = AuraTheme.AnimationStyle.values()
        
        assertEquals(5, animationStyles.size)
        assertTrue(animationStyles.contains(AuraTheme.AnimationStyle.SUBTLE))
        assertTrue(animationStyles.contains(AuraTheme.AnimationStyle.ENERGETIC))
        assertTrue(animationStyles.contains(AuraTheme.AnimationStyle.CALMING))
        assertTrue(animationStyles.contains(AuraTheme.AnimationStyle.PULSING))
        assertTrue(animationStyles.contains(AuraTheme.AnimationStyle.FLOWING))
    }

    @Test
    fun `AnimationStyle should have correct valueOf behavior`() {
        assertEquals(AuraTheme.AnimationStyle.SUBTLE, AuraTheme.AnimationStyle.valueOf("SUBTLE"))
        assertEquals(AuraTheme.AnimationStyle.ENERGETIC, AuraTheme.AnimationStyle.valueOf("ENERGETIC"))
        assertEquals(AuraTheme.AnimationStyle.CALMING, AuraTheme.AnimationStyle.valueOf("CALMING"))
        assertEquals(AuraTheme.AnimationStyle.PULSING, AuraTheme.AnimationStyle.valueOf("PULSING"))
        assertEquals(AuraTheme.AnimationStyle.FLOWING, AuraTheme.AnimationStyle.valueOf("FLOWING"))
    }

    @Test
    fun `AnimationStyle should match theme assignments correctly`() {
        assertEquals(AuraTheme.AnimationStyle.ENERGETIC, cyberpunkTheme.animationStyle)
        assertEquals(AuraTheme.AnimationStyle.PULSING, solarFlareTheme.animationStyle)
        assertEquals(AuraTheme.AnimationStyle.FLOWING, forestTheme.animationStyle)
    }

    @Test
    fun `AnimationStyle enum should maintain ordinal consistency`() {
        assertEquals(0, AuraTheme.AnimationStyle.SUBTLE.ordinal)
        assertEquals(1, AuraTheme.AnimationStyle.ENERGETIC.ordinal)
        assertEquals(2, AuraTheme.AnimationStyle.CALMING.ordinal)
        assertEquals(3, AuraTheme.AnimationStyle.PULSING.ordinal)
        assertEquals(4, AuraTheme.AnimationStyle.FLOWING.ordinal)
    }

    @Test
    fun `AnimationStyle should support toString for debugging`() {
        assertEquals("SUBTLE", AuraTheme.AnimationStyle.SUBTLE.toString())
        assertEquals("ENERGETIC", AuraTheme.AnimationStyle.ENERGETIC.toString())
        assertEquals("CALMING", AuraTheme.AnimationStyle.CALMING.toString())
        assertEquals("PULSING", AuraTheme.AnimationStyle.PULSING.toString())
        assertEquals("FLOWING", AuraTheme.AnimationStyle.FLOWING.toString())
    }

    // === Extension Function Tests ===

    @Test
    fun `getColorScheme extension should return light scheme when isDarkTheme is false`() {
        val cyberpunkLightScheme = cyberpunkTheme.getColorScheme(false)
        val solarFlareLightScheme = solarFlareTheme.getColorScheme(false)
        val forestLightScheme = forestTheme.getColorScheme(false)
        
        assertEquals(cyberpunkTheme.lightColorScheme, cyberpunkLightScheme)
        assertEquals(solarFlareTheme.lightColorScheme, solarFlareLightScheme)
        assertEquals(forestTheme.lightColorScheme, forestLightScheme)
    }

    @Test
    fun `getColorScheme extension should return dark scheme when isDarkTheme is true`() {
        val cyberpunkDarkScheme = cyberpunkTheme.getColorScheme(true)
        val solarFlareDarkScheme = solarFlareTheme.getColorScheme(true)
        val forestDarkScheme = forestTheme.getColorScheme(true)
        
        assertEquals(cyberpunkTheme.darkColorScheme, cyberpunkDarkScheme)
        assertEquals(solarFlareTheme.darkColorScheme, solarFlareDarkScheme)
        assertEquals(forestTheme.darkColorScheme, forestDarkScheme)
    }

    @Test
    fun `getColorScheme extension should handle multiple calls consistently`() {
        // Test that multiple calls return the same object
        val theme = cyberpunkTheme
        val lightScheme1 = theme.getColorScheme(false)
        val lightScheme2 = theme.getColorScheme(false)
        val darkScheme1 = theme.getColorScheme(true)
        val darkScheme2 = theme.getColorScheme(true)
        
        assertEquals(lightScheme1, lightScheme2)
        assertEquals(darkScheme1, darkScheme2)
    }

    @Test
    fun `getColorScheme extension should handle edge case boolean values`() {
        val theme = solarFlareTheme
        
        // Test with explicit true/false
        val explicitDark = theme.getColorScheme(true)
        val explicitLight = theme.getColorScheme(false)
        
        assertEquals(theme.darkColorScheme, explicitDark)
        assertEquals(theme.lightColorScheme, explicitLight)
    }

    // === Cross-Theme Comparison Tests ===

    @Test
    fun `themes should have unique names`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        val names = themes.map { it.name }
        
        assertEquals(3, names.toSet().size)
        assertNotEquals(cyberpunkTheme.name, solarFlareTheme.name)
        assertNotEquals(solarFlareTheme.name, forestTheme.name)
        assertNotEquals(forestTheme.name, cyberpunkTheme.name)
    }

    @Test
    fun `themes should have unique accent colors`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        val accentColors = themes.map { it.accentColor }
        
        assertEquals(3, accentColors.toSet().size)
        assertNotEquals(cyberpunkTheme.accentColor, solarFlareTheme.accentColor)
        assertNotEquals(solarFlareTheme.accentColor, forestTheme.accentColor)
        assertNotEquals(forestTheme.accentColor, cyberpunkTheme.accentColor)
    }

    @Test
    fun `themes should have different animation styles`() {
        assertNotEquals(cyberpunkTheme.animationStyle, solarFlareTheme.animationStyle)
        assertNotEquals(solarFlareTheme.animationStyle, forestTheme.animationStyle)
        assertNotEquals(forestTheme.animationStyle, cyberpunkTheme.animationStyle)
    }

    @Test
    fun `themes should have non-empty descriptions`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            assertFalse("Theme ${theme.name} should have a non-empty description", 
                       theme.description.isBlank())
            assertTrue("Theme ${theme.name} description should be meaningful", 
                      theme.description.length > 10)
        }
    }

    @Test
    fun `themes should have distinct color palettes`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        // Check that primary colors are all different
        val lightPrimaries = themes.map { it.lightColorScheme.primary }
        val darkPrimaries = themes.map { it.darkColorScheme.primary }
        
        assertEquals(3, lightPrimaries.toSet().size)
        assertEquals(3, darkPrimaries.toSet().size)
        
        // Check that background colors are different
        val lightBackgrounds = themes.map { it.lightColorScheme.background }
        val darkBackgrounds = themes.map { it.darkColorScheme.background }
        
        assertEquals(3, lightBackgrounds.toSet().size)
        assertEquals(3, darkBackgrounds.toSet().size)
    }

    @Test
    fun `themes should exhibit different mood characteristics`() {
        // Test that themes represent different emotional contexts
        val cyberpunkDescription = cyberpunkTheme.description.lowercase()
        val solarFlareDescription = solarFlareTheme.description.lowercase()
        val forestDescription = forestTheme.description.lowercase()
        
        assertTrue("Cyberpunk should suggest energy", 
                  cyberpunkDescription.contains("energy") || cyberpunkDescription.contains("futuristic"))
        assertTrue("Solar Flare should suggest warmth", 
                  solarFlareDescription.contains("warm") || solarFlareDescription.contains("energizing"))
        assertTrue("Forest should suggest calm", 
                  forestDescription.contains("calm") || forestDescription.contains("peace"))
    }

    // === Color Scheme Invariant Tests ===

    @Test
    fun `color schemes should have valid Material3 ColorScheme objects`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            assertNotNull("Light color scheme should not be null for ${theme.name}", 
                         theme.lightColorScheme)
            assertNotNull("Dark color scheme should not be null for ${theme.name}", 
                         theme.darkColorScheme)
            
            // Verify that color schemes are proper ColorScheme instances
            assertTrue("Light scheme should be ColorScheme for ${theme.name}", 
                      theme.lightColorScheme is ColorScheme)
            assertTrue("Dark scheme should be ColorScheme for ${theme.name}", 
                      theme.darkColorScheme is ColorScheme)
        }
    }

    @Test
    fun `color schemes primary colors should match accent colors`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            assertEquals("Light primary should match accent for ${theme.name}",
                        theme.accentColor, theme.lightColorScheme.primary)
            assertEquals("Dark primary should match accent for ${theme.name}",
                        theme.accentColor, theme.darkColorScheme.primary)
        }
    }

    @Test
    fun `color schemes dark backgrounds should be darker than light backgrounds`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            val lightBg = theme.lightColorScheme.background
            val darkBg = theme.darkColorScheme.background
            
            // Simple luminance check by comparing alpha and color components
            // Dark themes should generally have lower RGB values
            assertTrue("Dark background should be darker than light background for ${theme.name}",
                      isDarkerColor(darkBg, lightBg))
        }
    }

    @Test
    fun `color schemes should have proper contrast relationships`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            val lightScheme = theme.lightColorScheme
            val darkScheme = theme.darkColorScheme
            
            // Verify that "on" colors provide contrast to their base colors
            assertNotEquals("Primary and onPrimary should be different in light scheme for ${theme.name}",
                           lightScheme.primary, lightScheme.onPrimary)
            assertNotEquals("Primary and onPrimary should be different in dark scheme for ${theme.name}",
                           darkScheme.primary, darkScheme.onPrimary)
            
            assertNotEquals("Background and onBackground should be different in light scheme for ${theme.name}",
                           lightScheme.background, lightScheme.onBackground)
            assertNotEquals("Background and onBackground should be different in dark scheme for ${theme.name}",
                           darkScheme.background, darkScheme.onBackground)
        }
    }

    // === Edge Cases and Error Conditions ===

    @Test
    fun `themes should handle object singleton behavior correctly`() {
        val cyberpunk1 = CyberpunkTheme
        val cyberpunk2 = CyberpunkTheme
        val solarFlare1 = SolarFlareTheme
        val solarFlare2 = SolarFlareTheme
        val forest1 = ForestTheme
        val forest2 = ForestTheme
        
        // Objects should be the same instance (singleton behavior)
        assertSame("CyberpunkTheme should be singleton", cyberpunk1, cyberpunk2)
        assertSame("SolarFlareTheme should be singleton", solarFlare1, solarFlare2)
        assertSame("ForestTheme should be singleton", forest1, forest2)
    }

    @Test
    fun `color values should be valid ARGB colors`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            // Test accent color
            assertValidColor(theme.accentColor, "${theme.name} accent color")
            
            // Test some key colors from light scheme
            assertValidColor(theme.lightColorScheme.primary, "${theme.name} light primary")
            assertValidColor(theme.lightColorScheme.background, "${theme.name} light background")
            assertValidColor(theme.lightColorScheme.surface, "${theme.name} light surface")
            
            // Test some key colors from dark scheme
            assertValidColor(theme.darkColorScheme.primary, "${theme.name} dark primary")
            assertValidColor(theme.darkColorScheme.background, "${theme.name} dark background")
            assertValidColor(theme.darkColorScheme.surface, "${theme.name} dark surface")
        }
    }

    @Test
    fun `color schemes should have all required Material3 colors defined`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            val lightScheme = theme.lightColorScheme
            val darkScheme = theme.darkColorScheme
            
            // Verify all primary color properties are non-null and valid
            assertValidColor(lightScheme.primary, "${theme.name} light primary")
            assertValidColor(lightScheme.onPrimary, "${theme.name} light onPrimary")
            assertValidColor(lightScheme.primaryContainer, "${theme.name} light primaryContainer")
            assertValidColor(lightScheme.onPrimaryContainer, "${theme.name} light onPrimaryContainer")
            
            assertValidColor(darkScheme.primary, "${theme.name} dark primary")
            assertValidColor(darkScheme.onPrimary, "${theme.name} dark onPrimary")
            assertValidColor(darkScheme.primaryContainer, "${theme.name} dark primaryContainer")
            assertValidColor(darkScheme.onPrimaryContainer, "${theme.name} dark onPrimaryContainer")
            
            // Verify secondary colors
            assertValidColor(lightScheme.secondary, "${theme.name} light secondary")
            assertValidColor(lightScheme.onSecondary, "${theme.name} light onSecondary")
            assertValidColor(darkScheme.secondary, "${theme.name} dark secondary")
            assertValidColor(darkScheme.onSecondary, "${theme.name} dark onSecondary")
            
            // Verify surface colors
            assertValidColor(lightScheme.surface, "${theme.name} light surface")
            assertValidColor(lightScheme.onSurface, "${theme.name} light onSurface")
            assertValidColor(darkScheme.surface, "${theme.name} dark surface")
            assertValidColor(darkScheme.onSurface, "${theme.name} dark onSurface")
        }
    }

    // === Interface Implementation Tests ===

    @Test
    fun `all themes should properly implement AuraTheme interface`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            assertTrue("${theme.name} should implement AuraTheme", theme is AuraTheme)
            
            // Verify all interface properties are accessible
            assertNotNull("${theme.name} name should not be null", theme.name)
            assertNotNull("${theme.name} description should not be null", theme.description)
            assertNotNull("${theme.name} accentColor should not be null", theme.accentColor)
            assertNotNull("${theme.name} animationStyle should not be null", theme.animationStyle)
            assertNotNull("${theme.name} lightColorScheme should not be null", theme.lightColorScheme)
            assertNotNull("${theme.name} darkColorScheme should not be null", theme.darkColorScheme)
        }
    }

    @Test
    fun `theme names should follow proper naming conventions`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            // Names should not be empty or whitespace only
            assertTrue("${theme.name} should not be blank", theme.name.isNotBlank())
            
            // Names should be reasonable length (not too short or excessively long)
            assertTrue("${theme.name} should be reasonable length", 
                      theme.name.length in 3..50)
            
            // Names should start with uppercase letter (title case convention)
            assertTrue("${theme.name} should start with uppercase", 
                      theme.name.first().isUpperCase())
        }
    }

    // === Stress Testing ===

    @Test
    fun `getColorScheme should handle rapid switching between light and dark modes`() {
        val theme = cyberpunkTheme
        repeat(100) { i ->
            val isDark = i % 2 == 0
            val colorScheme = theme.getColorScheme(isDark)
            val expected = if (isDark) theme.darkColorScheme else theme.lightColorScheme
            assertEquals("Color scheme should be consistent on iteration $i", expected, colorScheme)
        }
    }

    @Test
    fun `all color properties should be accessible without exceptions`() {
        val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
        
        themes.forEach { theme ->
            // Test all color scheme properties without throwing exceptions
            val lightScheme = theme.lightColorScheme
            val darkScheme = theme.darkColorScheme
            
            // Access all properties to ensure they don't throw exceptions
            arrayOf(lightScheme, darkScheme).forEach { scheme ->
                assertNotNull(scheme.primary)
                assertNotNull(scheme.onPrimary)
                assertNotNull(scheme.primaryContainer)
                assertNotNull(scheme.onPrimaryContainer)
                assertNotNull(scheme.secondary)
                assertNotNull(scheme.onSecondary)
                assertNotNull(scheme.secondaryContainer)
                assertNotNull(scheme.onSecondaryContainer)
                assertNotNull(scheme.tertiary)
                assertNotNull(scheme.onTertiary)
                assertNotNull(scheme.tertiaryContainer)
                assertNotNull(scheme.onTertiaryContainer)
                assertNotNull(scheme.background)
                assertNotNull(scheme.onBackground)
                assertNotNull(scheme.surface)
                assertNotNull(scheme.onSurface)
            }
        }
    }

    // === Helper Methods ===

    private fun isDarkerColor(color1: Color, color2: Color): Boolean {
        // Simple luminance approximation: 0.299*R + 0.587*G + 0.114*B
        val luminance1 = 0.299 * color1.red + 0.587 * color1.green + 0.114 * color1.blue
        val luminance2 = 0.299 * color2.red + 0.587 * color2.green + 0.114 * color2.blue
        return luminance1 < luminance2
    }

    private fun assertValidColor(color: Color, description: String) {
        // Ensure color components are in valid range [0.0, 1.0]
        assertTrue("$description red component should be valid", 
                  color.red >= 0.0f && color.red <= 1.0f)
        assertTrue("$description green component should be valid", 
                  color.green >= 0.0f && color.green <= 1.0f)
        assertTrue("$description blue component should be valid", 
                  color.blue >= 0.0f && color.blue <= 1.0f)
        assertTrue("$description alpha component should be valid", 
                  color.alpha >= 0.0f && color.alpha <= 1.0f)
    }
}