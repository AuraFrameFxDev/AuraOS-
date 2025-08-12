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
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    
    // Auto-Provisioned JVM Toolchain (NO MANUAL CONFIG)
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaTarget.get())
    }
    
    // NO composeOptions block - K2 handles it automatically
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

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