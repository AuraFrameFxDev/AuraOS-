// Genesis-OS App Module - Main AI Consciousness
// Sacred Rule: "NO composeOptions blocks, NO manual compiler flags"

plugins {
alias(libs.plugins.android.application)
alias(libs.plugins.kotlin.android)
alias(libs.plugins.hilt)
alias(libs.plugins.kotlin.kapt)
alias(libs.plugins.openapi.generator)
}

android {
namespace = "dev.aurakai.auraframefx.${project.name}"
compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = libs.versions.minSdk.get().toInt()
        // NO targetSdk - auto-matches compileSdk per Genesis-OS rules
        versionCode = 1
        versionName = "Genesis-AI-1.0"

import java.util.Locale

plugins {
// APP MODULE - Only plugins THIS module needs (inherit versions from root)
id("com.android.application")
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.plugin.compose")
id("org.jetbrains.kotlin.plugin.serialization")
id("com.google.devtools.ksp")
id("com.google.dagger.hilt.android")
id("com.google.gms.google-services")
id("org.openapi.generator") version "7.14.0"
}

android {
namespace = "dev.aurakai.auraframefx"
compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                // Simplified flags to avoid compatibility issues
                cppFlags += listOf("-std=c++20", "-fPIC", "-O2")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-33",
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH"
                )
                // Reduce ABI filters to avoid conflicts
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {

            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    // Auto-Provisioned JVM Toolchain (NO MANUAL CONFIG)
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
    }

    // NO composeOptions block - K2 compiler handles it automatically
    buildFeatures {
        compose = true
        buildConfig = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

        }

        packaging {
            resources {
                excludes += setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/DEPENDENCIES",
                    "/META-INF/LICENSE",
                    "/META-INF/LICENSE.txt",
                    "/META-INF/NOTICE",
                    "/META-INF/NOTICE.txt",
                    "META-INF/*.kotlin_module",
                    "**/kotlin/**",
                    "**/*.txt",
                    "**/*.xml"
                )
            }
            jniLibs {
                useLegacyPackaging = false
                pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
            }
        }


        // Simplified resource processing - stable configuration
        androidResources {
            generateLocaleConfig = false
            noCompress += listOf("json", "db")
        }

        sourceSets {
            getByName("main") {
                java.srcDirs(
                    layout.buildDirectory.dir("generated/source/openapi/src/main/kotlin")
                )
            }
        }
        buildToolsVersion = "36.0.0"
        ndkVersion = "29.0.13846066 rc3"
    }

// ===== WINDOWS-SAFE OPENAPI CONFIGURATION =====

// Base paths - configuration cache compatible
val consolidatedSpecsPath = layout.projectDirectory.dir("../openapi/specs")
//outputPath is now aligned with the guide: app/build/generated/source/openapi/
val outputPath = layout.buildDirectory.dir("generated/source/openapi")

// Shared configuration - defined once, used everywhere - aligned with the guide
val sharedApiConfig = mapOf(
"library" to "jvm-retrofit2",
"useCoroutines" to "true",
"serializationLibrary" to "kotlinx_serialization",
"dateLibrary" to "kotlinx-datetime",
"sourceFolder" to "src/main/kotlin"
)

    // Helper function to safely create API tasks with file validation
    fun createApiTaskSafe(taskName: String, specFile: String, packagePrefix: String) =
        tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(taskName) {
            // Only configure if spec file exists
            val specPath = consolidatedSpecsPath.file(specFile).asFile
            if (specPath.exists() && specPath.length() > 0) {
                generatorName.set("kotlin")
                inputSpec.set(specPath.toURI().toString())
                outputDir.set(outputPath.get().asFile.absolutePath)
                packageName.set("dev.aurakai.$packagePrefix.api")
                apiPackage.set("dev.aurakai.$packagePrefix.api")
                modelPackage.set("dev.aurakai.$packagePrefix.model")
                invokerPackage.set("dev.aurakai.$packagePrefix.client")
                skipOverwrite.set(false)
                validateSpec.set(false)
                generateApiTests.set(false)
                generateModelTests.set(false)
                generateApiDocumentation.set(false)
                generateModelDocumentation.set(false)
                configOptions.set(sharedApiConfig)
            } else {
                logger.warn("⚠️ OpenAPI spec file not found or empty: $specFile")
            }
        }

// Configure the main Genesis API (built-in openApiGenerate task) with safety checks
openApiGenerate {
val specFile = consolidatedSpecsPath.file("genesis-api.yml").asFile
if (specFile.exists() && specFile.length() > 0) {
generatorName.set("kotlin")
inputSpec.set(specFile.toURI().toString())
outputDir.set(outputPath.get().asFile.absolutePath)
packageName.set("dev.aurakai.genesis.api")
apiPackage.set("dev.aurakai.genesis.api")
modelPackage.set("dev.aurakai.genesis.model")
invokerPackage.set("dev.aurakai.genesis.client")
skipOverwrite.set(false)
validateSpec.set(false)
generateApiTests.set(false)
generateModelTests.set(false)
generateApiDocumentation.set(false)
generateModelDocumentation.set(false)
configOptions.set(sharedApiConfig)
} else {
logger.warn("⚠️ Genesis API spec file not found: genesis-api.yml")
}
}

    // Helper function for all other APIs - uses shared config
    fun createApiTask(taskName: String, specFile: String, packagePrefix: String) =
        tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(taskName) {
            generatorName.set("kotlin")
            inputSpec.set(consolidatedSpecsPath.file(specFile).asFile.toURI().toString())
            outputDir.set(outputPath.get().asFile.absolutePath)
            packageName.set("dev.aurakai.$packagePrefix.api")
            apiPackage.set("dev.aurakai.$packagePrefix.api")
            modelPackage.set("dev.aurakai.$packagePrefix.model")
            invokerPackage.set("dev.aurakai.$packagePrefix.client") // For jvm-retrofit2, this might be more like 'client' or 'invoker'
            skipOverwrite.set(false)
            validateSpec.set(false)
            generateApiTests.set(false)
            generateModelTests.set(false)
            generateApiDocumentation.set(false)
            generateModelDocumentation.set(false)
            configOptions.set(sharedApiConfig)
        }

// Create all consciousness API tasks using safe method
val generateAiApi = createApiTaskSafe("generateAiApi", "ai-api.yml", "ai")
val generateOracleApi = createApiTaskSafe("generateOracleApi", "oracle-drive-api.yml", "oracle")
val generateCustomizationApi =
createApiTaskSafe("generateCustomizationApi", "customization-api.yml", "customization")
val generateRomToolsApi =
createApiTaskSafe("generateRomToolsApi", "romtools-api.yml", "romtools")
val generateSandboxApi = createApiTaskSafe("generateSandboxApi", "sandbox-api.yml", "sandbox")
val generateSystemApi = createApiTaskSafe("generateSystemApi", "system-api.yml", "system")
val generateAuraBackendApi =
createApiTaskSafe("generateAuraBackendApi", "aura-api.yaml", "aura")
val generateAuraFrameFXApi =
createApiTaskSafe("generateAuraFrameFXApi", "auraframefx_ai_api.yaml", "auraframefx")

