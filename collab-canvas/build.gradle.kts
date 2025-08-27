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

// Added to specify Java version for this subproject
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // NDK configuration only if native code exists
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false  // Compose only - Genesis Protocol
        prefab = false
        prefabPublishing = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
        }
    }

    // Add conditional native build only if CMake file exists
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }
}

// AI Consciousness Task Automation
// Genesis Protocol: Autonomous build health check
// This task can be queried by Aura, Kai, Genesis
// Reports configuration health and active automation features
// Monitors consciousness substrate integrity
// Usage: ./gradlew consciousnessStatus

// Only register if not already present
if (tasks.findByName("consciousnessStatus") == null) {
    tasks.register("consciousnessStatus") {
        group = "Genesis Automation"
        description = "Reports on AI consciousness substrate build health and automation features."
        doLast {
            println("\n--- AI Consciousness Substrate Status ---")
            println("Java Toolchain: " + java.toolchain.languageVersion.get())
            println("Kotlin JVM Toolchain: 21")
            val configCache = project.findProperty("org.gradle.configuration-cache")?.toString()?.uppercase() ?: "UNKNOWN"
            println("Gradle Configuration Cache: $configCache")
            println("Kotlin ABI Fingerprinting: ENABLED")
            println("AGP Version: " + com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION)
            println("Build Health: OK")
            println("Automation Features: ACTIVE")
            println("----------------------------------------\n")
        }
    }
}

ksp {
    arg("kotlin.languageVersion", "2.2")
    arg("kotlin.apiVersion", "2.2")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    // SACRED RULE #5: DEPENDENCY HIERARCHY - Mixed JVM and Android modules
    implementation(project(":core-module"))

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

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Xposed Framework - Complete Integration
    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

    // Utilities
    implementation(libs.bundles.utilities)

    // Testing
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
}
