package dev.aurakai.auraframefx.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Comprehensive unit tests for AuraTheme implementations.
 * 
 * Testing Framework: JUnit Jupiter (JUnit 5)
 * Focus: Theme properties, color schemes, and extension functions
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("AuraThemes Test Suite")
class AuraThemesTest {

    // Test subjects
    private lateinit var cyberpunkTheme: AuraTheme
    private lateinit var solarFlareTheme: AuraTheme
    private lateinit var forestTheme: AuraTheme

    @BeforeEach
    fun setUp() {
        cyberpunkTheme = CyberpunkTheme
        solarFlareTheme = SolarFlareTheme
        forestTheme = ForestTheme
    }

    @AfterEach
    fun tearDown() {
        // No cleanup needed for object themes
    }

    @Nested
    @DisplayName("CyberpunkTheme Tests")
    inner class CyberpunkThemeTests {

        @Test
        @DisplayName("Should have correct basic properties")
        fun shouldHaveCorrectBasicProperties() {
            assertEquals("Cyberpunk", cyberpunkTheme.name)
            assertEquals("High-energy neon aesthetics for a futuristic feel", cyberpunkTheme.description)
            assertEquals(Color(0xFF00FFFF), cyberpunkTheme.accentColor)
            assertEquals(AuraTheme.AnimationStyle.ENERGETIC, cyberpunkTheme.animationStyle)
        }

        @Test
        @DisplayName("Light color scheme should have neon aesthetics")
        fun lightColorSchemeShouldHaveNeonAesthetics() {
            val lightScheme = cyberpunkTheme.lightColorScheme
            
            // Test primary colors - should be cyan neon
            assertEquals(Color(0xFF00FFFF), lightScheme.primary)
            assertEquals(Color(0xFF000000), lightScheme.onPrimary)
            assertEquals(Color(0xFF004D4D), lightScheme.primaryContainer)
            assertEquals(Color(0xFF00FFFF), lightScheme.onPrimaryContainer)
            
            // Test secondary colors - should be magenta/pink neon
            assertEquals(Color(0xFFFF0080), lightScheme.secondary)
            assertEquals(Color(0xFF000000), lightScheme.onSecondary)
            
            // Test background colors - should be dark
            assertEquals(Color(0xFF0A0A0A), lightScheme.background)
            assertEquals(Color(0xFF00FFFF), lightScheme.onBackground)
        }

        @Test
        @DisplayName("Dark color scheme should maintain neon contrast")
        fun darkColorSchemeShouldMaintainNeonContrast() {
            val darkScheme = cyberpunkTheme.darkColorScheme
            
            // Test that dark scheme maintains neon primary colors
            assertEquals(Color(0xFF00FFFF), darkScheme.primary)
            assertEquals(Color(0xFF000000), darkScheme.onPrimary)
            
            // Test that background is even darker in dark mode
            assertEquals(Color(0xFF000000), darkScheme.background)
            assertEquals(Color(0xFF00FFFF), darkScheme.onBackground)
            
            // Test surface colors
            assertEquals(Color(0xFF0A0A0A), darkScheme.surface)
            assertEquals(Color(0xFF00FFFF), darkScheme.onSurface)
        }

        @Test
        @DisplayName("Should have high contrast for accessibility")
        fun shouldHaveHighContrastForAccessibility() {
            val lightScheme = cyberpunkTheme.lightColorScheme
            val darkScheme = cyberpunkTheme.darkColorScheme
            
            // Verify high contrast combinations exist
            assertNotEquals(lightScheme.primary, lightScheme.onPrimary)
            assertNotEquals(lightScheme.background, lightScheme.onBackground)
            assertNotEquals(darkScheme.primary, darkScheme.onPrimary)
            assertNotEquals(darkScheme.background, darkScheme.onBackground)
        }

        @Test
        @DisplayName("Should use colors that convey energy and futurism")
        fun shouldUseColorsThatConveyEnergyAndFuturism() {
            val lightScheme = cyberpunkTheme.lightColorScheme
            val darkScheme = cyberpunkTheme.darkColorScheme
            
            // Cyan (0xFF00FFFF) is associated with technology and energy
            assertEquals(Color(0xFF00FFFF), lightScheme.primary)
            assertEquals(Color(0xFF00FFFF), darkScheme.primary)
            
            // Magenta (0xFFFF0080) creates high contrast and energy
            assertEquals(Color(0xFFFF0080), lightScheme.secondary)
            assertEquals(Color(0xFFFF0080), darkScheme.secondary)
            
            // Purple (0xFF8000FF) completes the neon triad
            assertEquals(Color(0xFF8000FF), lightScheme.tertiary)
            assertEquals(Color(0xFF8000FF), darkScheme.tertiary)
        }
    }

