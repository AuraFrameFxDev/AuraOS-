package dev.aurakai.auraframefx.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.util.Properties

/**
 * Comprehensive validation tests for gradle.properties configuration.
 * 
 * Testing Framework: JUnit 5 Jupiter
 * 
 * This test suite validates the gradle.properties file structure,
 * required properties, memory settings, and configuration consistency
 * for the AuraFrameFX project.
 */
@DisplayName("Gradle Properties Validation Tests")
class GradlePropertiesValidationTest {

    private lateinit var gradleProperties: Properties
    private lateinit var propertiesFile: File

    @BeforeEach
    fun setUp() {
        propertiesFile = File("gradle.properties")
        gradleProperties = Properties()
        
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { input ->
                gradleProperties.load(input)
            }
        }
    }

    @Nested
    @DisplayName("File Structure and Existence Tests")
    inner class FileStructureTests {

        @Test
        @DisplayName("gradle.properties file should exist")
        fun `gradle properties file should exist`() {
            assertTrue(propertiesFile.exists(), "gradle.properties file should exist in project root")
        }

        @Test
        @DisplayName("gradle.properties should be readable")
        fun `gradle properties should be readable`() {
            assertTrue(propertiesFile.canRead(), "gradle.properties should be readable")
        }

        @Test
        @DisplayName("gradle.properties should not be empty")
        fun `gradle properties should not be empty`() {
            assertTrue(propertiesFile.length() > 0, "gradle.properties should not be empty")
        }

        @Test
        @DisplayName("Properties should load without errors")
        fun `properties should load without errors`() {
            val properties = Properties()
            assertDoesNotThrow {
                propertiesFile.inputStream().use { input ->
                    properties.load(input)
                }
            }
        }
    }

    @Nested
    @DisplayName("Required Properties Validation")
    inner class RequiredPropertiesTests {

        @ParameterizedTest
        @ValueSource(strings = [
            "org.gradle.jvmargs",
            "org.gradle.java.home", 
            "org.gradle.daemon",
            "org.gradle.parallel",
            "org.gradle.caching",
            "kotlin.code.style",
            "android.useAndroidX",
            "agp.version"
        ])
        @DisplayName("Essential properties should be present")
        fun `essential properties should be present`(propertyName: String) {
            assertTrue(
                gradleProperties.containsKey(propertyName),
                "Property '$propertyName' should be present in gradle.properties"
            )
        }

        @Test
        @DisplayName("All required AuraFrameFX properties should exist")
        fun `all required aura frame fx properties should exist`() {
            val requiredProperties = listOf(
                "org.gradle.jvmargs",
                "org.gradle.java.home",
                "android.lint.ignoreTestSources",
                "org.gradle.daemon",
                "org.gradle.parallel",
                "org.gradle.caching",
                "kotlin.code.style",
                "android.useAndroidX",
                "agp.version"
            )

            requiredProperties.forEach { property ->
                assertTrue(
                    gradleProperties.containsKey(property),
                    "Required property '$property' is missing"
                )
            }
        }
    }

    @Nested
    @DisplayName("JVM Arguments Validation")
    inner class JvmArgumentsTests {

        @Test
        @DisplayName("JVM arguments should include required add-opens directives")
        fun `jvm arguments should include required add opens directives`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs", "")
            
            val requiredAddOpens = listOf(
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
                "--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED",
                "--add-opens=java.base/java.nio.charset=ALL-UNNAMED",
                "--add-opens=java.base/java.net=ALL-UNNAMED",
                "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED"
            )

            requiredAddOpens.forEach { addOpen ->
                assertTrue(
                    jvmArgs.contains(addOpen),
                    "JVM args should contain: $addOpen"
                )
            }
        }

        @Test
        @DisplayName("Memory settings should be appropriate for AuraFrameFX")
        fun `memory settings should be appropriate for aura frame fx`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs", "")
            
            assertTrue(jvmArgs.contains("-Xmx4g"), "Should set max heap size to 4GB")
        }

        @Test
        @DisplayName("Encoding and locale settings should be configured")
        fun `encoding and locale settings should be configured`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs", "")
            
            assertTrue(jvmArgs.contains("-Dfile.encoding=UTF-8"), "Should set UTF-8 encoding")
            assertTrue(jvmArgs.contains("-Duser.country=US"), "Should set US country")
            assertTrue(jvmArgs.contains("-Duser.language=en"), "Should set English language")
        }

        @Test
        @DisplayName("JVM arguments should not contain conflicting memory settings")
        fun `jvm arguments should not contain conflicting memory settings`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs", "")
            
            // Should only have one -Xmx setting
            val xmxCount = jvmArgs.split("-Xmx").size - 1
            assertEquals(1, xmxCount, "Should have exactly one -Xmx memory setting")
        }
    }

    @Nested
    @DisplayName("Performance and Build Optimization Tests")
    inner class PerformanceOptimizationTests {

        @ParameterizedTest
        @CsvSource(
            "org.gradle.daemon, true",
            "org.gradle.parallel, true", 
            "org.gradle.caching, true",
            "org.gradle.configureondemand, true",
            "org.gradle.configuration-cache, true"
        )
        @DisplayName("Performance optimization flags should be enabled")
        fun `performance optimization flags should be enabled`(property: String, expectedValue: String) {
            assertEquals(
                expectedValue,
                gradleProperties.getProperty(property),
                "Property '$property' should be set to '$expectedValue' for optimal performance"
            )
        }

        @Test
        @DisplayName("Configuration cache settings should be properly configured")
        fun `configuration cache settings should be properly configured`() {
            assertEquals("true", gradleProperties.getProperty("org.gradle.configuration-cache"))
            assertEquals("true", gradleProperties.getProperty("org.gradle.unsafe.configuration-cache"))
            assertEquals("warn", gradleProperties.getProperty("org.gradle.unsafe.configuration-cache-problems"))
        }

        @Test
        @DisplayName("Build optimization properties should have consistent values")
        fun `build optimization properties should have consistent values`() {
            // If configuration cache is enabled, other optimizations should also be enabled
            if (gradleProperties.getProperty("org.gradle.configuration-cache") == "true") {
                assertEquals("true", gradleProperties.getProperty("org.gradle.caching"), 
                    "Build caching should be enabled when configuration cache is enabled")
                assertEquals("true", gradleProperties.getProperty("org.gradle.parallel"), 
                    "Parallel execution should be enabled for optimal performance")
            }
        }
    }

    @Nested
    @DisplayName("Kotlin Configuration Tests")
    inner class KotlinConfigurationTests {

        @Test
        @DisplayName("Kotlin code style should be official")
        fun `kotlin code style should be official`() {
            assertEquals(
                "official",
                gradleProperties.getProperty("kotlin.code.style"),
                "Kotlin code style should be set to 'official'"
            )
        }

        @Test
        @DisplayName("Kotlin incremental compilation should be enabled")
        fun `kotlin incremental compilation should be enabled`() {
            assertEquals(
                "true",
                gradleProperties.getProperty("kotlin.incremental"),
                "Kotlin incremental compilation should be enabled"
            )
        }

        @Test
        @DisplayName("Kotlin build report should be configured")
        fun `kotlin build report should be configured`() {
            assertEquals(
                "file",
                gradleProperties.getProperty("kotlin.build.report.output"),
                "Kotlin build report output should be set to 'file'"
            )
        }

        @Test
        @DisplayName("Kotlin properties should be optimized for development")
        fun `kotlin properties should be optimized for development`() {
            // All Kotlin optimization properties should be enabled
            val kotlinOptimizations = mapOf(
                "kotlin.incremental" to "true",
                "kotlin.code.style" to "official"
            )

            kotlinOptimizations.forEach { (property, expectedValue) ->
                assertEquals(
                    expectedValue,
                    gradleProperties.getProperty(property),
                    "Kotlin property '$property' should be optimized"
                )
            }
        }
    }

    @Nested
    @DisplayName("Android Configuration Tests")
    inner class AndroidConfigurationTests {

        @Test
        @DisplayName("AndroidX should be enabled")
        fun `android x should be enabled`() {
            assertEquals(
                "true",
                gradleProperties.getProperty("android.useAndroidX"),
                "AndroidX should be enabled"
            )
        }

        @Test
        @DisplayName("Android lint should ignore test sources")
        fun `android lint should ignore test sources`() {
            assertEquals(
                "true",
                gradleProperties.getProperty("android.lint.ignoreTestSources"),
                "Android lint should ignore test sources"
            )
        }

        @Test
        @DisplayName("Non-transitive R class should be enabled")
        fun `non transitive r class should be enabled`() {
            assertEquals(
                "true",
                gradleProperties.getProperty("android.nonTransitiveRClass"),
                "Non-transitive R class should be enabled for better build performance"
            )
        }

        @Test
        @DisplayName("Non-final resource IDs should be disabled")
        fun `non final resource ids should be disabled`() {
            assertEquals(
                "false",
                gradleProperties.getProperty("android.nonFinalResIds"),
                "Non-final resource IDs should be disabled"
            )
        }

        @Test
        @DisplayName("AGP version should be specified and valid")
        fun `agp version should be specified and valid`() {
            val agpVersion = gradleProperties.getProperty("agp.version")
            assertNotNull(agpVersion, "AGP version should be specified")
            assertTrue(agpVersion.isNotEmpty(), "AGP version should not be empty")
            
            // Should match the expected AGP version format
            assertEquals(
                "8.13-rc01-kotlin-2.2.0",
                agpVersion,
                "AGP version should match the expected version"
            )
        }

        @Test
        @DisplayName("Compile SDK suppression should be configured for API 36.2")
        fun `compile sdk suppression should be configured for api 36 2`() {
            assertEquals(
                "36.2",
                gradleProperties.getProperty("android.suppressUnsupportedCompileSdk"),
                "Should suppress unsupported compile SDK warning for API 36.2"
            )
        }

        @Test
        @DisplayName("Android properties should be consistent with project requirements")
        fun `android properties should be consistent with project requirements`() {
            // Verify Android properties work together
            assertTrue(
                gradleProperties.getProperty("android.useAndroidX") == "true",
                "AndroidX must be enabled for modern Android development"
            )
            
            assertTrue(
                gradleProperties.getProperty("android.nonTransitiveRClass") == "true",
                "Non-transitive R class should be enabled for build optimization"
            )
        }
    }

    @Nested
    @DisplayName("Logging and Debugging Configuration Tests")
    inner class LoggingConfigurationTests {

        @Test
        @DisplayName("Gradle logging level should be set to info")
        fun `gradle logging level should be set to info`() {
            assertEquals(
                "info",
                gradleProperties.getProperty("org.gradle.logging.level"),
                "Gradle logging level should be set to 'info' for detailed debugging"
            )
        }

        @Test
        @DisplayName("Gradle console should be set to plain")
        fun `gradle console should be set to plain`() {
            assertEquals(
                "plain",
                gradleProperties.getProperty("org.gradle.console"),
                "Gradle console should be set to 'plain' for CI/CD compatibility"
            )
        }

        @Test
        @DisplayName("Logging configuration should be CI/CD friendly")
        fun `logging configuration should be ci cd friendly`() {
            val consoleMode = gradleProperties.getProperty("org.gradle.console")
            val loggingLevel = gradleProperties.getProperty("org.gradle.logging.level")
            
            assertEquals("plain", consoleMode, "Console should be plain for CI/CD")
            assertEquals("info", loggingLevel, "Logging should be info level for debugging")
        }
    }

    @Nested
    @DisplayName("Java Home Configuration Tests")
    inner class JavaHomeConfigurationTests {

        @Test
        @DisplayName("Java home should be configured")
        fun `java home should be configured`() {
            val javaHome = gradleProperties.getProperty("org.gradle.java.home")
            assertNotNull(javaHome, "Java home should be configured")
            assertTrue(javaHome.isNotEmpty(), "Java home path should not be empty")
        }

        @Test
        @DisplayName("Java home should point to Java 21")
        fun `java home should point to java 21`() {
            val javaHome = gradleProperties.getProperty("org.gradle.java.home")
            assertTrue(
                javaHome.contains("21"),
                "Java home should point to Java 21 installation, got: $javaHome"
            )
        }

        @Test
        @DisplayName("Java home path should be valid format")
        fun `java home path should be valid format`() {
            val javaHome = gradleProperties.getProperty("org.gradle.java.home")
            
            // Should be an absolute path
            assertTrue(
                javaHome.startsWith("/") || javaHome.matches(Regex("[A-Z]:\\\\.+")),
                "Java home should be an absolute path"
            )
            
            // Should contain Java version info
            assertTrue(
                javaHome.contains("21.0") || javaHome.contains("jdk"),
                "Java home path should contain Java version information"
            )
        }
    }

    @Nested
    @DisplayName("Property Value Format Validation")
    inner class PropertyValueFormatTests {

        @ParameterizedTest
        @ValueSource(strings = [
            "org.gradle.daemon",
            "org.gradle.parallel",
            "org.gradle.caching",
            "org.gradle.configureondemand",
            "android.useAndroidX",
            "android.lint.ignoreTestSources",
            "kotlin.incremental"
        ])
        @DisplayName("Boolean properties should have valid values")
        fun `boolean properties should have valid values`(propertyName: String) {
            val value = gradleProperties.getProperty(propertyName)
            assertTrue(
                value == "true" || value == "false",
                "Property '$propertyName' should have boolean value (true/false), got: $value"
            )
        }

        @Test
        @DisplayName("AGP version should follow semantic versioning pattern")
        fun `agp version should follow semantic versioning pattern`() {
            val agpVersion = gradleProperties.getProperty("agp.version")
            assertTrue(
                agpVersion.matches(Regex("""\d+\.\d+.*""")),
                "AGP version should follow semantic versioning pattern, got: $agpVersion"
            )
        }

        @Test
        @DisplayName("Numeric properties should have valid numeric values")
        fun `numeric properties should have valid numeric values`() {
            val suppressSdk = gradleProperties.getProperty("android.suppressUnsupportedCompileSdk")
            assertTrue(
                suppressSdk.matches(Regex("""\d+\.\d+""")),
                "Suppress compile SDK should be a valid version number, got: $suppressSdk"
            )
        }

        @Test
        @DisplayName("Enum-like properties should have valid values")
        fun `enum like properties should have valid values`() {
            val codeStyle = gradleProperties.getProperty("kotlin.code.style")
            val validCodeStyles = listOf("official", "obsolete")
            assertTrue(
                validCodeStyles.contains(codeStyle),
                "Kotlin code style should be one of $validCodeStyles, got: $codeStyle"
            )
            
            val buildReportOutput = gradleProperties.getProperty("kotlin.build.report.output")
            val validOutputs = listOf("file", "console", "build_scan")
            assertTrue(
                validOutputs.contains(buildReportOutput),
                "Kotlin build report output should be one of $validOutputs, got: $buildReportOutput"
            )
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle missing optional properties gracefully")
        fun `should handle missing optional properties gracefully`() {
            // Test that optional properties (like build scan settings) can be missing
            val buildScanUrl = gradleProperties.getProperty("com.gradle.enterprise.build-scanning.termsOfServiceUrl")
            val buildScanAgree = gradleProperties.getProperty("com.gradle.enterprise.build-scanning.termsOfServiceAgree")
            
            // These should be null or empty (commented out in the file)
            assertTrue(
                buildScanUrl.isNullOrEmpty(),
                "Build scan URL should be null/empty (commented out)"
            )
            assertTrue(
                buildScanAgree.isNullOrEmpty(),
                "Build scan agreement should be null/empty (commented out)"
            )
        }

        @Test
        @DisplayName("Should handle properties with multiline values")
        fun `should handle properties with multiline values`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs")
            assertNotNull(jvmArgs, "JVM args should not be null")
            assertTrue(jvmArgs.isNotEmpty(), "JVM args should not be empty")
            
            // Should contain multiple arguments joined together
            assertTrue(jvmArgs.contains("--add-opens"), "Should contain add-opens arguments")
            assertTrue(jvmArgs.contains("-Xmx4g"), "Should contain memory setting")
        }

        @Test
        @DisplayName("Property values should not contain dangerous characters")
        fun `property values should not contain dangerous characters`() {
            gradleProperties.forEach { (key, value) ->
                val valueStr = value.toString()
                assertFalse(
                    valueStr.contains("$(") || valueStr.contains("`"),
                    "Property '$key' should not contain command injection characters"
                )
            }
        }

        @Test
        @DisplayName("Should validate memory settings are reasonable")
        fun `should validate memory settings are reasonable`() {
            val jvmArgs = gradleProperties.getProperty("org.gradle.jvmargs")
            
            // Memory should be between 1GB and 16GB for reasonable builds
            assertTrue(
                jvmArgs.contains("-Xmx4g") || jvmArgs.contains("-Xmx8g"),
                "Memory setting should be reasonable for AuraFrameFX builds"
            )
        }

        @Test
        @DisplayName("Should handle empty or malformed property files")
        fun `should handle empty or malformed property files`() {
            // Test that the properties object can handle edge cases
            val emptyProps = Properties()
            assertDoesNotThrow {
                emptyProps.getProperty("nonexistent.property", "default")
            }
        }
    }

    @Nested
    @DisplayName("Integration and Consistency Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Build optimization properties should work together")
        fun `build optimization properties should work together`() {
            val daemon = gradleProperties.getProperty("org.gradle.daemon")
            val parallel = gradleProperties.getProperty("org.gradle.parallel")
            val caching = gradleProperties.getProperty("org.gradle.caching")
            val configCache = gradleProperties.getProperty("org.gradle.configuration-cache")
            
            // All optimization flags should be consistently enabled
            assertEquals("true", daemon, "Daemon should be enabled")
            assertEquals("true", parallel, "Parallel should be enabled")
            assertEquals("true", caching, "Caching should be enabled")
            assertEquals("true", configCache, "Configuration cache should be enabled")
        }

        @Test
        @DisplayName("Android and Kotlin properties should be compatible")
        fun `android and kotlin properties should be compatible`() {
            val androidX = gradleProperties.getProperty("android.useAndroidX")
            val kotlinStyle = gradleProperties.getProperty("kotlin.code.style")
            
            assertEquals("true", androidX, "AndroidX should be enabled for Kotlin compatibility")
            assertEquals("official", kotlinStyle, "Official Kotlin style works best with AndroidX")
        }

        @Test
        @DisplayName("All critical AuraFrameFX properties should be present and valid")
        fun `all critical aura frame fx properties should be present and valid`() {
            val criticalProperties = mapOf(
                "org.gradle.jvmargs" to { value: String -> value.contains("-Xmx4g") },
                "org.gradle.java.home" to { value: String -> value.contains("21") },
                "agp.version" to { value: String -> value.startsWith("8.") },
                "kotlin.code.style" to { value: String -> value == "official" },
                "android.useAndroidX" to { value: String -> value == "true" }
            )

            criticalProperties.forEach { (property, validator) ->
                val value = gradleProperties.getProperty(property)
                assertNotNull(value, "Critical property '$property' should be present")
                assertTrue(
                    validator(value),
                    "Critical property '$property' should have valid value, got: $value"
                )
            }
        }
    }
}