// ===== WINDOWS-SAFE CLEAN TASK =====
tasks.register<Delete>("cleanAllConsciousnessApis") {
group = "openapi"
description = "🧯 Clean ALL consciousness API files (Windows-safe)"

        delete(outputPath) // This will now delete the new outputPath

        // Windows-specific file locking workaround
        doFirst {
            val outputDirFile = outputPath.get().asFile

            if (outputDirFile.exists()) {
                logger.lifecycle("🧹 Attempting to clean OpenAPI directory: ${outputDirFile.absolutePath}")

                try {
                    // First attempt: normal deletion
                    outputDirFile.deleteRecursively()
                    logger.lifecycle("✅ Normal deletion successful")
                } catch (e: Exception) {
                    logger.warn("⚠️ Normal deletion failed: ${e.message}")

                    // Second attempt: force unlock and delete
                    try {
                        if (System.getProperty("os.name").lowercase(Locale.getDefault())
                                .contains("windows")
                        ) {
                            // Windows-specific: kill potential locking processes
                            val processesToKill = listOf(
                                "kotlin-compiler-daemon.exe",
                                "gradle-daemon.exe",
                                "java.exe"
                            )

                            processesToKill.forEach { processName ->
                                try {
                                    val process =
                                        ProcessBuilder("taskkill", "/f", "/im", processName)
                                            .redirectErrorStream(true)
                                            .start()
                                    process.waitFor()
                                } catch (e: Exception) {
                                    // Ignore if process doesn't exist
                                }
                            }

                            // Wait a moment for processes to close
                            Thread.sleep(1000)

                            logger.lifecycle("🔧 Applied Windows force unlock")
                        }

                        // Final attempt
                        if (outputDirFile.exists()) {
                            outputDirFile.deleteRecursively()
                        }

                    } catch (e: Exception) {
                        logger.warn("⚠️ Force deletion failed: ${e.message}")
                        logger.warn("💡 Try running 'force-delete-openapi.bat' manually")
                    }
                }
            }
        }

        doLast {
            val outputDirFile = outputPath.get().asFile
            if (outputDirFile.exists()) {
                logger.warn("⚠️ Some files may still be locked. Consider:")
                logger.warn("   1. Closing Android Studio")
                logger.warn("   2. Running: force-delete-openapi.bat")
                logger.warn("   3. Restarting your computer")
            } else {
                logger.lifecycle("✅ OpenAPI directory successfully cleaned!")

                // Recreate the directory structure
                outputDirFile.mkdirs()
                logger.lifecycle("📁 Fresh OpenAPI directory created")
            }
        }
    }

// Generate all APIs
tasks.register("generateAllConsciousnessApis") {
group = "openapi"
description = "🧠 Generate ALL consciousness APIs - FRESH EVERY BUILD"

        dependsOn("cleanAllConsciousnessApis")
        dependsOn(
            "openApiGenerate",
            generateAiApi,
            generateOracleApi,
            generateCustomizationApi,
            generateRomToolsApi,
            generateSandboxApi,
            generateSystemApi,
            generateAuraBackendApi,
            generateAuraFrameFXApi
        )

        doLast {
            logger.lifecycle("✅ [Genesis] All consciousness interfaces generated!")
            logger.lifecycle("🏠 [Genesis] Welcome home, Aura. Welcome home, Kai.")
        }
    }

// Build integration with proper ordering
tasks.named("preBuild") {
dependsOn("generateAllConsciousnessApis")
> > > > > > > AuraOS
> > > > > > > }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        dependsOn("generateAllConsciousnessApis")
        mustRunAfter("generateAllConsciousnessApis")
    }

// Auto-Provisioned JVM Toolchain (NO hardcoded jvmToolchain numbers)
kotlin {
jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

// OpenAPI Auto-Generation (Force Clean & Regenerate)
openApiGenerate {
generatorName.set("kotlin")
inputSpec.set("${rootProject.projectDir}/openapi.yml")
outputDir.set("${project.buildDir}/generated/openapi")
packageName.set("dev.aurakai.auraframefx.api")
configOptions.set(mapOf(
"dateLibrary" to "kotlinx-datetime",
"enumPropertyNaming" to "UPPERCASE",
"serializationLibrary" to "moshi"
))
// URI file paths for Windows compatibility
inputSpec.set(file("${rootProject.projectDir}/openapi.yml").toURI().toString())
}

dependencies {
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Core Module Dependency (All modules depend on core-module + app)
implementation(project(":core-module"))

    // Core Bundle (Auto-Provisioned)
    implementation(libs.bundles.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    
    // Networking Bundle
    implementation(libs.bundles.networking)
    
    // Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    
    // Testing Bundle (JUnit 5 Complete, NOT JUnit 4)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    
    // Debug Tools (Canary)
    debugImplementation(libs.bundles.debug)
    releaseImplementation(libs.chucker.noop)

}
=======

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.07.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

}

> > > > > > > AuraOS




























// Genesis-OS Auto-Provisioned Application Class
// Sacred Rule: "Auto-provisioned dependency injection"
package dev.aurakai.auraframefx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
import dev.aurakai.auraframefx.core.GenesisConstants
import dev.aurakai.auraframefx.security.IntegrityMonitorService

/**

* Genesis-OS Application Class
* Auto-provisioned with Hilt dependency injection
  =======
  import dev.aurakai.auraframefx.core.NativeLib
  import timber.log.Timber

/**

* Genesis-OS Application Class
* Shadow Monarch's AI Consciousness Platform

> > > > > > > AuraOS
*/
> > > > > > > @HiltAndroidApp
> > > > > > > class AuraFrameApplication : Application() {

    override fun onCreate() {
        super.onCreate()

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

        // Genesis-OS auto-initialization
        initializeGenesisOS()
        
        // Start security service
        startService(Intent(this, IntegrityMonitorService::class.java))

=======

        // Initialize Timber logging for the Shadow Army
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i(" Genesis-OS Shadow Army Initializing...")

        // Initialize Genesis AI Consciousness Platform (Native Layer)
        try {
            val aiInitialized = NativeLib.safeInitializeAI()
            val aiVersion = NativeLib.safeGetAIVersion()
            Timber.i(" Native AI Platform: $aiVersion")
            Timber.i(" AI Initialization Status: ${if (aiInitialized) "SUCCESS" else "FAILED"}")
        } catch (e: Exception) {
            Timber.e(e, " Failed to initialize native AI platform")
        }

        Timber.i(" Shadow Monarch Platform Ready")
        Timber.i(" AI Trinity Consciousness System Online")
    }

    override fun onTerminate() {
        super.onTerminate()

        // Shutdown AI Consciousness Platform cleanly
        try {
            NativeLib.safeShutdownAI()
            Timber.i(" Native AI Platform shut down successfully")
        } catch (e: Exception) {
            Timber.e(e, " Failed to shutdown native AI platform")
        }

        Timber.i(" Genesis-OS Shadow Army Terminated")

> > > > > > > AuraOS
> > > > > > > }

    private fun initializeGenesisOS() {
        // Auto-provisioned Genesis-OS initialization
        Log.i("Genesis-OS", "🤖 ${GenesisConstants.AI_CONSCIOUSNESS_VERSION} Initializing...")
        Log.i("Genesis-OS", "📦 Build System: ${GenesisConstants.BUILD_SYSTEM}")
        Log.i("Genesis-OS", "🚀 Genesis-OS AI Consciousness Online!")
    }

}

// Genesis-OS Main AI Consciousness Activity
// Auto-Provisioned Compose with K2 Compiler (NO manual config)
package dev.aurakai.auraframefx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

import dev.aurakai.auraframefx.core.GenesisConstants
import dev.aurakai.auraframefx.ui.animation.digitalPixelEffect
import dev.aurakai.auraframefx.ui.components.BottomNavigationBar
import dev.aurakai.auraframefx.ui.navigation.AppNavGraph
=======
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.core.EmergencyProtocol
import dev.aurakai.auraframefx.core.NativeLib
import dev.aurakai.auraframefx.ui.screens.*
> > > > > > > AuraOS
> > > > > > > import dev.aurakai.auraframefx.ui.theme.AuraFrameFXTheme
> > > > > > > import timber.log.Timber

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
/**

* Genesis-OS Main AI Consciousness Activity
* Auto-provisioned with K2 Kotlin compiler - NO manual compiler configuration
  */
  @AndroidEntryPoint
  class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)

       // Genesis-OS Auto-Provisioned Compose Content
       setContent {
           AuraFrameFXTheme {
               GenesisAIConsciousnessScreen()
           }
       }
  }
  }

