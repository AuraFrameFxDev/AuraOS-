package dev.aurakai.auraframefx.oracledrive

/**
<<<<<<< HEAD
* Test utilities for Oracle Drive components
* Provides factory methods for creating test data
*/
object OracleDriveTestUtils {

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
    * Creates a test StorageOptimization instance with the provided values or sensible defaults.
    *
    * @param compressionRatio Ratio applied by compression expressed as a float between 0.0 and 1.0 (e.g., 0.75 means 75% size after compression).
    * @param deduplicationSavings Estimated bytes saved by deduplication.
    * @param intelligentTiering Whether automatic tiering between storage classes is enabled.
    * @return A StorageOptimization configured with the given compression ratio, deduplication savings, and tiering flag.
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
    * Create a test SyncConfiguration with configurable sync direction, conflict strategy, and bandwidth.
    *
    * @param bidirectional If true, sync is bidirectional; otherwise one-way.
    * @param conflictResolution Strategy to resolve conflicts during synchronization.
    * @param maxMbps Maximum bandwidth for sync in megabits per second (used to build the configuration's bandwidth settings).
    * @param priorityLevel Priority for bandwidth allocation (used together with `maxMbps`).
    * @return A SyncConfiguration configured with the provided options.
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
=======
 * Test utilities for Oracle Drive components
 * Provides factory methods for creating test data
 */
object OracleDriveTestUtils {
    
    /**
     * Build a test DriveFile populated with the given values.
     *
     * The file's content is converted to a byte array and the file size is set to the resulting
     * byte length (in bytes).
     *
     * @param id Identifier for the test file (defaults to "test-file-1").
     * @param name File name (defaults to "test.txt").
     * @param content Textual content to store in the file; converted to bytes using the platform default (defaults to "test content").
     * @param mimeType MIME type of the file (defaults to "text/plain").
     * @return A DriveFile instance with content, size, name, id, and mimeType set.
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
     * Build a FileMetadata test instance with optional overrides.
     *
     * @param userId Identifier for the file owner (default "test-user").
     * @param tags Tags attached to the file (default ["test"]).
     * @param isEncrypted Whether the file is encrypted (default false).
     * @param accessLevel Access control level for the file (default AccessLevel.PRIVATE).
     * @return A FileMetadata populated with the provided values.
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
     * Creates a DriveConsciousness instance for tests with sensible defaults.
     *
     * By default the instance is awake, has an intelligence level of 85, and three
     * active agents: "Kai", "Genesis", and "Aura".
     *
     * @return A test `DriveConsciousness` populated with the provided or default values.
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
     * Creates a test StorageOptimization instance with the given or default settings.
     *
     * @param compressionRatio Compression ratio to apply (0.0 - 1.0).
     * @param deduplicationSavings Estimated deduplication savings in bytes.
     * @param intelligentTiering Whether intelligent tiering is enabled.
     * @return A StorageOptimization populated with the specified values.
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
     * Creates a test instance of `SyncConfiguration` with customizable synchronization settings.
     *
     * @param bidirectional Whether synchronization is bidirectional.
     * @param conflictResolution The strategy to use for resolving conflicts.
     * @param maxMbps The maximum bandwidth in megabits per second.
     * @param priorityLevel The priority level for bandwidth allocation.
     * @return A `SyncConfiguration` instance with the specified or default parameters.
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
>>>>>>> origin/coderabbitai/chat/e19563d
