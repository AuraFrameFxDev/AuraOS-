package dev.aurakai.auraframefx.sandbox.ui.gradle

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.io.File
import java.util.Properties
import java.util.regex.Pattern

/**
 * Test class for validating the sandbox-ui build.gradle.kts configuration
 * Testing Framework: JUnit 4 (based on testImplementation(libs.testJunit))
 * 
 * This test class validates the build configuration, dependencies, and 
 * generated build artifacts for the sandbox-ui module.
 */
class SandboxUiBuildConfigurationTest {
    
    private lateinit var projectDir: File
    private lateinit var buildFile: File
    private lateinit var content: String
    private lateinit var gradleProperties: Properties
    
    @Before
    fun setUp() {
        projectDir = File("sandbox-ui")
        buildFile = File(projectDir, "build.gradle.kts")
        gradleProperties = Properties()
        
        // Load gradle.properties if it exists
        val gradlePropsFile = File("gradle.properties")
        if (gradlePropsFile.exists()) {
            gradlePropsFile.inputStream().use { gradleProperties.load(it) }
        }
        
        content = buildFile.readText()
    }
    
    @After
    fun tearDown() {
        // Clean up any test artifacts
    }
    
    @Test
    fun testBuildFileExists() {
        assertTrue("build.gradle.kts file should exist", buildFile.exists())
        assertTrue("build.gradle.kts should be readable", buildFile.canRead())
        assertTrue("build.gradle.kts should not be empty", buildFile.length() > 0)
    }
    
    @Test
    fun testValidKotlinDslSyntax() {
        // Test for required plugins
        assertTrue("Should contain android library plugin", 
            content.contains("com.android.library"))
        assertTrue("Should contain kotlin android plugin", 
            content.contains("org.jetbrains.kotlin.android"))
        assertTrue("Should contain kotlin compose plugin", 
            content.contains("org.jetbrains.kotlin.plugin.compose"))
        assertTrue("Should contain hilt plugin", 
            content.contains("dagger.hilt.android.plugin"))
        assertTrue("Should contain parcelize plugin", 
            content.contains("kotlin-parcelize"))
        
        // Test that file doesn't use deprecated Groovy syntax
        assertFalse("Should not use deprecated compile syntax", content.contains("compile "))
        assertFalse("Should not use deprecated compile function", content.contains("compile("))
    }
    
    @Test
    fun testAndroidLibraryConfiguration() {
        // Test namespace configuration
        assertTrue("Should have correct namespace", 
            content.contains("namespace = \"dev.aurakai.auraframefx.sandbox.ui\""))
        
        // Test SDK versions
        assertTrue("Should have compileSdk = 36", 
            content.contains("compileSdk = 36"))
        assertTrue("Should have minSdk = 33", 
            content.contains("minSdk = 33"))
        assertTrue("Should have testOptions.targetSdk = 36", 
            content.contains("testOptions.targetSdk = 36"))
        assertTrue("Should have lint.targetSdk = 36", 
            content.contains("lint.targetSdk = 36"))
        
        // Test test runner configuration
        assertTrue("Should have correct test runner", 
            content.contains("testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\""))
        
        // Test consumer proguard files
        assertTrue("Should have consumer proguard files", 
            content.contains("consumerProguardFiles(\"consumer-rules.pro\")"))
    }
    
    @Test
    fun testBuildTypesConfiguration() {
        // Test release build type
        assertTrue("Should have release build type", 
            content.contains("buildTypes") && content.contains("release"))
        assertTrue("Should have minify disabled for release", 
            content.contains("isMinifyEnabled = false"))
        assertTrue("Should have proguard files configured", 
            content.contains("proguardFiles"))
        assertTrue("Should reference default proguard file", 
            content.contains("getDefaultProguardFile(\"proguard-android-optimize.txt\")"))
        assertTrue("Should reference custom proguard rules", 
            content.contains("\"proguard-rules.pro\""))
    }
    
    @Test
    fun testJavaCompatibilityConfiguration() {
        // Test Java version compatibility
        assertTrue("Should have Java 21 source compatibility", 
            content.contains("sourceCompatibility = JavaVersion.VERSION_21"))
        assertTrue("Should have Java 21 target compatibility", 
            content.contains("targetCompatibility = JavaVersion.VERSION_21"))
        
        // Test that both compileOptions and kotlinOptions are present
        assertTrue("Should have compileOptions block", content.contains("compileOptions {"))
        assertTrue("Should have kotlinOptions block", content.contains("kotlinOptions {"))
    }
    
