package dev.aurakai.auraframefx.theme

import dev.aurakai.auraframefx.ai.services.AuraAIService
import dev.aurakai.auraframefx.ui.theme.AuraTheme
import dev.aurakai.auraframefx.ui.theme.CyberpunkTheme
import dev.aurakai.auraframefx.ui.theme.ForestTheme
import dev.aurakai.auraframefx.ui.theme.SolarFlareTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Comprehensive unit tests for ThemeManager.
 * 
 * Testing Framework: JUnit 5 with MockK for mocking
 * Focus: Testing theme application logic, AI integration, and error handling
 */
class ThemeManagerTest {

    private lateinit var mockAuraAIService: AuraAIService
    private lateinit var themeManager: ThemeManager

    @BeforeEach
    fun setUp() {
        mockAuraAIService = mockk()
        themeManager = ThemeManager(mockAuraAIService)
    }

    @Nested
    @DisplayName("applyThemeFromNaturalLanguage Tests")
    inner class ApplyThemeFromNaturalLanguageTests {

        @Test
        @DisplayName("Should successfully apply cyberpunk theme when AI returns 'cyberpunk' intent")
        fun `applyThemeFromNaturalLanguage - cyberpunk intent - returns success with CyberpunkTheme`() = runTest {
            // Given
            val query = "Make my phone look futuristic and neon"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "cyberpunk"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(CyberpunkTheme, successResult.appliedTheme)
            coVerify(exactly = 1) { mockAuraAIService.discernThemeIntent(query) }
        }

        @Test
        @DisplayName("Should successfully apply solar theme when AI returns 'solar' intent")
        fun `applyThemeFromNaturalLanguage - solar intent - returns success with SolarFlareTheme`() = runTest {
            // Given
            val query = "I want bright sunny colors"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "solar"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(SolarFlareTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should successfully apply forest theme when AI returns 'nature' intent")
        fun `applyThemeFromNaturalLanguage - nature intent - returns success with ForestTheme`() = runTest {
            // Given
            val query = "Something green and natural"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "nature"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(ForestTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should apply SolarFlareTheme when AI returns 'cheerful' intent")
        fun `applyThemeFromNaturalLanguage - cheerful intent - returns success with SolarFlareTheme`() = runTest {
            // Given
            val query = "Make my interface more cheerful"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "cheerful"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(SolarFlareTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should apply ForestTheme when AI returns 'calming' intent")
        fun `applyThemeFromNaturalLanguage - calming intent - returns success with ForestTheme`() = runTest {
            // Given
            val query = "I need something calming"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "calming"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(ForestTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should apply CyberpunkTheme when AI returns 'energetic' intent")
        fun `applyThemeFromNaturalLanguage - energetic intent - returns success with CyberpunkTheme`() = runTest {
            // Given
            val query = "I want something energetic and vibrant"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "energetic"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(CyberpunkTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should return UnderstandingFailed when AI returns unknown intent")
        fun `applyThemeFromNaturalLanguage - unknown intent - returns UnderstandingFailed`() = runTest {
            // Given
            val query = "Make it look like banana pudding"
            val unknownIntent = "banana_pudding"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns unknownIntent

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.UnderstandingFailed)
            val failedResult = result as ThemeManager.ThemeResult.UnderstandingFailed
            assertEquals(query, failedResult.originalQuery)
        }

        @Test
        @DisplayName("Should return UnderstandingFailed when AI returns null")
        fun `applyThemeFromNaturalLanguage - null intent - returns UnderstandingFailed`() = runTest {
            // Given
            val query = "asdfghjkl random gibberish"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns null

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.UnderstandingFailed)
            val failedResult = result as ThemeManager.ThemeResult.UnderstandingFailed
            assertEquals(query, failedResult.originalQuery)
        }

        @Test
        @DisplayName("Should return UnderstandingFailed when AI returns empty string")
        fun `applyThemeFromNaturalLanguage - empty intent - returns UnderstandingFailed`() = runTest {
            // Given
            val query = "unclear request"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns ""

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.UnderstandingFailed)
            val failedResult = result as ThemeManager.ThemeResult.UnderstandingFailed
            assertEquals(query, failedResult.originalQuery)
        }

        @Test
        @DisplayName("Should return Error when AI service throws exception")
        fun `applyThemeFromNaturalLanguage - AI service exception - returns Error`() = runTest {
            // Given
            val query = "Make it pretty"
            val expectedException = RuntimeException("AI service unavailable")
            coEvery { mockAuraAIService.discernThemeIntent(query) } throws expectedException

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Error)
            val errorResult = result as ThemeManager.ThemeResult.Error
            assertEquals(expectedException, errorResult.exception)
        }

        @Test
        @DisplayName("Should handle empty query string gracefully")
        fun `applyThemeFromNaturalLanguage - empty query - calls AI service and handles response`() = runTest {
            // Given
            val emptyQuery = ""
            coEvery { mockAuraAIService.discernThemeIntent(emptyQuery) } returns "cyberpunk"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(emptyQuery)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            coVerify(exactly = 1) { mockAuraAIService.discernThemeIntent(emptyQuery) }
        }

        @Test
        @DisplayName("Should handle whitespace-only query string")
        fun `applyThemeFromNaturalLanguage - whitespace query - calls AI service`() = runTest {
            // Given
            val whitespaceQuery = "   \n\t  "
            coEvery { mockAuraAIService.discernThemeIntent(whitespaceQuery) } returns "nature"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(whitespaceQuery)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            coVerify(exactly = 1) { mockAuraAIService.discernThemeIntent(whitespaceQuery) }
        }

        @Test
        @DisplayName("Should handle very long query strings")
        fun `applyThemeFromNaturalLanguage - long query - processes correctly`() = runTest {
            // Given
            val longQuery = "a".repeat(1000) + " make it cyberpunk"
            coEvery { mockAuraAIService.discernThemeIntent(longQuery) } returns "cyberpunk"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(longQuery)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(CyberpunkTheme, successResult.appliedTheme)
        }

        @Test
        @DisplayName("Should handle special characters in query")
        fun `applyThemeFromNaturalLanguage - special characters - processes correctly`() = runTest {
            // Given
            val specialCharQuery = "!@#$%^&*()_+ cyberpunk theme 😊🎨"
            coEvery { mockAuraAIService.discernThemeIntent(specialCharQuery) } returns "cyberpunk"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(specialCharQuery)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.Success)
            val successResult = result as ThemeManager.ThemeResult.Success
            assertEquals(CyberpunkTheme, successResult.appliedTheme)
        }
    }

    @Nested
    @DisplayName("suggestThemeBasedOnContext Tests")
    inner class SuggestThemeBasedOnContextTests {

        @Test
        @DisplayName("Should return mapped themes when AI suggests known intents")
        fun `suggestThemeBasedOnContext - known intents - returns mapped themes`() = runTest {
            // Given
            val timeOfDay = "evening"
            val userActivity = "working"
            val emotionalContext = "focused"
            val contextQuery = "Time: evening, Activity: working, Mood: focused"
            val aiSuggestions = listOf("cyberpunk", "solar", "nature")
            
            coEvery { mockAuraAIService.suggestThemes(contextQuery) } returns aiSuggestions

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity, emotionalContext)

            // Then
            assertEquals(3, result.size)
            assertTrue(result.contains(CyberpunkTheme))
            assertTrue(result.contains(SolarFlareTheme))
            assertTrue(result.contains(ForestTheme))
            coVerify(exactly = 1) { mockAuraAIService.suggestThemes(contextQuery) }
        }

        @Test
        @DisplayName("Should filter out unknown intents from suggestions")
        fun `suggestThemeBasedOnContext - mixed known and unknown intents - returns only known themes`() = runTest {
            // Given
            val timeOfDay = "morning"
            val userActivity = "exercising"
            val aiSuggestions = listOf("cyberpunk", "unknown_theme", "solar", "another_unknown")
            
            coEvery { mockAuraAIService.suggestThemes(any()) } returns aiSuggestions

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity)

            // Then
            assertEquals(2, result.size)
            assertTrue(result.contains(CyberpunkTheme))
            assertTrue(result.contains(SolarFlareTheme))
            assertFalse(result.any { it.name.contains("unknown") })
        }

        @Test
        @DisplayName("Should return empty list when AI suggests no known themes")
        fun `suggestThemeBasedOnContext - no known intents - returns empty list`() = runTest {
            // Given
            val timeOfDay = "afternoon"
            val userActivity = "sleeping"
            val aiSuggestions = listOf("unknown1", "unknown2", "unknown3")
            
            coEvery { mockAuraAIService.suggestThemes(any()) } returns aiSuggestions

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity)

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should build correct context query without emotional context")
        fun `suggestThemeBasedOnContext - no emotional context - builds correct query`() = runTest {
            // Given
            val timeOfDay = "night"
            val userActivity = "reading"
            val expectedQuery = "Time: night, Activity: reading"
            
            coEvery { mockAuraAIService.suggestThemes(expectedQuery) } returns listOf("nature")

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity, null)

            // Then
            assertEquals(1, result.size)
            assertEquals(ForestTheme, result[0])
            coVerify(exactly = 1) { mockAuraAIService.suggestThemes(expectedQuery) }
        }

        @Test
        @DisplayName("Should build correct context query with emotional context")
        fun `suggestThemeBasedOnContext - with emotional context - builds correct query`() = runTest {
            // Given
            val timeOfDay = "morning"
            val userActivity = "commuting"
            val emotionalContext = "stressed"
            val expectedQuery = "Time: morning, Activity: commuting, Mood: stressed"
            
            coEvery { mockAuraAIService.suggestThemes(expectedQuery) } returns listOf("nature")

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity, emotionalContext)

            // Then
            assertEquals(1, result.size)
            coVerify(exactly = 1) { mockAuraAIService.suggestThemes(expectedQuery) }
        }

        @Test
        @DisplayName("Should return empty list when AI service throws exception")
        fun `suggestThemeBasedOnContext - AI service exception - returns empty list`() = runTest {
            // Given
            val timeOfDay = "evening"
            val userActivity = "gaming"
            coEvery { mockAuraAIService.suggestThemes(any()) } throws RuntimeException("Service error")

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity)

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should return empty list when AI returns empty suggestions")
        fun `suggestThemeBasedOnContext - empty AI suggestions - returns empty list`() = runTest {
            // Given
            val timeOfDay = "dawn"
            val userActivity = "meditating"
            coEvery { mockAuraAIService.suggestThemes(any()) } returns emptyList()

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity)

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should handle empty string parameters gracefully")
        fun `suggestThemeBasedOnContext - empty parameters - handles gracefully`() = runTest {
            // Given
            val timeOfDay = ""
            val userActivity = ""
            val emotionalContext = ""
            val expectedQuery = "Time: , Activity: , Mood: "
            
            coEvery { mockAuraAIService.suggestThemes(expectedQuery) } returns listOf("cyberpunk")

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity, emotionalContext)

            // Then
            assertEquals(1, result.size)
            assertEquals(CyberpunkTheme, result[0])
        }

        @Test
        @DisplayName("Should handle duplicate suggestions from AI")
        fun `suggestThemeBasedOnContext - duplicate suggestions - returns unique themes`() = runTest {
            // Given
            val timeOfDay = "afternoon"
            val userActivity = "working"
            val aiSuggestions = listOf("cyberpunk", "cyberpunk", "solar", "cyberpunk", "nature", "solar")
            
            coEvery { mockAuraAIService.suggestThemes(any()) } returns aiSuggestions

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity)

            // Then
            assertEquals(3, result.size)
            assertTrue(result.contains(CyberpunkTheme))
            assertTrue(result.contains(SolarFlareTheme))
            assertTrue(result.contains(ForestTheme))
        }

