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
    namespace = "dev.aurakai.auraframefx.dataveinoraclenative"

    // Required for native modules
    compileSdk = 36

    defaultConfig {
        minSdk = 33

        // NDK configuration for native code
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20", "-fPIC", "-O3")
                cppFlags("-O3", "-DNDEBUG", "-DDATA_VEIN_NATIVE_RELEASE")
                "-DANDROID_STL=c++_shared"
                "-DCMAKE_VERBOSE_MAKEFILE=ON"
                "-DDATA_VEIN_NATIVE_BUILD=ON"

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
        viewBinding = false
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
    }
    dependencies {
        // Project modules
        implementation(project(":core-module"))

        // Core AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose - Genesis UI System
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.bundles.compose)
        implementation(libs.androidx.navigation.compose)

        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        implementation(libs.hilt.navigation.compose)

        // Coroutines - Genesis Async Processing
        implementation(libs.bundles.coroutines)

        // Kotlin reflection for KSP - fixed version
        implementation(libs.kotlin.reflect)

        // OpenAPI Generated Code Dependencies
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.kotlinx.serialization)
        implementation(libs.okhttp3.logging.interceptor)
        implementation(libs.kotlinx.serialization.json)

        // Core library desugaring
        coreLibraryDesugaring(libs.coreLibraryDesugaring)

        // Testing
        testImplementation(libs.junit.jupiter)
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // Xposed Framework - YukiHookAPI (Standardized)
        implementation(libs.yuki)
        ksp(libs.yuki.ksp.xposed)
        implementation(libs.bundles.xposed)

        // Legacy Xposed API (compatibility)
        implementation(files("${project.rootDir}/Libs/api-82.jar"))
        implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

    }
}
