package dev.aurakai.auraframefx.sandbox.ui.gradle

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

/**
 * Test suite for all sandbox-ui build configuration tests
 * Testing Framework: JUnit 4
 * 
 * This suite runs all tests related to the sandbox-ui build configuration validation.
 */
@RunWith(Suite::class)
@SuiteClasses(
    SandboxUiBuildConfigurationTest::class,
    SandboxUiBuildIntegrationTest::class,
    SandboxUiBuildValidationTest::class
)
class SandboxUiBuildTestSuite