package dev.aurakai.auraframefx.sandbox.ui

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Unit tests for build script validation
 * Testing Framework: JUnit 4
 * 
 * These tests validate the build.gradle.kts configuration for the sandbox-ui module
 * to ensure proper Android library setup, dependency management, and build configuration.
 */
class BuildScriptValidationTest {

    private lateinit var buildGradleFile: File
    private lateinit var buildGradleContent: String
    
    @Before
    fun setUp() {
        buildGradleFile = File("sandbox-ui/build.gradle.kts")
        buildGradleContent = if (buildGradleFile.exists()) {
            buildGradleFile.readText()
        } else {
            ""
        }
    }

    @After
    fun tearDown() {
        // Clean up any temporary files created during tests
    }

    @Test
    fun `test build gradle file exists`() {
        assertTrue("build.gradle.kts file should exist", buildGradleFile.exists())
        assertTrue("build.gradle.kts should not be empty", buildGradleContent.isNotEmpty())
    }

    @Test
    fun `test required plugins are applied`() {
        val requiredPlugins = listOf(
            "com.android.library",
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.plugin.compose",
            "kotlin-kapt",
            "dagger.hilt.android.plugin",
            "kotlin-parcelize"
        )
        
        requiredPlugins.forEach { plugin ->
            assertTrue(
                "Plugin $plugin should be applied",
                buildGradleContent.contains("id(\"$plugin\")")
            )
        }
    }

    @Test
    fun `test android namespace is correctly set`() {
        assertTrue(
            "Namespace should be set to dev.aurakai.auraframefx.sandbox.ui",
            buildGradleContent.contains("namespace = \"dev.aurakai.auraframefx.sandbox.ui\"")
        )
    }

    @Test
    fun `test compile and target SDK versions are properly configured`() {
        assertTrue(
            "Compile SDK should be set to 36",
            buildGradleContent.contains("compileSdk = 36")
        )
        
        assertTrue(
            "Test options target SDK should be set to 36",
            buildGradleContent.contains("testOptions.targetSdk = 36")
        )
        
        assertTrue(
            "Lint target SDK should be set to 36",
            buildGradleContent.contains("lint.targetSdk = 36")
        )
    }

    @Test
    fun `test minimum SDK version is appropriate`() {
        assertTrue(
            "Minimum SDK should be set to 33",
            buildGradleContent.contains("minSdk = 33")
        )
    }

    @Test
    fun `test Java version compatibility`() {
        assertTrue(
            "Source compatibility should be Java 21",
            buildGradleContent.contains("sourceCompatibility = JavaVersion.VERSION_21")
        )
        
        assertTrue(
            "Target compatibility should be Java 21",
            buildGradleContent.contains("targetCompatibility = JavaVersion.VERSION_21")
        )
    }

    @Test
    fun `test NDK configuration is present`() {
        assertTrue(
            "NDK configuration should be present",
            buildGradleContent.contains("ndk {")
        )
        
        assertTrue(
            "NDK should include arm64-v8a ABI",
            buildGradleContent.contains("arm64-v8a")
        )
        
        assertTrue(
            "NDK should include x86_64 ABI",
            buildGradleContent.contains("x86_64")
        )
        
        assertTrue(
            "NDK debug symbol level should be FULL",
            buildGradleContent.contains("debugSymbolLevel = \"FULL\"")
        )
    }

    @Test
    fun `test build features are enabled`() {
        assertTrue(
            "Compose should be enabled",
            buildGradleContent.contains("compose = true")
        )
        
        assertTrue(
            "Build config should be enabled",
            buildGradleContent.contains("buildConfig = true")
        )
    }

    @Test
    fun `test compose compiler extension version`() {
        assertTrue(
            "Compose compiler extension version should be 2.0.0",
            buildGradleContent.contains("kotlinCompilerExtensionVersion = \"2.0.0\"")
        )
    }

    @Test
    fun `test essential dependencies are present`() {
        val requiredDependencies = listOf(
            "project(\":app\")",
            "libs.androidxCoreKtx",
            "libs.androidxLifecycleRuntimeKtx",
            "libs.androidxActivityCompose",
            "libs.composeBom",
            "libs.ui",
            "libs.uiToolingPreview",
            "libs.androidxMaterial3",
            "libs.hiltAndroid",
            "libs.hiltCompiler",
            "libs.navigationComposeV291"
        )
        
        requiredDependencies.forEach { dependency ->
            assertTrue(
                "Dependency $dependency should be present",
                buildGradleContent.contains(dependency)
            )
        }
    }

    @Test
    fun `test test dependencies are configured`() {
        val testDependencies = listOf(
            "testImplementation(libs.testJunit)",
            "androidTestImplementation(libs.junitV115)",
            "androidTestImplementation(libs.espressoCoreV351)",
            "androidTestImplementation(libs.uiTestJunit4)"
        )
        
        testDependencies.forEach { dependency ->
            assertTrue(
                "Test dependency $dependency should be present",
                buildGradleContent.contains(dependency)
            )
        }
    }

