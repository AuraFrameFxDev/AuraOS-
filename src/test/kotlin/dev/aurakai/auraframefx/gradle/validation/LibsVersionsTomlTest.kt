package dev.aurakai.auraframefx.gradle.validation

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

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

    // Required Sections Tests
    @Test
    fun testRequiredVersionsSection() {
        // Test that versions section is required
        val tomlWithoutVersions = """
            [libraries]
            androidxCoreKtx = { module = "androidx.core:core-ktx", version = "1.0.0" }
        """.trimIndent()
        
        writeTomlFile(tomlWithoutVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML without versions section should fail validation", result.isValid)
        assertTrue("Should report missing versions section", 
            result.errors.any { it.contains("versions section is required") })
    }

    @Test
    fun testRequiredLibrariesSection() {
        // Test that libraries section is required
        val tomlWithoutLibraries = """
            [versions]
            agp = "8.11.1"
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithoutLibraries)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML without libraries section should fail validation", result.isValid)
        assertTrue("Should report missing libraries section", 
            result.errors.any { it.contains("libraries section is required") })
    }

    @Test
    fun testOptionalPluginsSection() {
        // Test that plugins section is optional
        val tomlWithoutPlugins = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            androidxCoreKtx = { module = "androidx.core:core-ktx", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithoutPlugins)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("TOML without plugins section should pass validation", result.isValid)
    }

    // Version Format Validation Tests
    @Test
    fun testVersionFormatValidation() {
        // Test that version strings follow semantic versioning
        val tomlWithInvalidVersions = """
            [versions]
            agp = "invalid.version"
            kotlin = "2.0.0"
            badVersion = "not-a-version"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(tomlWithInvalidVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with invalid version formats should fail validation", result.isValid)
        assertTrue("Should report invalid version format", 
            result.errors.any { it.contains("Invalid version format") })
    }

    @Test
    fun testValidVersionFormats() {
        // Test various valid version formats
        val tomlWithValidVersions = """
            [versions]
            semantic = "1.2.3"
            withBuild = "1.2.3-alpha"
            withSnapshot = "1.2.3-SNAPSHOT"
            withPlus = "1.2.+"
            range = "[1.0.0,2.0.0)"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "semantic" }
        """.trimIndent()
        
        writeTomlFile(tomlWithValidVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Valid version formats should pass validation", result.isValid)
    }

    // Duplicate Key Detection Tests
    @Test
    fun testDuplicateVersionKeys() {
        // Test that duplicate version keys are detected
        val tomlWithDuplicates = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            agp = "8.11.2"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(tomlWithDuplicates)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with duplicate keys should fail validation", result.isValid)
        assertTrue("Should report duplicate keys", 
            result.errors.any { it.contains("Duplicate key") })
    }

    @Test
    fun testDuplicateLibraryKeys() {
        // Test that duplicate library keys are detected
        val tomlWithDuplicateLibraries = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "agp" }
            testJunit = { module = "junit:junit", version = "4.13.2" }
        """.trimIndent()
        
        writeTomlFile(tomlWithDuplicateLibraries)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with duplicate library keys should fail validation", result.isValid)
        assertTrue("Should report duplicate library keys", 
            result.errors.any { it.contains("Duplicate key") })
    }

    // Version Reference Validation Tests
    @Test
    fun testUnreferencedVersions() {
        // Test that versions defined but not referenced are flagged
        val tomlWithUnreferencedVersions = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            unusedVersion = "1.0.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "kotlin" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithUnreferencedVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("TOML with unreferenced versions should pass validation but warn", result.isValid)
        assertTrue("Should report unreferenced version", 
            result.warnings.any { it.contains("Unreferenced version") })
    }

    @Test
    fun testMissingVersionReferences() {
        // Test that references to non-existent versions are detected
        val tomlWithMissingReferences = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "missingVersion" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithMissingReferences)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with missing version references should fail validation", result.isValid)
        assertTrue("Should report missing version reference", 
            result.errors.any { it.contains("Missing version reference") })
    }

    // Library Module Format Tests
    @Test
    fun testLibraryModuleFormat() {
        // Test that library module names follow expected format
        val tomlWithInvalidModules = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            invalidModule = { module = "invalid-module-name", version.ref = "agp" }
            validModule = { module = "androidx.core:core-ktx", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithInvalidModules)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with invalid module format should fail validation", result.isValid)
        assertTrue("Should report invalid module format", 
            result.errors.any { it.contains("Invalid module format") })
    }

    @Test
    fun testLibraryGroupNameFormat() {
        // Test library using group/name format
        val tomlWithGroupName = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            validLibrary = { group = "androidx.core", name = "core-ktx", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithGroupName)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("TOML with group/name format should pass validation", result.isValid)
    }

    // Plugin ID Format Tests
    @Test
    fun testPluginIdFormat() {
        // Test that plugin IDs follow expected format
        val tomlWithInvalidPlugins = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "agp" }
            
            [plugins]
            validPlugin = { id = "com.android.application", version.ref = "agp" }
            invalidPlugin = { id = "invalid_plugin_id", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithInvalidPlugins)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with invalid plugin ID format should fail validation", result.isValid)
        assertTrue("Should report invalid plugin ID format", 
            result.errors.any { it.contains("Invalid plugin ID format") })
    }

    @Test
    fun testValidPluginIds() {
        // Test valid plugin ID formats
        val tomlWithValidPlugins = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "agp" }
            
            [plugins]
            androidApp = { id = "com.android.application", version.ref = "agp" }
            kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(tomlWithValidPlugins)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Valid plugin IDs should pass validation", result.isValid)
    }

    // Critical Dependencies Tests
    @Test
    fun testCriticalDependenciesPresent() {
        // Test that critical dependencies are present
        val tomlWithoutCriticalDeps = """
            [versions]
            agp = "8.11.1"
            
            [libraries]
            someOtherLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithoutCriticalDeps)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("TOML without critical dependencies should pass validation but warn", result.isValid)
        assertTrue("Should report missing critical dependencies", 
            result.warnings.any { it.contains("Missing critical dependency") })
    }

    @Test
    fun testCriticalDependenciesPresent_WithTestDeps() {
        // Test that test dependencies are recognized as critical
        val tomlWithTestDeps = """
            [versions]
            agp = "8.11.1"
            junit = "4.13.2"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "junit" }
        """.trimIndent()
        
        writeTomlFile(tomlWithTestDeps)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("TOML with test dependencies should pass validation", result.isValid)
    }

    // Version Compatibility Tests
    @Test
    fun testVersionCompatibility() {
        // Test that version combinations are compatible
        val tomlWithIncompatibleVersions = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version = "4.13.2" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(tomlWithIncompatibleVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("TOML with incompatible versions should fail validation", result.isValid)
        assertTrue("Should report version incompatibility", 
            result.errors.any { it.contains("Version incompatibility") })
    }

    @Test
    fun testCompatibleVersions() {
        // Test that compatible versions pass validation
        val tomlWithCompatibleVersions = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version = "4.13.2" }
            
            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(tomlWithCompatibleVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Compatible versions should pass validation", result.isValid)
    }

    // Edge Cases Tests
    @Test
    fun testEmptyFile() {
        // Test handling of empty TOML file
        writeTomlFile("")
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("Empty TOML file should fail validation", result.isValid)
        assertTrue("Should report empty file", 
            result.errors.any { it.contains("Empty or invalid TOML file") })
    }

    @Test
    fun testMalformedToml() {
        // Test handling of malformed TOML syntax
        val malformedToml = """
            [versions
            agp = "8.11.1"
            kotlin = 2.0.0
            
            [libraries]
            testJunit = { module = "junit:junit" version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(malformedToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("Malformed TOML should fail validation", result.isValid)
        assertTrue("Should report syntax error", 
            result.errors.any { it.contains("Syntax error") })
    }

    @Test
    fun testFileNotFound() {
        // Test handling of non-existent file
        val nonExistentFile = File("non_existent_file.toml")
        
        val validator = LibsVersionsTomlValidator(nonExistentFile)
        val result = validator.validate()
        
        assertFalse("Non-existent file should fail validation", result.isValid)
        assertTrue("Should report file not found", 
            result.errors.any { it.contains("TOML file does not exist") })
    }

    // Advanced Validation Tests
    @Test
    fun testVersionRangeValidation() {
        // Test that version ranges are properly validated
        val tomlWithVersionRanges = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.+"
            compose = "[1.0.0,2.0.0)"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(tomlWithVersionRanges)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Version ranges should be valid", result.isValid)
    }

    @Test
    fun testSecurityVulnerabilityCheck() {
        // Test that known vulnerable versions are flagged
        val tomlWithVulnerableVersions = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "oldJunit" }
        """.trimIndent()
        
        writeTomlFile(tomlWithVulnerableVersions)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Should detect vulnerable versions", 
            result.warnings.any { it.contains("vulnerable version") })
    }

    @Test
    fun testBundleValidation() {
        // Test that bundles reference valid libraries
        val tomlWithBundles = """
            [versions]
            agp = "8.11.1"
            compose = "1.0.0"
            
            [libraries]
            composeUi = { module = "androidx.compose.ui:ui", version.ref = "compose" }
            composeMaterial = { module = "androidx.compose.material:material", version.ref = "compose" }
            
            [bundles]
            compose = ["composeUi", "composeMaterial"]
            invalid = ["nonExistentLibrary"]
        """.trimIndent()
        
        writeTomlFile(tomlWithBundles)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("Bundle with invalid library reference should fail validation", result.isValid)
        assertTrue("Should report invalid bundle reference", 
            result.errors.any { it.contains("Invalid bundle reference") })
    }

    // Validation Result Tests
    @Test
    fun testValidationResultDetails() {
        // Test that validation results contain proper details
        writeTomlFile(validTomlContent)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Validation result should not be null", result)
        assertTrue("Should have validation timestamp", result.timestamp > 0)
        assertNotNull("Should have error list", result.errors)
        assertNotNull("Should have warning list", result.warnings)
    }

    @Test
    fun testValidationResultWithErrorsAndWarnings() {
        // Test validation result with both errors and warnings
        val problematicToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"
            unusedVersion = "1.0.0"
            
            [libraries]
            testJunit = { module = "junit:junit", version.ref = "missingVersion" }
        """.trimIndent()
        
        writeTomlFile(problematicToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have errors", result.errors.isNotEmpty())
        assertTrue("Should have warnings", result.warnings.isNotEmpty())
    }

    // Helper Methods
    private fun writeTomlFile(content: String) {
        FileWriter(tempTomlFile).use { writer ->
            writer.write(content)
        }
    }

    // ============================================================================
    // Additional Comprehensive Test Coverage - Extended Edge Cases
    // Testing framework: JUnit 4 (maintaining consistency with existing tests)
    // ============================================================================

    // File Encoding and Character Set Tests
    @Test
    fun testFileWithDifferentEncodings() {
        // Test handling of files with different character encodings
        val unicodeToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            # Special chars: ñáéíóú αβγδε 中文 🚀
            
            [libraries]
            specialLib = { module = "com.example:special-chars", version.ref = "agp" }
        """.trimIndent()
        
        writeTomlFile(unicodeToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Should handle Unicode characters", result)
        assertTrue("Unicode should not break validation", result.isValid)
    }

    @Test
    fun testFileWithBOM() {
        // Test handling of files with Byte Order Mark (BOM)
        val bomContent = "\uFEFF" + validTomlContent
        writeTomlFile(bomContent)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Should handle BOM characters", result)
        assertTrue("BOM should not affect validation", result.isValid)
    }

    @Test
    fun testVeryLargeFileHandling() {
        // Test handling of extremely large TOML files
        val largeBuilder = StringBuilder()
        largeBuilder.append("[versions]\n")
        
        // Create 1000 version entries
        for (i in 1..1000) {
            largeBuilder.append("version$i = \"1.$i.0\"\n")
        }
        
        largeBuilder.append("\n[libraries]\n")
        for (i in 1..1000) {
            largeBuilder.append("lib$i = { module = \"com.example:lib$i\", version.ref = \"version$i\" }\n")
        }
        
        writeTomlFile(largeBuilder.toString())
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val startTime = System.currentTimeMillis()
        val result = validator.validate()
        val duration = System.currentTimeMillis() - startTime
        
        assertNotNull("Should handle large files", result)
        assertTrue("Large file validation should complete within 10 seconds", duration < 10000)
    }

    @Test
    fun testMultipleValidatorInstances() {
        // Test memory usage with multiple validator instances
        writeTomlFile(validTomlContent)
        
        val validators = mutableListOf<LibsVersionsTomlValidator>()
        repeat(50) {
            validators.add(LibsVersionsTomlValidator(tempTomlFile))
        }
        
        validators.forEach { validator ->
            val result = validator.validate()
            assertTrue("Each validator instance should work correctly", result.isValid)
        }
    }

    @Test
    fun testTomlArrayOfTables() {
        // Test TOML array of tables syntax
        val arrayTablesToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            
            [libraries]
            testLib = { module = "junit:junit", version.ref = "agp" }
            
            [[custom_configs]]
            name = "debug"
            optimize = false
            
            [[custom_configs]]
            name = "release"
            optimize = true
        """.trimIndent()
        
        writeTomlFile(arrayTablesToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Should handle array of tables", result)
        // Should be valid even with custom sections
        assertTrue("Array of tables should not break validation", result.isValid)
    }

    @Test
    fun testTomlMultilineStrings() {
        // Test TOML multiline string handling
        val multilineToml = "[versions]\n" +
            "agp = \"8.11.1\"\n" +
            "description = \"\"\"\n" +
            "This is a multiline\n" +
            "version description\n" +
            "with multiple lines\"\"\"\n\n" +
            "[libraries]\n" +
            "testLib = { module = \"junit:junit\", version.ref = \"agp\" }\n"
        
        writeTomlFile(multilineToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Should handle multiline strings", result)
        assertTrue("Multiline strings should not break validation", result.isValid)
    }

    @Test
    fun testSymbolicLinkHandling() {
        // Test handling of symbolic links (if supported by the system)
        writeTomlFile(validTomlContent)
        
        val linkFile = File.createTempFile("link", ".toml")
        linkFile.delete() // Remove the file so we can create a symbolic link
        
        try {
            // Create symbolic link using Java NIO
            val linkPath = linkFile.toPath()
            val targetPath = tempTomlFile.toPath()
            Files.createSymbolicLink(linkPath, targetPath)
            
            val validator = LibsVersionsTomlValidator(linkFile)
            val result = validator.validate()
            
            assertTrue("Should handle symbolic links", result.isValid)
            
            linkFile.delete()
        } catch (e: UnsupportedOperationException) {
            // Symbolic links not supported on this system, skip test
        } catch (e: Exception) {
            // Other error, just ensure cleanup
            if (linkFile.exists()) linkFile.delete()
        }
    }

    @Test
    fun testPluginWithAlternativeVersionFormats() {
        // Test plugins with various version specification formats
        val pluginVariationsToml = """
            [versions]
            kotlinVersion = "1.9.20"
            gradleVersion = "8.5"
            
            [libraries]
            kotlinStdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlinVersion" }
            
            [plugins]
            kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlinVersion" }
            kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlinVersion" }
            androidApp = { id = "com.android.application", version.ref = "gradleVersion" }
            gradleVersions = { id = "com.github.ben-manes.versions", version = "0.50.0" }
            detekt = { id = "io.gitlab.arturbosch.detekt", version = "1.23.4" }
        """.trimIndent()
        
        writeTomlFile(pluginVariationsToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertTrue("Plugin variations should be valid", result.isValid)
    }

    @Test
    fun testValidationResultMetadata() {
        // Test that validation results contain proper metadata
        writeTomlFile(validTomlContent)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertNotNull("Result should have metadata", result)
        assertTrue("Timestamp should be recent", 
            result.timestamp > System.currentTimeMillis() - 5000)
        assertNotNull("Errors list should exist", result.errors)
        assertNotNull("Warnings list should exist", result.warnings)
        
        // Test that multiple validations have different timestamps
        Thread.sleep(1) // Ensure different timestamp
        val result2 = validator.validate()
        assertTrue("Second validation should have later timestamp", 
            result2.timestamp >= result.timestamp)
    }

    @Test
    fun testMultipleErrorTypesInSingleFile() {
        // Test file with multiple different types of errors
        val multiErrorToml = """
            [versions]
            agp = "invalid.version"
            kotlin = "2.0.0"
            duplicateKey = "1.0.0"
            duplicateKey = "2.0.0"
            
            [libraries]
            badModule = { module = "invalid-module-format", version.ref = "nonexistent" }
            goodLib = { module = "com.example:good", version.ref = "kotlin" }
            duplicateLib = { module = "com.example:dup", version.ref = "agp" }
            duplicateLib = { module = "com.example:dup2", version.ref = "kotlin" }
            
            [plugins]
            badPlugin = { id = "invalid_plugin_id", version.ref = "kotlin" }
            goodPlugin = { id = "com.example.plugin", version.ref = "kotlin" }
        """.trimIndent()
        
        writeTomlFile(multiErrorToml)
        
        val validator = LibsVersionsTomlValidator(tempTomlFile)
        val result = validator.validate()
        
        assertFalse("Multiple errors should fail validation", result.isValid)
        assertTrue("Should report multiple errors", result.errors.size >= 3)
        
        val allErrors = result.errors.joinToString(" ").lowercase()
        assertTrue("Should detect version format error", 
            allErrors.contains("version") || allErrors.contains("format"))
        assertTrue("Should detect duplicate key error", 
            allErrors.contains("duplicate"))
        assertTrue("Should detect missing reference error", 
            allErrors.contains("reference") || allErrors.contains("missing"))
    }

    @Test
    fun testHighConcurrencyValidation() {
        // Test validation under high concurrency
        writeTomlFile(validTomlContent)
        
        val results = mutableListOf<Boolean>()
        val exceptions = mutableListOf<Exception>()
        val threads = mutableListOf<Thread>()
        
        repeat(20) { threadIndex ->
            val thread = Thread {
                try {
                    repeat(5) {
                        val validator = LibsVersionsTomlValidator(tempTomlFile)
                        val result = validator.validate()
                        synchronized(results) {
                            results.add(result.isValid)
                        }
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        threads.forEach { it.join(5000) } // 5 second timeout
        
        assertTrue("No exceptions should occur during concurrent validation", exceptions.isEmpty())
        assertEquals("All validations should complete", 100, results.size)
        assertTrue("All results should be valid", results.all { it })
    }
}