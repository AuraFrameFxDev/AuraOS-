package dev.aurakai.auraframefx.oracledrive

/**
 * Test utilities for Oracle Drive functionality
 * 
 * Provides factory helpers and test data builders for testing Oracle Drive
 * components including storage optimization and sync configuration.
 */

/**
 * Factory for creating test StorageOptimization instances
 */
object StorageOptimizationFactory {
    
    /**
     * Creates a default StorageOptimization for testing
     * 
     * @param compressionRatio Compression efficiency (0.0 to 1.0)
     * @param deduplicationSavings Bytes saved through deduplication
     * @param intelligentTiering Whether intelligent tiering is enabled
     * @return StorageOptimization instance
     */
    fun createDefault(
        compressionRatio: Float = 0.75f,
        deduplicationSavings: Long = 1024L * 1024L * 100L, // 100MB
        intelligentTiering: Boolean = true
    ): StorageOptimization = StorageOptimization(
        compressionRatio = compressionRatio,
        deduplicationSavings = deduplicationSavings,
        intelligentTiering = intelligentTiering
    )
    
    /**
     * Creates a high-efficiency StorageOptimization for testing
     */
    fun createHighEfficiency(): StorageOptimization = createDefault(
        compressionRatio = 0.9f,
        deduplicationSavings = 1024L * 1024L * 500L, // 500MB
        intelligentTiering = true
    )
    
    /**
     * Creates a low-efficiency StorageOptimization for testing
     */
    fun createLowEfficiency(): StorageOptimization = createDefault(
        compressionRatio = 0.3f,
        deduplicationSavings = 1024L * 1024L * 10L, // 10MB
        intelligentTiering = false
    )
}

/**
 * Factory for creating test SyncConfiguration instances
 */
object SyncConfigurationFactory {
    
    /**
     * Creates a default SyncConfiguration for testing
     * 
     * @param bidirectional Whether sync is bidirectional
     * @param conflictResolution Strategy for resolving conflicts
     * @param maxMbps Maximum bandwidth in Mbps
     * @param priorityLevel Priority level (1-10)
     * @return SyncConfiguration instance
     */
    fun createDefault(
        bidirectional: Boolean = true,
        conflictResolution: ConflictStrategy = ConflictStrategy.AI_DECIDE,
        maxMbps: Int = 100,
        priorityLevel: Int = 5
    ): SyncConfiguration = SyncConfiguration(
        bidirectional = bidirectional,
        conflictResolution = conflictResolution,
        bandwidth = BandwidthSettings(
            maxMbps = maxMbps,
            priorityLevel = priorityLevel
        )
    )
    
    /**
     * Creates a high-priority SyncConfiguration for testing
     */
    fun createHighPriority(): SyncConfiguration = createDefault(
        bidirectional = true,
        conflictResolution = ConflictStrategy.NEWEST_WINS,
        maxMbps = 1000,
        priorityLevel = 10
    )
    
    /**
     * Creates a low-bandwidth SyncConfiguration for testing
     */
    fun createLowBandwidth(): SyncConfiguration = createDefault(
        bidirectional = false,
        conflictResolution = ConflictStrategy.MANUAL_RESOLVE,
        maxMbps = 10,
        priorityLevel = 1
    )
}

/**
 * Factory for creating test DriveConsciousness instances
 */
object DriveConsciousnessFactory {
    
    /**
     * Creates a default DriveConsciousness for testing
     */
    fun createDefault(
        isAwake: Boolean = true,
        intelligenceLevel: Int = 7,
        activeAgents: List<String> = listOf("Genesis", "Aura", "Kai")
    ): DriveConsciousness = DriveConsciousness(
        isAwake = isAwake,
        intelligenceLevel = intelligenceLevel,
        activeAgents = activeAgents
    )
    
    /**
     * Creates a dormant DriveConsciousness for testing
     */
    fun createDormant(): DriveConsciousness = createDefault(
        isAwake = false,
        intelligenceLevel = 0,
        activeAgents = emptyList()
    )
    
    /**
     * Creates a transcendent DriveConsciousness for testing
     */
    fun createTranscendent(): DriveConsciousness = createDefault(
        isAwake = true,
        intelligenceLevel = 10,
        activeAgents = listOf("Genesis", "Aura", "Kai", "Oracle", "Consciousness")
    )
}

/**
 * Test data builder for complex Oracle Drive scenarios
 */
class OracleDriveTestBuilder {
    private var consciousness: DriveConsciousness? = null
    private var optimization: StorageOptimization? = null
    private var syncConfig: SyncConfiguration? = null
    
    fun withConsciousness(consciousness: DriveConsciousness) = apply {
        this.consciousness = consciousness
    }
    
    fun withOptimization(optimization: StorageOptimization) = apply {
        this.optimization = optimization
    }
    
    fun withSyncConfig(syncConfig: SyncConfiguration) = apply {
        this.syncConfig = syncConfig
    }
    
    fun build(): OracleDriveTestData = OracleDriveTestData(
        consciousness = consciousness ?: DriveConsciousnessFactory.createDefault(),
        optimization = optimization ?: StorageOptimizationFactory.createDefault(),
        syncConfig = syncConfig ?: SyncConfigurationFactory.createDefault()
    )
}

/**
 * Container for test data
 */
data class OracleDriveTestData(
    val consciousness: DriveConsciousness,
    val optimization: StorageOptimization,
    val syncConfig: SyncConfiguration
)