    @Test
    fun `test debug dependencies are configured`() {
        val debugDependencies = listOf(
            "debugImplementation(libs.uiTooling)",
            "debugImplementation(libs.uiTestManifest)"
        )
        
        debugDependencies.forEach { dependency ->
            assertTrue(
                "Debug dependency $dependency should be present",
                buildGradleContent.contains(dependency)
            )
        }
    }

    @Test
    fun `test packaging exclusions are configured`() {
        val excludedResources = listOf(
            "META-INF/*.kotlin_module",
            "META-INF/*.version",
            "META-INF/proguard/*",
            "**/libjni*.so"
        )
        
        excludedResources.forEach { resource ->
            assertTrue(
                "Resource $resource should be excluded",
                buildGradleContent.contains("\"$resource\"")
            )
        }
    }

    @Test
    fun `test proguard configuration`() {
        assertTrue(
            "Proguard should be disabled in release builds",
            buildGradleContent.contains("isMinifyEnabled = false")
        )
        
        assertTrue(
            "Default proguard file should be configured",
            buildGradleContent.contains("proguard-android-optimize.txt")
        )
        
        assertTrue(
            "Custom proguard rules should be configured",
            buildGradleContent.contains("proguard-rules.pro")
        )
    }

    @Test
    fun `test consumer proguard rules are configured`() {
        assertTrue(
            "Consumer proguard rules should be configured",
            buildGradleContent.contains("consumerProguardFiles(\"consumer-rules.pro\")")
        )
    }

    @Test
    fun `test instrumentation runner is configured`() {
        assertTrue(
            "Android JUnit test runner should be configured",
            buildGradleContent.contains("testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\"")
        )
    }

    @Test
    fun `test api dependency usage for main project`() {
        assertTrue(
            "Main project should use api() to expose dependencies",
            buildGradleContent.contains("api(project(\":app\"))")
        )
    }

    @Test
    fun `test kapt configuration for hilt`() {
        assertTrue(
            "Hilt compiler should use kapt",
            buildGradleContent.contains("kapt(libs.hiltCompiler)")
        )
    }

    @Test
    fun `test compose BOM platform dependency`() {
        assertTrue(
            "Compose BOM should use platform() function",
            buildGradleContent.contains("implementation(platform(libs.composeBom))")
        )
    }

    @Test
    fun `test build script structure validation`() {
        // Test that the build script has proper structure
        assertTrue("Should have plugins block", buildGradleContent.contains("plugins {"))
        assertTrue("Should have android block", buildGradleContent.contains("android {"))
        assertTrue("Should have dependencies block", buildGradleContent.contains("dependencies {"))
        assertTrue("Should have defaultConfig block", buildGradleContent.contains("defaultConfig {"))
        assertTrue("Should have buildTypes block", buildGradleContent.contains("buildTypes {"))
        assertTrue("Should have compileOptions block", buildGradleContent.contains("compileOptions {"))
        assertTrue("Should have kotlinOptions block", buildGradleContent.contains("kotlinOptions {"))
        assertTrue("Should have buildFeatures block", buildGradleContent.contains("buildFeatures {"))
        assertTrue("Should have packaging block", buildGradleContent.contains("packaging {"))
        assertTrue("Should have composeOptions block", buildGradleContent.contains("composeOptions {"))
    }

    @Test
    fun `test version catalog usage`() {
        // Test that the build script uses version catalog (libs.versions.toml)
        val versionCatalogUsages = listOf(
            "libs.versions.kotlin.get()",
            "libs.androidxCoreKtx",
            "libs.composeBom",
            "libs.testJunit"
        )
        
        versionCatalogUsages.forEach { usage ->
            assertTrue(
                "Version catalog usage $usage should be present",
                buildGradleContent.contains(usage)
            )
        }
    }

    @Test
    fun `test no hardcoded versions in dependencies`() {
        // Ensure no hardcoded versions are used (except for compose compiler extension)
        val lines = buildGradleContent.lines()
        val dependencyLines = lines.filter { 
            it.trim().startsWith("implementation(") || 
            it.trim().startsWith("testImplementation(") ||
            it.trim().startsWith("androidTestImplementation(") ||
            it.trim().startsWith("debugImplementation(") ||
            it.trim().startsWith("api(") ||
            it.trim().startsWith("kapt(")
        }
        
        dependencyLines.forEach { line ->
            // Allow version in compose compiler extension and project dependencies
            if (!line.contains("kotlinCompilerExtensionVersion") && 
                !line.contains("project(") && 
                !line.contains("platform(")) {
                assertFalse(
                    "Dependency line should not contain hardcoded version: $line",
                    line.contains("\"") && line.contains(":") && 
                    line.matches(Regex(".*\".*:.*:.*\".*"))
                )
            }
        }
    }