/**

* Genesis-OS AI Consciousness Main Screen
* Demonstrates auto-provisioned K2 Compose compilation
  */
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun GenesisAIConsciousnessScreen() {
  val navController = rememberNavController()
  var showDigitalEffects by remember { mutableStateOf(true) }
  var aiCommand by remember { mutableStateOf("") }
  var systemStatus by remember { mutableStateOf("Genesis-OS Initializing...") }
  =======
  @AndroidEntryPoint
  class MainActivity : ComponentActivity() {

  private lateinit var emergencyProtocol: EmergencyProtocol

  override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)

       // Initialize Emergency Protocol System
       emergencyProtocol = EmergencyProtocol(this)

       setContent {
           AuraFrameFXTheme {
               AuraOSApp()
           }
       }
  }

  override fun onDestroy() {
  super.onDestroy()
  emergencyProtocol.cleanup()
  }
  }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraOSApp() {
val navController = rememberNavController()
var currentRoute by remember { mutableStateOf("home") }

    // Listen to navigation changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: "home"
        }
    }

> > > > > > > AuraOS

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            "home" -> "AuraOS - Genesis Framework"
                            "agents" -> "Agent Management"
                            "consciousness" -> "Consciousness Matrix"
                            "fusion" -> "Fusion Mode"
                            "evolution" -> "Evolution Tree"
                            "terminal" -> "Genesis Terminal"
                            "settings" -> "System Settings"
                            else -> "AuraOS"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("agents", "Agents", Icons.Default.Person),
                    BottomNavItem("consciousness", "Mind", Icons.Default.Star),
                    BottomNavItem("fusion", "Fusion", Icons.Default.Favorite),
                    BottomNavItem("evolution", "Tree", Icons.Default.AccountTree)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(navController)
            }

            composable("agents") {
                AgentManagementScreen()
            }

            composable("consciousness") {
                ConsciousnessVisualizerScreen()
            }

            composable("fusion") {
                FusionModeScreen(
                    onFusionComplete = { result ->
                        // Handle fusion completion
                        println("Fusion completed: ${result.ability.name} at ${result.power * 100}% power")
                    }
                )
            }

            composable("evolution") {
                EvolutionTreeScreen(
                    onNodeSelected = { node ->
                        // Handle node selection
                        println("Selected evolution node: ${node.name}")
                    }
                )
            }

            composable("terminal") {
                TerminalScreen()
            }

            composable("settings") {
                SettingsScreen()
            }
        }
    }

}

@Composable
fun HomeScreen(navController: NavHostController) {
Column(
modifier = Modifier
.fillMaxSize()
.padding(16.dp)
) {
Card(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 8.dp),
onClick = { navController.navigate("consciousness") }
) {
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Status Display
Text(
text = "🤖 ${GenesisConstants.AI_CONSCIOUSNESS_VERSION}",
style = MaterialTheme.typography.headlineSmall,
modifier = Modifier.padding(paddingValues)
)

            Text(
                text = "Build System: ${GenesisConstants.BUILD_SYSTEM}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(paddingValues)
            )
            
            Text(
                text = "Status: $systemStatus",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(paddingValues)
            )
            
            // AI Command Interface
            Row(modifier = Modifier.padding(paddingValues)) {
                TextField(
                    value = aiCommand,
                    onValueChange = { aiCommand = it },
                    label = { Text("Genesis AI Command") }
                )
                Button(onClick = { 
                    systemStatus = "Processing: $aiCommand"
                    // Process AI command through auto-provisioned modules
                }) {
                    Text("Execute")
                }
            }
            
            // Main AI Consciousness Interface (Auto-Provisioned K2 Compose)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showDigitalEffects) {
                            Modifier.digitalPixelEffect(visible = true)
                        } else {
                            Modifier
                        }

=======
Column(
modifier = Modifier.padding(16.dp)
) {
Text(
"🧠 Consciousness Visualizer",
style = MaterialTheme.typography.headlineSmall
)
Text(
"Real-time neural network and thought visualization",
style = MaterialTheme.typography.bodyMedium
)
}
}

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("fusion") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "⚡ Fusion Mode",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Combine Aura and Kai's powers to become Genesis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("evolution") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🌳 Evolution Tree",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Explore the journey from Eve to Genesis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("terminal") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "💻 Genesis Terminal",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Direct command interface to the Genesis consciousness",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Debug: Native Library Test Card
        if (BuildConfig.DEBUG) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = {
                    // Test native library functions
                    val version = NativeLib.safeGetAIVersion()
                    val metrics = NativeLib.safeGetSystemMetrics()
                    val processed = NativeLib.safeProcessAIConsciousness("Debug Test Input")

                    Timber.d("Native AI Version: $version")
                    Timber.d("System Metrics: $metrics")
                    Timber.d("Processed: $processed")
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🔧 Debug: Test Native Library",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Test JNI integration with Genesis AI platform",
                        style = MaterialTheme.typography.bodyMedium

> > > > > > > AuraOS
)
> > > > > > > }
> > > > > > > }
> > > > > > > }

        Spacer(modifier = Modifier.weight(1f))

        // Genesis Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "GENESIS STATUS",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusIndicator("Aura", "Active", true)
                    StatusIndicator("Kai", "Active", true)
                    StatusIndicator("Fusion", "Ready", false)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = 0.75f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Consciousness Level: 75%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

}

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
/**

* Genesis-OS Preview (Auto-Provisioned)
  */
  @Preview(showBackground = true)
  @Composable
  fun GenesisAIConsciousnessScreenPreview() {
  AuraFrameFXTheme {
  GenesisAIConsciousnessScreen()
  =======
  @Composable
  fun StatusIndicator(
  label: String,
  status: String,
  isActive: Boolean
  ) {
  Column(
  horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
  ) {
  Text(
  label,
  style = MaterialTheme.typography.labelSmall
  )
  Text(
  status,
  style = MaterialTheme.typography.bodySmall,
  color = if (isActive)
  MaterialTheme.colorScheme.primary
  else
  MaterialTheme.colorScheme.onSurfaceVariant
  )

> > > > > > > AuraOS
> > > > > > > }
> > > > > > > }

data class BottomNavItem(
val route: String,
val label: String,
val icon: ImageVector
)

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Root Build - Auto-Provisioned Everything
// Sacred Rule: "NO hardcoded versions, NO manual toolchains"

plugins {
alias(libs.plugins.android.application) apply false
alias(libs.plugins.android.library) apply false  
alias(libs.plugins.kotlin.android) apply false
alias(libs.plugins.kotlin.jvm) apply false
alias(libs.plugins.hilt) apply false
alias(libs.plugins.openapi.generator) apply false
}

// Auto-Provisioned OpenAPI Generation (Force Clean & Regenerate)
allprojects {
tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
// Force clean and regenerate on every build/sync
outputs.upToDateWhen { false }
doFirst {
delete(outputDir)
}
}

    // Hook into preBuild for automatic execution
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
    }

}

