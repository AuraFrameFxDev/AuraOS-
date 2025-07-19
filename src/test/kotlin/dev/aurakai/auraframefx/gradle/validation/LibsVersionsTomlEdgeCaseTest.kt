package dev.aurakai.auraframefx.gradle.validation

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

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

    // Helper
    private fun write(content: String) {
        tempToml.writeText(content)
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    fun mixedQuoteTypes_areHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = '2.0.0'
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            kotlinLib = { module = 'org.jetbrains.kotlin:kotlin-stdlib', version.ref = "kotlin" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Mixed quotes should be valid", result.isValid)
        assertEquals("Should have no errors", 0, result.errors.size)
    }

    @Test
    fun escapedCharacters_doNotBreakParsing() {
        val toml = """
            [versions]
            agp = "8.11.1"
            special = "version-with-\"quotes\""
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            specialLib = { module = "com.example:special\\path", version.ref = "special" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Escaped characters should be valid", result.isValid)
        assertEquals("Should have no errors", 0, result.errors.size)
    }

    @Test
    fun inlineTableVariations_areSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            [libraries]
            compactLib = { module = "com.example:lib", version.ref = "agp" }
            spacedLib = { module = "com.example:spaced" , version.ref = "kotlin" }
            multilineLib = {
                module = "com.example:multiline",
                version.ref = "agp"
            }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Inline table variations should be valid", result.isValid)
        assertEquals("Should have no errors", 0, result.errors.size)
    }

    @Test
    fun bundleArrayFormats_areAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            [bundles]
            testing = ["testLib"]
            multiBundle = [
                "testLib",
            ]
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Bundle array formats should be valid", result.isValid)
        assertEquals("Should have no errors", 0, result.errors.size)
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
        assertFalse("Section names should be case sensitive", result.isValid)
        assertTrue("Should mention missing versions section",
            result.errors.any { it.contains("versions section is required") })
    }

    @Test
    fun veryLargeFile_isValidatedWithinMemoryLimits() {
        val tomlBuilder = StringBuilder()
        tomlBuilder.appendLine("[versions]")
        tomlBuilder.appendLine("agp = \"8.11.1\"")
        for (i in 1..1500) tomlBuilder.appendLine("version$i = \"1.0.$i\"")
        tomlBuilder.appendLine("[libraries]")
        tomlBuilder.appendLine("testLib = { module = \"com.example:lib\", version.ref = \"agp\" }")
        for (i in 1..1500) tomlBuilder.appendLine("lib$i = { module = \"com.example:lib$i\", version.ref = \"version$i\" }")
        write(tomlBuilder.toString())
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Large file should be valid", result.isValid)
        assertEquals("Should have no errors", 0, result.errors.size)
    }

    @Test
    fun fileWithOnlyComments_isHandledCorrectly() {
        val toml = """
            # This is a comment
            # Another comment
            ## More comments
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("File with only comments should be invalid", result.isValid)
        assertTrue("Should report missing required sections",
            result.errors.any { it.contains("versions section is required") })
    }

    @Test
    fun malformedTomlSyntax_isDetected() {
        val toml = """
            [versions
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Malformed TOML should be invalid", result.isValid)
        assertTrue("Should report syntax error",
            result.errors.any { it.contains("syntax") || it.contains("bracket") || it.contains("parse") || it.contains("Syntax error") })
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
    fun malformedToml_withMissingClosingBracket_isHandledGracefully() {
        val toml = """
            [versions
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Malformed TOML should be invalid", result.isValid)
        assertTrue(result.errors.any { it.contains("syntax") || it.contains("bracket") || it.contains("parse") || it.contains("Syntax error") })
    }

    @Test
    fun malformedToml_withInvalidKeyValueSeparator_isRejected() {
        val toml = """
            [versions]
            agp : "8.11.1"
            kotlin = "2.0.0"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid key-value separator should fail", result.isValid)
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
    fun malformedToml_withUnterminatedString_isHandled() {
        val toml = """
            [versions]
            agp = "8.11.1
            kotlin = "2.0.0"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Unterminated string should fail", result.isValid)
    }

    @Test
    fun malformedToml_withDuplicateKeys_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            agp = "8.11.2"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Duplicate keys should fail validation", result.isValid)
    }

    @Test
    fun malformedToml_withInvalidTableDefinition_isRejected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [[libraries]]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid table definition should fail", result.isValid)
    }

    @Test
    fun extremeVersionNumbers_areHandledCorrectly() {
        val toml = """
            [versions]
            zero = "0.0.0"
            large = "999999.999999.999999"
            alphanumeric = "1.0.0-alpha.1+build.123"
            semverPre = "2.0.0-SNAPSHOT"
            dateVersion = "20231225.1200"
            [libraries]
            zeroLib  = { module = "com.example:zero" , version.ref = "zero"   }
            largeLib = { module = "com.example:large", version.ref = "large" }
            alphaLib = { module = "com.example:alpha", version.ref = "alphanumeric" }
            preLib   = { module = "com.example:pre" , version.ref = "semverPre"   }
            dateLib  = { module = "com.example:date", version.ref = "dateVersion" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun unicodeCharacters_inVersionsAndModules_areSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-ñoño"
            emoji   = "2.0.0-🚀"
            [libraries]
            unicodeLib = { module = "com.exämple:ñoño", version.ref = "unicode" }
            emojiLib   = { module = "com.example:rocket", version.ref = "emoji"   }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun emptyFile_isInvalid() {
        write("")
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun whitespaceOnlyFile_isInvalid() {
        write("   \n\t  \n  ")
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
    }

    @Test
    fun commentsAndWhitespace_areIgnoredProperly() {
        val toml = """
            # Initial comment
            [versions]  # inline comment
            agp = "8.11.1"  # version comment
            [libraries] # section comment
            testLib = { module = "com.example:lib", version.ref = "agp" } # inline
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun versionReferences_withComplexPaths_areResolved() {
        val toml = """
            [versions]
            parent.child = "1.0.0"
            nested.deep.version = "2.0.0"
            [libraries]
            parentLib = { module = "com.example:parent" , version.ref = "parent.child" }
            nestedLib = { module = "com.example:nested", version.ref = "nested.deep.version" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun fileWithOnlyVersionsSection_isInvalid() {
        val toml = """
            [versions]
            agp = "8.11.1"
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("libraries") })
    }

    @Test
    fun fileWithOnlyLibrariesSection_isInvalid() {
        val toml = """
            [libraries]
            loneLib = { module = "com.example:lib", version = "1.0.0" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("versions") })
    }

    @Test
    fun libraryWithDirectVersionAndReference_conflictsAreDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            conflicted = { module = "com.example:lib", version = "1.0.0", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
    }

    @Test
    fun libraryWithoutModule_isInvalid() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            noModule = { version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("module") })
    }

    @Test
    fun concurrentValidation_handlesMultipleThreads() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val results = mutableListOf<Boolean>()
        val threads = (1..8).map {
            Thread {
                val ok = LibsVersionsTomlValidator(tempToml).validate().isValid
                synchronized(results) { results += ok }
            }.apply { start() }
        }
        threads.forEach { it.join() }
        assertEquals(8, results.size)
        assertTrue(results.all { it })
    }

    @Test
    fun tomlWithBOMCharacter_isHandledGracefully() {
        val bomToml = "\uFEFF[versions]\nagp = \"8.11.1\"\n\n[libraries]\ntest = { module = \"com.example:lib\", version.ref = \"agp\" }"
        write(bomToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue(result.isValid)
    }

    @Test
    fun validationPerformance_staysWithinReasonableBounds() {

    @Test
    fun libraryWithEmptyModuleString_isRejected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            emptyModule = { module = "", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty module string should be invalid", result.isValid)
        assertTrue("Should report module format error",
            result.errors.any { it.contains("module") && it.contains("format") })
    }

    @Test
    fun libraryWithoutVersionOrVersionRef_isRejected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            noVersion = { module = "com.example:lib" }
            validLib = { module = "com.example:valid", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Library without version should be invalid", result.isValid)
        assertTrue("Should report missing version",
            result.errors.any { it.contains("version") })
    }

    @Test
    fun pluginWithoutIdProperty_isRejected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            [plugins]
            noId = { version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Plugin without ID should be invalid", result.isValid)
        assertTrue("Should report missing plugin ID",
            result.errors.any { it.contains("id") || it.contains("plugin") })
    }

    @Test
    fun bundleWithInvalidLibraryReference_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            validLib = { module = "com.example:lib", version.ref = "agp" }
            [bundles]
            testBundle = ["validLib", "nonExistentLib"]
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Bundle with invalid library reference should be invalid", result.isValid)
        assertTrue("Should report invalid bundle reference",
            result.errors.any { it.contains("bundle") && it.contains("nonExistentLib") })
    }

    @Test
    fun versionStringWithOnlyWhitespace_isInvalid() {
        val toml = """
            [versions]
            whitespace = "   "
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Whitespace-only version should be invalid", result.isValid)
        assertTrue("Should report invalid version format",
            result.errors.any { it.contains("version") && it.contains("format") })
    }

    @Test
    fun moduleStringWithInvalidCoordinates_isDetected() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            noColon = { module = "invalidmodule", version.ref = "agp" }
            tooManyColons = { module = "group:name:extra", version.ref = "agp" }
            emptyGroup = { module = ":artifact", version.ref = "agp" }
            emptyArtifact = { module = "group:", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid module coordinates should be detected", result.isValid)
        assertTrue("Should report multiple module format errors",
            result.errors.count { it.contains("module") && it.contains("format") } >= 3)
    }

    @Test
    fun pluginIdWithInvalidFormat_isDetected() {

    @Test
    fun versionCatalogWithGroupNameSyntax_isSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            moduleLib = { module = "com.example:lib", version.ref = "agp" }
            groupNameLib = { group = "com.example", name = "library", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Group/name syntax should be valid", result.isValid)
    }

    @Test
    fun versionRangeFormats_areValidated() {
        val toml = """
            [versions]
            range1 = "[1.0,2.0)"
            range2 = "(1.0,2.0]"
            range3 = "[1.0,)"
            invalidRange = "[1.0,2.0"
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid range format should be detected", result.isValid)
        assertTrue("Should report invalid version format",
            result.errors.any { it.contains("version") && it.contains("format") })
    }

    @Test
    fun libraryWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            directVersion = { module = "com.example:lib", version = "2.0.0" }
            refVersion = { module = "com.example:lib2", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct version strings should be valid", result.isValid)
    }

    @Test
    fun pluginWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            [plugins]
            directPlugin = { id = "com.example.plugin", version = "1.0.0" }
            refPlugin = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct plugin versions should be valid", result.isValid)
    }

    @Test
    fun emptyRequiredSections_areDetected() {
        val toml = """
            [versions]
            [libraries]
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty required sections should be invalid", result.isValid)
        assertTrue("Should report empty sections",
            result.errors.any { it.contains("empty") || it.contains("cannot") })
    }

    @Test
    fun libraryWithAdditionalGradleProperties_isHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            richLib = {
                module = "com.example:lib",
                version.ref = "agp",
                classifier = "sources",
                type = "jar"
            }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Additional Gradle properties should be valid", result.isValid)
    }

    @Test
    fun malformedInlineTable_isDetected() {

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val toml = """

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [versions]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            agp = "8.11.1"

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [libraries]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            malformed = { module = "com.example:lib" version.ref = "agp" }

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        """.trimIndent()

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        write(toml)

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val result = LibsVersionsTomlValidator(tempToml).validate()

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        assertFalse("Malformed inline table should be invalid", result.isValid)

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        assertTrue("Should report syntax error",

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            result.errors.any { it.contains("syntax") || it.contains("error") })

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
    }

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val toml = """

    @Test
    fun versionCatalogWithGroupNameSyntax_isSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            moduleLib = { module = "com.example:lib", version.ref = "agp" }
            groupNameLib = { group = "com.example", name = "library", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Group/name syntax should be valid", result.isValid)
    }

    @Test
    fun versionRangeFormats_areValidated() {
        val toml = """
            [versions]
            range1 = "[1.0,2.0)"
            range2 = "(1.0,2.0]"
            range3 = "[1.0,)"
            invalidRange = "[1.0,2.0"
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid range format should be detected", result.isValid)
        assertTrue("Should report invalid version format",
            result.errors.any { it.contains("version") && it.contains("format") })
    }

    @Test
    fun libraryWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            directVersion = { module = "com.example:lib", version = "2.0.0" }
            refVersion = { module = "com.example:lib2", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct version strings should be valid", result.isValid)
    }

    @Test
    fun pluginWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            [plugins]
            directPlugin = { id = "com.example.plugin", version = "1.0.0" }
            refPlugin = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct plugin versions should be valid", result.isValid)
    }

    @Test
    fun emptyRequiredSections_areDetected() {
        val toml = """
            [versions]
            [libraries]
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty required sections should be invalid", result.isValid)
        assertTrue("Should report empty sections",
            result.errors.any { it.contains("empty") || it.contains("cannot") })
    }

    @Test
    fun libraryWithAdditionalGradleProperties_isHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            richLib = {
                module = "com.example:lib",
                version.ref = "agp",
                classifier = "sources",
                type = "jar"
            }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Additional Gradle properties should be valid", result.isValid)
    }

    @Test
    fun malformedInlineTable_isDetected() {

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val toml = """

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [versions]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            agp = "8.11.1"

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [libraries]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            malformed = { module = "com.example:lib" version.ref = "agp" }

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        """.trimIndent()

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        write(toml)

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val result = LibsVersionsTomlValidator(tempToml).validate()

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        assertFalse("Malformed inline table should be invalid", result.isValid)

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        assertTrue("Should report syntax error",

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            result.errors.any { it.contains("syntax") || it.contains("error") })

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
    }

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [versions]

    @Test
    fun versionCatalogWithGroupNameSyntax_isSupported() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            moduleLib = { module = "com.example:lib", version.ref = "agp" }
            groupNameLib = { group = "com.example", name = "library", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Group/name syntax should be valid", result.isValid)
    }

    @Test
    fun versionRangeFormats_areValidated() {
        val toml = """
            [versions]
            range1 = "[1.0,2.0)"
            range2 = "(1.0,2.0]"
            range3 = "[1.0,)"
            invalidRange = "[1.0,2.0"
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid range format should be detected", result.isValid)
        assertTrue("Should report invalid version format",
            result.errors.any { it.contains("version") && it.contains("format") })
    }

    @Test
    fun libraryWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            directVersion = { module = "com.example:lib", version = "2.0.0" }
            refVersion = { module = "com.example:lib2", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct version strings should be valid", result.isValid)
    }

    @Test
    fun pluginWithDirectVersionString_isAccepted() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            [plugins]
            directPlugin = { id = "com.example.plugin", version = "1.0.0" }
            refPlugin = { id = "com.android.application", version.ref = "agp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Direct plugin versions should be valid", result.isValid)
    }

    @Test
    fun emptyRequiredSections_areDetected() {
        val toml = """
            [versions]
            [libraries]
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Empty required sections should be invalid", result.isValid)
        assertTrue("Should report empty sections",
            result.errors.any { it.contains("empty") || it.contains("cannot") })
    }

    @Test
    fun libraryWithAdditionalGradleProperties_isHandled() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            richLib = {
                module = "com.example:lib",
                version.ref = "agp",
                classifier = "sources",
                type = "jar"
            }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Additional Gradle properties should be valid", result.isValid)
    }

    @Test
    fun malformedInlineTable_isDetected() {

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
        val toml = """

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [versions]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            agp = "8.11.1"

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            [libraries]

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01, 0x02, 0x03))
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Binary content should be invalid", result.isValid)
        assertTrue("Should report appropriate error",
            result.errors.any { it.contains("syntax") || it.contains("error") || it.contains("invalid") })
    }

    @Test
    fun validatorInstanceReuse_maintainsConsistency() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val validator = LibsVersionsTomlValidator(tempToml)
        val result1 = validator.validate()
        val result2 = validator.validate()
        val result3 = validator.validate()
        
        assertTrue("All validations should succeed", result1.isValid && result2.isValid && result3.isValid)
        assertEquals("Error counts should be consistent", result1.errors.size, result2.errors.size)
        assertEquals("Warning counts should be consistent", result1.warnings.size, result2.warnings.size)
    }

    @Test
    fun filePermissionErrors_areHandledGracefully() {
        val readOnlyDir = File.createTempFile("readonly", "dir")
        readOnlyDir.delete()
        readOnlyDir.mkdir()
        readOnlyDir.setReadOnly()
        
        try {
            val readOnlyFile = File(readOnlyDir, "libs.versions.toml")
            val result = LibsVersionsTomlValidator(readOnlyFile).validate()
            assertFalse("Non-accessible file should be invalid", result.isValid)
            assertTrue("Should report file access error",
                result.errors.any { it.contains("file") || it.contains("exist") })
        } finally {
            readOnlyDir.setWritable(true)
            readOnlyDir.delete()
        }
    }

    @Test
    fun malformedVersionReference_withSpecialCharacters() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            specialRef = { module = "com.example:lib", version.ref = "agp@#$%" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertFalse("Invalid version reference should be detected", result.isValid)
        assertTrue("Should report missing or invalid version reference",
            result.errors.any { it.contains("version") && it.contains("reference") })
    }

    @Test
    fun complexBundleConfiguration_isValidated() {
        val toml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "agp" }
            lib2 = { module = "com.example:lib2", version.ref = "agp" }
            lib3 = { module = "com.example:lib3", version.ref = "agp" }
            [bundles]
            small = ["lib1"]
            medium = ["lib1", "lib2"]
            large = ["lib1", "lib2", "lib3"]
            empty = []
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Complex bundle configuration should be valid", result.isValid)
    }

    @Test
    fun threadSafeValidation_withFileModification() {
        val validToml = """
            [versions]
            agp = "8.11.1"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
        """.trimIndent()
        write(validToml)
        
        val results = mutableListOf<Boolean>()
        val threads = (1..5).map {
            Thread {
                val result = LibsVersionsTomlValidator(tempToml).validate()
                synchronized(results) {
                    results.add(result.isValid)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertEquals("All validations should complete", 5, results.size)
        assertTrue("All results should be valid", results.all { it })
    }

    @Test
    fun errorMessageQuality_providesUsefulInformation() {
        val problematicToml = """
            [versions]
            # No actual versions defined
            [libraries]
            badLib = { module = "invalid", version.ref = "missing" }
        """.trimIndent()
        write(problematicToml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        
        assertFalse("Problematic TOML should fail validation", result.isValid)
        assertTrue("Should have multiple specific errors", result.errors.size >= 2)
        assertTrue("Error messages should be informative",
            result.errors.all { error ->
                error.isNotBlank() && error.length > 10 && (
                    error.contains("module") ||
                    error.contains("version") ||
                    error.contains("reference") ||
                    error.contains("format")
                )
            })
    }

    @Test
    fun versionWithSpecialSemanticVersioningFormats_isAccepted() {
        val toml = """
            [versions]
            snapshot = "1.0.0-SNAPSHOT"
            release = "1.0.0-RC1"
            build = "1.0.0+20231201"
            complex = "2.0.0-alpha.1+build.123"
            timestamp = "1.0.0-20231201.120000-1"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "snapshot" }
            lib2 = { module = "com.example:lib2", version.ref = "release" }
            lib3 = { module = "com.example:lib3", version.ref = "build" }
            lib4 = { module = "com.example:lib4", version.ref = "complex" }
            lib5 = { module = "com.example:lib5", version.ref = "timestamp" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Special semantic versioning formats should be valid", result.isValid)
    }

    @Test
    fun vulnerableVersionDetection_warnsCorrectly() {
        val toml = """
            [versions]
            agp = "8.11.1"
            oldJunit = "4.12"
            veryOldJunit = "4.10"
            [libraries]
            testLib = { module = "com.example:lib", version.ref = "agp" }
            vulnLib1 = { module = "junit:junit", version.ref = "oldJunit" }
            vulnLib2 = { module = "junit:junit", version.ref = "veryOldJunit" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Should be valid but with warnings", result.isValid)
        assertTrue("Should detect vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.12") })
        assertTrue("Should detect very old vulnerable versions",
            result.warnings.any { it.contains("vulnerable") && it.contains("4.10") })
    }
            malformed = { module = "com.example:lib" version.ref = "agp" }

    @Test
    fun versionWithUnicodeCharacters_isSupported() {
        val toml = """
            [versions]
            unicode = "1.0.0-αβγ"
            emoji = "2.0.0-🚀"
            chinese = "3.0.0-中文"
            [libraries]
            lib1 = { module = "com.example:lib1", version.ref = "unicode" }
            lib2 = { module = "com.example:lib2", version.ref = "emoji" }
            lib3 = { module = "com.example:lib3", version.ref = "chinese" }
        """.trimIndent()
        write(toml)
        val result = LibsVersionsTomlValidator(tempToml).validate()
        assertTrue("Unicode characters in versions should be valid", result.isValid)
    }

    @Test
    fun fileWithBinaryContent_isHandledGracefully() {
        tempToml.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 
        val toml = """
            [versions]
            agp = "8.11.1"
            kotlin = "2.0.0"
            [libraries]
            a = { module = "com.example:a", version.ref = "agp" }
            b = { module = "com.example:b", version.ref = "kotlin" }
        """.trimIndent()
        write(toml)
        val start = System.currentTimeMillis()
        val result = LibsVersionsTomlValidator(tempToml).validate()
        val duration = System.currentTimeMillis() - start
        assertTrue(result.isValid)
        assertTrue("Validation should complete < 3s", duration < 3000)
    }
}