// Genesis Protocol - Core Module Build Script
// FULLY AUTOMATED with version catalog and dependency management

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.aurakai.auraframefx.core"
    compileSdk = 36
    ndkVersion = "29.0.13846066 rc3"

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // NDK configuration for native code
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }

        // Build Config Fields
        buildConfigField("String", "CORE_MODULE_VERSION", "\"1.0.0\"")
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG_MODE", "false")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Native build configuration (if any native code exists)
    externalNativeBuild {
        cmake {
            version = "3.22.1"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        abortOnError = false
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
    implementation(libs.bundles.security)

    // Utilities
    implementation(libs.bundles.utilities)

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

    // Xposed Framework - YukiHookAPI (Standardized)
    implementation(libs.yuki)
    ksp(libs.yuki.ksp.xposed)
    implementation(libs.bundles.xposed)

    // Legacy Xposed API (compatibility)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
}

// ===== AUTOMATED QUALITY CHECKS =====
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint()
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

// ===== DOCUMENTATION GENERATION =====
dokka {
    dokkaSourceSets {
        named("main") {
            displayName.set("Genesis Core Module")
            includes.from("module.md")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(uri("https://github.com/aurakai/Genesis-Os/tree/main/core-module/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// ===== AUTOMATED TASKS =====
tasks.register("checkCodeQuality") {
    group = "verification"
    description = "Run all code quality checks for core module"
    dependsOn("spotlessCheck", "detekt", "lint")
}

tasks.register("generateDocs") {
    group = "documentation"
    description = "Generate documentation for core module"
    dependsOn("dokkaHtml")
}