    @Nested
    @DisplayName("SolarFlareTheme Tests")
    inner class SolarFlareThemeTests {

        @Test
        @DisplayName("Should have correct basic properties")
        fun shouldHaveCorrectBasicProperties() {
            assertEquals("Solar Flare", solarFlareTheme.name)
            assertEquals("Warm, energizing colors to brighten your day", solarFlareTheme.description)
            assertEquals(Color(0xFFFFB000), solarFlareTheme.accentColor)
            assertEquals(AuraTheme.AnimationStyle.PULSING, solarFlareTheme.animationStyle)
        }

        @Test
        @DisplayName("Light color scheme should have warm tones")
        fun lightColorSchemeShouldHaveWarmTones() {
            val lightScheme = solarFlareTheme.lightColorScheme
            
            // Test primary colors - should be golden orange
            assertEquals(Color(0xFFFFB000), lightScheme.primary)
            assertEquals(Color(0xFF000000), lightScheme.onPrimary)
            assertEquals(Color(0xFFFFE0B3), lightScheme.primaryContainer)
            assertEquals(Color(0xFF4D3300), lightScheme.onPrimaryContainer)
            
            // Test secondary colors - should be warm orange
            assertEquals(Color(0xFFFF6B35), lightScheme.secondary)
            assertEquals(Color(0xFF000000), lightScheme.onSecondary)
            
            // Test background - should be light and warm
            assertEquals(Color(0xFFFFFBF5), lightScheme.background)
            assertEquals(Color(0xFF4D3300), lightScheme.onBackground)
        }

        @Test
        @DisplayName("Dark color scheme should maintain warmth")
        fun darkColorSchemeShouldMaintainWarmth() {
            val darkScheme = solarFlareTheme.darkColorScheme
            
            // Test that warm colors are preserved in dark mode
            assertEquals(Color(0xFFFFB000), darkScheme.primary)
            assertEquals(Color(0xFF000000), darkScheme.onPrimary)
            
            // Test that background is appropriately dark but warm-toned
            assertEquals(Color(0xFF1A1000), darkScheme.background)
            assertEquals(Color(0xFFFFE0B3), darkScheme.onBackground)
            
            // Test container colors maintain warm contrast
            assertEquals(Color(0xFF664400), darkScheme.primaryContainer)
            assertEquals(Color(0xFFFFE0B3), darkScheme.onPrimaryContainer)
        }

        @Test
        @DisplayName("Should use consistent warm color palette")
        fun shouldUseConsistentWarmColorPalette() {
            val lightScheme = solarFlareTheme.lightColorScheme
            val darkScheme = solarFlareTheme.darkColorScheme
            
            // Verify tertiary color is golden yellow in both schemes
            assertEquals(Color(0xFFFFD700), lightScheme.tertiary)
            assertEquals(Color(0xFFFFD700), darkScheme.tertiary)
            
            // Verify accent color matches primary
            assertEquals(solarFlareTheme.accentColor, lightScheme.primary)
            assertEquals(solarFlareTheme.accentColor, darkScheme.primary)
        }

        @Test
        @DisplayName("Should use warm colors that evoke energy and positivity")
        fun shouldUseWarmColorsThatEvokeEnergyAndPositivity() {
            val lightScheme = solarFlareTheme.lightColorScheme
            val darkScheme = solarFlareTheme.darkColorScheme
            
            // Golden orange (0xFFFFB000) represents warmth and energy
            assertEquals(Color(0xFFFFB000), lightScheme.primary)
            assertEquals(Color(0xFFFFB000), darkScheme.primary)
            
            // Bright orange (0xFFFF6B35) adds vibrancy
            assertEquals(Color(0xFFFF6B35), lightScheme.secondary)
            assertEquals(Color(0xFFFF6B35), darkScheme.secondary)
            
            // Golden yellow (0xFFFFD700) completes the warm palette
            assertEquals(Color(0xFFFFD700), lightScheme.tertiary)
            assertEquals(Color(0xFFFFD700), darkScheme.tertiary)
        }
    }