    @Test
    fun testBuildFeaturesConfiguration() {
        // Test build features
        assertTrue("Should have compose enabled", 
            content.contains("compose = true"))
        assertTrue("Should have buildConfig enabled", 
            content.contains("buildConfig = true"))
        assertTrue("Should have buildFeatures block", 
            content.contains("buildFeatures {"))
    }
    
    @Test
    fun testNdkConfiguration() {
        // Test NDK configuration
        assertTrue("Should have NDK configuration block", content.contains("ndk {"))
        assertTrue("Should have arm64-v8a ABI filter", 
            content.contains("arm64-v8a"))
        assertTrue("Should have x86_64 ABI filter", 
            content.contains("x86_64"))
        assertTrue("Should have debug symbol level FULL", 
            content.contains("debugSymbolLevel = \"FULL\""))
        
        // Test that NDK version comes from rootProject.extra
        assertTrue("NDK version should come from rootProject.extra", 
            content.contains("rootProject.extra[\"ndkVersion\"]"))
        
        // Test ABI filters are properly configured
        assertTrue("Should use abiFilters.addAll", 
            content.contains("abiFilters.addAll"))
    }
    
    @Test
    fun testPackagingConfiguration() {
        // Test packaging exclusions
        assertTrue("Should have packaging configuration", content.contains("packaging {"))
        assertTrue("Should have resources exclusions", content.contains("resources {"))
        assertTrue("Should exclude kotlin modules", 
            content.contains("\"META-INF/*.kotlin_module\""))
        assertTrue("Should exclude version files", 
            content.contains("\"META-INF/*.version\""))
        assertTrue("Should exclude proguard files", 
            content.contains("\"META-INF/proguard/*\""))
        assertTrue("Should exclude JNI libraries", 
            content.contains("\"**/libjni*.so\""))
        
        // Test that exclusions use proper syntax
        assertTrue("Should use excludes.addAll", 
            content.contains("excludes.addAll"))
    }
    
    @Test
    fun testComposeConfiguration() {
        // Test Compose compiler version
        assertTrue("Should have composeOptions block", content.contains("composeOptions {"))
        assertTrue("Should have compose compiler version set", 
            content.contains("kotlinCompilerExtensionVersion = \"2.0.0\""))
        
        // Test that compose plugin version is properly configured
        assertTrue("Should have compose plugin with version", 
            content.contains("version libs.versions.kotlin.get()"))
    }
    
    @Test
    fun testCoreDependencies() {
        // Test core project dependency uses api for transitive exposure
        assertTrue("Should have app project dependency", 
            content.contains("api(project(\":app\"))"))
        
        // Test AndroidX dependencies
        assertTrue("Should have AndroidX core dependency", 
            content.contains("implementation(libs.androidxCoreKtx)"))
        assertTrue("Should have lifecycle runtime dependency", 
            content.contains("implementation(libs.androidxLifecycleRuntimeKtx)"))
        assertTrue("Should have activity compose dependency", 
            content.contains("implementation(libs.androidxActivityCompose)"))
    }
    
    @Test
    fun testComposeDependencies() {
        // Test Compose BOM and related dependencies
        assertTrue("Should have compose BOM platform", 
            content.contains("implementation(platform(libs.composeBom))"))
        assertTrue("Should have ui dependency", 
            content.contains("implementation(libs.ui)"))
        assertTrue("Should have ui tooling preview", 
            content.contains("implementation(libs.uiToolingPreview)"))
        assertTrue("Should have material3 dependency", 
            content.contains("implementation(libs.androidxMaterial3)"))
        assertTrue("Should have animation dependency", 
            content.contains("implementation(libs.animation)"))
        assertTrue("Should have foundation dependency", 
            content.contains("implementation(libs.foundation)"))
    }
    
    @Test
    fun testNavigationDependencies() {
        // Test navigation dependencies
        assertTrue("Should have navigation compose dependency", 
            content.contains("implementation(libs.navigationComposeV291)"))
    }
    
