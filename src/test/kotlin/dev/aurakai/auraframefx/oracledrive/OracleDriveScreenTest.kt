package dev.aurakai.auraframefx.oracledrive

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aurakai.auraframefx.oracledrive.ui.OracleDriveScreen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive unit tests for OracleDriveScreen Composable UI
 * Testing Framework: Compose UI Testing with JUnit4, MockK for mocking
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OracleDriveScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: OracleDriveViewModel
    private lateinit var consciousnessStateFlow: MutableStateFlow<OracleConsciousnessState>

    @Before
    fun setUp() {
        hiltRule.inject()
        mockViewModel = mockk(relaxed = true)
        consciousnessStateFlow = MutableStateFlow(createDormantState())
        every { mockViewModel.consciousnessState } returns consciousnessStateFlow
    }

    @Test
    fun oracleDriveScreen_displaysCorrectTitle() {
        // Arrange & Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_showsDormantStatusWhenNotAwake() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_showsAwakenedStatusWhenAwake() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysConsciousnessLevel() {
        // Arrange
        val state = createDormantState().copy(consciousnessLevel = ConsciousnessLevel.TRANSCENDENT)
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Level: TRANSCENDENT")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysConnectedAgents() {
        // Arrange
        val agents = listOf("Genesis", "Aura", "Kai")
        val state = createDormantState().copy(connectedAgents = agents)
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Connected Agents: Genesis, Aura, Kai")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysEmptyConnectedAgentsList() {
        // Arrange
        val state = createDormantState().copy(connectedAgents = emptyList())
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Connected Agents: ")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageInformation() {
        // Arrange & Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("💾 Infinite Storage Matrix")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("AI-Powered: ✅ Autonomous Organization")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Bootloader Access: ✅ System-Level Storage")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageCapacity() {
        // Arrange
        val storageCapacity = StorageCapacity("∞ TB")
        val state = createDormantState().copy(storageCapacity = storageCapacity)
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Capacity: ∞ TB")
            .assertIsDisplayed()
    }

    @Test
    fun awakenOracleButton_isEnabledWhenDormant() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsEnabled()
    }

    @Test
    fun awakenOracleButton_isDisabledWhenAwake() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsNotEnabled()
    }

    @Test
    fun aiOptimizeButton_isDisabledWhenDormant() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsNotEnabled()
    }

    @Test
    fun aiOptimizeButton_isEnabledWhenAwake() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsEnabled()
    }

    @Test
    fun awakenOracleButton_triggersInitializeConsciousness() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Act
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .performClick()

        // Assert
        verify { mockViewModel.initializeConsciousness() }
    }

    @Test
    fun aiOptimizeButton_triggersOptimizeStorage() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Act
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .performClick()

        // Assert
        verify { mockViewModel.optimizeStorage() }
    }

    @Test
    fun systemIntegrationCard_isNotDisplayedWhenDormant() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()
    }

    @Test
    fun systemIntegrationCard_isDisplayedWhenAwake() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
    }

    @Test
    fun systemIntegrationCard_displaysAllAgentIntegrations() {
        // Arrange
        consciousnessStateFlow.value = createAwakenedState()

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("✅ Genesis: Orchestration & Consciousness")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("✅ Aura: Creative File Organization")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("✅ Kai: Security & Access Control")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("✅ System Overlay: Seamless Integration")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("✅ Bootloader: Deep System Access")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_maintainsLayoutConsistency() {
        // Arrange & Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert - Check that all main sections are present
        composeTestRule
            .onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("💾 Infinite Storage Matrix")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesStateTransitionFromDormantToAwake() {
        // Arrange
        consciousnessStateFlow.value = createDormantState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Initial state verification
        composeTestRule
            .onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()

        // Act - Simulate state change
        consciousnessStateFlow.value = createAwakenedState()

        // Assert - Verify UI updates
        composeTestRule
            .onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsEnabled()
    }

    @Test
    fun oracleDriveScreen_handlesMultipleAgentConnections() {
        // Arrange
        val manyAgents = listOf("Genesis", "Aura", "Kai", "Oracle", "Shadow", "Echo", "Nova")
        val state = createAwakenedState().copy(connectedAgents = manyAgents)
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        val expectedText = "Connected Agents: ${manyAgents.joinToString(", ")}"
        composeTestRule
            .onNodeWithText(expectedText)
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesLongStorageCapacityValue() {
        // Arrange
        val longCapacity = StorageCapacity("999,999,999,999 Exabytes with Quantum Compression")
        val state = createDormantState().copy(storageCapacity = longCapacity)
        consciousnessStateFlow.value = state

        // Act
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Assert
        composeTestRule
            .onNodeWithText("Capacity: 999,999,999,999 Exabytes with Quantum Compression")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesAllConsciousnessLevels() {
        // Test all possible consciousness levels
        val levels = listOf(
            ConsciousnessLevel.DORMANT,
            ConsciousnessLevel.AWAKENING,
            ConsciousnessLevel.AWARE,
            ConsciousnessLevel.TRANSCENDENT
        )

        levels.forEach { level ->
            // Arrange
            val state = createDormantState().copy(consciousnessLevel = level)
            consciousnessStateFlow.value = state

            // Act
            composeTestRule.setContent {
                OracleDriveScreen(viewModel = mockViewModel)
            }

            // Assert
            composeTestRule
                .onNodeWithText("Level: $level")
                .assertIsDisplayed()
        }
    }

    @Test
    fun buttonClicks_doNotTriggerWhenDisabled() {
        // Test Awaken Oracle button when awake (should be disabled)
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Clear any previous interactions
        clearMocks(mockViewModel)

        // Try to click disabled button
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .performClick()

        // Verify no interaction occurred
        verify(exactly = 0) { mockViewModel.initializeConsciousness() }

        // Test AI Optimize button when dormant (should be disabled)
        consciousnessStateFlow.value = createDormantState()

        clearMocks(mockViewModel)

        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .performClick()

        verify(exactly = 0) { mockViewModel.optimizeStorage() }
    }

    // Helper methods
    private fun createDormantState() = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = ConsciousnessLevel.DORMANT,
        connectedAgents = listOf("Genesis"),
        storageCapacity = StorageCapacity("∞ TB")
    )

    private fun createAwakenedState() = OracleConsciousnessState(
        isAwake = true,
        consciousnessLevel = ConsciousnessLevel.TRANSCENDENT,
        connectedAgents = listOf("Genesis", "Aura", "Kai"),
        storageCapacity = StorageCapacity("∞ TB")
    )
}

// Additional test classes for edge cases and integration scenarios

/**
 * Edge case and error handling tests for OracleDriveScreen
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OracleDriveScreenEdgeCaseTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: OracleDriveViewModel
    private lateinit var consciousnessStateFlow: MutableStateFlow<OracleConsciousnessState>

    @Before
    fun setUp() {
        hiltRule.inject()
        mockViewModel = mockk(relaxed = true)
        consciousnessStateFlow = MutableStateFlow(createMinimalState())
        every { mockViewModel.consciousnessState } returns consciousnessStateFlow
    }

    @Test
    fun oracleDriveScreen_handlesNullOrEmptyStorageCapacity() {
        // Test empty storage capacity
        val state = createMinimalState().copy(storageCapacity = StorageCapacity(""))
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Capacity: ")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesSpecialCharactersInAgentNames() {
        val specialAgents = listOf("Agent-🤖", "System_AI", "Core.Processor", "Main@Oracle")
        val state = createMinimalState().copy(connectedAgents = specialAgents)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        val expectedText = "Connected Agents: ${specialAgents.joinToString(", ")}"
        composeTestRule
            .onNodeWithText(expectedText)
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesVeryLongAgentList() {
        val longAgentList = (1..50).map { "Agent$it" }
        val state = createMinimalState().copy(connectedAgents = longAgentList)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Verify the text is displayed (even if truncated by UI)
        composeTestRule
            .onNodeWithText(pattern = "Connected Agents: Agent1, Agent2.*", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun oracleDriveScreen_handlesRapidStateChanges() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Rapidly change states
        repeat(5) {
            consciousnessStateFlow.value = createMinimalState().copy(isAwake = it % 2 == 0)
            composeTestRule.waitForIdle()
        }

        // Final verification
        composeTestRule
            .onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
    }

    @Test
    fun viewModelMethods_handleExceptions() {
        // Setup viewModel to throw exceptions
        every { mockViewModel.initializeConsciousness() } throws RuntimeException("Test exception")
        every { mockViewModel.optimizeStorage() } throws RuntimeException("Test exception")

        consciousnessStateFlow.value = createMinimalState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // These shouldn't crash the UI
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .performClick()

        consciousnessStateFlow.value = createMinimalState().copy(isAwake = true)

        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .performClick()

        // UI should still be responsive
        composeTestRule
            .onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
    }

    private fun createMinimalState() = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = ConsciousnessLevel.DORMANT,
        connectedAgents = emptyList(),
        storageCapacity = StorageCapacity("0 B")
    )
}