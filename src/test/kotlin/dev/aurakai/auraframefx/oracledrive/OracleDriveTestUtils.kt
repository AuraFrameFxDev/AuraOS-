package dev.aurakai.auraframefx.oracledrive

/**
 * Test utilities for Oracle Drive components
 * Provides factory methods for creating test data
 */
object OracleDriveTestUtils {
    
    /**
     * Creates a test instance of `DriveFile` with the specified or default parameters.
     *
     * @param id The unique identifier for the file.
     * @param name The name of the file.
     * @param content The file content as a string, which will be converted to bytes.
     * @param mimeType The MIME type of the file.
     * @return A `DriveFile` instance populated with the provided or default values.
     */
    fun createTestDriveFile(
        id: String = "test-file-1",
        name: String = "test.txt",
        content: String = "test content",
        mimeType: String = "text/plain"
    ): DriveFile {
        val contentBytes = content.toByteArray()
        return DriveFile(
            id = id,
            name = name,
            content = contentBytes,
            size = contentBytes.size.toLong(),
            mimeType = mimeType
        )
    }
    
    /**
     * Creates a test instance of `FileMetadata` with customizable user ID, tags, encryption status, and access level.
     *
     * @param userId The user ID to associate with the metadata. Defaults to "test-user".
     * @param tags A list of tags for the file metadata. Defaults to a single "test" tag.
     * @param isEncrypted Indicates whether the file is encrypted. Defaults to false.
     * @param accessLevel The access level for the file. Defaults to `AccessLevel.PRIVATE`.
     * @return A `FileMetadata` instance populated with the specified or default values.
     */
    fun createTestFileMetadata(
        userId: String = "test-user",
        tags: List<String> = listOf("test"),
        isEncrypted: Boolean = false,
        accessLevel: AccessLevel = AccessLevel.PRIVATE
    ): FileMetadata {
        return FileMetadata(
            userId = userId,
            tags = tags,
            isEncrypted = isEncrypted,
            accessLevel = accessLevel
        )
    }
    
    /**
     * Creates a test instance of `DriveConsciousness` with specified or default values.
     *
     * @param isAwake Whether the drive is awake.
     * @param intelligenceLevel The intelligence level of the drive.
     * @param activeAgents List of active agent names.
     * @return A `DriveConsciousness` instance populated with the provided or default values.
     */
    fun createTestDriveConsciousness(
        isAwake: Boolean = true,
        intelligenceLevel: Int = 85,
        activeAgents: List<String> = listOf("Kai", "Genesis", "Aura")
    ): DriveConsciousness {
        return DriveConsciousness(
            isAwake = isAwake,
            intelligenceLevel = intelligenceLevel,
            activeAgents = activeAgents
        )
    }
    
    /**
     * Creates a test instance of `StorageOptimization` with specified or default parameters.
     *
     * @param compressionRatio The compression ratio to use for the test instance.
     * @param deduplicationSavings The deduplication savings value in bytes.
     * @param intelligentTiering Whether intelligent tiering is enabled.
     * @return A `StorageOptimization` instance populated with the provided or default values.
     */
    fun createTestStorageOptimization(
        compressionRatio: Float = 0.75f,
        deduplicationSavings: Long = 1024L,
        intelligentTiering: Boolean = true
    ): StorageOptimization {
        return StorageOptimization(
            compressionRatio = compressionRatio,
            deduplicationSavings = deduplicationSavings,
            intelligentTiering = intelligentTiering
        )
    }
    
    /**
     * Creates a test instance of `SyncConfiguration` with customizable synchronization direction, conflict resolution strategy, and bandwidth settings.
     *
     * @param bidirectional Whether synchronization is bidirectional.
     * @param conflictResolution The strategy used to resolve synchronization conflicts.
     * @param maxMbps The maximum bandwidth in megabits per second.
     * @param priorityLevel The priority level for bandwidth allocation.
     * @return A `SyncConfiguration` instance populated with the specified or default values.
     */
    fun createTestSyncConfiguration(
        bidirectional: Boolean = true,
        conflictResolution: ConflictStrategy = ConflictStrategy.AI_DECIDE,
        maxMbps: Int = 100,
        priorityLevel: Int = 5
    ): SyncConfiguration {
        return SyncConfiguration(
            bidirectional = bidirectional,
            conflictResolution = conflictResolution,
            bandwidth = BandwidthSettings(maxMbps, priorityLevel)
        )
    }
}