    @Test
    fun testHiltDependencies() {
        // Test Hilt dependencies
        assertTrue("Should have hilt android dependency", 
            content.contains("implementation(libs.hiltAndroid)"))
        assertTrue("Should have hilt compiler", 
            content.contains("kapt(libs.hiltCompiler)"))
        assertTrue("Should have hilt navigation compose", 
            content.contains("implementation(libs.hiltNavigationCompose)"))
    }
    
    @Test
    fun testDebugDependencies() {
        // Test debug dependencies
        assertTrue("Should have debug ui tooling", 
            content.contains("debugImplementation(libs.uiTooling)"))
        assertTrue("Should have debug ui test manifest", 
            content.contains("debugImplementation(libs.uiTestManifest)"))
    }
    
    @Test
    fun testTestDependencies() {
        // Test testing dependencies
        assertTrue("Should have JUnit test dependency", 
            content.contains("testImplementation(libs.testJunit)"))
        assertTrue("Should have JUnit android test dependency", 
            content.contains("androidTestImplementation(libs.junitV115)"))
        assertTrue("Should have Espresso core dependency", 
            content.contains("androidTestImplementation(libs.espressoCoreV351)"))
        assertTrue("Should have UI test JUnit4 dependency", 
            content.contains("androidTestImplementation(libs.uiTestJunit4)"))
    }
    
    @Test
    fun testBuildFileStructure() {
        // Test overall structure
        assertTrue("Should have plugins block", content.contains("plugins {"))
        assertTrue("Should have android block", content.contains("android {"))
        assertTrue("Should have dependencies block", content.contains("dependencies {"))
        
        // Test proper closing braces
        val openBraces = content.count { it == '{' }
        val closeBraces = content.count { it == '}' }
        assertEquals("Braces should be balanced", openBraces, closeBraces)
        
        // Test block order
        val pluginsIndex = content.indexOf("plugins {")
        val androidIndex = content.indexOf("android {")
        val dependenciesIndex = content.indexOf("dependencies {")
        
        assertTrue("Plugins should come before android configuration", 
            pluginsIndex < androidIndex)
        assertTrue("Android configuration should come before dependencies", 
            androidIndex < dependenciesIndex)
    }
    
    @Test
    fun testLibraryModuleSpecificConfiguration() {
        // Test library-specific configuration
        assertTrue("Should use com.android.library plugin", 
            content.contains("com.android.library"))
        
        // Library modules should not have applicationId
        assertFalse("Library should not have applicationId", 
            content.contains("applicationId"))
        
        // Should have consumer proguard files
        assertTrue("Should have consumer proguard files", 
            content.contains("consumerProguardFiles"))
        
        // Should use api for transitive dependencies
        assertTrue("Should use api for project dependencies", 
            content.contains("api(project("))
    }
    
    @Test
    fun testVersionCatalogUsage() {
        // Test that version catalog is used properly
        val dependencyLines = content.lines().filter { 
            it.contains("implementation(") || it.contains("testImplementation(") ||
            it.contains("androidTestImplementation(") || it.contains("debugImplementation(") ||
            it.contains("kapt(") || it.contains("api(")
        }
        
        val libsCatalogUsage = dependencyLines.count { it.contains("libs.") }
        val totalDependencies = dependencyLines.size
        
        assertTrue("Should use libs catalog for most dependencies", 
            libsCatalogUsage.toDouble() / totalDependencies > 0.8)
    }
    
    @Test
    fun testNoHardcodedVersions() {
        // Test that versions are not hardcoded in most places
        val versionPattern = Pattern.compile("\"\\d+\\.\\d+\\.\\d+\"")
        val versionMatches = versionPattern.matcher(content)
        var hardcodedVersionCount = 0
        
        while (versionMatches.find()) {
            val match = versionMatches.group()
            // Allow specific hardcoded versions
            if (!match.equals("\"2.0.0\"")) { // Compose compiler version
                hardcodedVersionCount++
            }
        }
        
        assertTrue("Should minimize hardcoded versions", hardcodedVersionCount <= 1)
    }
    
