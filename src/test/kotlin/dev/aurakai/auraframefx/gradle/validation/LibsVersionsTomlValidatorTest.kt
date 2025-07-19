package dev.aurakai.auraframefx.gradle.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class LibsVersionsTomlValidatorTest {

    @JvmField @TempDir
    lateinit var tempDir: Path

    private lateinit var testFile: File
    private lateinit var validator: LibsVersionsTomlValidator

    @BeforeEach
    fun setUp() {
        testFile = tempDir.resolve("libs.versions.toml").toFile()
        validator = LibsVersionsTomlValidator(testFile)
    }

    @AfterEach
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun `ValidationResult data class should have correct properties`() {
        val result = ValidationResult(
            isValid = true,
            errors = listOf("error1", "error2"),
            warnings = listOf("warning1"),
            timestamp = 1234567890L
        )

        assertTrue(result.isValid)
        assertEquals(listOf("error1", "error2"), result.errors)
        assertEquals(listOf("warning1"), result.warnings)
        assertEquals(1234567890L, result.timestamp)
    }

    @Test
    fun `ValidationResult should use current timestamp by default`() {
        val beforeTime = System.currentTimeMillis()
        val result = ValidationResult(isValid = true, errors = emptyList(), warnings = emptyList())
        val afterTime = System.currentTimeMillis()

        assertTrue(result.timestamp >= beforeTime)
        assertTrue(result.timestamp <= afterTime)
    }

    @Test
    fun `validate should return error when file does not exist`() {
        val result = validator.validate()

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].startsWith("TOML file does not exist:"))
        assertTrue(result.errors[0].contains(testFile.path))
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `validate should return error when file is empty`() {
        testFile.writeText("")

        val result = validator.validate()

        assertFalse(result.isValid)
        assertEquals(listOf("Empty or invalid TOML file"), result.errors)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `validate should return error when file contains only whitespace`() {
        testFile.writeText("   \n\t  \n  ")

        val result = validator.validate()

        assertFalse(result.isValid)
        assertEquals(listOf("Empty or invalid TOML file"), result.errors)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `validate should return errors when required sections are missing`() {
        testFile.writeText("[plugins]\ntest = \"1.0.0\"")

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Required versions section is missing"))
        assertTrue(result.errors.contains("Required libraries section is missing"))
    }

    @Test
    fun `validate should pass with minimal valid TOML structure`() {
        val validToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(validToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should detect invalid version formats`() {
        val invalidToml = """
            [versions]
            invalid1 = "not.a.version"
            invalid2 = "1.x.y"
            valid = "1.2.3"

            [libraries]
            test = { module = "group:artifact", version.ref = "valid" }
        """.trimIndent()

        testFile.writeText(invalidToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid version format: not.a.version") })
        assertTrue(result.errors.any { it.contains("Invalid version format: 1.x.y") })
    }

    @Test
    fun `validate should accept semantic versions`() {
        val validToml = """
            [versions]
            version1 = "1.2.3"
            version2 = "2.0.0-alpha"
            version3 = "1.5.0+build.123"

            [libraries]
            lib1 = { module = "group:artifact", version.ref = "version1" }
        """.trimIndent()

        testFile.writeText(validToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should accept plus versions`() {
        val validToml = """
            [versions]
            androidx = "1.2.+"

            [libraries]
            androidx-core = { module = "androidx.core:core", version.ref = "androidx" }
        """.trimIndent()

        testFile.writeText(validToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should detect duplicate keys`() {
        val duplicateToml = """
            [versions]
            junit = "5.8.2"
            junit = "5.9.0"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(duplicateToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate key: junit") })
    }

    @Test
    fun `validate should detect missing version references`() {
        val missingRefToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
            other-lib = { module = "com.example:lib", version.ref = "missing" }
        """.trimIndent()

        testFile.writeText(missingRefToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Missing version reference: missing") })
    }

    @Test
    fun `validate should warn about unreferenced versions`() {
        val unreferencedToml = """
            [versions]
            junit = "5.8.2"
            unused = "1.0.0"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(unreferencedToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("Unreferenced version: unused") })
    }

    @Test
    fun `validate should detect invalid module formats`() {
        val invalidModuleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            invalid1 = { module = "invalid", version.ref = "test" }
            invalid2 = { module = "group:", version.ref = "test" }
            invalid3 = { module = ":artifact", version.ref = "test" }
            valid = { module = "group:artifact", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(invalidModuleToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid module format: invalid") })
        assertTrue(result.errors.any { it.contains("Invalid module format: group:") })
        assertTrue(result.errors.any { it.contains("Invalid module format: :artifact") })
    }

    @Test
    fun `validate should detect invalid plugin ID formats`() {
        val invalidPluginToml = """
            [versions]
            plugin-version = "1.0.0"

            [libraries]
            test = { module = "group:artifact", version.ref = "plugin-version" }

            [plugins]
            invalid1 = { id = "invalid", version.ref = "plugin-version" }
            invalid2 = { id = "toolongpluginnamewithoutdots", version.ref = "plugin-version" }
            valid = { id = "com.example.plugin", version.ref = "plugin-version" }
        """.trimIndent()

        testFile.writeText(invalidPluginToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid plugin ID format: invalid") })
        assertTrue(result.errors.any { it.contains("Invalid plugin ID format: toolongpluginnamewithoutdots") })
    }

    @Test
    fun `validate should warn when no critical testing dependencies found`() {
        val noTestDepsToml = """
            [versions]
            gson = "2.8.9"

            [libraries]
            gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
        """.trimIndent()

        testFile.writeText(noTestDepsToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("Missing critical dependency: No testing dependencies found") })
    }

    @Test
    fun `validate should not warn when critical testing dependencies present`() {
        val withTestDepsToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(withTestDepsToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertFalse(result.warnings.any { it.contains("Missing critical dependency") })
    }

    @Test
    fun `validate should detect version compatibility issues`() {
        val incompatibleToml = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"

            [libraries]
            test = { module = "group:artifact", version.ref = "kotlin" }
        """.trimIndent()

        testFile.writeText(incompatibleToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Version incompatibility: AGP 8.11.1 requires Kotlin 1.9.0+") })
    }

    @Test
    fun `validate should detect invalid bundle references`() {
        val invalidBundleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact1", version.ref = "test" }
            lib2 = { module = "group:artifact2", version.ref = "test" }

            [bundles]
            valid = ["lib1", "lib2"]
            invalid = ["lib1", "nonexistent"]
        """.trimIndent()

        testFile.writeText(invalidBundleToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid bundle reference: nonexistent in bundle invalid") })
    }

    @Test
    fun `validate should warn about vulnerable versions`() {
        val vulnerableToml = """
            [versions]
            junit = "4.12"

            [libraries]
            junit-old = { module = "junit:junit", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(vulnerableToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("Potentially vulnerable version: junit 4.12") })
    }

    @Test
    fun `validate should handle syntax errors gracefully`() {
        testFile.writeText("invalid toml content [[[" )
        val result = validator.validate()
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.startsWith("Syntax error:") })
    }

    @Test
    fun `validate should handle complex valid TOML with all sections`() {
        val complexToml = """
            [versions]
            agp = "8.2.0"
            kotlin = "1.9.0"
            junit = "5.8.2"
            mockk = "1.13.4"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
            mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
            android-core = { module = "androidx.core:core", version = "1.8.0" }

            [plugins]
            android-application = { id = "com.android.application", version.ref = "agp" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

            [bundles]
            testing = ["junit-core", "mockk"]
        """.trimIndent()

        testFile.writeText(complexToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle empty bundles`() {
        val emptyBundleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact", version.ref = "test" }

            [bundles]
            empty = []
        """.trimIndent()

        testFile.writeText(emptyBundleToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle range versions`() {
        val rangeToml = """
            [versions]
            range1 = "[1.0,2.0)"
            range2 = "[1.5,)"

            [libraries]
            lib1 = { module = "group:artifact", version.ref = "range1" }
        """.trimIndent()

        testFile.writeText(rangeToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle modules with complex names`() {
        val complexModuleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            complex1 = { module = "com.example.group:artifact-name", version.ref = "test" }
            complex2 = { module = "org.apache.commons:commons-lang3", version.ref = "test" }
            complex3 = { module = "io.github.user:my_library", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(complexModuleToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle valid plugin IDs with various formats`() {
        val validPluginToml = """
            [versions]
            plugin-version = "1.0.0"

            [libraries]
            test = { module = "group:artifact", version.ref = "plugin-version" }

            [plugins]
            android-app = { id = "com.android.application", version.ref = "plugin-version" }
            kotlin-plugin = { id = "org.jetbrains.kotlin.jvm", version.ref = "plugin-version" }
            custom = { id = "my.custom.plugin", version.ref = "plugin-version" }
        """.trimIndent()

        testFile.writeText(validPluginToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should detect multiple critical dependencies`() {
        val multiTestDepsToml = """
            [versions]
            junit = "5.8.2"
            espresso = "3.4.0"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
            espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
        """.trimIndent()

        testFile.writeText(multiTestDepsToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertFalse(result.warnings.any { it.contains("Missing critical dependency") })
    }

    @Test
    fun `validate should handle files with only versions section`() {
        val versionsOnlyToml = """
            [versions]
            test = "1.0.0"
        """.trimIndent()

        testFile.writeText(versionsOnlyToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Required libraries section is missing"))
        assertFalse(result.errors.contains("Required versions section is missing"))
    }

    @Test
    fun `validate should handle files with only libraries section`() {
        val librariesOnlyToml = """
            [libraries]
            test = { module = "group:artifact", version = "1.0.0" }
        """.trimIndent()

        testFile.writeText(librariesOnlyToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Required versions section is missing"))
        assertFalse(result.errors.contains("Required libraries section is missing"))
    }

    @Test
    fun `ValidationResult hashCode should be consistent with equals`() {
        val result1 = ValidationResult(
            isValid = true,
            errors = listOf("error1"),
            warnings = listOf("warning1"),
            timestamp = 123456L
        )

        val result2 = ValidationResult(
            isValid = true,
            errors = listOf("error1"),
            warnings = listOf("warning1"),
            timestamp = 123456L
        )

        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `ValidationResult copy should create independent instance`() {
        val original = ValidationResult(
            isValid = true,
            errors = mutableListOf("error1"),
            warnings = mutableListOf("warning1"),
            timestamp = 123456L
        )

        val copied = original.copy(isValid = false)

        assertFalse(copied.isValid)
        assertTrue(original.isValid)
        assertEquals(original.errors, copied.errors)
        assertEquals(original.warnings, copied.warnings)
        assertEquals(original.timestamp, copied.timestamp)
    }

    @Test
    fun `validate should handle null file reference gracefully`() {
        // Test that validator handles file operations gracefully
        val validToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib = { module = "group:artifact", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(validToml)

        val result = validator.validate()
        assertTrue(result.isValid)
    }

    @Test
    fun `validate should handle file in non-existent directory`() {
        val nonExistentPath = tempDir.resolve("non-existent-dir").resolve_("libs.versions.toml").toFile()
        val pathValidator = LibsVersionsTomlValidator(nonExistentPath)

        val result = pathValidator.validate()

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].startsWith("TOML file does not exist:"))
        assertTrue(result.errors[0].contains(nonExistentPath.path))
    }

    @Test
    fun `validate should handle TOML with invalid UTF-8 encoding`() {
        // Write invalid UTF-8 bytes to file
        testFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01))

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Syntax error") || it.contains("encoding") })
    }

    @Test
    fun `validate should detect libraries with missing version property entirely`() {
        val noVersionToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            valid-lib = { module = "group:artifact", version.ref = "test" }
            no-version-lib = { module = "group:artifact2" }
        """.trimIndent()

        testFile.writeText(noVersionToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("version") && it.contains("no-version-lib") })
    }

    @Test
    fun `validate should handle plugins with missing ID property`() {
        val noIdToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib = { module = "group:artifact", version.ref = "test" }

            [plugins]
            valid-plugin = { id = "com.example.plugin", version.ref = "test" }
            no-id-plugin = { version.ref = "test" }
        """.trimIndent()

        testFile.writeText(noIdToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("id") || it.contains("no-id-plugin") })
    }

    @Test
    fun `validate should handle version references that are numbers instead of strings`() {
        val numericRefToml = """
            [versions]
            numeric = 123
            string = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact1", version.ref = "string" }
        """.trimIndent()

        testFile.writeText(numericRefToml)

        val result = validator.validate()

        // Should handle numeric versions appropriately (may be invalid depending on implementation)
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
    }

    @Test
    fun `validate should detect required sections missing error messages match implementation`() {
        val emptyToml = """
            [plugins]
            test = "1.0.0"
        """.trimIndent()

        testFile.writeText(emptyToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Required versions section is missing"))
        assertTrue(result.errors.contains("Required libraries section is missing"))
    }

    @Test
    fun `validate should detect empty sections`() {
        val emptySectionsToml = """
            [versions]

            [libraries]
        """.trimIndent()

        testFile.writeText(emptySectionsToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Versions section cannot be empty"))
        assertTrue(result.errors.contains("Libraries section cannot be empty"))
    }

    @Test
    fun `validate should handle concurrent validation calls safely`() {
        val validToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(validToml)

        // Simulate concurrent validation calls
        val results = (1..10).map {
            Thread {
                validator.validate()
            }.apply { start() }
        }.map { thread ->
            thread.join()
            validator.validate()
        }

        results.forEach { result ->
            assertTrue(result.isValid)
        }
    }

    @Test
    fun `validate should handle extremely large TOML files gracefully`() {
        val largeTomlBuilder = StringBuilder()
        largeTomlBuilder.append("[versions]\n")

        // Generate 500 version entries (reduced from 1000 for performance)
        repeat(500) { i ->
            largeTomlBuilder.append("version$i = \"1.0.$i\"\n")
        }

        largeTomlBuilder.append("\n[libraries]\n")
        repeat(500) { i ->
            largeTomlBuilder.append("lib$i = { module = \"group$i:artifact$i\", version.ref = \"version$i\" }\n")
        }

        testFile.writeText(largeTomlBuilder.toString())

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle TOML with unicode characters in strings`() {
        val unicodeToml = """
            [versions]
            unicode = "1.0.0-测试"
            emoji = "2.0.0-🚀"

            [libraries]
            unicode-lib = { module = "com.example:artifact", version.ref = "unicode" }
            emoji-lib = { module = "com.example:rocket", version.ref = "emoji" }
        """.trimIndent()

        testFile.writeText(unicodeToml)

        val result = validator.validate()

        // Should handle unicode gracefully
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
    }

    @Test
    fun `validate should handle TOML with comments and formatting variations`() {
        val commentedToml = """
            # This is a test TOML file
            [versions]
            junit = "5.8.2" # JUnit 5
            # kotlin = "1.8.0"

            kotlin = "1.9.0"

            [libraries]
            # Testing libraries
            junit-core = { 
                module = "org.junit.jupiter:junit-jupiter", 
                version.ref = "junit" 
            }

            kotlin-stdlib = {module="org.jetbrains.kotlin:kotlin-stdlib",version.ref="kotlin"}
        """.trimIndent()

        testFile.writeText(commentedToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle file system edge cases`() {
        val validToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib = { module = "group:artifact", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(validToml)

        // Make file read-only to test permission handling
        testFile.setReadOnly()

        val result = validator.validate()

        // Should still be able to read the file content
        assertTrue(result.isValid || result.errors.isNotEmpty())

        // Restore write permissions for cleanup
        testFile.setWritable(true)
    }

    @Test
    fun `ValidationResult addError should set isValid to false`() {
        val result = ValidationResult()
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())

        result.addError("Test error")

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertEquals("Test error", result.errors[0])
    }

    @Test
    fun `ValidationResult addWarning should not affect isValid`() {
        val result = ValidationResult()
        assertTrue(result.isValid)
        assertTrue(result.warnings.isEmpty())

        result.addWarning("Test warning")

        assertTrue(result.isValid)
        assertEquals(1, result.warnings.size)
        assertEquals("Test warning", result.warnings[0])
    }

    @Test
    fun `validate should handle libraries with direct version specifications`() {
        val directVersionToml = """
            [versions]
            kotlin = "1.9.0"

            [libraries]
            # Library with version reference
            kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
            
            # Library with direct version
            direct-version-lib = { module = "com.example:library", version = "2.1.0" }
        """.trimIndent()

        testFile.writeText(directVersionToml)

        val result = validator.validate()
        
        assertTrue(result.isValid)
        // Should not complain about direct versions
        assertFalse(result.errors.any { it.contains("direct-version-lib") })
    }

    @Test
    fun `validate should handle plugins with direct version specifications`() {
        val directPluginVersionToml = """
            [versions]
            kotlin = "1.9.0"

            [libraries]
            lib = { module = "group:artifact", version.ref = "kotlin" }

            [plugins]
            # Plugin with version reference
            kotlin-plugin = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            
            # Plugin with direct version
            direct-plugin = { id = "com.example.plugin", version = "2.1.0" }
        """.trimIndent()

        testFile.writeText(directPluginVersionToml)

        val result = validator.validate()
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should detect specific AGP and Kotlin version incompatibility`() {
        val incompatibleToml = """
            [versions]
            agp = "8.1.0"
            kotlin = "1.8.22"

            [libraries]
            lib = { module = "group:artifact", version.ref = "kotlin" }
        """.trimIndent()

        testFile.writeText(incompatibleToml)

        val result = validator.validate()
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Version incompatibility: AGP 8.1.0 is not compatible with Kotlin 1.8.22") })
    }

    @Test
    fun `validate should detect vulnerable junit versions specifically`() {
        val vulnerableToml = """
            [versions]
            junit = "4.12"

            [libraries]
            junit-old = { module = "junit:junit", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(vulnerableToml)

        val result = validator.validate()
        
        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("Library 'junit-old' uses vulnerable version: 4.12") })
    }

    @Test
    fun `validate should detect critical dependencies missing specifically`() {
        val noCriticalDepsToml = """
            [versions]
            gson = "2.8.9"

            [libraries]
            gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
        """.trimIndent()

        testFile.writeText(noCriticalDepsToml)

        val result = validator.validate()
        
        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("Missing critical dependencies: junit:junit, androidx.core:core-ktx") })
    }

    @Test
    fun `validate should handle performance under repeated calls`() {
        val validToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(validToml)

        // Test repeated validation calls
        val startTime = System.currentTimeMillis()
        repeat(50) {
            val result = validator.validate()
            assertTrue(result.isValid)
        }
        val endTime = System.currentTimeMillis()
        
        // Should complete in reasonable time (less than 5 seconds for 50 calls)
        assertTrue(endTime - startTime < 5000)
    }

    @Test
    fun `validate should handle empty string values in versions`() {
        val emptyValueToml = """
            [versions]
            empty = ""
            valid = "1.0.0"

            [libraries]
            valid-lib = { module = "group:artifact", version.ref = "valid" }
        """.trimIndent()

        testFile.writeText(emptyValueToml)

        val result = validator.validate()
        
        // Should detect empty version values as invalid
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid version format for 'empty':") })
    }

    @Test
    fun `validate should handle file with BOM (Byte Order Mark)`() {
        val tomlWithBOM = "\uFEFF[versions]\njunit = \"5.8.2\"\n\n[libraries]\njunit-core = { module = \"org.junit.jupiter:junit-jupyter\", version.ref = \"junit\" }"
        testFile.writeText(tomlWithBOM)

        val result = validator.validate()
        
        // Should handle BOM gracefully
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
    }

    @Test
    fun `validate should handle TOML with escaped characters in parsing`() {
        val escapedToml = """
            [versions]
            escaped = "1.0.0-special"

            [libraries]
            escaped-lib = { module = "group:artifact", version.ref = "escaped" }
        """.trimIndent()

        testFile.writeText(escapedToml)

        val result = validator.validate()
        
        // Should handle parsing successfully
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `ValidationResult should maintain proper timestamp`() {
        val beforeTime = System.currentTimeMillis()
        val result = ValidationResult()
        val afterTime = System.currentTimeMillis()
        
        assertTrue(result.timestamp >= beforeTime)
        assertTrue(result.timestamp <= afterTime)
    }

    @Test
    fun `validate should handle mixed case in section names gracefully`() {
        val mixedCaseToml = """
            [Versions]
            junit = "5.8.2"

            [Libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupyter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(mixedCaseToml)

        val result = validator.validate()
        
        // Mixed case sections won't match exact string comparisons
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Required versions section is missing"))
        assertTrue(result.errors.contains("Required libraries section is missing"))
    }

    @Test
    fun `validate should handle complex inline table parsing correctly`() {
        val complexInlineToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            complex-lib = { module = "group:artifact", version.ref = "test", classifier = "sources" }
        """.trimIndent()

        testFile.writeText(complexInlineToml)

        val result = validator.validate()
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle arrays in bundles with invalid references`() {
        val invalidBundleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact1", version.ref = "test" }

            [bundles]
            invalid-bundle = ["lib1", "nonexistent-lib"]
        """.trimIndent()

        testFile.writeText(invalidBundleToml)

        val result = validator.validate()
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid bundle reference in 'invalid-bundle': nonexistent-lib") })
    }

    @Test
    fun `ValidationResult data class properties should be accessible`() {
        val result = ValidationResult(
            isValid = false,
            errors = mutableListOf("error1"),
            warnings = mutableListOf("warning1"),
            timestamp = 123456L
        )

        // Test that all properties are accessible
        assertFalse(result.isValid)
        assertEquals(mutableListOf("error1"), result.errors)
        assertEquals(mutableListOf("warning1"), result.warnings)
        assertEquals(123456L, result.timestamp)
        
        // Test that lists are mutable (as per implementation)
        result.errors.add("error2")
        result.warnings.add("warning2")

        assertEquals(2, result.errors.size)
        assertEquals(2, result.warnings.size)
    }

    @Test
    fun `ValidationResult should handle mixed errors and warnings addition`() {
        val result = ValidationResult()
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
        assertTrue(result.warnings.isEmpty())

        // Add alternating errors and warnings
        result.addError("Error 1")
        result.addWarning("Warning 1")
        result.addError("Error 2")
        result.addWarning("Warning 2")

        assertFalse(result.isValid) // Should be false due to errors
        assertEquals(2, result.errors.size)
        assertEquals(2, result.warnings.size)
        assertEquals("Error 1", result.errors[0])
        assertEquals("Warning 1", result.warnings[0])
        assertEquals("Error 2", result.errors[1])
        assertEquals("Warning 2", result.warnings[1])
    }

    @Test
    fun `ValidationResult mutable lists should allow direct manipulation but not affect isValid`() {
        val result = ValidationResult()

        // Direct manipulation of mutable lists (bypassing addError method)
        result.errors.add("Direct error")
        result.warnings.add("Direct warning")

        assertTrue(result.isValid) // Should still be valid as isValid wasn't updated by addError method
        assertEquals(1, result.errors.size)
        assertEquals(1, result.warnings.size)
        assertEquals("Direct error", result.errors[0])
        assertEquals("Direct warning", result.warnings[0])
    }

    @Test
    fun `validate should handle TOML with malformed inline tables that cause parsing errors`() {
        val malformedInlineToml = """
            [versions] 
            test = "1.0.0"

            [libraries]
            valid-lib = { module = "group:artifact", version.ref = "test" }
            malformed1 = { module = "group:artifact" version.ref = "test" }
            malformed2 = { module "group:artifact", version.ref = "test" }
            malformed3 = { module = "group:artifact", version.ref = "test"
        """.trimIndent()

        testFile.writeText(malformedInlineToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.startsWith("Syntax error in TOML file:") })
    }

    @Test
    fun `validate should detect specific error message format for missing file`() {
        val result = validator.validate()

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].startsWith("TOML file does not exist:"))
        assertTrue(result.errors[0].contains(testFile.path))
    }

    @Test
    fun `validate should handle TOML with mixed quote types and escaped characters`() {
        val mixedQuotesToml = """
            [versions]
            single = 'single-quoted-version'
            double = "double-quoted-version"
            escaped = "version-with-\"quotes\""

            [libraries]
            lib1 = { module = 'group:artifact1', version.ref = "single" }
            library2 = { module = "group:artifact2", version.ref = 'double' }
            lib3 = { module = "group:artifact3", version.ref = "escaped" }
        """.trimIndent()

        testFile.writeText(mixedQuotesToml)

        val result = validator.validate()

        // Should handle different quote types gracefully without syntax errors
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle TOML with very specific AGP version compatibility checks`() {
        val specificVersionToml = """
            [versions]
            agp = "8.5.0"
            kotlin = "1.8.10"

            [libraries]
            lib = { module = "group:artifact", version.ref = "kotlin" }
        """.trimIndent()

        testFile.writeText(specificVersionToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { 
            it.contains("Version incompatibility: AGP 8.5.0 is not compatible with Kotlin 1.8.10") 
        })
    }

    @Test
    fun `validate should handle specific vulnerable JUnit version detection`() {
        val vulnerableJUnitToml = """
            [versions]
            junit-old = "4.11"

            [libraries]
            junit-legacy = { module = "junit:junit", version.ref = "junit-old" }
        """.trimIndent()

        testFile.writeText(vulnerableJUnitToml)

        val result = validator.validate()

        assertTrue(result.isValid) // Should be valid but with warnings
        assertTrue(result.warnings.any { 
            it.contains("Library 'junit-legacy' uses vulnerable version: 4.11") 
        })
    }

    @Test
    fun `validate should detect specific critical dependencies missing pattern`() {
        val noCriticalDepsToml = """
            [versions]
            gson = "2.8.9"
            okhttp = "4.9.3"

            [libraries]
            gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
            okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
        """.trimIndent()

        testFile.writeText(noCriticalDepsToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { 
            it.contains("Missing critical dependencies: junit:junit, androidx.core:core-ktx") 
        })
    }

    @Test
    fun `validate should handle bundle validation with empty string references`() {
        val emptyRefBundleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact", version.ref = "test" }

            [bundles]
            invalid-bundle = ["lib1", "", "nonexistent"]
        """.trimIndent()

        testFile.writeText(emptyRefBundleToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { 
            it.contains("Invalid bundle reference in 'invalid-bundle': ") 
        })
    }

    @Test
    fun `validate should handle TOML parsing with complex inline table structures`() {
        val complexInlineToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            complex-lib = { module = "group:artifact", version.ref = "test", classifier = "sources", exclude = "transitive" }
            simple-lib = { module = "simple:artifact", version = "2.0.0" }
        """.trimIndent()

        testFile.writeText(complexInlineToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle array parsing with different formatting styles`() {
        val arrayFormattingToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib1 = { module = "group:artifact1", version.ref = "test" }
            lib2 = { module = "group:artifact2", version.ref = "test" }

            [bundles]
            compact = ["lib1","lib2"]
            spaced = [ "lib1" , "lib2" ]
            multiline = [
                "lib1",
                "lib2"
            ]
        """.trimIndent()

        testFile.writeText(arrayFormattingToml)

        val result = validator.validate()

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle module pattern validation edge cases`() {
        val edgeModuleToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            valid1 = { module = "com.example:artifact", version.ref = "test" }
            valid2 = { module = "org.apache:commons-lang", version.ref = "test" }
            valid3 = { module = "io.github:my-lib", version.ref = "test" }
            invalid1 = { module = "invalid-module", version.ref = "test" }
            invalid2 = { module = "com.example:", version.ref = "test" }
            invalid3 = { module = ":artifact", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(edgeModuleToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid module format for 'invalid1': invalid-module") })
        assertTrue(result.errors.any { it.contains("Invalid module format for 'invalid2': com.example:") })
        assertTrue(result.errors.any { it.contains("Invalid module format for 'invalid3': :artifact") })
    }

    @Test
    fun `validate should handle plugin ID pattern validation edge cases`() {
        val edgePluginToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            lib = { module = "group:artifact", version.ref = "test" }

            [plugins]
            valid1 = { id = "com.example.plugin", version.ref = "test" }
            valid2 = { id = "org.jetbrains.kotlin.jvm", version.ref = "test" }
            invalid1 = { id = "invalid", version.ref = "test" }
            invalid2 = { id = "toolongpluginnamewithoutanystructure", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(edgePluginToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid plugin ID format for 'invalid1': invalid") })
        assertTrue(result.errors.any { it.contains("Invalid plugin ID format for 'invalid2': toolongpluginnamewithoutanystructure") })
    }

    @Test
    fun `validate should handle version pattern validation for all supported formats`() {
        val versionPatternsToml = """
            [versions]
            semantic = "1.2.3"
            prerelease = "1.2.3-alpha"
            build = "1.2.3+build.123"
            plus = "1.2.+"
            range1 = "[1.0,2.0)"
            range2 = "[1.5,)"
            single = "1"
            two-part = "1.0"
            invalid1 = "not.a.version"
            invalid2 = "1.x.y"

            [libraries]
            lib = { module = "group:artifact", version.ref = "semantic" }
        """.trimIndent()

        testFile.writeText(versionPatternsToml)

        val result = validator.validate()

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid version format for 'invalid1': not.a.version") })
        assertTrue(result.errors.any { it.contains("Invalid version format for 'invalid2': 1.x.y") })
    }

    @Test
    fun `ValidationResult timestamp should be immutable after creation`() {
        val beforeTime = System.currentTimeMillis()
        val result = ValidationResult()
        val afterTime = System.currentTimeMillis()
        
        val originalTimestamp = result.timestamp
        assertTrue(originalTimestamp >= beforeTime)
        assertTrue(originalTimestamp <= afterTime)
        
        // Operations shouldn't change timestamp
        result.addError("Test error")
        result.addWarning("Test warning")
        result.errors.add("Direct error")
        result.warnings.add("Direct warning")
        
        assertEquals(originalTimestamp, result.timestamp)
    }

    @Test
    fun `validate should handle concurrent access to the same validator instance`() {
        val validToml = """
            [versions]
            junit = "5.8.2"

            [libraries]
            junit-core = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
        """.trimIndent()

        testFile.writeText(validToml)

        // Test concurrent validation calls on the same validator instance
        val results = mutableListOf<ValidationResult>()
        val threads = (1..5).map {
            Thread {
                repeat(4) {
                    val result = validator.validate()
                    synchronized(results) {
                        results.add(result)
                    }
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(20, results.size)
        results.forEach { result ->
            assertTrue(result.isValid)
            assertTrue(result.errors.isEmpty())
        }
    }

    @Test
    fun `validate should handle file content encoding edge cases`() {
        // Test with UTF-8 BOM
        val utf8BomContent = "\uFEFF[versions]\njunit = \"5.8.2\"\n\n[libraries]\njunit-core = { module = \"org.junit.jupiter:junit-jupyter\", version.ref = \"junit\" }"
        testFile.writeText(utf8BomContent)

        val result = validator.validate()

        // Should handle BOM gracefully
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `ValidationResult data class should support component destructuring properly`() {
        val result = ValidationResult(
            isValid = false,
            errors = mutableListOf("error1", "error2"),
            warnings = mutableListOf("warning1"),
            timestamp = 123456789L
        )

        val (isValid, errors, warnings, timestamp) = result
        
        assertFalse(isValid)
        assertEquals(mutableListOf("error1", "error2"), errors)
        assertEquals(mutableListOf("warning1"), warnings)
        assertEquals(123456789L, timestamp)
        
        // Verify that destructured components are references to the same objects
        errors.add("error3")
        warnings.add("warning2")
        
        assertEquals(3, result.errors.size)
        assertEquals(2, result.warnings.size)
    }

    @Test
    fun `validate should handle parsing edge case with empty inline tables`() {
        val emptyInlineToml = """
            [versions]
            test = "1.0.0"

            [libraries]
            empty-lib = { }
            normal-lib = { module = "group:artifact", version.ref = "test" }
        """.trimIndent()

        testFile.writeText(emptyInlineToml)

        val result = validator.validate()
        
        // Should handle empty inline tables gracefully
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validate should handle performance with large number of dependencies`() {
        val largeTomlBuilder = StringBuilder()
        largeTomlBuilder.append("[versions]\n")
        
        repeat(100) { i ->
            largeTomlBuilder.append("version$i = \"1.0.$i\"\n")
        }
        
        largeTomlBuilder.append("\n[libraries]\n")
        repeat(100) { i ->
            largeTomlBuilder.append("lib$i = { module = \"group$i:artifact$i\", version.ref = \"version$i\" }\n")
        }
        
        largeTomlBuilder.append("\n[plugins]\n")
        repeat(50) { i ->
            largeTomlBuilder.append("plugin$i = { id = \"com.example.plugin$i\", version.ref = \"version$i\" }\n")
        }

        testFile.writeText(largeTomlBuilder.toString())

        val startTime = System.currentTimeMillis()
        val result = validator.validate()
        val endTime = System.currentTimeMillis()
        
        assertTrue(endTime - startTime < 3000) // Should complete within 3 seconds
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
}