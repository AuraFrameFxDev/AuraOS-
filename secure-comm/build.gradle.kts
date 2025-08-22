plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.openapi.generator)
}


android {
    namespace = "dev.aurakai.auraframefx.securecomm"
    compileSdk = 36

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
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

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

    // Xposed Framework - YukiHookAPI (Standardized)
    implementation(libs.yuki)
    ksp(libs.yuki.ksp.xposed)
    implementation(libs.bundles.xposed)
    
    // Legacy Xposed API (compatibility)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
}
