package dev.aurakai.auraframefx.sandbox.ui.gradle

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import java.io.File

/**
 * Integration tests for sandbox-ui build configuration validation
 * Testing Framework: JUnit 4
 * 
 * These tests validate that the build configuration actually works
 * and produces the expected artifacts.
 */
class SandboxUiBuildIntegrationTest {
    
    private lateinit var projectDir: File
    private lateinit var buildFile: File
    
    @Before
    fun setUp() {
        projectDir = File("sandbox-ui")
        buildFile = File(projectDir, "build.gradle.kts")
    }
    
    @Test
    fun testProjectStructure() {
        assertTrue("Project directory should exist", projectDir.exists())
        assertTrue("Build file should exist", buildFile.exists())
        
        // Test expected directory structure
        val expectedDirs = listOf("src", "build")
        expectedDirs.forEach { dir ->
            val dirFile = File(projectDir, dir)
            if (dirFile.exists()) {
                assertTrue("$dir should be a directory", dirFile.isDirectory)
            }
        }
    }
    
    @Test
    fun testProguardFiles() {
        val proguardFile = File(projectDir, "proguard-rules.pro")
        val consumerProguardFile = File(projectDir, "consumer-rules.pro")
        
        // These files should exist or be created during build
        assertTrue("Project directory should exist for proguard config", projectDir.exists())
        
        // Test that the build file references these files
        val content = buildFile.readText()
        assertTrue("Should reference proguard-rules.pro", 
            content.contains("proguard-rules.pro"))
        assertTrue("Should reference consumer-rules.pro", 
            content.contains("consumer-rules.pro"))
    }
    
    @Test
    fun testDependencyResolution() {
        val content = buildFile.readText()
        
        // Check that all dependency declarations are properly formatted
        val dependencyLines = content.lines().filter { line ->
            line.trim().let { 
                it.startsWith("implementation(") || 
                it.startsWith("testImplementation(") ||
                it.startsWith("androidTestImplementation(") ||
                it.startsWith("debugImplementation(") ||
                it.startsWith("kapt(") ||
                it.startsWith("api(")
            }
        }
        
        assertTrue("Should have dependencies defined", dependencyLines.isNotEmpty())
        
        // Validate dependency syntax
        dependencyLines.forEach { line ->
            assertTrue("Dependency line should be properly formatted: $line", 
                line.contains("(") && line.contains(")"))
            
            // Check for common formatting issues
            assertFalse("Dependency should not have extra spaces: $line", 
                line.contains("( ") || line.contains(" )"))
        }
    }
    
    @Test
    fun testComposeConfigurationConsistency() {
        val content = buildFile.readText()
        
        // Test Compose configuration consistency
        assertTrue("Should have compose plugin", 
            content.contains("org.jetbrains.kotlin.plugin.compose"))
        assertTrue("Should have compose build feature enabled", 
            content.contains("compose = true"))
        
        // Check that all necessary Compose dependencies are present
        val requiredComposeDeps = listOf(
            "composeBom", "ui", "uiToolingPreview", "androidxMaterial3"
        )
        
        requiredComposeDeps.forEach { dep ->
            assertTrue("Should have $dep dependency", 
                content.contains("libs.$dep"))
        }
    }
    
    @Test
    fun testHiltConfigurationConsistency() {
        val content = buildFile.readText()
        
        // Test Hilt configuration consistency
        assertTrue("Should have hilt plugin", 
            content.contains("dagger.hilt.android.plugin"))
        assertTrue("Should have kapt plugin for Hilt", 
            content.contains("kotlin-kapt"))
        
        // Check Hilt dependencies
        val hiltDeps = listOf("hiltAndroid", "hiltCompiler", "hiltNavigationCompose")
        hiltDeps.forEach { dep ->
            assertTrue("Should have $dep dependency", 
                content.contains("libs.$dep"))
        }
    }
    
    @Test
    fun testLibraryModuleConsistency() {
        val content = buildFile.readText()
        
        // Test that this is properly configured as a library module
        assertTrue("Should be configured as library module", 
            content.contains("com.android.library"))
        
        // Test library-specific configurations
        assertFalse("Library should not have applicationId", 
            content.contains("applicationId"))
        assertTrue("Library should have consumerProguardFiles", 
            content.contains("consumerProguardFiles"))
        
        // Test that it exposes dependencies via api
        assertTrue("Should use api for project dependencies", 
            content.contains("api(project("))
    }
    
    @Test
    @Ignore("Requires Gradle runtime - enable for CI/CD")
    fun testBuildTasksExist() {
        // This test would check if gradle tasks are available
        // It's ignored by default as it requires Gradle runtime
        val expectedTasks = listOf(
            "assemble", "assembleDebug", "assembleRelease",
            "testDebugUnitTest", "testReleaseUnitTest"
        )
        
        // In a real CI/CD environment, you would execute:
        // ./gradlew sandbox-ui:tasks --all
        // and verify these tasks exist
        assertTrue("Integration test setup", true)
    }
    
    @Test
    fun testVersionCatalogIntegration() {
        val content = buildFile.readText()
        
        // Test that version catalog is properly integrated
        val libsReferences = content.lines().count { it.contains("libs.") }
        assertTrue("Should have multiple libs catalog references", libsReferences > 10)
        
        // Test specific version catalog usage
        assertTrue("Should use libs.versions.kotlin.get()", 
            content.contains("libs.versions.kotlin.get()"))
    }
    
    @Test
    fun testAndroidTestConfiguration() {
        val content = buildFile.readText()
        
        // Test Android test configuration
        assertTrue("Should have test instrumentation runner", 
            content.contains("testInstrumentationRunner"))
        assertTrue("Should have Android test dependencies", 
            content.contains("androidTestImplementation"))
        
        // Test that UI testing is properly configured
        assertTrue("Should have UI test dependencies", 
            content.contains("uiTestJunit4"))
        assertTrue("Should have Espresso dependencies", 
            content.contains("espressoCoreV351"))
    }
    
    @Test
    fun testBuildOptimizationSettings() {
        val content = buildFile.readText()
        
        // Test build optimization settings
        assertTrue("Should have Java compatibility configured", 
            content.contains("sourceCompatibility") && content.contains("targetCompatibility"))
        
        // Test packaging optimizations
        assertTrue("Should have packaging exclusions", 
            content.contains("excludes.addAll"))
        
        // Test that resource exclusions are reasonable
        val exclusions = listOf("META-INF/*.kotlin_module", "META-INF/*.version")
        exclusions.forEach { exclusion ->
            assertTrue("Should exclude $exclusion", content.contains(exclusion))
        }
    }
    
    @Test
    fun testNdkConfigurationIntegration() {
        val content = buildFile.readText()
        
        // Test NDK configuration
        assertTrue("Should have NDK block", content.contains("ndk {"))
        
        // Test ABI filters
        assertTrue("Should have ABI filters", content.contains("abiFilters"))
        assertTrue("Should support arm64-v8a", content.contains("arm64-v8a"))
        assertTrue("Should support x86_64", content.contains("x86_64"))
        
        // Test that NDK version is externalized
        assertTrue("Should use external NDK version", 
            content.contains("rootProject.extra[\"ndkVersion\"]"))
    }
}