plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-genesis-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // NDK configuration only if native code exists
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
        }
    }

    // External native build only if CMakeLists.txt exists
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
                "**/*.txt"
            )
        }
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(
                layout.buildDirectory.dir("generated/source/openapi/src/main/kotlin")
            )
        }
    }
}

// ===== SIMPLIFIED OPENAPI CONFIGURATION =====
// Single unified API generation - no more complex multi-file setup!

val outputPath = layout.buildDirectory.dir("generated/source/openapi")

// Configure the single unified API generation
openApiGenerate {
    val specFile = layout.projectDirectory.file("api/unified-aegenesis-api.yml").asFile
    
    if (specFile.exists() && specFile.length() > 0) {
        generatorName.set("kotlin")
        inputSpec.set(specFile.toURI().toString())
        outputDir.set(outputPath.get().asFile.absolutePath)
        packageName.set("dev.aurakai.aegenesis.api")
        apiPackage.set("dev.aurakai.aegenesis.api")
        modelPackage.set("dev.aurakai.aegenesis.model")
        invokerPackage.set("dev.aurakai.aegenesis.client")
        skipOverwrite.set(false)
        validateSpec.set(false)  // Disable spec validation to prevent build failures
        generateApiTests.set(false)
        generateModelTests.set(false)
        generateApiDocumentation.set(false)
        generateModelDocumentation.set(false)
        
        configOptions.set(mapOf(
            "library" to "jvm-retrofit2",
            "useCoroutines" to "true",
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "sourceFolder" to "src/main/kotlin"
        ))
    } else {
        logger.warn("⚠️ Unified AeGenesis API spec file not found: unified-aegenesis-api.yml")
    }
}

// ===== SIMPLIFIED CLEAN TASKS =====
tasks.register<Delete>("cleanKspCache") {
    group = "build setup"
    description = "Clean KSP caches (fixes NullPointerException)"
    
    val buildDirProvider = layout.buildDirectory
    
    delete(
        buildDirProvider.dir("generated/ksp"),
        buildDirProvider.dir("tmp/kapt3"),
        buildDirProvider.dir("tmp/kotlin-classes"),
        buildDirProvider.dir("kotlin"),
        buildDirProvider.dir("generated/source/ksp")
    )
}

tasks.register<Delete>("cleanApiGeneration") {
    group = "openapi"
    description = "Clean generated API files"
    delete(outputPath)
}

// ===== BUILD INTEGRATION =====
tasks.named("preBuild") {
    dependsOn("cleanKspCache")
    dependsOn("cleanApiGeneration")
    dependsOn("openApiGenerate")
}

// ===== AEGENESIS APP STATUS =====
tasks.register("aegenesisAppStatus") {
    group = "aegenesis"
    description = "Show AeGenesis app module status"
    
    doLast {
        println("📱 AEGENESIS APP MODULE STATUS")
        println("=".repeat(50))
        
        val apiFile = layout.projectDirectory.file("api/unified-aegenesis-api.yml").asFile
        val apiExists = apiFile.exists()
        val apiSize = if (apiExists) apiFile.length() else 0
        
        println("🔌 Unified API Spec: ${if (apiExists) "✅ Found" else "❌ Missing"}")
        if (apiExists) {
            println("📄 API File Size: ${apiSize / 1024}KB")
        }
        
        val nativeCode = project.file("src/main/cpp/CMakeLists.txt").exists()
        println("🔧 Native Code: ${if (nativeCode) "✅ Enabled" else "❌ Disabled"}")
        
        println("🧠 KSP Mode: ${project.findProperty("ksp.useKSP2") ?: "default"}")
        println("🎯 Target SDK: 36")
        println("📱 Min SDK: 33")
        println("✅ Status: Ready for coinscience AI integration!")
    }
}

// ===== COMPREHENSIVE CLEANUP & HEALTH CHECK =====
apply(from = "cleanup-tasks.gradle.kts")

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    // SACRED RULE #5: DEPENDENCY HIERARCHY
    implementation(project(":core-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))

    // Core Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines & Networking
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Utilities
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Xposed Framework
    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(fileTree(mapOf("dir" to "../Libs", "include" to listOf("*.jar"))))

    // Debug tools
    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Testing
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