        @Test
        @DisplayName("Should handle special characters in context parameters")
        fun `suggestThemeBasedOnContext - special characters - processes correctly`() = runTest {
            // Given
            val timeOfDay = "12:30 AM"
            val userActivity = "coding/debugging"
            val emotionalContext = "😴 tired"
            
            coEvery { mockAuraAIService.suggestThemes(any()) } returns listOf("nature")

            // When
            val result = themeManager.suggestThemeBasedOnContext(timeOfDay, userActivity, emotionalContext)

            // Then
            assertEquals(1, result.size)
            assertEquals(ForestTheme, result[0])
        }
    }

    @Nested
    @DisplayName("ThemeResult Sealed Class Tests")
    inner class ThemeResultTests {

        @Test
        @DisplayName("Success result should contain applied theme")
        fun `ThemeResult Success - contains applied theme`() {
            // Given
            val theme = CyberpunkTheme
            
            // When
            val result = ThemeManager.ThemeResult.Success(theme)
            
            // Then
            assertEquals(theme, result.appliedTheme)
        }

        @Test
        @DisplayName("UnderstandingFailed result should contain original query")
        fun `ThemeResult UnderstandingFailed - contains original query`() {
            // Given
            val originalQuery = "incomprehensible request"
            
            // When
            val result = ThemeManager.ThemeResult.UnderstandingFailed(originalQuery)
            
            // Then
            assertEquals(originalQuery, result.originalQuery)
        }

        @Test
        @DisplayName("Error result should contain exception")
        fun `ThemeResult Error - contains exception`() {
            // Given
            val exception = RuntimeException("Test exception")
            
            // When
            val result = ThemeManager.ThemeResult.Error(exception)
            
            // Then
            assertEquals(exception, result.exception)
        }
    }

    @Nested
    @DisplayName("Edge Case and Stress Tests")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Should handle concurrent theme application requests")
        fun `applyThemeFromNaturalLanguage - concurrent requests - handles properly`() = runTest {
            // Given
            val query1 = "cyberpunk theme"
            val query2 = "nature theme"
            coEvery { mockAuraAIService.discernThemeIntent(query1) } returns "cyberpunk"
            coEvery { mockAuraAIService.discernThemeIntent(query2) } returns "nature"

            // When
            val result1 = themeManager.applyThemeFromNaturalLanguage(query1)
            val result2 = themeManager.applyThemeFromNaturalLanguage(query2)

            // Then
            assertTrue(result1 is ThemeManager.ThemeResult.Success)
            assertTrue(result2 is ThemeManager.ThemeResult.Success)
            assertEquals(CyberpunkTheme, (result1 as ThemeManager.ThemeResult.Success).appliedTheme)
            assertEquals(ForestTheme, (result2 as ThemeManager.ThemeResult.Success).appliedTheme)
        }

        @Test
        @DisplayName("Should handle null AI service responses gracefully")
        fun `suggestThemeBasedOnContext - null AI response - returns empty list`() = runTest {
            // Given
            coEvery { mockAuraAIService.suggestThemes(any()) } returns null

            // When
            val result = themeManager.suggestThemeBasedOnContext("evening", "working")

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should handle case-sensitive intent matching")
        fun `applyThemeFromNaturalLanguage - case sensitive intents - handles correctly`() = runTest {
            // Given
            val query = "CYBERPUNK theme"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns "CYBERPUNK"

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.UnderstandingFailed)
        }

        @Test
        @DisplayName("Should handle intent strings with whitespace")
        fun `applyThemeFromNaturalLanguage - intent with whitespace - handles correctly`() = runTest {
            // Given
            val query = "cyberpunk style"
            coEvery { mockAuraAIService.discernThemeIntent(query) } returns " cyberpunk "

            // When
            val result = themeManager.applyThemeFromNaturalLanguage(query)

            // Then
            assertTrue(result is ThemeManager.ThemeResult.UnderstandingFailed)
        }
    }
}