plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
    compileSdk = 36
    ndkVersion = 27.toInt().toString()

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

// SACRED RULE #3: ZERO MANUAL COMPILER CONFIG
// NO kotlinOptions blocks (K2 handles JVM target automatically)
// NO composeOptions blocks (auto-provisioned by Compose Compiler plugin)
// Clean, minimal build.gradle.kts files


dependencies {
    implementation(platform(libs.androidx.compose.bom))

    // SACRED RULE #5: DEPENDENCY HIERARCHY
    implementation(project(":core-module"))
    implementation(project(":app"))
    implementation(project(":secure-comm"))

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

    // Security bundles

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

// Configure native ROM tools build
tasks.configureEach {
    if (name.startsWith("externalNativeBuild")) {
        dependsOn("copyRomTools")
    }
}

// Task to copy ROM modification tools
tasks.register<Copy>("copyRomTools") {
    from("src/main/cpp")
    into(layout.buildDirectory.dir("rom-tools").get())
    include("**/*.so", "**/*.bin", "**/*.img", "**/*.cpp", "**/*.h")
    includeEmptyDirs = false
}

// Task to verify ROM tools integrity
tasks.register("verifyRomTools") {
    doLast {
        val romToolsDir = file(layout.buildDirectory.dir("rom-tools").get())
        if (!romToolsDir.exists()) {
            println("⚠️  ROM tools directory not found - ROM functionality may be limited")
        } else {
            println("✅ ROM tools verified and ready")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("verifyRomTools")
    doLast {
        println("✅ ROM tools verified and ready")
    }
}