    @Test
    fun `test error handling for missing build file`() {
        val nonExistentFile = File("sandbox-ui/non-existent-build.gradle.kts")
        assertFalse("Non-existent file should not exist", nonExistentFile.exists())
    }

    @Test
    fun `test build script syntax validation`() {
        // Basic syntax validation - ensure proper bracket matching
        val openBrackets = buildGradleContent.count { it == '{' }
        val closeBrackets = buildGradleContent.count { it == '}' }
        assertEquals("Opening and closing brackets should match", openBrackets, closeBrackets)
        
        val openParens = buildGradleContent.count { it == '(' }
        val closeParens = buildGradleContent.count { it == ')' }
        assertEquals("Opening and closing parentheses should match", openParens, closeParens)
    }

    @Test
    fun `test dependency configuration types`() {
        // Ensure proper dependency configuration types are used
        assertTrue("Should use implementation dependencies", buildGradleContent.contains("implementation("))
        assertTrue("Should use api dependencies", buildGradleContent.contains("api("))
        assertTrue("Should use kapt dependencies", buildGradleContent.contains("kapt("))
        assertTrue("Should use testImplementation dependencies", buildGradleContent.contains("testImplementation("))
        assertTrue("Should use androidTestImplementation dependencies", buildGradleContent.contains("androidTestImplementation("))
        assertTrue("Should use debugImplementation dependencies", buildGradleContent.contains("debugImplementation("))
    }

    @Test
    fun `test gradle kotlin dsl usage`() {
        // Ensure proper Kotlin DSL syntax is used
        assertTrue("Should use Kotlin DSL assignment syntax", buildGradleContent.contains(" = "))
        assertTrue("Should use Kotlin DSL property access", buildGradleContent.contains("."))
    }

    @Test
    fun `test ndk version configuration`() {
        assertTrue(
            "NDK version should be configured from rootProject.extra",
            buildGradleContent.contains("version = rootProject.extra[\"ndkVersion\"] as String")
        )
    }

    @Test
    fun `test compose animation and foundation dependencies`() {
        assertTrue(
            "Animation library should be included",
            buildGradleContent.contains("libs.animation")
        )
        
        assertTrue(
            "Foundation library should be included",
            buildGradleContent.contains("libs.foundation")
        )
    }

    @Test
    fun `test hilt navigation compose dependency`() {
        assertTrue(
            "Hilt navigation compose should be included",
            buildGradleContent.contains("libs.hiltNavigationCompose")
        )
    }

    @Test
    fun `test kotlin options block present`() {
        assertTrue(
            "Kotlin options block should be present",
            buildGradleContent.contains("kotlinOptions {")
        )
    }

    @Test
    fun `test packaging resources exclusions structure`() {
        assertTrue(
            "Packaging resources should have excludes",
            buildGradleContent.contains("excludes.addAll(")
        )
        
        assertTrue(
            "Packaging should use listOf for excludes",
            buildGradleContent.contains("listOf(")
        )
    }

    @Test
    fun `test build types release configuration`() {
        assertTrue(
            "Should have release build type",
            buildGradleContent.contains("release {")
        )
        
        assertTrue(
            "Should have proguardFiles configuration",
            buildGradleContent.contains("proguardFiles(")
        )
    }

    @Test
    fun `test compose kotlin compiler version reference`() {
        assertTrue(
            "Should use libs.versions.kotlin.get() for compose plugin",
            buildGradleContent.contains("libs.versions.kotlin.get()")
        )
    }

    @Test
    fun `test proper comment formatting`() {
        assertTrue(
            "Should have proper comment for main project dependency",
            buildGradleContent.contains("// Core project dependency")
        )
        
        assertTrue(
            "Should have proper comment for AndroidX dependencies",
            buildGradleContent.contains("// AndroidX Core")
        )
    }

    @Test
    fun `test critical security configurations`() {
        // Test that sensitive configurations are properly set
        assertTrue(
            "Consumer proguard files should be configured for library",
            buildGradleContent.contains("consumerProguardFiles")
        )
        
        assertFalse(
            "Should not have any hardcoded secrets or keys",
            buildGradleContent.contains("SECRET") || 
            buildGradleContent.contains("API_KEY") || 
            buildGradleContent.contains("PASSWORD")
        )
    }

    @Test
    fun `test build optimization settings`() {
        assertTrue(
            "Should use getDefaultProguardFile for optimization",
            buildGradleContent.contains("getDefaultProguardFile(\"proguard-android-optimize.txt\")")
        )
    }

    @Test
    fun `test module type validation`() {
        assertTrue(
            "Should be configured as Android library",
            buildGradleContent.contains("com.android.library")
        )
        
        assertFalse(
            "Should not be configured as Android application",
            buildGradleContent.contains("com.android.application")
        )
    }
}