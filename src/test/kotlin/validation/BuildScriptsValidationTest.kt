package validation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.clearAllMocks
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking

/**
 * Comprehensive unit tests for BuildScriptsValidation functionality.
 * Tests cover validation of Gradle build scripts, dependency checks,
 * syntax validation, and security scanning.
 *
 * Testing Framework: JUnit 5 with MockK for mocking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildScriptsValidationTest {

    private val fileSystem: FileSystem = mockk()
    private val gradleRunner: GradleRunner = mockk()
    private val securityScanner: SecurityScanner = mockk()
    private lateinit var validator: BuildScriptsValidator

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        validator = BuildScriptsValidator(fileSystem, gradleRunner, securityScanner)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("Build Script Discovery Tests")
    inner class BuildScriptDiscoveryTests {

        @Test
        @DisplayName("Should find all Gradle build scripts in project directory")
        fun shouldFindAllGradleBuildScripts() {
            // Given
            val projectRoot = tempDir.toFile()
            val buildGradle = File(projectRoot, "build.gradle").apply { 
                writeText("plugins { id 'java' }") 
            }
            val buildGradleKts = File(projectRoot, "build.gradle.kts").apply { 
                writeText("plugins { kotlin(\"jvm\") }") 
            }
            val submoduleBuild = File(projectRoot, "submodule/build.gradle").apply { 
                parentFile.mkdirs()
                writeText("dependencies { implementation 'junit:junit:4.13' }")
            }

            every { fileSystem.findBuildScripts(projectRoot) } returns 
                listOf(buildGradle, buildGradleKts, submoduleBuild)

            // When
            val foundScripts = validator.discoverBuildScripts(projectRoot)

            // Then
            assertEquals(3, foundScripts.size)
            assertTrue(foundScripts.contains(buildGradle))
            assertTrue(foundScripts.contains(buildGradleKts))
            assertTrue(foundScripts.contains(submoduleBuild))
            verify { fileSystem.findBuildScripts(projectRoot) }
        }

        @Test
        @DisplayName("Should handle empty project directory gracefully")
        fun shouldHandleEmptyProjectDirectory() {
            // Given
            val emptyDir = tempDir.toFile()
            every { fileSystem.findBuildScripts(emptyDir) } returns emptyList()

            // When
            val foundScripts = validator.discoverBuildScripts(emptyDir)

            // Then
            assertTrue(foundScripts.isEmpty())
            verify { fileSystem.findBuildScripts(emptyDir) }
        }

        @Test
        @DisplayName("Should ignore invalid or inaccessible directories")
        fun shouldIgnoreInvalidDirectories() {
            // Given
            val invalidDir = File("/invalid/path/that/does/not/exist")

            // When & Then
            assertFailsWith<IllegalArgumentException> {
                validator.discoverBuildScripts(invalidDir)
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts"])
        @DisplayName("Should recognize valid Gradle script file names")
        fun shouldRecognizeValidGradleScriptNames(fileName: String) {
            // Given
            val scriptFile = File(tempDir.toFile(), fileName).apply { 
                writeText("// Valid gradle script") 
            }

            // When
            val isValid = validator.isValidBuildScript(scriptFile)

            // Then
            assertTrue(isValid)
        }

        @ParameterizedTest
        @ValueSource(strings = ["invalid.txt", "notabuild.groovy", "script.kts"])
        @DisplayName("Should reject invalid build script file names")
        fun shouldRejectInvalidBuildScriptNames(fileName: String) {
            // Given
            val scriptFile = File(tempDir.toFile(), fileName).apply { 
                writeText("// Invalid gradle script") 
            }

            // When
            val isValid = validator.isValidBuildScript(scriptFile)

            // Then
            assertFalse(isValid)
        }
    }

    @Nested
    @DisplayName("Syntax Validation Tests")
    inner class SyntaxValidationTests {

        @Test
        @DisplayName("Should validate correct Groovy syntax in build.gradle")
        fun shouldValidateCorrectGroovySyntax() {
            // Given
            val validGroovyScript = """
                plugins {
                    id 'java'
                    id 'application'
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation 'org.springframework:spring-core:5.3.0'
                    testImplementation 'junit:junit:4.13.2'
                }
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply { 
                writeText(validGroovyScript) 
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()

            // When
            val result = validator.validateSyntax(scriptFile)

            // Then
            assertTrue(result.isValid)
            assertNull(result.errorMessage)
            verify { gradleRunner.validateSyntax(scriptFile) }
        }

        @Test
        @DisplayName("Should detect syntax errors in Groovy build scripts")
        fun shouldDetectGroovySyntaxErrors() {
            // Given
            val invalidGroovyScript = """
                plugins {
                    id 'java'
                    // Missing closing brace
                
                repositories {
                    mavenCentral()
                }
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply { 
                writeText(invalidGroovyScript) 
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns 
                ValidationResult.error("Unexpected token at line 4")

            // When
            val result = validator.validateSyntax(scriptFile)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.errorMessage)
            assertTrue(result.errorMessage!!.contains("Unexpected token"))
        }

        @Test
        @DisplayName("Should validate correct Kotlin DSL syntax in build.gradle.kts")
        fun shouldValidateCorrectKotlinDslSyntax() {
            // Given
            val validKotlinScript = """
                plugins {
                    kotlin("jvm") version "1.8.0"
                    application
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation("org.springframework:spring-core:5.3.0")
                    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
                }
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle.kts").apply { 
                writeText(validKotlinScript) 
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()

            // When
            val result = validator.validateSyntax(scriptFile)

            // Then
            assertTrue(result.isValid)
        }

        @Test
        @DisplayName("Should handle Gradle runner exceptions gracefully")
        fun shouldHandleGradleRunnerExceptions() {
            // Given
            val scriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText("plugins { id 'java' }")
            }

            every { gradleRunner.validateSyntax(scriptFile) } throws 
                RuntimeException("Gradle validation timeout")

            // When
            val result = validator.validateSyntax(scriptFile)

            // Then
            assertFalse(result.isValid)
            assertTrue(result.errorMessage!!.contains("timeout"))
        }

        @Test
        @DisplayName("Should validate syntax asynchronously")
        fun shouldValidateSyntaxAsynchronously() = runTest {
            // Given
            val scriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText("plugins { id 'java' }")
            }

            every { gradleRunner.validateSyntaxAsync(scriptFile) } returns ValidationResult.success()

            // When
            val result = validator.validateSyntaxAsync(scriptFile)

            // Then
            assertTrue(result.isValid)
            verify { gradleRunner.validateSyntaxAsync(scriptFile) }
        }
    }

    @Nested
    @DisplayName("Dependency Validation Tests")
    inner class DependencyValidationTests {

        @Test
        @DisplayName("Should validate dependencies with proper version specifications")
        fun shouldValidateDependenciesWithProperVersions() {
            // Given
            val dependencies = listOf(
                "org.springframework:spring-core:5.3.0",
                "junit:junit:4.13.2",
                "org.slf4j:slf4j-api:1.7.30"
            )

            // When
            val result = validator.validateDependencies(dependencies)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.validDependencies.containsAll(dependencies))
            assertTrue(result.invalidDependencies.isEmpty())
        }

        @Test
        @DisplayName("Should detect dependencies with invalid version formats")
        fun shouldDetectInvalidVersionFormats() {
            // Given
            val dependencies = listOf(
                "org.springframework:spring-core:5.3.0",
                "junit:junit:invalid-version",
                "org.slf4j:slf4j-api:" // missing version
            )

            // When
            val result = validator.validateDependencies(dependencies)

            // Then
            assertFalse(result.isValid)
            assertEquals(1, result.validDependencies.size)
            assertEquals(2, result.invalidDependencies.size)
        }

        @ParameterizedTest
        @CsvSource(
            "org.springframework:spring-core:5.3.0, true",
            "junit:junit:4.13.2, true",
            "invalid-dependency, false",
            "group:artifact:, false",
            ":artifact:version, false",
            "group::version, false"
        )
        @DisplayName("Should validate individual dependency format")
        fun shouldValidateIndividualDependencyFormat(dependency: String, expected: Boolean) {
            // When
            val isValid = validator.isValidDependencyFormat(dependency)

            // Then
            assertEquals(expected, isValid)
        }

        @Test
        @DisplayName("Should detect potential security vulnerabilities in dependencies")
        fun shouldDetectSecurityVulnerabilities() {
            // Given
            val dependencies = listOf(
                "org.springframework:spring-core:4.3.0", // Old vulnerable version
                "junit:junit:4.13.2"
            )

            every { securityScanner.scanDependencies(dependencies) } returns
                SecurityScanResult(
                    vulnerableDependencies = listOf("org.springframework:spring-core:4.3.0"),
                    severityLevel = SeverityLevel.HIGH
                )

            // When
            val result = validator.performSecurityScan(dependencies)

            // Then
            assertFalse(result.isSecure)
            assertEquals(1, result.vulnerableDependencies.size)
            assertEquals(SeverityLevel.HIGH, result.severityLevel)
        }

        @Test
        @DisplayName("Should validate dependencies against allowed list")
        fun shouldValidateAgainstAllowedList() {
            // Given
            val allowedDependencies = setOf(
                "org.springframework:spring-core",
                "junit:junit",
                "org.slf4j:slf4j-api"
            )
            val dependencies = listOf(
                "org.springframework:spring-core:5.3.0",
                "malicious:package:1.0.0" // Not in allowed list
            )

            // When
            val result = validator.validateAgainstAllowedList(dependencies, allowedDependencies)

            // Then
            assertFalse(result.isValid)
            assertEquals(1, result.allowedDependencies.size)
            assertEquals(1, result.blockedDependencies.size)
        }

        @Test
        @DisplayName("Should handle empty dependency lists")
        fun shouldHandleEmptyDependencyLists() {
            // Given
            val emptyDependencies = emptyList<String>()

            // When
            val result = validator.validateDependencies(emptyDependencies)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.validDependencies.isEmpty())
            assertTrue(result.invalidDependencies.isEmpty())
        }

        @Test
        @DisplayName("Should validate version ranges")
        fun shouldValidateVersionRanges() {
            // Given
            val dependenciesWithRanges = listOf(
                "org.springframework:spring-core:[5.0,6.0)",
                "junit:junit:4.+",
                "org.slf4j:slf4j-api:latest.release"
            )

            // When
            val result = validator.validateDependencies(dependenciesWithRanges)

            // Then
            assertTrue(result.isValid)
            assertEquals(3, result.validDependencies.size)
        }
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    inner class ConfigurationValidationTests {

        @Test
        @DisplayName("Should validate Gradle wrapper configuration")
        fun shouldValidateGradleWrapperConfiguration() {
            // Given
            val wrapperProperties = """
                distributionBase=GRADLE_USER_HOME
                distributionPath=wrapper/dists
                distributionUrl=https\://services.gradle.org/distributions/gradle-7.4-bin.zip
                zipStoreBase=GRADLE_USER_HOME
                zipStorePath=wrapper/dists
            """.trimIndent()

            val wrapperFile = File(tempDir.toFile(), "gradle/wrapper/gradle-wrapper.properties").apply {
                parentFile.mkdirs()
                writeText(wrapperProperties)
            }

            // When
            val result = validator.validateWrapperConfiguration(wrapperFile)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.hasSecureDistributionUrl)
            assertNotNull(result.gradleVersion)
            assertEquals("7.4", result.gradleVersion)
        }

        @Test
        @DisplayName("Should detect insecure Gradle wrapper URLs")
        fun shouldDetectInsecureWrapperUrls() {
            // Given
            val insecureWrapperProperties = """
                distributionBase=GRADLE_USER_HOME
                distributionPath=wrapper/dists
                distributionUrl=http\://malicious-site.com/gradle-7.4-bin.zip
                zipStoreBase=GRADLE_USER_HOME
                zipStorePath=wrapper/dists
            """.trimIndent()

            val wrapperFile = File(tempDir.toFile(), "gradle/wrapper/gradle-wrapper.properties").apply {
                parentFile.mkdirs()
                writeText(insecureWrapperProperties)
            }

            // When
            val result = validator.validateWrapperConfiguration(wrapperFile)

            // Then
            assertFalse(result.isValid)
            assertFalse(result.hasSecureDistributionUrl)
            assertTrue(result.securityIssues.contains("Insecure distribution URL"))
        }

        @Test
        @DisplayName("Should validate repository configurations")
        fun shouldValidateRepositoryConfigurations() {
            // Given
            val buildScript = """
                repositories {
                    mavenCentral()
                    gradlePluginPortal()
                    maven { url = uri("https://repo.spring.io/release") }
                }
            """.trimIndent()

            // When
            val result = validator.validateRepositories(buildScript)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.hasSecureRepositories)
            assertFalse(result.hasInsecureHttpRepositories)
        }

        @Test
        @DisplayName("Should detect insecure HTTP repositories")
        fun shouldDetectInsecureHttpRepositories() {
            // Given
            val buildScript = """
                repositories {
                    mavenCentral()
                    maven { url = uri("http://insecure-repo.com/maven") }
                }
            """.trimIndent()

            // When
            val result = validator.validateRepositories(buildScript)

            // Then
            assertFalse(result.isValid)
            assertTrue(result.hasInsecureHttpRepositories)
            assertEquals(1, result.insecureRepositories.size)
        }

        @Test
        @DisplayName("Should validate plugin configurations")
        fun shouldValidatePluginConfigurations() {
            // Given
            val buildScript = """
                plugins {
                    id("java")
                    id("org.springframework.boot") version "2.7.0"
                    kotlin("jvm") version "1.7.10"
                }
            """.trimIndent()

            // When
            val result = validator.validatePlugins(buildScript)

            // Then
            assertTrue(result.isValid)
            assertEquals(3, result.detectedPlugins.size)
            assertTrue(result.detectedPlugins.contains("java"))
            assertTrue(result.detectedPlugins.contains("org.springframework.boot"))
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Should perform complete validation of build script")
        fun shouldPerformCompleteValidation() {
            // Given
            val buildScript = """
                plugins {
                    id 'java'
                    id 'application'
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation 'org.springframework:spring-core:5.3.0'
                    testImplementation 'junit:junit:4.13.2'
                }
                
                application {
                    mainClass = 'com.example.Main'
                }
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply { 
                writeText(buildScript) 
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()
            every { securityScanner.scanDependencies(any()) } returns 
                SecurityScanResult(emptyList(), SeverityLevel.NONE)

            // When
            val result = validator.validateBuildScript(scriptFile)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.syntaxValid)
            assertTrue(result.dependenciesValid)
            assertTrue(result.securityScanPassed)
            verifyOrder {
                gradleRunner.validateSyntax(scriptFile)
                securityScanner.scanDependencies(any())
            }
        }

        @Test
        @DisplayName("Should fail validation when multiple issues are present")
        fun shouldFailValidationWithMultipleIssues() {
            // Given
            val problematicScript = """
                plugins {
                    id 'java'
                    // Missing closing brace
                
                repositories {
                    maven { url = uri("http://insecure-repo.com/maven") }
                }
                
                dependencies {
                    implementation 'vulnerable:package:1.0.0'
                }
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply { 
                writeText(problematicScript) 
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns 
                ValidationResult.error("Syntax error")
            every { securityScanner.scanDependencies(any()) } returns 
                SecurityScanResult(listOf("vulnerable:package:1.0.0"), SeverityLevel.HIGH)

            // When
            val result = validator.validateBuildScript(scriptFile)

            // Then
            assertFalse(result.isValid)
            assertFalse(result.syntaxValid)
            assertFalse(result.securityScanPassed)
            assertTrue(result.issues.size >= 2)
        }

        @Test
        @DisplayName("Should validate multiple build scripts in project")
        fun shouldValidateMultipleBuildScripts() {
            // Given
            val mainBuild = File(tempDir.toFile(), "build.gradle").apply {
                writeText("plugins { id 'java' }")
            }
            val submoduleBuild = File(tempDir.toFile(), "submodule/build.gradle").apply {
                parentFile.mkdirs()
                writeText("dependencies { implementation 'junit:junit:4.13.2' }")
            }

            every { fileSystem.findBuildScripts(tempDir.toFile()) } returns 
                listOf(mainBuild, submoduleBuild)
            every { gradleRunner.validateSyntax(any()) } returns ValidationResult.success()
            every { securityScanner.scanDependencies(any()) } returns 
                SecurityScanResult(emptyList(), SeverityLevel.NONE)

            // When
            val results = validator.validateProject(tempDir.toFile())

            // Then
            assertEquals(2, results.size)
            assertTrue(results.all { it.isValid })
        }

        @Test
        @DisplayName("Should handle mixed Groovy and Kotlin DSL scripts")
        fun shouldHandleMixedGroovyAndKotlinDslScripts() {
            // Given
            val groovyScript = File(tempDir.toFile(), "build.gradle").apply {
                writeText("plugins { id 'java' }")
            }
            val kotlinScript = File(tempDir.toFile(), "module/build.gradle.kts").apply {
                parentFile.mkdirs()
                writeText("plugins { kotlin(\"jvm\") }")
            }

            every { fileSystem.findBuildScripts(tempDir.toFile()) } returns 
                listOf(groovyScript, kotlinScript)
            every { gradleRunner.validateSyntax(any()) } returns ValidationResult.success()
            every { securityScanner.scanDependencies(any()) } returns 
                SecurityScanResult(emptyList(), SeverityLevel.NONE)

            // When
            val results = validator.validateProject(tempDir.toFile())

            // Then
            assertEquals(2, results.size)
            assertTrue(results.all { it.isValid })
            verify(exactly = 2) { gradleRunner.validateSyntax(any()) }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle file system errors gracefully")
        fun shouldHandleFileSystemErrorsGracefully() {
            // Given
            val inaccessibleFile = File("/root/restricted/build.gradle")

            // When & Then
            assertFailsWith<ValidationException> {
                validator.validateBuildScript(inaccessibleFile)
            }
        }

        @Test
        @DisplayName("Should handle security scanner failures")
        fun shouldHandleSecurityScannerFailures() {
            // Given
            val dependencies = listOf("org.springframework:spring-core:5.3.0")

            every { securityScanner.scanDependencies(dependencies) } throws 
                RuntimeException("Security scanner unavailable")

            // When
            val result = validator.performSecurityScan(dependencies)

            // Then
            assertFalse(result.isSecure)
            assertTrue(result.errorMessage!!.contains("scanner unavailable"))
        }

        @Test
        @DisplayName("Should handle corrupted wrapper properties files")
        fun shouldHandleCorruptedWrapperProperties() {
            // Given
            val corruptedWrapperFile = File(tempDir.toFile(), "gradle/wrapper/gradle-wrapper.properties").apply {
                parentFile.mkdirs()
                writeText("corrupted file content without proper format")
            }

            // When
            val result = validator.validateWrapperConfiguration(corruptedWrapperFile)

            // Then
            assertFalse(result.isValid)
            assertTrue(result.securityIssues.contains("Insecure distribution URL"))
        }

        @Test
        @DisplayName("Should handle network timeouts during security scanning")
        fun shouldHandleNetworkTimeouts() {
            // Given
            val dependencies = listOf("org.springframework:spring-core:5.3.0")

            every { securityScanner.scanDependencies(dependencies) } throws 
                java.net.SocketTimeoutException("Network timeout")

            // When
            val result = validator.performSecurityScan(dependencies)

            // Then
            assertFalse(result.isSecure)
            assertTrue(result.errorMessage!!.contains("Network timeout"))
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    inner class PerformanceTests {

        @Test
        @DisplayName("Should validate large build scripts efficiently")
        fun shouldValidateLargeBuildScriptsEfficiently() {
            // Given
            val largeBuildScript = buildString {
                appendLine("plugins { id 'java' }")
                appendLine("dependencies {")
                repeat(1000) { i ->
                    appendLine("    implementation 'group$i:artifact$i:1.0.0'")
                }
                appendLine("}")
            }

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText(largeBuildScript)
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()

            // When
            val startTime = System.currentTimeMillis()
            val result = validator.validateSyntax(scriptFile)
            val endTime = System.currentTimeMillis()

            // Then
            assertTrue(result.isValid)
            assertTrue((endTime - startTime) < 5000) // Should complete within 5 seconds
        }

        @Test
        @DisplayName("Should handle concurrent validation requests")
        fun shouldHandleConcurrentValidationRequests() = runBlocking {
            // Given
            val scriptFiles = (1..10).map { i ->
                File(tempDir.toFile(), "build$i.gradle").apply {
                    writeText("plugins { id 'java' }")
                }
            }

            every { gradleRunner.validateSyntax(any()) } returns ValidationResult.success()

            // When
            val results = scriptFiles.map { file ->
                kotlinx.coroutines.async {
                    validator.validateSyntax(file)
                }
            }.map { it.await() }

            // Then
            assertEquals(10, results.size)
            assertTrue(results.all { it.isValid })
        }

        @Test
        @DisplayName("Should cache validation results for identical files")
        fun shouldCacheValidationResults() {
            // Given
            val scriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText("plugins { id 'java' }")
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()

            // When - validate the same file multiple times
            val result1 = validator.validateSyntax(scriptFile)
            val result2 = validator.validateSyntax(scriptFile)
            val result3 = validator.validateSyntax(scriptFile)

            // Then
            assertTrue(result1.isValid)
            assertTrue(result2.isValid)
            assertTrue(result3.isValid)
            // Should only call the gradle runner once due to caching
            verify(exactly = 1) { gradleRunner.validateSyntax(scriptFile) }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty build scripts")
        fun shouldHandleEmptyBuildScripts() {
            // Given
            val emptyScriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText("")
            }

            every { gradleRunner.validateSyntax(emptyScriptFile) } returns ValidationResult.success()

            // When
            val result = validator.validateBuildScript(emptyScriptFile)

            // Then
            assertTrue(result.isValid)
            assertTrue(result.syntaxValid)
        }

        @Test
        @DisplayName("Should handle build scripts with only comments")
        fun shouldHandleBuildScriptsWithOnlyComments() {
            // Given
            val commentOnlyScript = """
                // This is a comment
                /* Multi-line comment
                   spanning multiple lines */
                // Another comment
            """.trimIndent()

            val scriptFile = File(tempDir.toFile(), "build.gradle").apply {
                writeText(commentOnlyScript)
            }

            every { gradleRunner.validateSyntax(scriptFile) } returns ValidationResult.success()

            // When
            val result = validator.validateBuildScript(scriptFile)

            // Then
            assertTrue(result.isValid)
        }

        @Test
        @DisplayName("Should handle very long dependency names")
        fun shouldHandleVeryLongDependencyNames() {
            // Given
            val longGroupName = "a".repeat(100)
            val longArtifactName = "b".repeat(100)
            val dependency = "$longGroupName:$longArtifactName:1.0.0"

            // When
            val isValid = validator.isValidDependencyFormat(dependency)

            // Then
            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should handle dependencies with special characters")
        fun shouldHandleDependenciesWithSpecialCharacters() {
            // Given
            val dependencies = listOf(
                "org.springframework:spring-core-test:5.3.0",
                "com.example:my_artifact:1.0.0",
                "io.github.user:artifact.name:2.1.0"
            )

            // When
            val result = validator.validateDependencies(dependencies)

            // Then
            assertTrue(result.isValid)
            assertEquals(3, result.validDependencies.size)
        }

        @Test
        @DisplayName("Should handle nested subproject build scripts")
        fun shouldHandleNestedSubprojectBuildScripts() {
            // Given
            val deeplyNestedScript = File(tempDir.toFile(), "a/b/c/d/build.gradle").apply {
                parentFile.mkdirs()
                writeText("plugins { id 'java' }")
            }

            every { fileSystem.findBuildScripts(tempDir.toFile()) } returns listOf(deeplyNestedScript)
            every { gradleRunner.validateSyntax(deeplyNestedScript) } returns ValidationResult.success()
            every { securityScanner.scanDependencies(any()) } returns 
                SecurityScanResult(emptyList(), SeverityLevel.NONE)

            // When
            val results = validator.validateProject(tempDir.toFile())

            // Then
            assertEquals(1, results.size)
            assertTrue(results.first().isValid)
        }
    }
}

/**
 * Helper data classes and interfaces for testing
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun success() = ValidationResult(true)
        fun error(message: String) = ValidationResult(false, message)
    }
}

data class SecurityScanResult(
    val vulnerableDependencies: List<String>,
    val severityLevel: SeverityLevel,
    val errorMessage: String? = null
) {
    val isSecure: Boolean get() = vulnerableDependencies.isEmpty() && errorMessage == null
}

enum class SeverityLevel { NONE, LOW, MEDIUM, HIGH, CRITICAL }

data class DependencyValidationResult(
    val isValid: Boolean,
    val validDependencies: List<String>,
    val invalidDependencies: List<String>
)

data class AllowedListValidationResult(
    val isValid: Boolean,
    val allowedDependencies: List<String>,
    val blockedDependencies: List<String>
)

data class WrapperValidationResult(
    val isValid: Boolean,
    val hasSecureDistributionUrl: Boolean,
    val gradleVersion: String?,
    val securityIssues: List<String> = emptyList()
)

data class RepositoryValidationResult(
    val isValid: Boolean,
    val hasSecureRepositories: Boolean,
    val hasInsecureHttpRepositories: Boolean,
    val insecureRepositories: List<String> = emptyList()
)

data class PluginValidationResult(
    val isValid: Boolean,
    val detectedPlugins: List<String>,
    val issues: List<String> = emptyList()
)

data class BuildScriptValidationResult(
    val isValid: Boolean,
    val syntaxValid: Boolean,
    val dependenciesValid: Boolean,
    val securityScanPassed: Boolean,
    val issues: List<String> = emptyList()
)

class ValidationException(message: String, cause: Throwable? = null) : Exception(message, cause)

// Mock interfaces for testing
interface FileSystem {
    fun findBuildScripts(directory: File): List<File>
}

interface GradleRunner {
    fun validateSyntax(scriptFile: File): ValidationResult
    suspend fun validateSyntaxAsync(scriptFile: File): ValidationResult
}

interface SecurityScanner {
    fun scanDependencies(dependencies: List<String>): SecurityScanResult
}

// Main class being tested (stub implementation for comprehensive testing)
class BuildScriptsValidator(
    private val fileSystem: FileSystem,
    private val gradleRunner: GradleRunner,
    private val securityScanner: SecurityScanner
) {
    
    private val validationCache = mutableMapOf<String, ValidationResult>()
    
    fun discoverBuildScripts(projectRoot: File): List<File> {
        if (!projectRoot.exists() || !projectRoot.isDirectory) {
            throw IllegalArgumentException("Invalid project directory: ${projectRoot.path}")
        }
        return fileSystem.findBuildScripts(projectRoot)
    }
    
    fun isValidBuildScript(file: File): Boolean {
        return file.name.matches(Regex("(build|settings)\\.gradle(\\.kts)?"))
    }
    
    fun validateSyntax(scriptFile: File): ValidationResult {
        val cacheKey = "${scriptFile.absolutePath}:${scriptFile.lastModified()}"
        return validationCache.getOrPut(cacheKey) {
            try {
                gradleRunner.validateSyntax(scriptFile)
            } catch (e: Exception) {
                ValidationResult.error("Validation failed: ${e.message}")
            }
        }
    }
    
    suspend fun validateSyntaxAsync(scriptFile: File): ValidationResult {
        return try {
            gradleRunner.validateSyntaxAsync(scriptFile)
        } catch (e: Exception) {
            ValidationResult.error("Async validation failed: ${e.message}")
        }
    }
    
    fun validateDependencies(dependencies: List<String>): DependencyValidationResult {
        val valid = mutableListOf<String>()
        val invalid = mutableListOf<String>()
        
        dependencies.forEach { dep ->
            if (isValidDependencyFormat(dep)) {
                valid.add(dep)
            } else {
                invalid.add(dep)
            }
        }
        
        return DependencyValidationResult(
            isValid = invalid.isEmpty(),
            validDependencies = valid,
            invalidDependencies = invalid
        )
    }
    
    fun isValidDependencyFormat(dependency: String): Boolean {
        val parts = dependency.split(":")
        return parts.size == 3 && parts.all { it.isNotBlank() } && parts[2] !in listOf("+", "latest.release", "[,)")
                || dependency.matches(Regex("^[^:]+:[^:]+:\\[[^\\]]+\\)$")) // Version ranges
                || dependency.matches(Regex("^[^:]+:[^:]+:\\d+\\.\\+$")) // Plus notation
                || dependency.matches(Regex("^[^:]+:[^:]+:latest\\.[a-z]+$")) // Latest versions
    }
    
    fun performSecurityScan(dependencies: List<String>): SecurityScanResult {
        return try {
            securityScanner.scanDependencies(dependencies)
        } catch (e: Exception) {
            SecurityScanResult(emptyList(), SeverityLevel.NONE, "Security scan failed: ${e.message}")
        }
    }
    
    fun validateAgainstAllowedList(dependencies: List<String>, allowedList: Set<String>): AllowedListValidationResult {
        val allowed = mutableListOf<String>()
        val blocked = mutableListOf<String>()
        
        dependencies.forEach { dep ->
            val groupAndArtifact = dep.substringBeforeLast(":")
            if (allowedList.contains(groupAndArtifact)) {
                allowed.add(dep)
            } else {
                blocked.add(dep)
            }
        }
        
        return AllowedListValidationResult(
            isValid = blocked.isEmpty(),
            allowedDependencies = allowed,
            blockedDependencies = blocked
        )
    }
    
    fun validateWrapperConfiguration(wrapperFile: File): WrapperValidationResult {
        val content = wrapperFile.readText()
        val distributionUrl = content.lines()
            .find { it.startsWith("distributionUrl=") }
            ?.substringAfter("=")
            ?.replace("\\", "")
        
        val isSecure = distributionUrl?.startsWith("https://services.gradle.org") == true
        val version = distributionUrl?.let { Regex("gradle-(\\d+\\.\\d+)-").find(it)?.groupValues?.get(1) }
        val issues = if (!isSecure) listOf("Insecure distribution URL") else emptyList()
        
        return WrapperValidationResult(
            isValid = isSecure,
            hasSecureDistributionUrl = isSecure,
            gradleVersion = version,
            securityIssues = issues
        )
    }
    
    fun validateRepositories(buildScript: String): RepositoryValidationResult {
        val httpRepos = Regex("http://[^\"'\\s]+").findAll(buildScript).map { it.value }.toList()
        
        return RepositoryValidationResult(
            isValid = httpRepos.isEmpty(),
            hasSecureRepositories = httpRepos.isEmpty(),
            hasInsecureHttpRepositories = httpRepos.isNotEmpty(),
            insecureRepositories = httpRepos
        )
    }
    
    fun validatePlugins(buildScript: String): PluginValidationResult {
        val pluginRegex = Regex("id\\s*\\(?[\"']([^\"']+)[\"']\\)?")
        val kotlinPluginRegex = Regex("kotlin\\([\"']([^\"']+)[\"']\\)")
        
        val plugins = mutableListOf<String>()
        plugins.addAll(pluginRegex.findAll(buildScript).map { it.groupValues[1] })
        plugins.addAll(kotlinPluginRegex.findAll(buildScript).map { "kotlin.${it.groupValues[1]}" })
        
        return PluginValidationResult(
            isValid = true,
            detectedPlugins = plugins.distinct()
        )
    }
    
    fun validateBuildScript(scriptFile: File): BuildScriptValidationResult {
        if (!scriptFile.exists() || !scriptFile.canRead()) {
            throw ValidationException("Cannot access build script: ${scriptFile.path}")
        }
        
        val syntaxResult = validateSyntax(scriptFile)
        val content = scriptFile.readText()
        val repoResult = validateRepositories(content)
        
        // Extract dependencies for security scan
        val dependencies = extractDependencies(content)
        val securityResult = performSecurityScan(dependencies)
        
        val issues = mutableListOf<String>()
        if (!syntaxResult.isValid) issues.add("Syntax error: ${syntaxResult.errorMessage}")
        if (!repoResult.isValid) issues.add("Insecure repositories found")
        if (!securityResult.isSecure) issues.add("Security vulnerabilities detected")
        
        return BuildScriptValidationResult(
            isValid = syntaxResult.isValid && repoResult.isValid && securityResult.isSecure,
            syntaxValid = syntaxResult.isValid,
            dependenciesValid = true, // Simplified for demo
            securityScanPassed = securityResult.isSecure,
            issues = issues
        )
    }
    
    fun validateProject(projectRoot: File): List<BuildScriptValidationResult> {
        val buildScripts = discoverBuildScripts(projectRoot)
        return buildScripts.map { validateBuildScript(it) }
    }
    
    private fun extractDependencies(buildScript: String): List<String> {
        val implementationRegex = Regex("implementation\\s*\\(?[\"']([^\"']+)[\"']\\)?")
        val testImplementationRegex = Regex("testImplementation\\s*\\(?[\"']([^\"']+)[\"']\\)?")
        
        val dependencies = mutableListOf<String>()
        dependencies.addAll(implementationRegex.findAll(buildScript).map { it.groupValues[1] })
        dependencies.addAll(testImplementationRegex.findAll(buildScript).map { it.groupValues[1] })
        
        return dependencies
    }
}