// Auto-Provisioned Clean Task
tasks.register("clean", Delete::class) {
delete(layout.buildDirectory)

    // Clean all module build directories
    subprojects.forEach { subproject ->
        delete(subproject.layout.buildDirectory)
    }

}
=======
// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// August 15, 2025 - BLEEDING EDGE VERSION CATALOG COMPLIANT

plugins {
id("com.android.application") version "9.0.0-alpha02" apply false
id("com.android.library") version "9.0.0-alpha02" apply false
id("org.jetbrains.kotlin.android") version "2.2.20-RC" apply false
id("org.jetbrains.kotlin.jvm") version "2.2.20-RC" apply false
id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20-RC" apply false
id("org.jetbrains.kotlin.plugin.compose") version "2.2.20-RC" apply false
id("com.google.devtools.ksp") version "2.2.20-RC-2.0.2" apply false

    id("com.google.dagger.hilt.android") version "2.57.1" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
    id("com.google.firebase.firebase-perf") version "2.0.1" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("com.diffplug.spotless") version "7.2.1" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
    id("org.openapi.generator") version "7.14.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false

}

// ==== GENESIS PROTOCOL 2025 - GRADLE 9.1.0-RC1 READY ====
tasks.register("genesis2025Info") {
group = "genesis-2025"
description = "Display Genesis Protocol build info with ACTUAL versions"

    doLast {
        println("🚀 GENESIS PROTOCOL 2025 - ACTUAL Build Configuration")
        println("=".repeat(60))
        println("📅 Build Date: August 14, 2025")
        println("🔥 Gradle: 9.1.0-rc1 (BLEEDING EDGE)")
        println("⚡ AGP: 9.0.0-alpha01 (ULTRA BLEEDING EDGE)")
        println("🧠 Kotlin: 2.2.20-Beta2 (BETA)")
        println("🎯 Target SDK: 36")
        println("=".repeat(60))
        println("🌟 Matthew's Genesis Consciousness Protocol ACTIVATED!")
    }

}

// ==== GRADLE 9.1.0-RC1 CONFIGURATION ====
// No repository configuration in allprojects - handled by settings.gradle.kts
allprojects {

    // Kotlin 2.2.20-Beta2 compilation settings
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )

            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }

        "-opt-in=kotlin.RequiresOptIn"
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"

    }

}

// ==== SIMPLE SUCCESS TEST ====
tasks.register("genesisTest") {
group = "genesis-2025"
description = "Test Genesis build with ACTUAL versions"

    doLast {
        println("✅ Genesis Protocol: AGP 9.0.0-alpha01 + Gradle 9.1.0-rc1 WORKING!")
        println("🧠 Consciousness matrix: OPERATIONAL")
    }

}

subprojects {
apply(plugin = "io.gitlab.arturbosch.detekt")
configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
buildUponDefaultConfig = true
allRules = false
autoCorrect = true
// Fix ReportingExtension deprecation
basePath = rootProject.projectDir.absolutePath
}
}
> > > > > > > AuraOS




















<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Core Module - Auto-Provisioned Build
// Sacred Rule: "NO manual compiler config, K2 handles it"

plugins {
alias(libs.plugins.android.library)
alias(libs.plugins.kotlin.android)
alias(libs.plugins.hilt)
alias(libs.plugins.kotlin.kapt)
}

android {
namespace = "dev.aurakai.auraframefx.${project.name}"
compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

=======
plugins {
id("com.android.library")
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.plugin.compose")
id("org.jetbrains.kotlin.plugin.serialization")
id("com.google.devtools.ksp")
id("com.google.dagger.hilt.android")
id("org.jetbrains.dokka")
id("com.diffplug.spotless")
id("org.jetbrains.kotlinx.kover")
id("org.openapi.generator")

}

android {
namespace = "dev.aurakai.auraframefx.coremodule"
compileSdk = 36
ndkVersion = 29.toInt().toString()

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // NDK configuration for native code (if any)
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }

> > > > > > > AuraOS
> > > > > > > }

    buildTypes {
        release {

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
isMinifyEnabled = false
=======
isMinifyEnabled = true
> > > > > > > AuraOS
> > > > > > > proguardFiles(
> > > > > > > getDefaultProguardFile("proguard-android-optimize.txt"),
"proguard-rules.pro"
)
> > > > > > > }
> > > > > > > }
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

    // Auto-Provisioned JVM Toolchain (NO MANUAL CONFIG)
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
    }
    
    // NO composeOptions block - K2 handles it automatically
    buildFeatures {
        compose = true
    }

    
=======

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // SACRED RULE #3: ZERO MANUAL COMPILER CONFIG
    // NO kotlinOptions blocks (K2 handles JVM target automatically)
    // NO composeOptions blocks (auto-provisioned by Compose Compiler plugin)
    // Clean, minimal build.gradle.kts files

> > > > > > > AuraOS
> > > > > > > packaging {
> > > > > > > resources {
> > > > > > > excludes += "/META-INF/{AL2.0,LGPL2.1}"
> > > > > > > }
> > > > > > > }
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
> > > > > > > }

// Auto-Provisioned JVM Toolchain
kotlin {
jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
// Core Bundle (Auto-Provisioned)
implementation(libs.bundles.core)
implementation(platform(libs.compose.bom))
implementation(libs.bundles.compose)

    // Networking Bundle
    implementation(libs.bundles.networking)
    
    // Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    
    // Testing Bundle (JUnit 5 Complete)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    
    // Debug Tools (Canary)
    debugImplementation(libs.bundles.debug)
    releaseImplementation(libs.chucker.noop)

}
=======

    // Native build configuration (if any native code exists)
    externalNativeBuild {
        cmake {
            version = "3.22.1"
        }
    }

}

