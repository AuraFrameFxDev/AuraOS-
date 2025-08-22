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
}
