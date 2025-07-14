package dev.aurakai.auraframefx.sandbox.ui.gradle

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.util.regex.Pattern

/**
 * Validation tests for sandbox-ui build script best practices
 * Testing Framework: JUnit 4
 * 
 * These tests validate that the build script follows Kotlin DSL best practices
 * and Android library module configuration standards.
 */
class SandboxUiBuildValidationTest {
    
    private lateinit var buildFile: File
    private lateinit var content: String
    
    @Before
    fun setUp() {
        buildFile = File("sandbox-ui/build.gradle.kts")
        content = buildFile.readText()
    }
    
    @Test
    fun testKotlinDslBestPractices() {
        // Test proper Kotlin DSL syntax
        assertFalse("Should not use Groovy syntax", content.contains("compile "))
        assertFalse("Should not use deprecated apply plugin", content.contains("apply plugin:"))
        
        // Test plugin block syntax
        assertTrue("Should use plugins block", content.contains("plugins {"))
        
        // Count plugin declarations
        val pluginPattern = Pattern.compile("id\\(\"[^\"]+\"\\)")
        val pluginMatches = pluginPattern.matcher(content)
        var pluginCount = 0
        while (pluginMatches.find()) {
            pluginCount++
        }
        assertTrue("Should have at least 5 plugins", pluginCount >= 5)
    }
    
    @Test
    fun testConfigurationBlockOrder() {
        // Test that configuration blocks are in logical order
        val blockIndices = mapOf(
            "plugins" to content.indexOf("plugins {"),
            "android" to content.indexOf("android {"),
            "dependencies" to content.indexOf("dependencies {")
        )
        
        assertTrue("Plugins should come first", 
            blockIndices["plugins"]!! < blockIndices["android"]!!)
        assertTrue("Android should come before dependencies", 
            blockIndices["android"]!! < blockIndices["dependencies"]!!)
    }
    
    @Test
    fun testStringLiteralsAndQuoting() {
        // Test proper string quoting
        val lines = content.lines()
        
        // Check for consistent quoting style
        val quotedStrings = lines.flatMap { line ->
            val pattern = Pattern.compile("\"([^\"]+)\"")
            val matcher = pattern.matcher(line)
            val matches = mutableListOf<String>()
            while (matcher.find()) {
                matches.add(matcher.group(1))
            }
            matches
        }
        
        // Ensure no empty quoted strings
        quotedStrings.forEach { quotedString ->
            assertTrue("Quoted string should not be empty", quotedString.isNotEmpty())
        }
    }
    
    @Test
    fun testVersionManagement() {
        // Test version management practices
        val versionPattern = Pattern.compile("\"\\d+\\.\\d+\\.\\d+\"")
        val versionMatches = versionPattern.matcher(content)
        val hardcodedVersions = mutableListOf<String>()
        
        while (versionMatches.find()) {
            hardcodedVersions.add(versionMatches.group())
        }
        
        // Allow some specific hardcoded versions
        val allowedVersions = setOf("\"2.0.0\"") // Compose compiler version
        val unexpectedVersions = hardcodedVersions.filterNot { it in allowedVersions }
        
        assertTrue("Should minimize hardcoded versions, found: $unexpectedVersions", 
            unexpectedVersions.size <= 1)
    }
    
    @Test
    fun testDependencyManagementPractices() {
        // Test dependency management best practices
        val dependencyTypes = listOf("implementation", "api", "testImplementation", 
            "androidTestImplementation", "debugImplementation", "kapt")
        
        dependencyTypes.forEach { type ->
            val typePattern = Pattern.compile("$type\\([^)]+\\)")
            val matches = typePattern.matcher(content)
            
            while (matches.find()) {
                val match = matches.group()
                assertTrue("$type should use proper syntax: $match", 
                    match.contains("libs.") || match.contains("project(") || 
                    match.contains("platform("))
            }
        }
        
        // Test that api is used appropriately
        val apiUsages = content.lines().filter { it.contains("api(") }
        assertTrue("Should use api for transitive dependencies", 
            apiUsages.any { it.contains("project(") })
    }
    
    @Test
    fun testConfigurationCompleteness() {
        // Test that all required configuration blocks exist
        val requiredBlocks = listOf(
            "plugins {", "android {", "dependencies {", "defaultConfig {",
            "buildTypes {", "compileOptions {", "buildFeatures {", "packaging {",
            "composeOptions {", "ndk {"
        )
        
        requiredBlocks.forEach { block ->
            assertTrue("Should have $block configuration", content.contains(block))
        }
    }
    
