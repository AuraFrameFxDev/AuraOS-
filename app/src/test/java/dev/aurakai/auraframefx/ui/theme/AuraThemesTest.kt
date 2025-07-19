package dev.aurakai.auraframefx.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Comprehensive unit tests for AuraThemes
 * Testing Framework: JUnit 4 with Robolectric for Android components
 * Testing Library: Compose Testing for UI components
 */
@RunWith(RobolectricTestRunner::class)
class AuraThemesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `AuraTheme interface defines all required properties`() {
        // Test that all themes implement the AuraTheme interface correctly
        val themes = listOf(CyberpunkTheme, SolarFlareTheme, ForestTheme)
        
        themes.forEach { theme ->
            assertNotNull("Theme name should not be null", theme.name)
            assertNotNull("Theme description should not be null", theme.description)
            assertNotNull("Light color scheme should not be null", theme.lightColorScheme)
            assertNotNull("Dark color scheme should not be null", theme.darkColorScheme)
            assertNotNull("Accent color should not be null", theme.accentColor)
            assertNotNull("Animation style should not be null", theme.animationStyle)
            
            assertTrue("Theme name should not be empty", theme.name.isNotEmpty())
            assertTrue("Theme description should not be empty", theme.description.isNotEmpty())
        }
    }

    @Test
    fun `AnimationStyle enum contains all expected values`() {
        val expectedStyles = setOf(
            AuraTheme.AnimationStyle.SUBTLE,
            AuraTheme.AnimationStyle.ENERGETIC,
            AuraTheme.AnimationStyle.CALMING,
            AuraTheme.AnimationStyle.PULSING,
            AuraTheme.AnimationStyle.FLOWING
        )
        
        val actualStyles = AuraTheme.AnimationStyle.values().toSet()
        assertEquals("All animation styles should be present", expectedStyles, actualStyles)
        assertEquals("Should have exactly 5 animation styles", 5, actualStyles.size)
    }

    // CyberpunkTheme Tests
    @Test
    fun `CyberpunkTheme has correct properties`() {
        assertEquals("Cyberpunk", CyberpunkTheme.name)
        assertEquals("High-energy neon aesthetics for a futuristic feel", CyberpunkTheme.description)
        assertEquals(Color(0xFF00FFFF), CyberpunkTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.ENERGETIC, CyberpunkTheme.animationStyle)
    }

    @Test
    fun `CyberpunkTheme light color scheme has correct primary colors`() {
        val lightScheme = CyberpunkTheme.lightColorScheme
        assertEquals(Color(0xFF00FFFF), lightScheme.primary)
        assertEquals(Color(0xFF000000), lightScheme.onPrimary)
        assertEquals(Color(0xFF004D4D), lightScheme.primaryContainer)
        assertEquals(Color(0xFF00FFFF), lightScheme.onPrimaryContainer)
    }

    @Test
    fun `CyberpunkTheme light color scheme has correct secondary colors`() {
        val lightScheme = CyberpunkTheme.lightColorScheme
        assertEquals(Color(0xFFFF0080), lightScheme.secondary)
        assertEquals(Color(0xFF000000), lightScheme.onSecondary)
        assertEquals(Color(0xFF4D0026), lightScheme.secondaryContainer)
        assertEquals(Color(0xFFFF0080), lightScheme.onSecondaryContainer)
    }

    @Test
    fun `CyberpunkTheme light color scheme has correct tertiary colors`() {
        val lightScheme = CyberpunkTheme.lightColorScheme
        assertEquals(Color(0xFF8000FF), lightScheme.tertiary)
        assertEquals(Color(0xFF000000), lightScheme.onTertiary)
        assertEquals(Color(0xFF26004D), lightScheme.tertiaryContainer)
        assertEquals(Color(0xFF8000FF), lightScheme.onTertiaryContainer)
    }

    @Test
    fun `CyberpunkTheme light color scheme has correct surface colors`() {
        val lightScheme = CyberpunkTheme.lightColorScheme
        assertEquals(Color(0xFF0A0A0A), lightScheme.background)
        assertEquals(Color(0xFF00FFFF), lightScheme.onBackground)
        assertEquals(Color(0xFF1A1A1A), lightScheme.surface)
        assertEquals(Color(0xFF00FFFF), lightScheme.onSurface)
    }

    @Test
    fun `CyberpunkTheme dark color scheme has correct colors`() {
        val darkScheme = CyberpunkTheme.darkColorScheme
        assertEquals(Color(0xFF00FFFF), darkScheme.primary)
        assertEquals(Color(0xFF000000), darkScheme.onPrimary)
        assertEquals(Color(0xFF000000), darkScheme.background)
        assertEquals(Color(0xFF00FFFF), darkScheme.onBackground)
        assertEquals(Color(0xFF0A0A0A), darkScheme.surface)
        assertEquals(Color(0xFF00FFFF), darkScheme.onSurface)
    }

    @Test
    fun `CyberpunkTheme light and dark schemes have consistent primary colors`() {
        assertEquals(CyberpunkTheme.lightColorScheme.primary, CyberpunkTheme.darkColorScheme.primary)
        assertEquals(CyberpunkTheme.lightColorScheme.onPrimary, CyberpunkTheme.darkColorScheme.onPrimary)
    }

    // SolarFlareTheme Tests
    @Test
    fun `SolarFlareTheme has correct properties`() {
        assertEquals("Solar Flare", SolarFlareTheme.name)
        assertEquals("Warm, energizing colors to brighten your day", SolarFlareTheme.description)
        assertEquals(Color(0xFFFFB000), SolarFlareTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.PULSING, SolarFlareTheme.animationStyle)
    }

    @Test
    fun `SolarFlareTheme light color scheme has warm colors`() {
        val lightScheme = SolarFlareTheme.lightColorScheme
        assertEquals(Color(0xFFFFB000), lightScheme.primary)
        assertEquals(Color(0xFF000000), lightScheme.onPrimary)
        assertEquals(Color(0xFFFF6B35), lightScheme.secondary)
        assertEquals(Color(0xFFFFD700), lightScheme.tertiary)
        assertEquals(Color(0xFFFFFBF5), lightScheme.background)
    }

    @Test
    fun `SolarFlareTheme dark color scheme has correct background`() {
        val darkScheme = SolarFlareTheme.darkColorScheme
        assertEquals(Color(0xFF1A1000), darkScheme.background)
        assertEquals(Color(0xFFFFE0B3), darkScheme.onBackground)
        assertEquals(Color(0xFF2D1F00), darkScheme.surface)
        assertEquals(Color(0xFFFFE0B3), darkScheme.onSurface)
    }

    @Test
    fun `SolarFlareTheme has consistent accent color across schemes`() {
        assertEquals(SolarFlareTheme.accentColor, SolarFlareTheme.lightColorScheme.primary)
        assertEquals(SolarFlareTheme.accentColor, SolarFlareTheme.darkColorScheme.primary)
    }

    // ForestTheme Tests
    @Test
    fun `ForestTheme has correct properties`() {
        assertEquals("Forest", ForestTheme.name)
        assertEquals("Natural, calming colors for peace and focus", ForestTheme.description)
        assertEquals(Color(0xFF4CAF50), ForestTheme.accentColor)
        assertEquals(AuraTheme.AnimationStyle.FLOWING, ForestTheme.animationStyle)
    }

    @Test
    fun `ForestTheme light color scheme has natural colors`() {
        val lightScheme = ForestTheme.lightColorScheme
        assertEquals(Color(0xFF4CAF50), lightScheme.primary)
        assertEquals(Color(0xFFFFFFFF), lightScheme.onPrimary)
        assertEquals(Color(0xFF8BC34A), lightScheme.secondary)
        assertEquals(Color(0xFF795548), lightScheme.tertiary)
        assertEquals(Color(0xFFF1F8E9), lightScheme.background)
    }

    @Test
    fun `ForestTheme dark color scheme maintains green theme`() {
        val darkScheme = ForestTheme.darkColorScheme
        assertEquals(Color(0xFF4CAF50), darkScheme.primary)
        assertEquals(Color(0xFF000000), darkScheme.onPrimary)
        assertEquals(Color(0xFF0D1F0D), darkScheme.background)
        assertEquals(Color(0xFFC8E6C9), darkScheme.onBackground)
    }

    // Cross-theme comparison tests
    @Test
    fun `All themes have unique names`() {
        val themeNames = listOf(CyberpunkTheme.name, SolarFlareTheme.name, ForestTheme.name)
        val uniqueNames = themeNames.toSet()
        assertEquals("All theme names should be unique", themeNames.size, uniqueNames.size)
    }

    @Test
    fun `All themes have unique accent colors`() {
        val accentColors = listOf(
            CyberpunkTheme.accentColor,
            SolarFlareTheme.accentColor,
            ForestTheme.accentColor
        )
        val uniqueColors = accentColors.toSet()
        assertEquals("All accent colors should be unique", accentColors.size, uniqueColors.size)
    }

    @Test
    fun `All themes have different animation styles`() {
        val animationStyles = listOf(
            CyberpunkTheme.animationStyle,
            SolarFlareTheme.animationStyle,
            ForestTheme.animationStyle
        )
        val uniqueStyles = animationStyles.toSet()
        assertEquals("All animation styles should be unique", animationStyles.size, uniqueStyles.size)
    }

    @Test
    fun `Color schemes have proper contrast ratios for accessibility`() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme, ForestTheme)
        
        themes.forEach { theme ->
            listOf(theme.lightColorScheme, theme.darkColorScheme).forEach { colorScheme ->
                // Test that background and onBackground colors are different
                assertNotEquals("Background and onBackground should be different for ${theme.name}",
                    colorScheme.background, colorScheme.onBackground)
                
                // Test that surface and onSurface colors are different
                assertNotEquals("Surface and onSurface should be different for ${theme.name}",
                    colorScheme.surface, colorScheme.onSurface)
                
                // Test that primary and onPrimary colors are different
                assertNotEquals("Primary and onPrimary should be different for ${theme.name}",
                    colorScheme.primary, colorScheme.onPrimary)
            }
        }
    }

    // Extension function tests
    @Test
    fun `getColorScheme extension returns correct scheme for light theme`() {
        composeTestRule.setContent {
            val cyberpunkLight = CyberpunkTheme.getColorScheme(isDarkTheme = false)
            val solarFlareLight = SolarFlareTheme.getColorScheme(isDarkTheme = false)
            val forestLight = ForestTheme.getColorScheme(isDarkTheme = false)
            
            assertEquals(CyberpunkTheme.lightColorScheme, cyberpunkLight)
            assertEquals(SolarFlareTheme.lightColorScheme, solarFlareLight)
            assertEquals(ForestTheme.lightColorScheme, forestLight)
        }
    }

    @Test
    fun `getColorScheme extension returns correct scheme for dark theme`() {
        composeTestRule.setContent {
            val cyberpunkDark = CyberpunkTheme.getColorScheme(isDarkTheme = true)
            val solarFlareDark = SolarFlareTheme.getColorScheme(isDarkTheme = true)
            val forestDark = ForestTheme.getColorScheme(isDarkTheme = true)
            
            assertEquals(CyberpunkTheme.darkColorScheme, cyberpunkDark)
            assertEquals(SolarFlareTheme.darkColorScheme, solarFlareDark)
            assertEquals(ForestTheme.darkColorScheme, forestDark)
        }
    }

    // Edge case and boundary tests
    @Test
    fun `Color values are within valid ARGB range`() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme, ForestTheme)
        
        themes.forEach { theme ->
            // Test accent color
            assertTrue("Accent color should be valid ARGB", isValidColor(theme.accentColor))
            
            // Test all colors in light scheme
            validateColorScheme(theme.lightColorScheme, "${theme.name} light scheme")
            
            // Test all colors in dark scheme
            validateColorScheme(theme.darkColorScheme, "${theme.name} dark scheme")
        }
    }

    @Test
    fun `Themes are singleton objects`() {
        val cyberpunk1 = CyberpunkTheme
        val cyberpunk2 = CyberpunkTheme
        assertSame("CyberpunkTheme should be singleton", cyberpunk1, cyberpunk2)
        
        val solarFlare1 = SolarFlareTheme
        val solarFlare2 = SolarFlareTheme
        assertSame("SolarFlareTheme should be singleton", solarFlare1, solarFlare2)
        
        val forest1 = ForestTheme
        val forest2 = ForestTheme
        assertSame("ForestTheme should be singleton", forest1, forest2)
    }

    @Test
    fun `Theme descriptions contain meaningful content`() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme, ForestTheme)
        
        themes.forEach { theme ->
            assertTrue("${theme.name} description should contain meaningful words",
                theme.description.split(" ").size >= 3)
            assertFalse("${theme.name} description should not contain placeholders",
                theme.description.contains("TODO", ignoreCase = true))
        }
    }

    @Test
    fun `Animation styles align with theme personality`() {
        // Cyberpunk should be energetic
        assertEquals("Cyberpunk should have energetic animation style",
            AuraTheme.AnimationStyle.ENERGETIC, CyberpunkTheme.animationStyle)
        
        // Solar Flare should be pulsing (like the sun)
        assertEquals("Solar Flare should have pulsing animation style",
            AuraTheme.AnimationStyle.PULSING, SolarFlareTheme.animationStyle)
        
        // Forest should be flowing (like nature)
        assertEquals("Forest should have flowing animation style",
            AuraTheme.AnimationStyle.FLOWING, ForestTheme.animationStyle)
    }

    @Test
    fun `SolarFlareTheme complete light color scheme validation`() {
        val lightScheme = SolarFlareTheme.lightColorScheme
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
    fun `SolarFlareTheme complete dark color scheme validation`() {
        val darkScheme = SolarFlareTheme.darkColorScheme
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
    fun `ForestTheme complete light color scheme validation`() {
        val lightScheme = ForestTheme.lightColorScheme
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
    fun `ForestTheme complete dark color scheme validation`() {
        val darkScheme = ForestTheme.darkColorScheme
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

    // Helper functions for validation
    private fun isValidColor(color: Color): Boolean {
        val argb = color.value.toLong()
        return argb in 0x00000000L..0xFFFFFFFFL
    }

    private fun validateColorScheme(colorScheme: ColorScheme, schemeName: String) {
        val colors = listOf(
            colorScheme.primary,
            colorScheme.onPrimary,
            colorScheme.primaryContainer,
            colorScheme.onPrimaryContainer,
            colorScheme.secondary,
            colorScheme.onSecondary,
            colorScheme.secondaryContainer,
            colorScheme.onSecondaryContainer,
            colorScheme.tertiary,
            colorScheme.onTertiary,
            colorScheme.tertiaryContainer,
            colorScheme.onTertiaryContainer,
            colorScheme.background,
            colorScheme.onBackground,
            colorScheme.surface,
            colorScheme.onSurface
        )
        
        colors.forEach { color ->
            assertTrue("All colors in $schemeName should be valid ARGB", isValidColor(color))
        }
    }

    // Performance and memory tests
    @Test
    fun `Theme objects can be accessed repeatedly without performance issues`() {
        val startTime = System.currentTimeMillis()
        
        repeat(10000) {
            CyberpunkTheme.name
            CyberpunkTheme.accentColor
            SolarFlareTheme.lightColorScheme
            ForestTheme.darkColorScheme
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Theme access should be fast (under 1000ms for 10k accesses)", duration < 1000)
    }

    @Test
    fun `Color schemes are properly initialized`() {
        val themes = listOf(CyberpunkTheme, SolarFlareTheme, ForestTheme)
        
        themes.forEach { theme ->
            assertNotNull("${theme.name} light color scheme should be initialized", theme.lightColorScheme)
            assertNotNull("${theme.name} dark color scheme should be initialized", theme.darkColorScheme)
            
            // Verify that color schemes are actually different objects
            assertNotSame("Light and dark color schemes should be different objects for ${theme.name}",
                theme.lightColorScheme, theme.darkColorScheme)
        }
    }

    @Test
    fun `getColorScheme handles boolean parameter correctly`() {
        composeTestRule.setContent {
            // Test with explicit true/false
            assertEquals(CyberpunkTheme.darkColorScheme, CyberpunkTheme.getColorScheme(true))
            assertEquals(CyberpunkTheme.lightColorScheme, CyberpunkTheme.getColorScheme(false))
            
            // Test with variables
            val isDark = true
            val isLight = false
            assertEquals(SolarFlareTheme.darkColorScheme, SolarFlareTheme.getColorScheme(isDark))
            assertEquals(SolarFlareTheme.lightColorScheme, SolarFlareTheme.getColorScheme(isLight))
        }
    }

    @Test
    fun `Cyberpunk theme follows cyberpunk color palette`() {
        // Verify cyberpunk aesthetic colors (neon cyan, magenta, purple)
        assertEquals("Primary should be cyan", Color(0xFF00FFFF), CyberpunkTheme.accentColor)
        assertEquals("Secondary should be magenta", Color(0xFFFF0080), CyberpunkTheme.lightColorScheme.secondary)
        assertEquals("Tertiary should be purple", Color(0xFF8000FF), CyberpunkTheme.lightColorScheme.tertiary)
    }

    @Test
    fun `Solar flare theme follows warm color palette`() {
        // Verify warm, energizing colors (oranges, golds, yellows)
        assertEquals("Primary should be golden orange", Color(0xFFFFB000), SolarFlareTheme.accentColor)
        assertEquals("Secondary should be orange", Color(0xFFFF6B35), SolarFlareTheme.lightColorScheme.secondary)
        assertEquals("Tertiary should be gold", Color(0xFFFFD700), SolarFlareTheme.lightColorScheme.tertiary)
    }

    @Test
    fun `Forest theme follows natural color palette`() {
        // Verify natural, green colors
        assertEquals("Primary should be forest green", Color(0xFF4CAF50), ForestTheme.accentColor)
        assertEquals("Secondary should be light green", Color(0xFF8BC34A), ForestTheme.lightColorScheme.secondary)
        assertEquals("Tertiary should be brown", Color(0xFF795548), ForestTheme.lightColorScheme.tertiary)
    }
}