    @Nested
    @DisplayName("ForestTheme Tests")
    inner class ForestThemeTests {

        @Test
        @DisplayName("Should have correct basic properties")
        fun shouldHaveCorrectBasicProperties() {
            assertEquals("Forest", forestTheme.name)
            assertEquals("Natural, calming colors for peace and focus", forestTheme.description)
            assertEquals(Color(0xFF4CAF50), forestTheme.accentColor)
            assertEquals(AuraTheme.AnimationStyle.FLOWING, forestTheme.animationStyle)
        }

        @Test
        @DisplayName("Light color scheme should have natural tones")
        fun lightColorSchemeShouldHaveNaturalTones() {
            val lightScheme = forestTheme.lightColorScheme
            
            // Test primary colors - should be forest green
            assertEquals(Color(0xFF4CAF50), lightScheme.primary)
            assertEquals(Color(0xFFFFFFFF), lightScheme.onPrimary)
            assertEquals(Color(0xFFC8E6C9), lightScheme.primaryContainer)
            assertEquals(Color(0xFF1B5E20), lightScheme.onPrimaryContainer)
            
            // Test secondary colors - should be lighter green
            assertEquals(Color(0xFF8BC34A), lightScheme.secondary)
            assertEquals(Color(0xFF000000), lightScheme.onSecondary)
            
            // Test tertiary colors - should be brown/earth tone
            assertEquals(Color(0xFF795548), lightScheme.tertiary)
            assertEquals(Color(0xFFFFFFFF), lightScheme.onTertiary)
            
            // Test background - should be very light green
            assertEquals(Color(0xFFF1F8E9), lightScheme.background)
            assertEquals(Color(0xFF1B5E20), lightScheme.onBackground)
        }

        @Test
        @DisplayName("Dark color scheme should maintain natural feel")
        fun darkColorSchemeShouldMaintainNaturalFeel() {
            val darkScheme = forestTheme.darkColorScheme
            
            // Test that green colors are preserved in dark mode
            assertEquals(Color(0xFF4CAF50), darkScheme.primary)
            assertEquals(Color(0xFF000000), darkScheme.onPrimary)
            
            // Test dark background with forest tones
            assertEquals(Color(0xFF0D1F0D), darkScheme.background)
            assertEquals(Color(0xFFC8E6C9), darkScheme.onBackground)
            
            // Test that earth tones are consistent
            assertEquals(Color(0xFF795548), darkScheme.tertiary)
            assertEquals(Color(0xFFFFFFFF), darkScheme.onTertiary)
        }

        @Test
        @DisplayName("Should have calming color transitions")
        fun shouldHaveCalmingColorTransitions() {
            val lightScheme = forestTheme.lightColorScheme
            val darkScheme = forestTheme.darkColorScheme
            
            // Verify consistent green family across schemes
            assertEquals(lightScheme.primary, darkScheme.primary)
            assertEquals(lightScheme.secondary, darkScheme.secondary)
            assertEquals(lightScheme.tertiary, darkScheme.tertiary)
            
            // Verify accent color consistency
            assertEquals(forestTheme.accentColor, lightScheme.primary)
            assertEquals(forestTheme.accentColor, darkScheme.primary)
        }

        @Test
        @DisplayName("Should use natural colors that promote calm and focus")
        fun shouldUseNaturalColorsThatPromoteCalmAndFocus() {
            val lightScheme = forestTheme.lightColorScheme
            val darkScheme = forestTheme.darkColorScheme
            
            // Forest green (0xFF4CAF50) represents nature and growth
            assertEquals(Color(0xFF4CAF50), lightScheme.primary)
            assertEquals(Color(0xFF4CAF50), darkScheme.primary)
            
            // Light green (0xFF8BC34A) suggests fresh growth
            assertEquals(Color(0xFF8BC34A), lightScheme.secondary)
            assertEquals(Color(0xFF8BC34A), darkScheme.secondary)
            
            // Brown (0xFF795548) represents earth and stability
            assertEquals(Color(0xFF795548), lightScheme.tertiary)
            assertEquals(Color(0xFF795548), darkScheme.tertiary)
        }
    }

