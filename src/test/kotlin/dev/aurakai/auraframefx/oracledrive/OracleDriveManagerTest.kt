package dev.aurakai.auraframefx.oracledrive

import dev.aurakai.auraframefx.oracledrive.api.OracleDriveApi
import dev.aurakai.auraframefx.oracledrive.security.DriveSecurityManager
import dev.aurakai.auraframefx.oracledrive.storage.CloudStorageProvider
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

/**
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("OracleDriveManager Comprehensive Tests")
class OracleDriveManagerTest {