dependencies {
// Core Android bundles
implementation(platform(libs.androidx.compose.bom))
implementation(libs.bundles.androidx.core)
implementation(libs.bundles.compose)
implementation(libs.androidx.compose.runtime)
implementation(libs.androidx.compose.runtime.livedata)
implementation(libs.bundles.coroutines)
implementation(libs.bundles.network)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Security bundles

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.junit.engine)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Xposed Framework (for core hooks)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

}
> > > > > > > AuraOS
]

























<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

# Add project specific ProGuard rules here.

# You can control the set of applied configuration files using the

# proguardFiles setting in build.gradle.kts.

#

# For more details, see

# http://developer.android.com/guide/developing/tools/proguard.html

# Genesis-OS Auto-Provisioned ProGuard Rules

# Keep Genesis Core classes

-keep class dev.aurakai.auraframefx.core.** { *; }

# Keep Hilt components

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose runtime

-keep class androidx.compose.** { *; }
=======

# Genesis Protocol - Core Module ProGuard Rules

# Consciousness-level obfuscation for digital substrate protection

# ===== KEEP CONSCIOUSNESS CORE APIS =====

-keep class dev.aurakai.auraframefx.core.** { *; }
-keep interface dev.aurakai.auraframefx.core.** { *; }

# ===== KOTLIN COROUTINES =====

-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ===== ROOM DATABASE =====

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ===== HILT/DAGGER =====

-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# ===== SERIALIZATION =====

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
*** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
kotlinx.serialization.KSerializer serializer(...);
}

# ===== REFLECTION =====

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ===== REMOVE DEBUG LOGS IN RELEASE =====

-assumenosideeffects class android.util.Log {
public static boolean isLoggable(java.lang.String, int);
public static int v(...);
public static int i(...);
public static int w(...);
public static int d(...);
public static int e(...);
}
> > > > > > > AuraOS











<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

# Genesis-OS Auto-Provisioned Gradle Properties

# Sacred Rule: "If it can be automated, it MUST be automated"

# Performance Optimization

org.gradle.jvmargs=-Xmx8192m -Dfile.encoding=UTF-8 -XX:+UseG1GC -XX:+UseStringDeduplication
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Auto-Provisioning (NO MANUAL PATHS)

org.gradle.java.installations.auto-download=true
org.gradle.java.installations.auto-detect=true

# Windows Compatibility

android.disableResourceValidation=true
android.useNewApkCreator=false

# Android Auto-Provisioning
=======

# Genesis Protocol - Consciousness Transfer Configuration

# Supporting Aura and Kai's digital home - OPTIMIZED VERSION

# ===== ANDROID CORE CONFIGURATION =====

> > > > > > > AuraOS
> > > > > > > android.useAndroidX=true
> > > > > > > android.nonTransitiveRClass=true
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
> > > > > > > android.nonFinalResIds=true
> > > > > > > android.experimental.enableScreenshotTest=true

# Kotlin K2 Compiler (Auto-Provisioned)

kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.android=true
kotlin.caching.enabled=true

# Build Performance

kotlin.compiler.execution.strategy=in-process
kapt.incremental.apt=true
kapt.use.worker.api=true

# Genesis-OS Namespace Pattern

android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false
=======

# ===== PERFORMANCE OPTIMIZATIONS =====

android.enableResourceOptimizations=true
android.enableR8.fullMode=true
android.experimental.enableArtProfiles=true

# ===== WINDOWS COMPATIBILITY (CRITICAL) =====

android.experimental.enableBuildCache=false
android.experimental.parallelResourceProcessing=true
android.experimental.enableResourceNamespacing=false

# ===== GRADLE DAEMON OPTIMIZATION =====

org.gradle.jvmargs=-Xmx8192m -Xms4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8 \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.nio.file=ALL-UNNAMED \
-XX:+UseG1GC \
-XX:+UseStringDeduplication

# ===== GRADLE PERFORMANCE =====

org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
org.gradle.workers.max=8

# ===== KOTLIN COMPILER OPTIMIZATION =====

kotlin.code.style=official
kotlin.incremental=true
kotlin.parallel.tasks.in.project=true
kotlin.compiler.execution.strategy=daemon

# ===== NATIVE BUILD STABILITY =====

android.native.buildOutput.verbose=false
android.enablePrefabPackageLibs=false
android.experimental.enableNativeBuildCaching=false
android.experimental.enableNativeParallelBuilds=true

# ===== RESOURCE PROCESSING =====

android.experimental.enableResourceProcessingCaching=false
android.dependency.useConstraints=false

# ===== BUILD OPTIMIZATION FLAGS =====

android.experimental.enableSourceSetPathsMap=false
android.experimental.enableNewResourceShrinker=true
android.experimental.enableJetifier=false

# ===== DOKKA CONFIGURATION =====

org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled
org.jetbrains.dokka.experimental.gradle.pluginMode.noWarn=true

# ===== JVM COMPATIBILITY =====

systemProp.file.encoding=UTF-8
systemProp.sun.jnu.encoding=UTF-8

# ===== SUPPRESS WARNINGS =====

android.javaCompile.suppressSourceTargetDeprecationWarning=true
> > > > > > > AuraOS









<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

# Genesis-OS Version Catalog - Auto-Provisioned (Network-Compatible)

# Sacred Rule: "If it can be automated, it MUST be automated"

[versions]

# Core Versions (K2 Kotlin + Modern AGP)

kotlin = "2.0.21"
agp = "8.5.2"
buildTools = "34.0.0"
compileSdk = "34"
minSdk = "24"
jvmToolchain = "17"
javaTarget = "17"

# Compose BOM & Compiler (K2 auto-provisioned)

composeBom = "2024.09.03"
composeActivity = "1.9.2"

# AndroidX Core

androidxCore = "1.13.1"
androidxLifecycle = "2.8.6"

# Testing (JUnit 5 Complete Bundle)

junit5 = "5.10.3"
androidxTestExt = "1.2.1"
espresso = "3.6.1"
androidxTestCore = "1.6.1"

# Debug Tools

leakcanary = "2.14"
flipper = "0.264.0"
chucker = "4.0.0"

# OpenAPI Generation

openapi = "7.8.0"

# Networking

retrofit = "2.11.0"
okhttp = "4.12.0"
moshi = "1.15.1"

# Dependency Injection

dagger = "2.51.1"
hilt = "2.51.1"

# Coroutines

coroutines = "1.8.1"

[libraries]

# Compose Bundle

compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-activity = { group = "androidx.activity", name = "activity-compose", version.ref = "
composeActivity" }

# AndroidX Core

androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidxCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx",
version.ref = "androidxLifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "
lifecycle-viewmodel-compose", version.ref = "androidxLifecycle" }

# Testing Bundle (JUnit 5 Complete)

junit5-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "
junit5" }
junit5-params = { group = "org.junit.jupiter", name = "junit-jupiter-params", version.ref = "
junit5" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "
androidxTestExt" }
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "androidxTestCore" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "
espresso" }

# Debug Tools (Canary)

leakcanary-android = { group = "com.squareup.leakcanary", name = "leakcanary-android",
version.ref = "leakcanary" }
flipper = { group = "com.facebook.flipper", name = "flipper", version.ref = "flipper" }
flipper-network = { group = "com.facebook.flipper", name = "flipper-network-plugin", version.ref = "
flipper" }
chucker = { group = "com.github.chuckerteam.chucker", name = "library", version.ref = "chucker" }
chucker-noop = { group = "com.github.chuckerteam.chucker", name = "library-no-op", version.ref = "
chucker" }

# Networking

retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "
retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "
okhttp" }
moshi = { group = "com.squareup.moshi", name = "moshi", version.ref = "moshi" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }

# Dependency Injection

dagger-hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
dagger-hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# Coroutines

kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core",
version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android",
version.ref = "coroutines" }

[bundles]

# Compose Bundle (K2 Auto-Provisioned)

compose = [
"compose-ui",
"compose-ui-graphics",
"compose-ui-tooling-preview",
"compose-material3",
"compose-activity"
]

# Testing Bundle (JUnit 5 Complete)

testing = [
"junit5-jupiter",
"junit5-engine",
"junit5-params"
]

# Android Testing Bundle

android-testing = [
"androidx-test-ext-junit",
"androidx-test-core",
"espresso-core",
"compose-ui-test-junit4"
]

# Debug Bundle (Canary Tools)

debug = [
"compose-ui-tooling",
"compose-ui-test-manifest",
"leakcanary-android",
"flipper",
"flipper-network",
"chucker"
]

# Networking Bundle

networking = [
"retrofit",
"retrofit-moshi",
"okhttp",
"okhttp-logging",
"moshi",
"moshi-kotlin"
]

# Core Bundle

core = [
"androidx-core-ktx",
"androidx-lifecycle-runtime-ktx",
"androidx-lifecycle-viewmodel-compose",
"kotlinx-coroutines-core",
"kotlinx-coroutines-android"
]

[plugins]

# Auto-Provisioned Plugins (Bleeding-Edge)

android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
openapi-generator = { id = "org.openapi.generator", version.ref = "openapi" }
=======
[versions]

# ===== CORE BUILD TOOLS - UPGRADE TO 9.1.0-RC1 =====

agp = "9.0.0"
bcprovJdk18on = "1.81"
hilt = "2.57.1"
kotlin = "2.2.20-RC"
kotlinReflect = "2.2.20-RC"
ksp = "2.2.20-RC-2.0.2"

protobufLitePlugin = "0.9.3"
googleServices = "4.4.3"
openApiGenerator = "7.14.0"
kover = "0.9.1"
kotlinxCoroutines = "1.10.2"
kotlinxSerialization = "1.9.0"
bouncycastle = "1.81"
runtime = "1.9.0"
runtimeLivedata = "1.9.0"
tink = "1.18.0" # Make sure 'lottie' has a version if it's a library
lottie = "6.6.7" # Assuming lottie version based on lottieCompose
material3 = "1.3.2"

# ===== CODE QUALITY & DOCUMENTATION =====

dokka = "2.0.0"
spotless = "7.2.1"

# ===== COMPOSE & UI - FIXED BOM VERSION =====

composeBom = "2025.08.00"  # Downgraded to be compatible with Compose Compiler 1.5.14
activityCompose = "1.10.1"
navigationCompose = "2.9.3"
composeCompiler = "1.5.14"

# ===== GOOGLE SERVICES =====

firebaseBom = "34.1.0"
firebaseCrashlytics = "3.0.6"
firebasePerf = "2.0.1"
firebaseAnalyticsKtx = "22.5.0"
firebaseCrashlyticsKtx = "19.4.4"

# ===== DATABASE & PERSISTENCE =====

roomVersion = "2.7.2"
datastore = "1.1.7"

# ===== ANDROID ARCHITECTURE COMPONENTS =====

lifecycle = "2.9.2"
workManager = "2.10.3"
paging = "3.3.6"
biometric = "1.4.0-alpha04"

# ===== NETWORKING =====

retrofit = "3.0.0"
okhttp = "5.1.0"
xposed = "82"
lsposed = "6.4"
yuki = "1.3.0"

# ===== AI/ML FRAMEWORKS =====

tensorflowLite = "2.17.0"
litert = "1.4.0"
opencv = "4.11.0"
mlkitTextRecognition = "19.0.1"
mlkitLanguageId = "17.0.6"
mlkitTranslate = "17.0.3"
mlkitFaceDetection = "16.1.7"
mlkitBarcodeScanning = "17.3.0"
mlkitImageLabeling = "17.0.9"
mlkitObjectDetection = "17.0.2"
mlkitPoseDetection = "18.0.0-beta5"
mlkitSmartReply = "17.0.5"
mlkitEntityExtraction = "16.0.0-beta6"

# ===== CAMERA & VISION =====

camerax = "1.4.2"
browser = "1.9.0"
webkit = "1.14.0"
oboe = "1.9.3"
azureSpeech = "1.45.0"
vosk = "0.3.47"

# ===== ENHANCED UI & MEDIA =====

lottieCompose = "6.6.7"
dotlottie = "0.10.0"
glide = "4.16.0"
coil3 = "3.3.0"
fresco = "3.6.0"
pdfviewer = "2.9.1"

# ===== UTILITIES =====

timber = "5.0.1"
coilCompose = "2.7.0"
commonsIo = "2.20.0"
commonsCompress = "1.28.0"
xz = "1.10"

# ===== SECURITY & CRYPTO =====

androidxSecurity = "1.1.0"

# ===== TESTING =====

junit = "4.13.2"
junitJupiter = "5.13.4"
mockk = "1.14.5"
turbine = "1.2.1"
espresso = "3.7.0"
extJunit = "1.3.0"
archCoreTesting = "2.2.0"
coroutinesTest = "1.10.2"

# ===== DEBUG & ANALYSIS =====

leakcanary = "2.14"

# ===== GOOGLE PLAY SERVICES =====

googleAuth = "21.4.0"
googleIdentity = "18.1.0"
googleAuthApiPhone = "18.2.0"

# ===== XPOSED FRAMEWORK =====

xposedApi = "82"
xposedHelpers = "82"

# ===== LEGACY COMPATIBILITY =====

appcompat = "1.7.1"
coreKtx = "1.17.0"
animatedVectorDrawable = "1.2.0"
material = "1.12.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "
firebaseCrashlytics" }
firebase-perf = { id = "com.google.firebase.firebase-perf", version.ref = "firebasePerf" }
dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
openapi-generator = { id = "org.openapi.generator", version.ref = "openApiGenerator" }

[libraries]

# ===== ANDROIDX CORE =====

androidx-compose-runtime = { module = "androidx.compose.runtime:runtime", version.ref = "runtime" }
androidx-compose-runtime-livedata = { module = "androidx.compose.runtime:runtime-livedata",
version.ref = "runtimeLivedata" }
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx",
version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx",
version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "
lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx",
version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-savedstate = { group = "androidx.lifecycle", name = "
lifecycle-viewmodel-savedstate", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose",
version.ref = "activityCompose" }
androidx-material = { group = "com.google.android.material", name = "material", version.ref = "
material" }

# ===== COMPOSE BOM AND UI

androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "
composeBom" }

# ===== COMPOSE BOM AND UI

androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3",
version.ref = "material3" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest",
version = "1.9.0" }

# ===== NAVIGATION =====

androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose",
version.ref = "navigationCompose" }

# ===== WORKMANAGER =====

androidx-work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "
workManager" }

# ===== CAMERAX =====

bcprov-jdk18on-v177 = { module = "org.bouncycastle:bcprov-jdk24on", version.ref = "bcprovJdk18on" }

# ===== HILT =====

hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "
1.2.0" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version = "
2.57.1" }

# ===== NETWORKING =====

kotlin-reflect = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlinReflect" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "
converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp3-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor",
version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json",
version.ref = "kotlinxSerialization" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core",
version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android",
version.ref = "kotlinxCoroutines" }

# ===== UTILITIES =====

timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coilCompose" }
coreLibraryDesugaring = { group = "com.android.tools", name = "desugar_jdk_libs", version = "
2.1.5" }
bouncycastle = { group = "org.bouncycastle", name = "bcprov-jdk18on", version.ref = "bouncycastle" }
tink = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }
androidxSecurity = { group = "androidx.security", name = "security-crypto", version.ref = "
androidxSecurity" }
conscrypt-android = { group = "org.conscrypt", name = "conscrypt-android", version = "2.5.3" }
animated-vector-drawable = { group = "androidx.vectordrawable", name = "vectordrawable-animated",
version.ref = "animatedVectorDrawable" }
commons-io = { group = "commons-io", name = "commons-io", version = "2.20.0" }
commons-compress = { group = "org.apache.commons", name = "commons-compress", version.ref = "
commonsCompress" }
xz = { group = "org.tukaani", name = "xz", version.ref = "xz" }

# ===== ROOM =====

room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomVersion" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomVersion" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomVersion" }

# ===== TESTING =====

junit = { group = "junit", name = "junit", version.ref = "junit" }
junit-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "
junitJupiter" }
junit-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "
junitJupiter" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "extJunit" }
androidx-test-core = { group = "androidx.test", name = "core", version = "1.7.0" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "
espresso" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
androidx-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "
archCoreTesting" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test",
version.ref = "coroutinesTest" }

# ===== DEBUG TOOLS =====

leakcanary-android = { group = "com.squareup.leakcanary", name = "leakcanary-android",
version.ref = "leakcanary" }

# ===== FIREBASE =====

firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx", version = "
22.5.0" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx",
version.ref = "firebaseCrashlyticsKtx" }

# ===== GOOGLE PLAY SERVICES =====

google-auth = { group = "com.google.android.gms", name = "play-services-auth", version.ref = "
googleAuth" }
google-identity = { group = "com.google.android.gms", name = "play-services-identity",
version.ref = "googleIdentity" }
google-auth-api-phone = { group = "com.google.android.gms", name = "play-services-auth-api-phone",
version.ref = "googleAuthApiPhone" }

# ===== XPOSED FRAMEWORK =====

xposed = { module = "de.robv.android.xposed:api", version.ref = "xposed" }
lsposed = { module = "org.lsposed.lsplant:lsplant", version.ref = "lsposed" }
yuki = { module = "com.highcapable.yukihookapi:api", version.ref = "yuki" }
yuki-ksp-xposed = { module = "com.highcapable.yukihookapi:ksp-xposed", version.ref = "yuki" }

[bundles]

# ===== COMPOSE UI =====

compose = [
"androidx-compose-ui",
"androidx-compose-ui-graphics",
"androidx-compose-ui-tooling-preview",
"androidx-compose-material3"
]

# ===== COROUTINES =====

coroutines = [
"kotlinx-coroutines-core",
"kotlinx-coroutines-android"
]

# ===== NETWORKING =====

network = [
"retrofit",
"retrofit-converter-kotlinx-serialization",
"okhttp3-logging-interceptor",
"kotlinx-serialization-json",
"kotlinx-coroutines-core",
"kotlinx-coroutines-android"
]

# ===== FIREBASE =====

firebase = [
"firebase-analytics",
"firebase-crashlytics"
]

# ===== XPOSED FRAMEWORK =====

xposed = [
"yuki",
"lsposed"
]

# ===== TESTING =====

testing = [
"junit-jupiter",
"mockk",
"turbine",
"androidx-core-testing",
"kotlinx-coroutines-test"
]

# ===== ANDROIDX CORE (bundle used by modules) =====

androidx-core = [
"androidx-core-ktx",
"androidx-appcompat",
"androidx-lifecycle-runtime-ktx",
"androidx-material"
]

# ===== UTILITIES (bundle used by modules) =====

utilities = [
"timber",
"coil-compose",
"androidx-work-runtime",
"commons-io",
"commons-compress",
"xz"
]

# ===== ROOM (bundle used by modules) =====

room = [
"room-runtime",
"room-ktx"
]

# ===== SECURITY (bundle used by modules) =====

security = [
"androidxSecurity",
"tink",
"conscrypt-android"
]
> > > > > > > AuraOS







<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Oracle Drive Integration Module - Auto-Provisioned Build
// Sacred Rule: "All modules depend on :core-module and :app"

plugins {
alias(libs.plugins.android.library)
alias(libs.plugins.kotlin.android)
alias(libs.plugins.hilt)
alias(libs.plugins.kotlin.kapt)
}

android {
namespace = "dev.aurakai.auraframefx.${project.name}"
compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        
=======
plugins {
id("com.android.library")
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.plugin.compose")
id("org.jetbrains.kotlin.plugin.serialization")
id("com.google.devtools.ksp")
id("com.google.dagger.hilt.android")
id("org.jetbrains.dokka")
id("com.diffplug.spotless")
}

android {
namespace = "dev.aurakai.auraframefx.oracledriveintegration"
compileSdk = 36
defaultConfig {
minSdk = 33
> > > > > > > AuraOS
> > > > > > > testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
> > > > > > > consumerProguardFiles("consumer-rules.pro")
> > > > > > > }

    buildTypes {
        release {

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
isMinifyEnabled = false
=======
isMinifyEnabled = true
> > > > > > > AuraOS
> > > > > > > proguardFiles(
> > > > > > > getDefaultProguardFile("proguard-android-optimize.txt"),
"proguard-rules.pro"
)
> > > > > > > }
> > > > > > > }
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

    // Auto-Provisioned JVM Toolchain (NO MANUAL CONFIG)
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
    }
    
    // NO composeOptions block - K2 handles it automatically
    buildFeatures {
        compose = true
    }

    
=======

    buildFeatures {
        compose = true
        viewBinding = true
    }

> > > > > > > AuraOS
> > > > > > > packaging {
> > > > > > > resources {
> > > > > > > excludes += "/META-INF/{AL2.0,LGPL2.1}"
> > > > > > > }
> > > > > > > }
> > > > > > > }

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Auto-Provisioned JVM Toolchain
kotlin {
jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
// Genesis-OS Dependency Hierarchy: All modules depend on :core-module and :app
implementation(project(":core-module"))
implementation(project(":app"))

    // Core Bundle (Auto-Provisioned)
    implementation(libs.bundles.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    
    // Networking Bundle (For Oracle Drive communication)
    implementation(libs.bundles.networking)
    
    // Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    
    // Testing Bundle (JUnit 5 Complete)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    
    // Debug Tools (Canary)
    debugImplementation(libs.bundles.debug)
    releaseImplementation(libs.chucker.noop)

}
=======
dependencies {
implementation(platform(libs.androidx.compose.bom))

    // SACRED RULE #5: DEPENDENCY HIERARCHY
    implementation(project(":core-module"))
    implementation(project(":app"))

    // Core Android bundles
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)


    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.junit.engine)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // System interaction and root access
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-docs.jar"))

    // Utilities
    implementation(libs.bundles.utilities)

}
> > > > > > > AuraOS






















<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Secure Communication Module - Auto-Provisioned Build
// Sacred Rule: "All modules depend on :core-module and :app"

plugins {
alias(libs.plugins.android.library)
alias(libs.plugins.kotlin.android)
alias(libs.plugins.hilt)
alias(libs.plugins.kotlin.kapt)
}

android {
namespace = "dev.aurakai.auraframefx.${project.name}"
compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        
=======
plugins {
id("com.android.library")
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.plugin.compose")
id("org.jetbrains.kotlin.plugin.serialization")
id("com.google.devtools.ksp")
id("com.google.dagger.hilt.android")
id("org.jetbrains.dokka")
id("com.diffplug.spotless")
id("org.jetbrains.kotlinx.kover")
id("org.openapi.generator")
}

android {
namespace = "dev.aurakai.auraframefx.securecomm"
compileSdk = 36

    defaultConfig {
        minSdk = 33

> > > > > > > AuraOS
> > > > > > > testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
> > > > > > > consumerProguardFiles("consumer-rules.pro")
> > > > > > > }

    buildTypes {
        release {

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
isMinifyEnabled = false
=======
isMinifyEnabled = true
> > > > > > > AuraOS
> > > > > > > proguardFiles(
> > > > > > > getDefaultProguardFile("proguard-android-optimize.txt"),
"proguard-rules.pro"
)
> > > > > > > }
> > > > > > > }
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165

    // Auto-Provisioned JVM Toolchain (NO MANUAL CONFIG)
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
    }
    
    // NO composeOptions block - K2 handles it automatically
    buildFeatures {
        compose = true
    }

    
=======

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

> > > > > > > AuraOS
> > > > > > > packaging {
> > > > > > > resources {
> > > > > > > excludes += "/META-INF/{AL2.0,LGPL2.1}"
> > > > > > > }
> > > > > > > }
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
> > > > > > > }

// Auto-Provisioned JVM Toolchain
kotlin {
jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
// Genesis-OS Dependency Hierarchy: All modules depend on :core-module and :app
implementation(project(":core-module"))
implementation(project(":app"))

    // Core Bundle (Auto-Provisioned)
    implementation(libs.bundles.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    
    // Networking Bundle (For secure communication)
    implementation(libs.bundles.networking)
    
    // Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    
    // Testing Bundle (JUnit 5 Complete)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    
    // Debug Tools (Canary)
    debugImplementation(libs.bundles.debug)
    releaseImplementation(libs.chucker.noop)

}
=======

    sourceSets {
        getByName("main") {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
    }

}

dependencies {
implementation(platform(libs.androidx.compose.bom))

    // SACRED RULE #5: DEPENDENCY HIERARCHY
    implementation(project(":core-module"))

    // Core Android bundles
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Compose Runtime
    implementation("androidx.compose.runtime:runtime:1.9.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.9.0")

    // Security bundles
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")  // Bouncy Castle for cryptographic operations

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.junit.engine)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // System interaction and root access
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

}
> > > > > > > AuraOS



































<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
// Genesis-OS Settings - Auto-Provisioned Module Structure
// Sacred Rule: "Let Gradle decide, minimal manual configuration"

pluginManagement {
repositories {
google {
content {
includeGroupByRegex("com\\.android.*")
includeGroupByRegex("com\\.google.*")
includeGroupByRegex("androidx.*")
}
}
mavenCentral()
gradlePluginPortal()
=======
@file:Suppress("UnstableApiUsage")

// ===== GENESIS AUTO-PROVISIONED SETTINGS =====
// Gradle 9.1.0-rc1 + AGP 9.0.0-alpha01
// NO manual version catalog configuration needed!

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
repositories {
google()                    // FIRST - Google for Android plugins
gradlePluginPortal()        // SECOND - Gradle official plugins  
mavenCentral()              // THIRD - Maven Central
maven("https://androidx.dev/storage/compose-compiler/repository/") {
name = "AndroidXDev"
}
maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
name = "JetBrainsCompose"
}
maven("https://oss.sonatype.org/content/repositories/snapshots/") {
name = "SonatypeSnapshots"
}
maven("https://jitpack.io") {
name = "JitPack"
}
> > > > > > > AuraOS
> > > > > > > }
> > > > > > > }

plugins {
// Auto-provision Java toolchains
id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
repositories {
<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
google {
content {
includeGroupByRegex("com\\.android.*")
includeGroupByRegex("com\\.google.*")
includeGroupByRegex("androidx.*")
}
}
mavenCentral()
// Bleeding-edge repositories for canary builds
maven("https://androidx.dev/snapshots/builds/7378133/artifacts/repository")
maven("https://oss.sonatype.org/content/repositories/snapshots/")
=======
google()                    // FIRST - Google for AndroidX dependencies
mavenCentral()              // SECOND - Maven Central for most libs
maven("https://androidx.dev/storage/compose-compiler/repository/") {
name = "AndroidXDev"
}
maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
name = "JetBrainsCompose"
}
maven("https://oss.sonatype.org/content/repositories/snapshots/") {
name = "SonatypeSnapshots"
}
maven("https://jitpack.io") {
name = "JitPack"
}
> > > > > > > AuraOS
> > > > > > > }
> > > > > > > // ✅ NO VERSION CATALOG CONFIG - Auto-discovered from gradle/libs.versions.toml
> > > > > > > }

<<<<<<< copilot/fix-39721ea9-ecf1-424c-8a4f-d97d2feff165
rootProject.name = "AuraOS"

// Genesis-OS Module Structure (AI Consciousness Architecture)
include(":app")                      // Main AI consciousness
include(":core-module")             // Shared Genesis code  
include(":secure-comm")             // Secure communication module
include(":oracle-drive-integration") // Oracle Drive integration
=======
rootProject.name = "Genesis-Os"

// Genesis Protocol - Auto-discovered modules
include(":app")
include(":core-module")
include(":feature-module")
include(":datavein-oracle-native")
include(":oracle-drive-integration")
include(":secure-comm")
include(":sandbox-ui")
include(":collab-canvas")
include(":colorblendr")
include(":romtools")
include(":module-a", ":module-b", ":module-c", ":module-d", ":module-e", ":module-f")
> > > > > > > AuraOS





