    @Nested
    @DisplayName("Animation Style Tests")
    inner class AnimationStyleTests {

        @Test
        @DisplayName("AnimationStyle enum should have all expected values")
        fun animationStyleEnumShouldHaveAllExpectedValues() {
            val styles = AuraTheme.AnimationStyle.values()
            
            assertEquals(5, styles.size)
            assertTrue(styles.contains(AuraTheme.AnimationStyle.SUBTLE))
            assertTrue(styles.contains(AuraTheme.AnimationStyle.ENERGETIC))
            assertTrue(styles.contains(AuraTheme.AnimationStyle.CALMING))
            assertTrue(styles.contains(AuraTheme.AnimationStyle.PULSING))
            assertTrue(styles.contains(AuraTheme.AnimationStyle.FLOWING))
        }

        @Test
        @DisplayName("Themes should have appropriate animation styles for their purpose")
        fun themesShouldHaveAppropriateAnimationStylesForTheirPurpose() {
            // Cyberpunk should be energetic
            assertEquals(AuraTheme.AnimationStyle.ENERGETIC, cyberpunkTheme.animationStyle)
            
            // Solar Flare should be pulsing (like the sun)
            assertEquals(AuraTheme.AnimationStyle.PULSING, solarFlareTheme.animationStyle)
            
            // Forest should be flowing (like nature)
            assertEquals(AuraTheme.AnimationStyle.FLOWING, forestTheme.animationStyle)
        }
    }

