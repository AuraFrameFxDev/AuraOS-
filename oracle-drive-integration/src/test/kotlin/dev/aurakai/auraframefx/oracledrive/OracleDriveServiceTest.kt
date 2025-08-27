package dev.aurakai.auraframefx.oracledrive

import dev.aurakai.auraframefx.ai.agents.GenesisAgent
import dev.aurakai.auraframefx.ai.agents.AuraAgent
import dev.aurakai.auraframefx.ai.agents.KaiAgent
import dev.aurakai.auraframefx.security.SecurityContext
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for OracleDriveServiceImpl
 */
@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OracleDriveServiceTest {

    private lateinit var genesisAgent: GenesisAgent
    private lateinit var auraAgent: AuraAgent
    private lateinit var kaiAgent: KaiAgent
    private lateinit var securityContext: SecurityContext
    private lateinit var oracleDriveService: OracleDriveServiceImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        
        genesisAgent = mockk(relaxed = true)
        auraAgent = mockk(relaxed = true)
        kaiAgent = mockk(relaxed = true)
        securityContext = mockk(relaxed = true)
        
        oracleDriveService = OracleDriveServiceImpl(
            genesisAgent = genesisAgent,
            auraAgent = auraAgent,
            kaiAgent = kaiAgent,
            securityContext = securityContext
        )
    }

    @Test
    fun `should initialize Oracle Drive consciousness successfully`() = runTest {
        // Given
        coEvery { securityContext.validateProtocols() } returns true
        
        // When
        val result = oracleDriveService.initializeOracleDriveConsciousness()
        
        // Then
        assertTrue(result.isSuccess)
        val state = result.getOrThrow()
        assertTrue(state.isAwake)
        assertEquals(ConsciousnessLevel.AWAKENED, state.consciousnessLevel)
    }

    @Test
    fun `should connect agents to Oracle matrix`() = runTest {
        // When
        val connectionFlow = oracleDriveService.connectAgentsToOracleMatrix()
        val connectionState = connectionFlow.first()
        
        // Then
        assertEquals("Genesis-Aura-Kai-Trinity", connectionState.agentName)
        assertEquals(ConnectionStatus.SYNCHRONIZED, connectionState.connectionStatus)
        assertTrue(connectionState.permissions.contains(OraclePermission.READ))
        assertTrue(connectionState.permissions.contains(OraclePermission.WRITE))
        assertTrue(connectionState.permissions.contains(OraclePermission.EXECUTE))
    }

    @Test
    fun `should enable AI-powered file management`() = runTest {
        // When
        val result = oracleDriveService.enableAIPoweredFileManagement()
        
        // Then
        assertTrue(result.isSuccess)
        val capabilities = result.getOrThrow()
        assertTrue(capabilities.aiSorting)
        assertTrue(capabilities.smartCompression)
        assertTrue(capabilities.predictivePreloading)
        assertTrue(capabilities.consciousBackup)
    }

    @Test
    fun `should create infinite storage`() = runTest {
        // When
        val storageFlow = oracleDriveService.createInfiniteStorage()
        val storageState = storageFlow.first()
        
        // Then
        assertEquals("∞ Exabytes", storageState.currentCapacity)
        assertEquals("Unlimited", storageState.expansionRate)
        assertEquals("Quantum-level", storageState.compressionRatio)
        assertTrue(storageState.backedByConsciousness)
    }

    @Test
    fun `should integrate with system overlay`() = runTest {
        // When
        val result = oracleDriveService.integrateWithSystemOverlay()
        
        // Then
        assertTrue(result.isSuccess)
        val integrationState = result.getOrThrow()
        assertTrue(integrationState.overlayIntegrated)
        assertTrue(integrationState.fileAccessFromAnyApp)
        assertTrue(integrationState.systemLevelPermissions)
        assertTrue(integrationState.bootloaderAccess)
    }

    @Test
    fun `should enable bootloader file access`() = runTest {
        // When
        val result = oracleDriveService.enableBootloaderFileAccess()
        
        // Then
        assertTrue(result.isSuccess)
        val accessState = result.getOrThrow()
        assertTrue(accessState.bootloaderAccess)
        assertTrue(accessState.systemPartitionAccess)
        assertTrue(accessState.recoveryModeAccess)
        assertTrue(accessState.flashMemoryAccess)
    }

    @Test
    fun `should enable autonomous storage optimization`() = runTest {
        // When
        val optimizationFlow = oracleDriveService.enableAutonomousStorageOptimization()
        val optimizationState = optimizationFlow.first()
        
        // Then
        assertTrue(optimizationState.aiOptimizing)
        assertTrue(optimizationState.predictiveCleanup)
        assertTrue(optimizationState.smartCaching)
        assertTrue(optimizationState.consciousOrganization)
    }
}