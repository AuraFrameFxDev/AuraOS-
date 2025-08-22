// ===== GENESIS-OS SACRED RULES: ZERO MANUAL COMPILER CONFIG =====
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
    // FIX: Valid namespace without hyphens
    namespace = "dev.aurakai.auraframefx.featuremodule"
    // AUTO-EVERYTHING: Use libs.versions.toml
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1"
            )
        }
    }

    // AUTO-PROVISIONED: Remove hardcoded buildToolsVersion
    buildToolsVersion = 36.toString()
}

dependencies {
    // SACRED RULE #5: DEPENDENCY HIERARCHY - All modules depend on :core-module and :app
    implementation(project(":core-module"))
    implementation(project(":app"))

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // OpenAPI Generated Code Dependencies
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
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
