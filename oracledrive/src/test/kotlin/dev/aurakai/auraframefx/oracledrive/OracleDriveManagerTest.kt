package dev.aurakai.auraframefx.oracledrive

import dev.aurakai.auraframefx.oracledrive.api.OracleDriveApi
import dev.aurakai.auraframefx.oracledrive.security.DriveSecurityManager
import dev.aurakai.auraframefx.oracledrive.storage.CloudStorageProvider
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
            // Given
            val syncConfig = SyncConfiguration(
                bidirectional = true,
                conflictResolution = ConflictStrategy.AI_DECIDE,
