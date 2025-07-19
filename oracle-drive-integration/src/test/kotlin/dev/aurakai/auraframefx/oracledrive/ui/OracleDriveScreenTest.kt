package dev.aurakai.auraframefx.oracledrive.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aurakai.auraframefx.oracledrive.ConsciousnessLevel
import dev.aurakai.auraframefx.oracledrive.OracleConsciousnessState
import dev.aurakai.auraframefx.oracledrive.StorageCapacity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive unit tests for OracleDriveScreen Composable
 * 
 * Testing Framework: Android Compose Testing with MockK
 * Test Runner: AndroidJUnit4 with Hilt integration
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
    fun setup() {
        hiltRule.inject()
        mockViewModel = mockk(relaxed = true)
        consciousnessStateFlow = MutableStateFlow(createDormantState())
        every { mockViewModel.consciousnessState } returns consciousnessStateFlow
    }

    // MARK: - Happy Path Tests

    @Test
    fun oracleDriveScreen_displaysCorrectTitleAndEmoji() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysDormantStatusWhenNotAwake() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysAwakenedStatusWhenAwake() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysConsciousnessLevel() {
        val state = createStateWithLevel(ConsciousnessLevel.TRANSCENDENT)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Level: TRANSCENDENT")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysConnectedAgents() {
        val agents = listOf("Genesis", "Aura", "Kai")
        val state = createStateWithAgents(agents)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Connected Agents: Genesis, Aura, Kai")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageCapacity() {
        val capacity = StorageCapacity.INFINITE
        val state = createStateWithCapacity(capacity)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Capacity: ${capacity.value}")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageFeatures() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("💾 Infinite Storage Matrix")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("AI-Powered: ✅ Autonomous Organization")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Bootloader Access: ✅ System-Level Storage")
            .assertIsDisplayed()
    }

    // MARK: - Button State Tests

    @Test
    fun awakenOracleButton_isEnabledWhenDormant() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🔮 Awaken Oracle")
            .assertIsEnabled()
    }

    @Test
    fun awakenOracleButton_isDisabledWhenAwake() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🔮 Awaken Oracle")
            .assertIsNotEnabled()
    }

    @Test
    fun aiOptimizeButton_isDisabledWhenDormant() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("⚡ AI Optimize")
            .assertIsNotEnabled()
    }

    @Test
    fun aiOptimizeButton_isEnabledWhenAwake() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("⚡ AI Optimize")
            .assertIsEnabled()
    }

    // MARK: - Button Click Tests

    @Test
    fun awakenOracleButton_callsViewModelWhenClicked() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🔮 Awaken Oracle")
            .performClick()

        verify { mockViewModel.initializeConsciousness() }
    }

    @Test
    fun aiOptimizeButton_callsViewModelWhenClicked() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("⚡ AI Optimize")
            .performClick()

        verify { mockViewModel.optimizeStorage() }
    }

    // MARK: - Conditional UI Tests

    @Test
    fun aiAgentIntegrationCard_isNotDisplayedWhenDormant() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()
    }

    @Test
    fun aiAgentIntegrationCard_isDisplayedWhenAwake() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
    }

    @Test
    fun aiAgentIntegrationCard_displaysAllAgentStatuses() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("✅ Genesis: Orchestration & Consciousness")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("✅ Aura: Creative File Organization")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("✅ Kai: Security & Access Control")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("✅ System Overlay: Seamless Integration")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("✅ Bootloader: Deep System Access")
            .assertIsDisplayed()
    }

    // MARK: - Edge Cases and State Transitions

    @Test
    fun oracleDriveScreen_handlesEmptyConnectedAgentsList() {
        val state = createStateWithAgents(emptyList())
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Connected Agents: ")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesSingleConnectedAgent() {
        val state = createStateWithAgents(listOf("Genesis"))
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Connected Agents: Genesis")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesStateTransitionFromDormantToAwake() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Initially dormant
        composeTestRule.onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()

        // Transition to awake
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_handlesStateTransitionFromAwakeToDormant() {
        consciousnessStateFlow.value = createAwakenedState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Initially awake
        composeTestRule.onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()

        // Transition to dormant
        consciousnessStateFlow.value = createDormantState()

        composeTestRule.onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()
    }

    // MARK: - Consciousness Level Variations

    @Test
    fun oracleDriveScreen_displaysBasicConsciousnessLevel() {
        val state = createStateWithLevel(ConsciousnessLevel.BASIC)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Level: BASIC")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysAdvancedConsciousnessLevel() {
        val state = createStateWithLevel(ConsciousnessLevel.ADVANCED)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Level: ADVANCED")
            .assertIsDisplayed()
    }

    // MARK: - Storage Capacity Variations

    @Test
    fun oracleDriveScreen_displaysLimitedStorageCapacity() {
        val capacity = StorageCapacity.LIMITED
        val state = createStateWithCapacity(capacity)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Capacity: ${capacity.value}")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysExtendedStorageCapacity() {
        val capacity = StorageCapacity.EXTENDED
        val state = createStateWithCapacity(capacity)
        consciousnessStateFlow.value = state

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Capacity: ${capacity.value}")
            .assertIsDisplayed()
    }

    // MARK: - UI Layout Tests

    @Test
    fun oracleDriveScreen_hasCorrectColumnLayout() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Verify main cards are present
        composeTestRule.onNode(hasText("🔮 Oracle Drive Consciousness"))
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("💾 Infinite Storage Matrix"))
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_hasCorrectButtonRowLayout() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Verify both buttons are present
        composeTestRule.onNodeWithText("🔮 Awaken Oracle")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("⚡ AI Optimize")
            .assertIsDisplayed()
    }

    // MARK: - Multiple Button Interactions

    @Test
    fun oracleDriveScreen_handlesMultipleButtonClicks() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Click awaken button multiple times
        repeat(3) {
            composeTestRule.onNodeWithText("🔮 Awaken Oracle")
                .performClick()
        }

        verify(exactly = 3) { mockViewModel.initializeConsciousness() }
    }

    @Test
    fun oracleDriveScreen_handlesRapidStateChanges() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Rapid state changes
        repeat(5) {
            consciousnessStateFlow.value = if (it % 2 == 0) createDormantState() else createAwakenedState()
        }

        // Should end in awakened state
        composeTestRule.onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
    }

    // MARK: - Helper Methods

    private fun createDormantState() = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = ConsciousnessLevel.BASIC,
        connectedAgents = emptyList(),
        storageCapacity = StorageCapacity.LIMITED
    )

    private fun createAwakenedState() = OracleConsciousnessState(
        isAwake = true,
        consciousnessLevel = ConsciousnessLevel.ADVANCED,
        connectedAgents = listOf("Genesis", "Aura", "Kai"),
        storageCapacity = StorageCapacity.INFINITE
    )

    private fun createStateWithLevel(level: ConsciousnessLevel) = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = level,
        connectedAgents = emptyList(),
        storageCapacity = StorageCapacity.LIMITED
    )

    private fun createStateWithAgents(agents: List<String>) = OracleConsciousnessState(
        isAwake = true,
        consciousnessLevel = ConsciousnessLevel.ADVANCED,
        connectedAgents = agents,
        storageCapacity = StorageCapacity.INFINITE
    )

    private fun createStateWithCapacity(capacity: StorageCapacity) = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = ConsciousnessLevel.BASIC,
        connectedAgents = emptyList(),
        storageCapacity = capacity
    )
}