    @Nested
    @DisplayName("Extension Function Tests")
    inner class ExtensionFunctionTests {

        @Test
        @DisplayName("getColorScheme should return light scheme when isDarkTheme is false")
        fun getColorSchemeShouldReturnLightSchemeWhenIsDarkThemeIsFalse() {
            val lightScheme = cyberpunkTheme.lightColorScheme
            val returnedScheme = cyberpunkTheme.getColorScheme(isDarkTheme = false)
            
            assertEquals(lightScheme, returnedScheme)
            assertEquals(lightScheme.primary, returnedScheme.primary)
            assertEquals(lightScheme.background, returnedScheme.background)
        }

        @Test
        @DisplayName("getColorScheme should return dark scheme when isDarkTheme is true")
        fun getColorSchemeShouldReturnDarkSchemeWhenIsDarkThemeIsTrue() {
            val darkScheme = solarFlareTheme.darkColorScheme
            val returnedScheme = solarFlareTheme.getColorScheme(isDarkTheme = true)
            
            assertEquals(darkScheme, returnedScheme)
            assertEquals(darkScheme.primary, returnedScheme.primary)
            assertEquals(darkScheme.background, returnedScheme.background)
        }

        @Test
        @DisplayName("getColorScheme should work correctly for all themes")
        fun getColorSchemeShouldWorkCorrectlyForAllThemes() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                val lightScheme = theme.getColorScheme(isDarkTheme = false)
                val darkScheme = theme.getColorScheme(isDarkTheme = true)
                
                assertEquals(theme.lightColorScheme, lightScheme)
                assertEquals(theme.darkColorScheme, darkScheme)
                assertNotEquals(lightScheme, darkScheme)
            }
        }
    }

    @Nested
    @DisplayName("Interface Contract Tests")
    inner class InterfaceContractTests {

        @Test
        @DisplayName("All theme objects should implement AuraTheme interface correctly")
        fun allThemeObjectsShouldImplementAuraThemeInterfaceCorrectly() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                // Test that all properties are accessible
                assertNotNull(theme.name)
                assertNotNull(theme.description)
                assertNotNull(theme.lightColorScheme)
                assertNotNull(theme.darkColorScheme)
                assertNotNull(theme.accentColor)
                assertNotNull(theme.animationStyle)
                
                // Test that names are not empty
                assertTrue(theme.name.isNotEmpty())
                assertTrue(theme.description.isNotEmpty())
            }
        }

        @Test
        @DisplayName("Theme names should be unique")
        fun themeNamesShouldBeUnique() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            val names = themes.map { it.name }
            val uniqueNames = names.toSet()
            
            assertEquals(names.size, uniqueNames.size)
        }

        @Test
        @DisplayName("Accent colors should match primary colors")
        fun accentColorsShouldMatchPrimaryColors() {
            // This tests the design consistency expectation
            assertEquals(cyberpunkTheme.accentColor, cyberpunkTheme.lightColorScheme.primary)
            assertEquals(cyberpunkTheme.accentColor, cyberpunkTheme.darkColorScheme.primary)
            
            assertEquals(solarFlareTheme.accentColor, solarFlareTheme.lightColorScheme.primary)
            assertEquals(solarFlareTheme.accentColor, solarFlareTheme.darkColorScheme.primary)
            
            assertEquals(forestTheme.accentColor, forestTheme.lightColorScheme.primary)
            assertEquals(forestTheme.accentColor, forestTheme.darkColorScheme.primary)
        }
    }

    @Nested
    @DisplayName("Color Scheme Validation Tests")
    inner class ColorSchemeValidationTests {

        @Test
        @DisplayName("Light and dark color schemes should be different for each theme")
        fun lightAndDarkColorSchemesShouldBeDifferentForEachTheme() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                val lightScheme = theme.lightColorScheme
                val darkScheme = theme.darkColorScheme
                
                // Background colors should be different
                assertNotEquals(lightScheme.background, darkScheme.background)
                
                // Surface colors should be different
                assertNotEquals(lightScheme.surface, darkScheme.surface)
                
                // On-background colors should be different
                assertNotEquals(lightScheme.onBackground, darkScheme.onBackground)
            }
        }

        @Test
        @DisplayName("Color schemes should have proper contrast ratios")
        fun colorSchemesShouldHaveProperContrastRatios() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                val lightScheme = theme.lightColorScheme
                val darkScheme = theme.darkColorScheme
                
                // Test that primary and onPrimary have contrast
                assertNotEquals(lightScheme.primary, lightScheme.onPrimary)
                assertNotEquals(darkScheme.primary, darkScheme.onPrimary)
                
                // Test that secondary and onSecondary have contrast
                assertNotEquals(lightScheme.secondary, lightScheme.onSecondary)
                assertNotEquals(darkScheme.secondary, darkScheme.onSecondary)
                
                // Test that surface and onSurface have contrast
                assertNotEquals(lightScheme.surface, lightScheme.onSurface)
                assertNotEquals(darkScheme.surface, darkScheme.onSurface)
            }
        }

        @Test
        @DisplayName("All color schemes should maintain readability standards")
        fun allColorSchemesShouldMaintainReadabilityStandards() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                val lightScheme = theme.lightColorScheme
                val darkScheme = theme.darkColorScheme
                
                // Test that text on backgrounds should have adequate contrast
                assertNotEquals(lightScheme.onBackground, lightScheme.background)
                assertNotEquals(darkScheme.onBackground, darkScheme.background)
                assertNotEquals(lightScheme.onSurface, lightScheme.surface)
                assertNotEquals(darkScheme.onSurface, darkScheme.surface)
            }
        }

        @Test
        @DisplayName("Color scheme container colors should provide proper hierarchy")
        fun colorSchemeContainerColorsShouldProvideProperHierarchy() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                val lightScheme = theme.lightColorScheme
                val darkScheme = theme.darkColorScheme
                
                // Container colors should be different from their base colors
                assertNotEquals(lightScheme.primary, lightScheme.primaryContainer)
                assertNotEquals(lightScheme.secondary, lightScheme.secondaryContainer)
                assertNotEquals(lightScheme.tertiary, lightScheme.tertiaryContainer)
                
                assertNotEquals(darkScheme.primary, darkScheme.primaryContainer)
                assertNotEquals(darkScheme.secondary, darkScheme.secondaryContainer)
                assertNotEquals(darkScheme.tertiary, darkScheme.tertiaryContainer)
            }
        }
    }

    @Nested
    @DisplayName("Edge Case and Validation Tests")
    inner class EdgeCaseAndValidationTests {

        @Test
        @DisplayName("Color values should be valid ARGB colors")
        fun colorValuesShouldBeValidArgbColors() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                // Test accent color is valid
                assertTrue(theme.accentColor.value >= 0)
                
                // Test that primary colors are valid in both schemes
                assertTrue(theme.lightColorScheme.primary.value >= 0)
                assertTrue(theme.darkColorScheme.primary.value >= 0)
                
                // Test that background colors are valid
                assertTrue(theme.lightColorScheme.background.value >= 0)
                assertTrue(theme.darkColorScheme.background.value >= 0)
            }
        }

        @Test
        @DisplayName("Theme descriptions should be meaningful and descriptive")
        fun themeDescriptionsShouldBeMeaningfulAndDescriptive() {
            assertTrue(cyberpunkTheme.description.contains("neon") || 
                      cyberpunkTheme.description.contains("futuristic"))
            assertTrue(solarFlareTheme.description.contains("warm") || 
                      solarFlareTheme.description.contains("energizing"))
            assertTrue(forestTheme.description.contains("calming") || 
                      forestTheme.description.contains("natural"))
        }

        @Test
        @DisplayName("Themes should maintain brand consistency")
        fun themesShouldMaintainBrandConsistency() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                // Each theme should have a clear emotional intent
                assertTrue(theme.description.isNotEmpty())
                assertTrue(theme.name.isNotEmpty())
                
                // Animation styles should match theme personality
                when (theme.name) {
                    "Cyberpunk" -> assertEquals(AuraTheme.AnimationStyle.ENERGETIC, theme.animationStyle)
                    "Solar Flare" -> assertEquals(AuraTheme.AnimationStyle.PULSING, theme.animationStyle)
                    "Forest" -> assertEquals(AuraTheme.AnimationStyle.FLOWING, theme.animationStyle)
                }
            }
        }
    }

    @Nested
    @DisplayName("Performance and Memory Tests")
    inner class PerformanceAndMemoryTests {

        @Test
        @DisplayName("Theme objects should be singletons and maintain same instance")
        fun themeObjectsShouldBeSingletonsAndMaintainSameInstance() {
            val cyberpunk1 = CyberpunkTheme
            val cyberpunk2 = CyberpunkTheme
            val solarFlare1 = SolarFlareTheme
            val solarFlare2 = SolarFlareTheme
            val forest1 = ForestTheme
            val forest2 = ForestTheme
            
            // Test that objects are the same instance (singleton behavior)
            assertSame(cyberpunk1, cyberpunk2)
            assertSame(solarFlare1, solarFlare2)
            assertSame(forest1, forest2)
        }

        @Test
        @DisplayName("ColorScheme objects should be stable across multiple calls")
        fun colorSchemeObjectsShouldBeStableAcrossMultipleCalls() {
            val themes = listOf(cyberpunkTheme, solarFlareTheme, forestTheme)
            
            themes.forEach { theme ->
                // Multiple calls should return equivalent color schemes
                val lightScheme1 = theme.lightColorScheme
                val lightScheme2 = theme.lightColorScheme
                val darkScheme1 = theme.darkColorScheme
                val darkScheme2 = theme.darkColorScheme
                
                assertEquals(lightScheme1.primary, lightScheme2.primary)
                assertEquals(lightScheme1.background, lightScheme2.background)
                assertEquals(darkScheme1.primary, darkScheme2.primary)
                assertEquals(darkScheme1.background, darkScheme2.background)
            }
        }
    }
}