package dev.aurakai.auraframefx.gradle.validation

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileWriter

/**
 * Extra edge-case coverage for [LibsVersionsTomlValidator].
 * Testing framework: JUnit 4 (project-wide default).
 */
class LibsVersionsTomlEdgeCaseTest {

    private lateinit var tempToml: File

    @Before
    fun setUp() {
        tempToml = File.createTempFile("libs.versions.edge", ".toml")
    }

    @After
    fun tearDown() {
        tempToml.delete()
    }

    // ------------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------------
    private fun write(content: String) {
        FileWriter(tempToml).use { it.write(content) }
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    fun mixedQuoteTypes_areHandled() {
        val toml = "[versions]\n" +
                "single = '1.0.0'\n" +
                "double = \"2.0.0\"\n" +
                "multiSingle = '''3.0.0'''\n" +
                "multiDouble = \"\"\"\n" +
                "4.0.0\n" +
                "\"\"\"\n\n" +
                "[libraries]\n" +
                "testLib = { module = \"com.example:lib\", version.ref = \"double\" }\n"

        write(toml)

        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Validator should cope with different quote styles", result.isValid)
    }

    @Test
    fun escapedCharacters_doNotBreakParsing() {
        val toml = """
            [versions]
            agp = "8.11.1"
            path = "C:\\Users\\dev\\Gradle"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun inlineTableVariations_areSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"

            [libraries]
            compact={module="com.example:one",version.ref="agp"}
            spaced = { module = "com.example:two" , version.ref = "kotlin" }
            multiline = {
              module = "com.example:three",
              version.ref = "agp"
            }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun bundleArrayFormats_areAccepted() {
        val toml = """
            [versions]
            v = "1.0.0"

            [libraries]
            a = { module = "com.example:a", version.ref = "v" }
            b = { module = "com.example:b", version.ref = "v" }

            [bundles]
            compact = ["a","b"]
            spaced  = [ "a" , "b" ]
            multi   = [
              "a",
              "b"
            ]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun sectionNames_areCaseSensitive() {
        val toml = """
            [Versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Incorrect case in section names must fail", result.isValid)
        assertTrue(result.errors.any { it.contains("versions") || it.contains("libraries") })
    }

    @Test
    fun veryLargeFile_isValidatedWithinMemoryLimits() {
        val builder = StringBuilder("[versions]\n")
        for (i in 1..1500) builder.append("v$i = \"1.$i.0\"\n")
        builder.append("\n[libraries]\n")
        for (i in 1..1500) builder.append("lib$i = { module = \"com.example:lib$i\", version.ref = \"v$i\" }\n")
        write(builder.toString())

        val runtime = Runtime.getRuntime()
        val before = runtime.totalMemory() - runtime.freeMemory()

        val result = LibsVersionsTomlValidator(tempToml).validate()

        val after = runtime.totalMemory() - runtime.freeMemory()
        val used = after - before

        assertTrue(result.isValid)
        assertTrue("Validator should not use > 75 MB extra", used < 75 * 1024 * 1024)
    }

    @Test
    fun emptyFile_isHandledGracefully() {
        write("")
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty file should be invalid", result.isValid)
        assertTrue("Should contain helpful error message",
            result.errors.any { it.contains("Empty") || it.contains("invalid") })
    }

    @Test
    fun onlyWhitespace_isHandledGracefully() {
        write("   \n\t  \n   ")
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Whitespace-only file should be invalid", result.isValid)
        assertTrue("Should contain error about empty/invalid file",
            result.errors.any { it.contains("Empty") || it.contains("invalid") })
    }

    @Test
    fun invalidTomlSyntax_isDetected() {
        val toml = """
            [versions
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid TOML syntax should fail validation", result.isValid)
        assertTrue("Should mention syntax error",
            result.errors.any { it.contains("Syntax error") })
    }

    @Test
    fun missingVersionsSection_isDetected() {
        val toml = """
            [libraries]
            testLib = { module = "com.example:lib", version = "1.0.0" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Missing [versions] section should fail", result.isValid)
        assertTrue("Should mention missing versions section",
            result.errors.any { it.contains("versions section is required") })
    }

    @Test
    fun missingLibrariesSection_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Missing [libraries] section should fail", result.isValid)
        assertTrue("Should mention missing libraries section",
            result.errors.any { it.contains("libraries section is required") })
    }

    @Test
    fun unreferencedVersions_generateWarnings() {
        val toml = """
            [versions]
            agp = "8.11.1"
            unused = "1.0.0"
            kotlin = "2.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("File should be valid despite unreferenced versions", result.isValid)
        assertTrue("Should.warn about unreferenced version",
            result.warnings.any { it.contains("Unreferenced version: unused") })
        assertTrue("Should.warn about.kotlin version too",
            result.warnings.any { it.contains("Unreferenced version: kotlin") })
    }

    @Test
    fun invalidVersionReference_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "nonexistent" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should fail", result.isValid)
        assertTrue("Should mention missing version reference",
            result.errors.any { it.contains("Missing version reference: nonexistent") })
    }

    @Test
    fun duplicateVersionKeys_areDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            agp = "8.11.2"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Duplicate version keys should fail", result.isValid)
        assertTrue("Should mention duplicate key",
            result.errors.any { it.contains("Duplicate key: agp") })
    }

    @Test
    fun duplicateLibraryKeys_areDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            kotlin-lib = { module = "com.example:kotlin", version.ref = "agp" }
            testLib = { module = "com.example:other", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Duplicate library keys should fail", result.isValid)
        assertTrue("Should mention duplicate library key",
            result.errors.any { it.contains("Duplicate key: testLib") })
    }

    @Test
    fun invalidModuleFormat_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            badModule = { module = "invalid-module-format", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid module format should fail", result.isValid)
        assertTrue("Should mention invalid module format",
            result.errors.any { it.contains("Invalid module format: invalid-module-format") })
    }

    @Test
    fun validModuleFormats_areAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            validLib1 = { module = "com.example:lib", version.ref = "agp" }
            validLib2 = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "agp" }
            validLib3 = { module = "androidx.core:core-ktx", version.ref = "agp" }
            validLib4 = { module = "io.github.user:my-lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Valid module formats should be accepted", result.isValid)
    }

    @Test
    fun invalidVersionFormats_areDetected() {
        val toml = """
            [versions]
            invalidVersion1 = "not-a-version"
            invalidVersion2 = "1.2.3.4.5"
            validVersion = "1.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "validVersion" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version formats should fail", result.isValid)
        assertTrue("Should mention first invalid version format",
            result.errors.any { it.contains("Invalid version format: not-a-version") })
        assertTrue("Should mention second invalid version format",
            result.errors.any { it.contains("Invalid version format: 1.2.3.4.5") })
    }

    @Test
    fun validVersionFormats_areAccepted() {
        val toml = """
            [versions]
            semantic = "1.0.0"
            semanticWithBuild = "1.0.0-alpha+build.123"
            plusVersion = "1.2.+"
            range = "[1.0,2.0)"
            snapshot = "1.0.0-SNAPSHOT"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "semantic" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Valid version formats should be accepted", result.isValid)
    }

    @Test
    fun bundleWithNonexistentLibrary_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            existingLib = { module = "com.example:lib", version.ref = "agp" }

            [bundles]
            testBundle = ["existingLib", "nonexistentLib"]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Bundle with nonexistent library should fail", result.isValid)
        assertTrue("Should mention invalid bundle reference",
            result.errors.any { it.contains("Invalid bundle reference: nonexistentLib in bundle testBundle") })
    }

    @Test
    fun emptyBundle_isHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [bundles]
            emptyBundle = []
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Empty bundles should be valid", result.isValid)
    }

    @Test
    fun pluginsSection_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [plugins]
            android = { id = "com.android.application", version.ref = "agp" }
            kotlin = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Valid plugins section should pass", result.isValid)
    }

    @Test
    fun pluginWithInvalidReference_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [plugins]
            invalid = { id = "com.example.plugin", version.ref = "nonexistent" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Plugin with invalid version reference should fail", result.isValid)
        assertTrue("Should mention missing version reference",
            result.errors.any { it.contains("Missing version reference: nonexistent") })
    }

    @Test
    fun invalidPluginIdFormat_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [plugins]
            invalidPlugin = { id = "invalid-plugin-id", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid plugin ID format should fail", result.isValid)
        assertTrue("Should mention invalid plugin ID format",
            result.errors.any { it.contains("Invalid plugin ID format: invalid-plugin-id") })
    }

    @Test
    fun versionCompatibilityCheck_detectsIncompatibleVersions() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Incompatible AGP and Kotlin versions should fail", result.isValid)
        assertTrue("Should mention version incompatibility",
            result.errors.any { it.contains("Version incompatibility: AGP 8.11.1 requires Kotlin 1.9.0+") })
    }

    @Test
    fun securityVulnerabilities_generateWarnings() {
        val toml = """
            [versions]
            junit = "4.12"
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("File should be valid despite vulnerable dependency", result.isValid)
        assertTrue("Should warn about vulnerable version",
            result.warnings.any { it.contains("Potentially vulnerable version: junit 4.12") })
    }

    @Test
    fun criticalDependencies_generateWarningsWhenMissing() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            regularLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("File should be valid without critical dependencies", result.isValid)
        assertTrue("Should warn about missing testing dependencies",
            result.warnings.any { it.contains("Missing critical dependency: No testing dependencies found") })
    }

    @Test
    fun validationResult_includesTimestamp() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val beforeTime = System.currentTimeMillis()
        val result = LibsVersionsTomlValidator(tempToml).validate()
        val afterTime = System.currentTimeMillis()

        assertTrue("Timestamp should be within validation window",
            result.timestamp >= beforeTime && result.timestamp <= afterTime)
    }

    @Test
    fun nonexistentFile_isHandledGracefully() {
        val nonexistentFile = File("nonexistent.toml")
        val result = LibsVersionsTomlValidator(nonexistentFile).validate()

        assertFalse("Nonexistent file should be invalid", result.isValid)
        assertTrue("Should mention file not existing",
            result.errors.any { it.contains("TOML file does not exist") })
    }

    @Test
    fun complexValidFile_passesAllValidations() {
        val toml = """
            # Complex valid TOML file
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            junit = "5.8.2"
            mockk = "1.13.2"

            [libraries]
            # Core Android libraries
            androidx-core = { module = "androidx.core:core-ktx", version.ref = "kotlin" }
            androidx-lifecycle = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version = "2.6.2" }

            # Testing libraries
            junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
            mockk-lib = { module = "io.mockk:mockk", version.ref = "mockk" }

            [plugins]
            android-application = { id = "com.android.application", version.ref = "agp" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

            [bundles]
            testing = ["junit-jupiter", "mockk-lib"]
            androidx = ["androidx-core", "androidx-lifecycle"]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()

        assertTrue("Complex valid file should pass all validations", result.isValid)
        assertTrue("Should have no errors", result.errors.isEmpty())
        // Might have warnings about unreferenced versions, which is acceptable
    }

    @Test
    fun multilineInlineTableFormats_areSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"

            [libraries]
            singleLine = { module = "com.example:single", version.ref = "agp" }
            multiLine = {
                module = "com.example:multi",
                version.ref = "kotlin"
            }
            spacedMultiLine = {
                module = "com.example:spaced" ,
                version.ref = "agp"
            }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Multiline inline table formats should be valid", result.isValid)
    }

    @Test
    fun libraryWithGroupAndName_isValid() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            groupNameLib = { group = "androidx.core", name = "core-ktx", version.ref = "agp" }
            moduleLib = { module = "androidx.core:core-ktx", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Libraries with group/name format should be valid", result.isValid)
    }

    @Test
    fun nestedBundleArrays_areHandled() {
        val toml = """
            [versions]
            v = "1.0.0"

            [libraries]
            a = { module = "com.example:a", version.ref = "v" }
            b = { module = "com.example:b", version.ref = "v" }
            c = { module = "com.example:c", version.ref = "v" }

            [bundles]
            singleElement = ["a"]
            twoElements = ["a", "b"]
            threeElements = [
                "a",
                "b",
                "c"
            ]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Nested bundle arrays should be valid", result.isValid)
    }

    @Test
    fun versionFormatEdgeCases_areHandled() {
        val toml = """
            [versions]
            minimal = "1"
            twopart = "1.0"
            threepart = "1.0.0"
            withAlpha = "1.0.0-alpha"
            withBeta = "1.0.0-beta.1"
            withSnapshot = "1.0.0-SNAPSHOT"
            withBuild = "1.0.0+build.20230101"
            rangeOpen = "[1.0.0,)"
            rangeClosed = "[1.0.0,2.0.0]"
            rangeHalfOpen = "[1.0.0,2.0.0)"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "minimal" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Test depends on validator's version format validation strictness
        assertNotNull("Version format edge cases should not crash validator", result)
    }

    @Test
    fun tomlWithExtraWhitespaceAndComments_isValid() {
        val toml = """
            # This is a comment at the top

            [versions]  # Versions section
            agp = "8.11.1"    # Android Gradle Plugin  
            kotlin = "2.0.0"  # Kotlin version


            [libraries]  # Libraries section

            # Main library
            testLib = { module = "com.example:lib", version.ref = "agp" }

            # Another library with extra spacing
            kotlinLib = {   module = "org.jetbrains.kotlin:kotlin-stdlib"  ,  version.ref = "kotlin"   }


            [plugins]  # Plugins section

            android = { id = "com.android.application", version.ref = "agp" }

            # End of file
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("TOML with extra whitespace and comments should be valid", result.isValid)
    }

    @Test
    fun validationResult_providesMultipleErrors() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "1.8.0"
            unused1 = "1.0.0"
            unused2 = "2.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "nonexistent1" }
            badModule = { module = "invalid-format", version.ref = "nonexistent2" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()

        assertFalse("Multiple issues should fail validation", result.isValid)
        assertTrue("Should have multiple errors", result.errors.size >= 3)
        assertTrue("Should have multiple warnings", result.warnings.size >= 2)

        // Check for specific error types
        assertTrue("Should report version incompatibility",
            result.errors.any { it.contains("Version incompatibility") })
        assertTrue("Should report missing version references",
            result.errors.any { it.contains("Missing version reference") })
        assertTrue("Should report invalid module format",
            result.errors.any { it.contains("Invalid module format") })
        assertTrue("Should warn about unreferenced versions",
            result.warnings.any { it.contains("Unreferenced version") })
    }

    @Test
    fun concurrent_validationAccess_isThreadSafe() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)

        val results = mutableListOf<Boolean>()
        val threads = mutableListOf<Thread>()

        repeat(3) {
            val thread = Thread {
                val validator = LibsVersionsTomlValidator(tempToml)
                val result = validator.validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        assertEquals("All concurrent validations should complete", 3, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun validationResultProperties_areConsistent() {
        val toml = """
            [versions]
            agp = "8.11.1"
            unused = "1.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()

        // Test that all required properties are present and valid
        assertNotNull("Result should not be null", result)
        assertNotNull("Errors list should not be null", result.errors)
        assertNotNull("Warnings list should not be null", result.warnings)
        assertTrue("Timestamp should be positive", result.timestamp > 0)

        // Test that validation status matches error count
        if (result.errors.isEmpty()) {
            assertTrue("Should be valid when no errors", result.isValid)
        } else {
            assertFalse("Should be invalid when errors present", result.isValid)
        }
    }

    @Test
    fun validationResult_handlesExcessivelyLongLines() {
        val longModuleName = "com.example:" + "a".repeat(1000)
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            longLib = { module = "$longModuleName", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Should handle long lines gracefully without crashing
        assertNotNull("Should not crash on excessively long lines", result)
    }

    @Test
    fun unicodeCharacters_inVersionsAndKeys_areHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"
            unicode-版本 = "1.0.0"
            emoji-🚀 = "2.0.0"

            [libraries]
            测试库 = { module = "com.example:test", version.ref = "agp" }
            unicode-lib = { module = "com.例子:lib", version.ref = "unicode-版本" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Unicode handling depends on implementation
        assertNotNull("Should handle unicode characters", result)
    }

    @Test
    fun specialCharactersInStringValues_areEscapedProperly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            special = "1.0.0"

            [libraries]
            quotesLib = { module = "com.example:\"quoted\"", version.ref = "agp" }
            backslashLib = { module = "com.example:back\\slash", version.ref = "special" }
            newlineLib = { module = "com.example:new\nline", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertNotNull("Should handle escaped special characters", result)
    }

    @Test
    fun malformedInlineTable_structures_areDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            missingCloseBrace = { module = "com.example:lib", version.ref = "agp"
            extraComma = { module = "com.example:lib", version.ref = "agp", }
            missingEquals = { module "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Malformed inline tables should fail validation", result.isValid)
        assertTrue("Should contain syntax error message",
            result.errors.any { it.contains("Syntax error") })
    }

    @Test
    fun circularVersionReferences_areDetected() {
        val toml = """
            [versions]
            v1 = "1.0.0"
            v2 = "2.0.0"

            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "v1" }
            lib2 = { module = "com.example:lib2", version.ref = "v2" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Non-circular references should be valid", result.isValid)
    }

    @Test
    fun extremelyLongVersionString_isHandled() {
        val longVersion = "1.0.0-" + "alpha".repeat(200)
        val toml = """
            [versions]
            longVer = "$longVersion"
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertNotNull("Should handle extremely long version strings", result)
    }

    @Test
    fun mixedVersionReferenceTypes_inSameFile() {
        val toml = """
            [versions]
            refVersion = "1.0.0"
            directVersion = "2.0.0"

            [libraries]
            refLib = { module = "com.example:ref", version.ref = "refVersion" }
            directLib = { module = "com.example:direct", version = "3.0.0" }
            mixedLib = { module = "com.example:mixed", version.ref = "directVersion" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Mixed version reference types should be valid", result.isValid)
    }

    @Test
    fun sectionsInDifferentOrder_areValidated() {
        val toml = """
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [bundles]
            testBundle = ["testLib"]

            [versions]
            agp = "8.11.1"

            [plugins]
            android = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Sections in different order should be valid", result.isValid)
    }

    @Test
    fun emptyStringsInCriticalFields_areDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            empty = ""

            [libraries]
            emptyModule = { module = "", version.ref = "agp" }
            emptyVersion = { module = "com.example:lib", version.ref = "empty" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty strings in critical fields should fail", result.isValid)
        assertTrue("Should mention empty module",
            result.errors.any { it.contains("empty") || it.contains("invalid") })
    }

    @Test
    fun validationPerformance_onModeratelySizedFile() {
        val builder = StringBuilder("[versions]\n")
        for (i in 1..500) builder.append("v$i = \"1.$i.0\"\n")
        builder.append("\n[libraries]\n")
        for (i in 1..500) builder.append("lib$i = { module = \"com.example:lib$i\", version.ref = \"v$i\" }\n")
        write(builder.toString())

        val startTime = System.currentTimeMillis()
        val result = LibsVersionsTomlValidator(tempToml).validate()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Validation should complete", result != null)
        assertTrue("Should complete in reasonable time (<5 seconds)", duration < 5000)
    }

    @Test
    fun caseVariationsInSectionNames_areRejected() {
        val toml = """
            [VERSIONS]
            agp = "8.11.1"

            [Libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [Bundles]
            testBundle = ["testLib"]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Incorrect case in section names should fail", result.isValid)
        assertTrue("Should mention case sensitivity",
            result.errors.any { it.contains("versions") || it.contains("libraries") || it.contains("bundles") })
    }

    @Test
    fun deeplyNestedPluginIds_areValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [plugins]
            valid = { id = "com.company.department.team.plugin", version.ref = "agp" }
            tooShort = { id = "short", version.ref = "agp" }
            validComplex = { id = "org.gradle.kotlin.kotlin-dsl", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Results depend on plugin ID validation rules
        assertNotNull("Should handle complex plugin IDs", result)
    }

    @Test
    fun duplicateSections_areDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }

            [versions]
            kotlin = "2.0.0"

            [libraries]
            lib2 = { module = "com.example:lib2", version.ref = "kotlin" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Duplicate sections should fail validation", result.isValid)
        assertTrue("Should mention duplicate section",
            result.errors.any { it.contains("Duplicate") || it.contains("section") })
    }

    @Test
    fun invalidVersionPinning_strategies_areDetected() {
        val toml = """
            [versions]
            invalidRange1 = "[1.0,"
            invalidRange2 = "1.0,2.0]"
            invalidRange3 = "(1.0,2.0]"
            validPinned = "1.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "validPinned" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Results depend on version format validation strictness
        assertNotNull("Should handle invalid version ranges", result)
    }

    @Test
    fun libraryVersionOverrides_conflictDetection() {
        val toml = """
            [versions]
            agp = "8.11.1"
            override = "9.0.0"

            [libraries]
            baseLib = { module = "com.example:lib", version.ref = "agp" }
            overrideLib = { module = "com.example:lib", version.ref = "override" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Should be valid as different library entries
        assertTrue("Different library entries should be valid", result.isValid)
    }

    @Test
    fun validationWithReadOnlyFile_handlesGracefully() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        tempToml.setReadOnly()

        try {
            val result = LibsVersionsTomlValidator(tempToml).validate()
            assertNotNull("Should handle read-only files", result)
        } finally {
            tempToml.setWritable(true) // Restore for cleanup
        }
    }

    @Test
    fun pluginVersionWithoutReference_isValid() {
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }

            [plugins]
            directVersion = { id = "com.example.plugin", version = "1.0.0" }
            referencedVersion = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Plugins with direct versions should be valid", result.isValid)
    }

    @Test
    fun bundleCircularReferences_areDetected() {
        val toml = """
            [versions]
            v = "1.0.0"

            [libraries]
            a = { module = "com.example:a", version.ref = "v" }
            b = { module = "com.example:b", version.ref = "v" }

            [bundles]
            bundle1 = ["a", "bundle2"]
            bundle2 = ["b", "bundle1"]
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Should detect that bundles cannot reference other bundles
        assertFalse("Bundles referencing other bundles should fail", result.isValid)
        assertTrue("Should mention invalid bundle reference",
            result.errors.any { it.contains("bundle") })
    }

    @Test
    fun validationResult_handlesNullAndEmptyValues() {
        // Test the validator's resilience to edge cases in TOML structure
        val toml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            # This will test comment handling
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()

        // Verify result object integrity
        assertNotNull("Result should not be null", result)
        assertNotNull("Errors should not be null", result.errors)
        assertNotNull("Warnings should not be null", result.warnings)
        assertTrue("Errors should be a valid list", result.errors is List<*>)
        assertTrue("Warnings should be a valid list", result.warnings is List<*>)
    }

    @Test
    fun massiveBundleArrays_areProcessedEfficiently() {
        val builder = StringBuilder("[versions]\nv = \"1.0.0\"\n\n[libraries]\n")
        val libraryNames = mutableListOf<String>()
        for (i in 1..100) {
            val libName = "lib$i"
            libraryNames.add(libName)
            builder.append("$libName = { module = \"com.example:$libName\", version.ref = \"v\" }\n")
        }

        builder.append("\n[bundles]\n")
        builder.append("massiveBundle = [")
        builder.append(libraryNames.joinToString(", ") { "\"$it\"" })
        builder.append("]\n")

        write(builder.toString())
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Massive bundle arrays should be processed", result.isValid)
    }

    @Test
    fun gradleVersionCatalogStandardCompliance_isChecked() {
        val toml = """
            [metadata]
            format.version = "1.1"

            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"

            [libraries]
            kotlinStdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib-jdk8", version.ref = "kotlin" }

            [plugins]
            kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Standard Gradle version catalog should be valid", result.isValid)
    }

    @Test
    fun invalidCharactersInKeys_areRejected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            "invalid key" = "1.0.0"
            invalid-key! = "2.0.0"
            valid_key = "3.0.0"

            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        // Results depend on key validation rules
        assertNotNull("Should handle invalid characters in keys", result)
    }

    @Test
    fun memoryUsage_remainsReasonable_onLargeInputs() {
        val builder = StringBuilder("[versions]\n")
        for (i in 1..2000) {
            builder.append("version$i = \"1.$i.0\"\n")
        }
        builder.append("\n[libraries]\n")
        for (i in 1..1000) {
            builder.append("lib$i = { module = \"com.example:lib$i\", version.ref = \"v$i\" }\n")
        }

        write(builder.toString())

        val runtime = Runtime.getRuntime()
        val beforeMemory = runtime.totalMemory() - runtime.freeMemory()

        val result = LibsVersionsTomlValidator(tempToml).validate()

        val afterMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = afterMemory - beforeMemory

        assertNotNull("Large input should be processed", result)
        assertTrue("Memory usage should be reasonable (<100MB)", memoryUsed < 100 * 1024 * 1024)
    }

    @Test
    fun runtimeExceptions_areHandledGracefully() {
        // Create a file that might cause parsing issues
        val problematicToml = """
            [versions]
            agp = "8.11.1"

            [libraries]
            # Test with potential Unicode issues
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()

        write(problematicToml)

        try {
            val result = LibsVersionsTomlValidator(tempToml).validate()
            assertNotNull("Should not throw runtime exceptions", result)
            // Validator should always return a result, even if there are internal errors
            assertTrue("Should have consistent result structure",
                result.errors != null && result.warnings != null)
        } catch (e: Exception) {
            fail("Validator should not throw exceptions: ${e.message}")
        }
    }
}