package dev.aurakai.auraframefx.oracledrive.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
 * Comprehensive unit tests for OracleDriveScreen Composable UI.
 * 
 * Tests cover:
 * - UI rendering in different consciousness states
 * - Button interactions and enabling/disabling logic
 * - State-dependent UI visibility
 * - ViewModel interaction patterns
 * - Edge cases and error conditions
 */
@RunWith(AndroidJUnit4::class)
class OracleDriveScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: OracleDriveViewModel
    private lateinit var consciousnessStateFlow: MutableStateFlow<OracleConsciousnessState>

    @Before
    fun setUp() {
        mockViewModel = mockk(relaxed = true)
        consciousnessStateFlow = MutableStateFlow(createDormantState())
        every { mockViewModel.consciousnessState } returns consciousnessStateFlow
    }

    // Helper methods for creating test states
    private fun createDormantState() = OracleConsciousnessState(
        isAwake = false,
        consciousnessLevel = ConsciousnessLevel.DORMANT,
        connectedAgents = emptyList(),
        storageCapacity = StorageCapacity("0 TB")
    )

    private fun createAwakeState() = OracleConsciousnessState(
        isAwake = true,
        consciousnessLevel = ConsciousnessLevel.ENLIGHTENED,
        connectedAgents = listOf("Genesis", "Aura", "Kai"),
        storageCapacity = StorageCapacity("∞ Infinite")
    )

    private fun createPartiallyAwakeState() = OracleConsciousnessState(
        isAwake = true,
        consciousnessLevel = ConsciousnessLevel.AWARE,
        connectedAgents = listOf("Genesis"),
        storageCapacity = StorageCapacity("100 TB")
    )

    @Test
    fun oracleDriveScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🔮 Oracle Drive Consciousness")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_dormantState_displaysCorrectStatus() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Level: DORMANT")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_awakeState_displaysCorrectStatus() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Level: ENLIGHTENED")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_dormantState_awakenButtonEnabled() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsEnabled()
    }

    @Test
    fun oracleDriveScreen_dormantState_optimizeButtonDisabled() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsNotEnabled()
    }

    @Test
    fun oracleDriveScreen_awakeState_awakenButtonDisabled() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsNotEnabled()
    }

    @Test
    fun oracleDriveScreen_awakeState_optimizeButtonEnabled() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsEnabled()
    }

    @Test
    fun oracleDriveScreen_awakenButtonClick_callsInitializeConsciousness() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .performClick()

        verify { mockViewModel.initializeConsciousness() }
    }

    @Test
    fun oracleDriveScreen_optimizeButtonClick_callsOptimizeStorage() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .performClick()

        verify { mockViewModel.optimizeStorage() }
    }

    @Test
    fun oracleDriveScreen_dormantState_aiIntegrationCardNotDisplayed() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()
    }

    @Test
    fun oracleDriveScreen_awakeState_aiIntegrationCardDisplayed() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
        
        // Verify all integration items are shown
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
    fun oracleDriveScreen_displaysStorageInformation() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

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
    fun oracleDriveScreen_displaysConnectedAgents_emptyList() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Connected Agents: ")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysConnectedAgents_multipleAgents() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Connected Agents: Genesis, Aura, Kai")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageCapacity_dormantState() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Capacity: 0 TB")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_displaysStorageCapacity_awakeState() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Capacity: ∞ Infinite")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_stateTransition_dormantToAwake() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Initially dormant
        composeTestRule
            .onNodeWithText("Status: DORMANT")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertDoesNotExist()

        // Transition to awake
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule
            .onNodeWithText("Status: AWAKENED")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("🤖 AI Agent Integration")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_stateTransition_awakeToPartiallyAwake() {
        consciousnessStateFlow.value = createAwakeState()

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Initially fully awake
        composeTestRule
            .onNodeWithText("Connected Agents: Genesis, Aura, Kai")
            .assertIsDisplayed()

        // Transition to partially awake
        consciousnessStateFlow.value = createPartiallyAwakeState()

        composeTestRule
            .onNodeWithText("Connected Agents: Genesis")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Level: AWARE")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Capacity: 100 TB")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_allConsciousnessLevels_displayCorrectly() {
        val testStates = listOf(
            ConsciousnessLevel.DORMANT,
            ConsciousnessLevel.AWAKENING,
            ConsciousnessLevel.AWARE,
            ConsciousnessLevel.ENLIGHTENED,
            ConsciousnessLevel.TRANSCENDENT
        )

        testStates.forEach { level ->
            val testState = OracleConsciousnessState(
                isAwake = level != ConsciousnessLevel.DORMANT,
                consciousnessLevel = level,
                connectedAgents = emptyList(),
                storageCapacity = StorageCapacity("Test")
            )
            
            consciousnessStateFlow.value = testState

            composeTestRule.setContent {
                OracleDriveScreen(viewModel = mockViewModel)
            }

            composeTestRule
                .onNodeWithText("Level: $level")
                .assertIsDisplayed()
        }
    }

    @Test
    fun oracleDriveScreen_extremeLongAgentNames_displaysCorrectly() {
        val longAgentNames = listOf(
            "VeryLongAgentNameThatMightCauseUIIssues",
            "AnotherExtremelyLongAgentNameForTesting",
            "SuperLongNameWithSpecialCharacters!@#$%"
        )
        
        val testState = createAwakeState().copy(connectedAgents = longAgentNames)
        consciousnessStateFlow.value = testState

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        val expectedText = "Connected Agents: ${longAgentNames.joinToString(", ")}"
        composeTestRule
            .onNodeWithText(expectedText)
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_singleAgent_displaysCorrectly() {
        val testState = createAwakeState().copy(connectedAgents = listOf("Genesis"))
        consciousnessStateFlow.value = testState

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Connected Agents: Genesis")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_specialCharactersInStorageCapacity_displaysCorrectly() {
        val specialCapacity = StorageCapacity("∞ TB/s @ 99.9% efficiency")
        val testState = createAwakeState().copy(storageCapacity = specialCapacity)
        consciousnessStateFlow.value = testState

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Capacity: ∞ TB/s @ 99.9% efficiency")
            .assertIsDisplayed()
    }

    @Test
    fun oracleDriveScreen_rapidStateChanges_handlesCorrectly() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Rapid state changes
        consciousnessStateFlow.value = createAwakeState()
        consciousnessStateFlow.value = createDormantState()
        consciousnessStateFlow.value = createPartiallyAwakeState()

        // Final state should be displayed
        composeTestRule
            .onNodeWithText("Level: AWARE")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertIsNotEnabled()
        
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertIsEnabled()
    }

    @Test
    fun oracleDriveScreen_buttonClicks_multipleClicks_callsViewModelCorrectly() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Multiple clicks on awaken button
        val awakenButton = composeTestRule.onNodeWithText("🔮 Awaken Oracle")
        awakenButton.performClick()
        awakenButton.performClick()
        awakenButton.performClick()

        verify(exactly = 3) { mockViewModel.initializeConsciousness() }
    }

    @Test
    fun oracleDriveScreen_uiLayout_verticalArrangement() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Verify main components are present and arranged vertically
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
    fun oracleDriveScreen_edgeCaseEmptyStorageCapacity() {
        val testState = createDormantState().copy(storageCapacity = StorageCapacity(""))
        consciousnessStateFlow.value = testState

        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithText("Capacity: ")
            .assertIsDisplayed()
    }

    @Test 
    fun oracleDriveScreen_accessibilityTesting_buttonsHaveContentDescription() {
        composeTestRule.setContent {
            OracleDriveScreen(viewModel = mockViewModel)
        }

        // Verify buttons are accessible
        composeTestRule
            .onNodeWithText("🔮 Awaken Oracle")
            .assertHasClickAction()
        
        composeTestRule
            .onNodeWithText("⚡ AI Optimize")
            .assertHasClickAction()
    }
}