    @Test
    fun testAndroidLibrarySpecificValidation() {
        // Test Android library specific configuration
        assertTrue("Should be configured as library", 
            content.contains("com.android.library"))
        
        // Test namespace format
        val namespacePattern = Pattern.compile("namespace = \"([^\"]+)\"")
        val namespaceMatcher = namespacePattern.matcher(content)
        if (namespaceMatcher.find()) {
            val namespace = namespaceMatcher.group(1)
            assertTrue("Namespace should follow package naming", 
                namespace.matches(Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$")))
        }
        
        // Test that library doesn't have application-specific configs
        assertFalse("Library should not have applicationId", 
            content.contains("applicationId"))
        assertFalse("Library should not have signingConfigs", 
            content.contains("signingConfigs"))
    }
    
    @Test
    fun testSdkVersionValidation() {
        // Test SDK version constraints
        val sdkVersionPattern = Pattern.compile("(compileSdk|minSdk|targetSdk) = (\\d+)")
        val matcher = sdkVersionPattern.matcher(content)
        
        while (matcher.find()) {
            val versionType = matcher.group(1)
            val version = matcher.group(2).toInt()
            
            when (versionType) {
                "compileSdk" -> {
                    assertTrue("compileSdk should be reasonable", version in 30..40)
                }
                "minSdk" -> {
                    assertTrue("minSdk should be reasonable", version in 21..35)
                }
                "targetSdk" -> {
                    assertTrue("targetSdk should be reasonable", version in 30..40)
                }
            }
        }
    }
    
    @Test
    fun testComposeConfigurationValidation() {
        // Test Compose configuration is consistent
        assertTrue("Should have compose plugin", 
            content.contains("org.jetbrains.kotlin.plugin.compose"))
        assertTrue("Should have compose build feature", 
            content.contains("compose = true"))
        assertTrue("Should have compose compiler extension version", 
            content.contains("kotlinCompilerExtensionVersion"))
        
        // Test Compose dependencies are properly configured
        assertTrue("Should have compose BOM", 
            content.contains("platform(libs.composeBom)"))
        assertTrue("Should have compose UI dependencies", 
            content.contains("libs.ui"))
    }
    
    @Test
    fun testHiltConfigurationValidation() {
        // Test Hilt configuration is complete
        assertTrue("Should have hilt plugin", 
            content.contains("dagger.hilt.android.plugin"))
        assertTrue("Should have kapt plugin for Hilt", 
            content.contains("kotlin-kapt"))
        
        // Test Hilt dependencies
        assertTrue("Should have hilt runtime", 
            content.contains("libs.hiltAndroid"))
        assertTrue("Should have hilt compiler", 
            content.contains("kapt(libs.hiltCompiler)"))
    }
    
    @Test
    fun testBuildOptimizationValidation() {
        // Test build optimization configurations
        assertTrue("Should have Java version configured", 
            content.contains("JavaVersion.VERSION_21"))
        
        // Test packaging exclusions
        assertTrue("Should exclude unnecessary files", 
            content.contains("excludes.addAll"))
        
        // Test that common exclusions are present
        val commonExclusions = listOf("META-INF/*.kotlin_module", "META-INF/*.version")
        commonExclusions.forEach { exclusion ->
            assertTrue("Should exclude $exclusion", content.contains(exclusion))
        }
    }
    
    @Test
    fun testTestConfigurationValidation() {
        // Test testing configuration
        assertTrue("Should have test instrumentation runner", 
            content.contains("testInstrumentationRunner"))
        
        // Test that all test dependency types are used appropriately
        assertTrue("Should have unit test dependencies", 
            content.contains("testImplementation"))
        assertTrue("Should have instrumentation test dependencies", 
            content.contains("androidTestImplementation"))
        assertTrue("Should have debug test dependencies", 
            content.contains("debugImplementation"))
        
        // Test specific test libraries
        val testLibraries = listOf("testJunit", "junitV115", "espressoCoreV351")
        testLibraries.forEach { lib ->
            assertTrue("Should have $lib test library", 
                content.contains("libs.$lib"))
        }
    }
    
    @Test
    fun testCommentAndDocumentationQuality() {
        // Test that important configurations have comments
        assertTrue("Should have comment about core dependency", 
            content.contains("// Core project dependency"))
        assertTrue("Should have comment about NDK", 
            content.contains("// Configure NDK"))
        assertTrue("Should have comment about deprecated targetSdk", 
            content.contains("// targetSdk is deprecated"))
        
        // Test that comments are helpful
        val commentLines = content.lines().filter { it.trim().startsWith("//") }
        assertTrue("Should have meaningful comments", commentLines.isNotEmpty())
    }
    
    @Test
    fun testErrorPronePatterns() {
        // Test for common error-prone patterns
        
        // Test for potential circular dependencies
        assertFalse("Should not have circular project dependencies", 
            content.contains("project(\":sandbox-ui\")"))
        
        // Test for duplicate dependencies
        val dependencyDeclarations = content.lines().filter { 
            it.contains("implementation(libs.") || it.contains("testImplementation(libs.")
        }
        val uniqueDependencies = dependencyDeclarations.toSet()
        assertEquals("Should not have duplicate dependencies", 
            dependencyDeclarations.size, uniqueDependencies.size)
        
        // Test for missing required plugins
        val requiredPlugins = listOf("kotlin-kapt", "dagger.hilt.android.plugin")
        requiredPlugins.forEach { plugin ->
            assertTrue("Should have required plugin $plugin", 
                content.contains(plugin))
        }
    }
}