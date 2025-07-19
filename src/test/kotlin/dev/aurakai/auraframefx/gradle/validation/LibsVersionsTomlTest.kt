package dev.aurakai.auraframefx.gradle.validation

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.io.File
import java.io.FileWriter

/**
 * Comprehensive unit tests for LibsVersionsToml validation functionality.
 * Testing framework: JUnit 4
 * 
 * This test suite validates the structure and content of Gradle version catalog files (libs.versions.toml).
 * It covers happy paths, edge cases, and failure conditions for TOML validation.
 */
class LibsVersionsTomlTest {

    private lateinit var tempTomlFile: File
    private lateinit var validTomlContent: String

    @Before
    fun setUp() {
        tempTomlFile = File.createTempFile("libs.versions", ".toml")
        validTomlContent = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            composeBom = "2024.04.00"
            junit = "4.13.2"
            coreKtx = "1.16.0"
            
            [libraries]
            androidxCoreKtx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
            composeBom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
            testJunit = { module = "junit:junit", version.ref = "junit" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        """.trimIndent()
    }

    @After
    fun tearDown() {
        if (tempTomlFile.exists()) {
            tempTomlFile.delete()
        }
    }

    // Happy Path Tests
    @Test
    fun testValidTomlStructure() {
        // Test that a valid TOML file passes validation
        writeTomlFile(validTomlContent)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Valid TOML should pass validation", result.isValid)
        assertTrue("Valid TOML should have no errors", result.errors.isEmpty())
    }

    @Test
    fun testValidTomlWithAllSections() {
        // Test TOML with all required sections present
        val completeToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            
            [libraries]
            androidxCoreKtx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            
            [bundles]
            compose = ["compose-ui", "compose-material"]
        """.trimIndent()
        
        writeTomlFile(completeToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Complete TOML should pass validation", result.isValid)
    }

    // ... all other tests unchanged ...

    @Test
    fun testValidationResultWithMixedErrorsAndWarnings() {
        // Test validation result with both errors and warnings
        val mixedToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"
            unusedVersion = "1.0.0"
            vulnerableJunit = "4.12"

            [libraries]
            missingRefLib = { module = "junit:junit", version.ref = "missingVersion" }
            vulnerableLib = { module = "junit:junit", version.ref = "vulnerableJunit" }
        """.trimIndent()

        writeTomlFile(mixedToml)

        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()

        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have errors", result.errors.isNotEmpty())
        assertTrue("Should have warnings", result.warnings.isNotEmpty())

        // Test specific error types
        assertTrue("Should report missing version reference",
            result.errors.any { it.contains("Missing version reference") })
        assertTrue("Should report unreferenced version",
            result.warnings.any { it.contains("Unreferenced version") })
        assertTrue("Should report vulnerable version",
            result.warnings.any { it.contains("vulnerable") })
    }

    // Helper Methods
    private fun writeTomlFile(content: String) {
        FileWriter(tempTomlFile).use { writer ->
            writer.write(content)
        }
    }
}