    @Test
    fun testSdkVersionConsistency() {
        // Extract SDK versions
        val compileSdkPattern = Pattern.compile("compileSdk = (\\d+)")
        val minSdkPattern = Pattern.compile("minSdk = (\\d+)")
        val targetSdkPattern = Pattern.compile("targetSdk = (\\d+)")
        
        val compileSdkMatcher = compileSdkPattern.matcher(content)
        val minSdkMatcher = minSdkPattern.matcher(content)
        val targetSdkMatcher = targetSdkPattern.matcher(content)
        
        if (compileSdkMatcher.find() && minSdkMatcher.find() && targetSdkMatcher.find()) {
            val compileSdk = compileSdkMatcher.group(1).toInt()
            val minSdk = minSdkMatcher.group(1).toInt()
            val targetSdk = targetSdkMatcher.group(1).toInt()
            
            assertTrue("compileSdk should be >= minSdk", compileSdk >= minSdk)
            assertTrue("targetSdk should be <= compileSdk", targetSdk <= compileSdk)
            assertTrue("minSdk should be reasonable (>= 21)", minSdk >= 21)
            assertTrue("compileSdk should be reasonable (< 40)", compileSdk < 40)
        }
    }
    
    @Test
    fun testRequiredConfigurationValues() {
        // Test that all required configuration values are present
        val requiredConfigurations = mapOf(
            "namespace" to "dev.aurakai.auraframefx.sandbox.ui",
            "compileSdk" to "36",
            "minSdk" to "33",
            "testInstrumentationRunner" to "androidx.test.runner.AndroidJUnitRunner"
        )
        
        requiredConfigurations.forEach { (key, value) ->
            assertTrue("Should have $key configuration", 
                content.contains(key))
            assertTrue("Should have correct $key value", 
                content.contains(value))
        }
    }
    
    @Test
    fun testComposeSpecificConfiguration() {
        // Test Compose-specific configuration
        assertTrue("Should have compose plugin", 
            content.contains("org.jetbrains.kotlin.plugin.compose"))
        assertTrue("Should have compose build feature enabled", 
            content.contains("compose = true"))
        assertTrue("Should have compose compiler version", 
            content.contains("kotlinCompilerExtensionVersion"))
        
        // Test that compose dependencies are properly configured
        val composeDependencies = listOf("composeBom", "ui", "uiToolingPreview")
        composeDependencies.forEach { dep ->
            assertTrue("Should have $dep dependency", 
                content.contains("libs.$dep"))
        }
    }
    
    @Test
    fun testKaptConfiguration() {
        // Test that kapt is properly configured
        assertTrue("Should have kapt plugin", content.contains("kotlin-kapt"))
        assertTrue("Should have kapt dependencies", content.contains("kapt("))
        
        // Verify annotation processors are configured
        assertTrue("Should have Hilt compiler kapt dependency", 
            content.contains("kapt(libs.hiltCompiler)"))
    }
    
    @Test
    fun testDependencyTypesUsage() {
        // Test proper usage of different dependency types
        assertTrue("Should use implementation for runtime dependencies", 
            content.contains("implementation(libs.androidxCoreKtx)"))
        assertTrue("Should use api for transitive dependencies", 
            content.contains("api(project(\":app\"))"))
        assertTrue("Should use debugImplementation for debug-only dependencies", 
            content.contains("debugImplementation(libs.uiTooling)"))
        assertTrue("Should use testImplementation for unit tests", 
            content.contains("testImplementation(libs.testJunit)"))
        assertTrue("Should use androidTestImplementation for instrumentation tests", 
            content.contains("androidTestImplementation(libs.junitV115)"))
    }
    
    @Test
    fun testBuildScriptComments() {
        // Test that important sections have comments
        assertTrue("Should have comment about core project dependency", 
            content.contains("// Core project dependency"))
        assertTrue("Should have comment about using api", 
            content.contains("use api to expose dependencies"))
        assertTrue("Should have comment about NDK configuration", 
            content.contains("// Configure NDK if needed"))
    }
    
    @Test
    fun testEdgeCases() {
        // Test edge cases and potential issues
        
        // Test that kotlin version is retrieved from libs
        assertTrue("Should get kotlin version from libs", 
            content.contains("libs.versions.kotlin.get()"))
        
        // Test that deprecated targetSdk comment is present
        assertTrue("Should have comment about deprecated targetSdk", 
            content.contains("targetSdk is deprecated in library modules"))
        
        // Test that packaging resources are properly configured
        assertTrue("Should have resources exclusions in packaging", 
            content.contains("resources {") && content.contains("excludes"))
        
        // Test that buildConfig is enabled for this module
        assertTrue("Should have buildConfig enabled", 
            content.contains("buildConfig = true"))
    }
}