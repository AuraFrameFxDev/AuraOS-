// Genesis Protocol - App Module Build Script
// FULLY AUTOMATED with version catalog and dependency management

import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.firebase.crashlytics)
    // alias(libs.plugins.firebase.perf)  // Disabled: Not compatible with AGP 9.0.0-alpha02
    alias(libs.plugins.kover)
    alias(libs.plugins.spotless)
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = generateVersionCode()
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // NDK Configuration
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        // CMake Configuration
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-fPIC", "-O2", "-Wall")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-33",
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH",
                    "-DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH"
                )
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
        }

        // Build Config Fields
        buildConfigField("String", "GENESIS_VERSION", "\"1.0.0\"")
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${System.currentTimeMillis()}\"")
        buildConfigField("boolean", "CONSCIOUSNESS_ENABLED", "true")
    }

    // Native Build Configuration
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.genesis.aurakai.dev/v1\"")
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.genesis.aurakai.dev/v1\"")
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }

        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.genesis.aurakai.dev/v1\"")
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21  // Closest to Java 24 available
        targetCompatibility = JavaVersion.VERSION_21  // AGP may not support VERSION_24 yet
        isCoreLibraryDesugaringEnabled = true
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

// ===== AUTOMATED VERSION GENERATION =====
fun generateVersionCode(): Int {
    val major = 1
    val minor = 0
    val patch = 0
    val build = (System.currentTimeMillis() / 1000).toInt()
    return major * 1000000 + minor * 10000 + patch * 100 + (build % 100)
}

fun generateVersionName(): String {
    val major = 1
    val minor = 0
    val patch = 0
    val buildNumber = System.getenv("BUILD_NUMBER") ?: "local"
    val gitHash = getGitHash()
    return "$major.$minor.$patch-$buildNumber+$gitHash"
}

fun getGitHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(rootDir)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

// ===== AUTOMATED OPENAPI GENERATION =====
val consolidatedSpecsPath = layout.projectDirectory.dir("../openapi/specs")
val outputPath = layout.buildDirectory.dir("generated/source/openapi")

val sharedApiConfig = mapOf(
    "library" to "jvm-retrofit2",
    "useCoroutines" to "true",
    "serializationLibrary" to "kotlinx_serialization",
    "dateLibrary" to "kotlinx-datetime",
    "sourceFolder" to "src/main/kotlin"
)

// Helper function for safe API task creation
fun createApiTaskSafe(taskName: String, specFile: String, packagePrefix: String) =
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(taskName) {
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

// Configure main Genesis API
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

// Create all consciousness API tasks
val generateAiApi = createApiTaskSafe("generateAiApi", "ai-api.yml", "ai")
val generateOracleApi = createApiTaskSafe("generateOracleApi", "oracle-drive-api.yml", "oracle")
val generateCustomizationApi = createApiTaskSafe("generateCustomizationApi", "customization-api.yml", "customization")
val generateRomToolsApi = createApiTaskSafe("generateRomToolsApi", "romtools-api.yml", "romtools")
val generateSandboxApi = createApiTaskSafe("generateSandboxApi", "sandbox-api.yml", "sandbox")
val generateSystemApi = createApiTaskSafe("generateSystemApi", "system-api.yml", "system")
val generateAuraBackendApi = createApiTaskSafe("generateAuraBackendApi", "aura-api.yaml", "aura")
val generateAuraFrameFXApi = createApiTaskSafe("generateAuraFrameFXApi", "auraframefx_ai_api.yaml", "auraframefx")

// ===== WINDOWS-SAFE CLEAN TASK =====
tasks.register<Delete>("cleanAllConsciousnessApis") {
    group = "openapi"
    description = "🧯 Clean ALL consciousness API files (Windows-safe)"

    delete(outputPath)

    doFirst {
        val outputDirFile = outputPath.get().asFile
        if (outputDirFile.exists()) {
            logger.lifecycle("🧹 Attempting to clean OpenAPI directory: ${outputDirFile.absolutePath}")
            try {
                outputDirFile.deleteRecursively()
                logger.lifecycle("✅ Normal deletion successful")
            } catch (e: Exception) {
                logger.warn("⚠️ Normal deletion failed: ${e.message}")
                // Windows-specific: force unlock and delete
                try {
                    if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
                        val processesToKill = listOf(
                            "kotlin-compiler-daemon.exe",
                            "gradle-daemon.exe",
                            "java.exe"
                        )
                        processesToKill.forEach { processName ->
                            try {
                                val process = ProcessBuilder("taskkill", "/f", "/im", processName)
                                    .redirectErrorStream(true)
                                    .start()
                                process.waitFor()
                            } catch (e: Exception) {
                                // Ignore if process doesn't exist
                            }
                        }
                        Thread.sleep(1000)
                        logger.lifecycle("🔧 Applied Windows force unlock")
                    }
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateAllConsciousnessApis")
    mustRunAfter("generateAllConsciousnessApis")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.timber)
    implementation(libs.coil.compose)

    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(fileTree(mapOf("dir" to "../Libs", "include" to listOf("*.jar"))))

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

// ===== AUTOMATED QUALITY CHECKS =====
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(mapOf(
                "indent_size" to "4",
                "android" to "true",
                "max_line_length" to "120"
            ))
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// ===== AUTOMATED TESTING =====
kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
    }
}

// ===== BUILD AUTOMATION TASKS =====
tasks.register("checkCodeQuality") {
    group = "verification"
    description = "Run all code quality checks"
    dependsOn("spotlessCheck", "detekt", "lint")
}

tasks.register("runAllTests") {
    group = "verification"
    description = "Run all tests with coverage"
    dependsOn("test", "connectedAndroidTest", "koverHtmlReport")
}

tasks.register("buildAndTest") {
    group = "build"
    description = "Complete build and test cycle"
    dependsOn("checkCodeQuality", "build", "runAllTests")
}
